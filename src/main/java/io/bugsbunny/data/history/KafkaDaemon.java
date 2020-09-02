package io.bugsbunny.data.history;

import io.bugsbunny.persistence.MongoDBJsonStore;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndTimestamp;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.common.TopicPartition;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.net.InetAddress;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.LinkedTransferQueue;

@ApplicationScoped
public class KafkaDaemon {
    private static Logger logger = LoggerFactory.getLogger(KafkaDaemon.class);

    private KafkaProducer<String,String> kafkaProducer;
    private KafkaConsumer<String,String> kafkaConsumer;
    private CountDownLatch shutdownLatch;
    private Map<String,List<TopicPartition>> topicPartitions;

    private Queue<NotificationContext> readNotificationsQueue;
    private Map<String,Map<String, JsonArray>> lookupTable;
    private List<String> topics = new ArrayList<>();

    private Queue<DataSetFromBegginningOffset> dataSetFromQueue;

    @Inject
    private MongoDBJsonStore mongoDBJsonStore;

    private ForkJoinPool commonPool;

    private KafkaDaemonListener daemonListener;

    public KafkaDaemon()
    {
        this.topicPartitions = new HashMap<>();
        this.readNotificationsQueue = new LinkedTransferQueue<>();
        this.shutdownLatch = new CountDownLatch(1);
        this.commonPool = ForkJoinPool.commonPool();
        this.lookupTable = new HashMap<>();
        this.dataSetFromQueue = new LinkedTransferQueue<>();
    }

    public Map<String, Map<String, JsonArray>> getLookupTable() {
        return lookupTable;
    }

    public void registerDaemonListener(KafkaDaemonListener daemonListener)
    {
        this.daemonListener = daemonListener;
    }

    @PostConstruct
    public void start()
    {
        this.topics = this.mongoDBJsonStore.findKafakaDaemonBootstrapData();
        if(this.kafkaConsumer == null) {
            try {
                Properties config = new Properties();
                config.put("client.id", InetAddress.getLocalHost().getHostName());
                config.put("group.id", "foodRunnerSyncProtocol_notifications");
                config.put("bootstrap.servers", "localhost:9092");
                config.put("key.deserializer", org.apache.kafka.common.serialization.StringDeserializer.class);
                config.put("value.deserializer", org.springframework.kafka.support.serializer.JsonDeserializer.class);
                config.put("key.serializer", org.apache.kafka.common.serialization.StringSerializer.class);
                config.put("value.serializer", org.springframework.kafka.support.serializer.JsonSerializer.class);
                config.put("auto.commit.interval.ms", 1000);
                config.put("enable.auto.commit", true);
                config.put("max.poll.records", 100);

                this.kafkaConsumer = new KafkaConsumer<String, String>(config);
            }
            catch(Exception e)
            {
                logger.error(e.getMessage(), e);
                throw new RuntimeException(e);
            }

            KafkaRebalanceListener rebalanceListener = new KafkaRebalanceListener(this.kafkaConsumer, this.readNotificationsQueue, this.topics,
                    this.topicPartitions);
            this.kafkaConsumer.subscribe(this.topics, rebalanceListener);
        }

        if(this.kafkaProducer == null) {
            try {
                Properties config = new Properties();
                config.put("client.id", InetAddress.getLocalHost().getHostName());
                config.put("group.id", "foodRunnerSyncProtocol_notifications");
                config.put("bootstrap.servers", "localhost:9092");
                config.put("key.deserializer", org.apache.kafka.common.serialization.StringDeserializer.class);
                config.put("value.deserializer", org.springframework.kafka.support.serializer.JsonDeserializer.class);
                config.put("key.serializer", org.apache.kafka.common.serialization.StringSerializer.class);
                config.put("value.serializer", org.springframework.kafka.support.serializer.JsonSerializer.class);
                this.kafkaProducer = new KafkaProducer<>(config);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                throw new RuntimeException(e);
            }
        }

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                findNotifications();
            }
        });
        t.start();
    }

    @PreDestroy
    public void stop()
    {
        this.readNotificationsQueue = null;
        this.dataSetFromQueue = null;
        this.kafkaProducer.close();
        this.kafkaConsumer.close();
        this.shutdownLatch.countDown();
    }

    public void addTopic(String topic)
    {
        this.topics.add(topic);
    }

    public Boolean getActive() {
        return (this.kafkaProducer != null && this.kafkaConsumer != null);
    }

    public Queue<DataSetFromBegginningOffset> getDataSetFromQueue() {
        return dataSetFromQueue;
    }

    public void logStartUp()
    {
        logger.info("**********");
        logger.info("STARTING_KAFKA_DAEMON");
        logger.info("**********");
    }

    public void produceData(String topic, JsonObject jsonObject)
    {
        ProducerTask producerTask = new ProducerTask(this.kafkaProducer,topic, jsonObject);
        this.commonPool.execute(producerTask);
    }

    public JsonArray readNotifications(String topic, MessageWindow messageWindow)
    {
        NotificationContext notificationContext = new NotificationContext(topic, messageWindow);
        readNotificationsQueue.add(notificationContext);
        NotificationFinderTask notificationFinderTask = new NotificationFinderTask(notificationContext, this.lookupTable);
        this.commonPool.execute(notificationFinderTask);
        return notificationFinderTask.join();
    }

    /*public void produceActiveFoodRunnerData(String topic, List<ActiveFoodRunnerData> activeFoodRunnerData)
    {
        for(ActiveFoodRunnerData local:activeFoodRunnerData) {
            OffsetDateTime start = OffsetDateTime.now(ZoneOffset.UTC);
            OffsetDateTime end = OffsetDateTime.now(ZoneOffset.UTC);
            MessageWindow messageWindow = new MessageWindow(start,end);
            NotificationContext notificationContext = new NotificationContext(topic,messageWindow);
            readNotificationsQueue.add(notificationContext);
            io.appgal.cloud.messaging.ProducerTask producerTask = new io.appgal.cloud.messaging.ProducerTask(this.kafkaProducer, topic, local.toJson());
            this.commonPool.execute(producerTask);
        }
    }*/

    /*public JsonArray findTheClosestFoodRunner(SourceNotification sourceNotification)
    {
        JsonArray jsonArray = new JsonArray();

        double sourceLatitude = Double.parseDouble(sourceNotification.getLatitude());
        double sourceLongitude = Double.parseDouble(sourceNotification.getLongitude());
        Iterator<DataSetFromBegginningOffset> iterator = this.dataSetFromQueue.iterator();
        while(iterator.hasNext())
        {
            DataSetFromBegginningOffset local = iterator.next();
            JsonArray activeFoodRunnerData = local.getJsonArray();
            String jsonString = activeFoodRunnerData.iterator().next().getAsString();
            JsonObject jsonObject = JsonParser.parseString(jsonString).getAsJsonObject();
            try {
                if (!jsonObject.has("latitude") || !jsonObject.has("longitude")) {
                    logger.info("IGNORING_INVALID_DATA: " + jsonString);
                    continue;
                }

                String latitude = jsonObject.get("latitude").getAsString();
                String longitude = jsonObject.get("longitude").getAsString();
                double foodRunnerLatitude = 0.0d;
                double foodRunnerLongitude = 0.0d;
                try {
                    foodRunnerLatitude = Double.parseDouble(latitude);
                    foodRunnerLongitude = Double.parseDouble(longitude);
                } catch (Exception e) {
                    logger.info("IGNORING_INVALID_DATA: " + jsonString);
                    continue;
                }

                //Match the coordinates with the FoodRunner
                double distance = MapUtils.calculateDistance(sourceLatitude, sourceLongitude, foodRunnerLatitude, foodRunnerLongitude);
                //if(distance < 5d)
                //{
                //    jsonArray.add(jsonObject);
                //}
                //logger.info("....");
                //logger.info("Distance: "+distance);
                //logger.info("....");
                jsonArray.add(jsonObject);
            }
            catch(Exception e)
            {
                logger.info("IGNORING_INVALID_DATA: " + jsonString);
                continue;
            }
        }
        return jsonArray;
    }*/

    private void findNotifications()
    {
        try {
            do {
                logger.debug("Start Long Poll");
                ConsumerRecords<String, String> records = kafkaConsumer.poll(20000);
                records.forEach(record -> process(record));

                //TODO: Read multiple NotificationContexts during this run
                //printNotificationsQueue();
                if (readNotificationsQueue.isEmpty()) {
                    logger.debug("NO_ACTIVE_READS_IN_PROGRESS");
                    continue;
                }
                this.processNotifications(records);
            } while (true);
        }
        catch(Exception ie)
        {
            logger.error(ie.getMessage(), ie);
            throw new RuntimeException(ie);
        }
    }

    private void processNotifications(ConsumerRecords<String, String> records)
    {
        NotificationContext notificationContext = readNotificationsQueue.poll();
        MessageWindow messageWindow = notificationContext.getMessageWindow();

        String topic = notificationContext.getTopic();
        try {
            for (ConsumerRecord<String, String> record : records) {
                String jsonValue = record.value();

                JsonObject jsonObject = JsonParser.parseString(jsonValue).getAsJsonObject();
                messageWindow.addMessage(jsonObject);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        String lookupTableIndex = messageWindow.getLookupTableIndex();
        JsonArray copyOfMessages = messageWindow.getCopyOfMessages();
        Map<String, JsonArray> topicTable = lookupTable.get(topic);
        if(topicTable == null)
        {
            topicTable = new HashMap<>();
            lookupTable.put(topic, topicTable);
            topicTable.put(lookupTableIndex, copyOfMessages);
        }
        topicTable.put(lookupTableIndex, copyOfMessages);
        logger.info(copyOfMessages.toString());

        if(daemonListener != null)
        {
            daemonListener.receiveNotifications(messageWindow);
        }

        logger.info("*********KAFKA_DAEMON***********");
        logger.info("END_READ_NOTIFICATIONS");
        logger.info("********************");

        this.readLogForTopicFromTheBeginning(topic);
    }

    private void readLogForTopicFromTheBeginning(String topic)
    {
        OffsetDateTime start = OffsetDateTime.now(ZoneOffset.UTC);

        //Construct the parameters to read the Kafka Log
        Map<TopicPartition, Long> partitionParameter = new HashMap<>();
        List<TopicPartition> currentTopicPartitions = topicPartitions.get(topic);
        for (TopicPartition topicPartition : currentTopicPartitions) {
            partitionParameter.put(topicPartition, start.toEpochSecond());
            partitionParameter.put(topicPartition, start.toEpochSecond());
        }

        //
        Map<TopicPartition, OffsetAndTimestamp> topicPartitionOffsetAndTimestampMap = kafkaConsumer.offsetsForTimes(partitionParameter);
        kafkaConsumer.poll(100);

        //Make sure only unique data gets put in the Queue
        OffsetAndTimestamp offsetAndTimestamp = topicPartitionOffsetAndTimestampMap.values().iterator().next();
        kafkaConsumer.seek(currentTopicPartitions.get(0), offsetAndTimestamp.offset());
        ConsumerRecords<String,String> records = kafkaConsumer.poll(100);
        for(ConsumerRecord<String, String> record:records)
        {
            JsonArray jsonArray = new JsonArray();
            jsonArray.add(record.value());
            DataSetFromBegginningOffset dataSetFromBegginningOffset = new DataSetFromBegginningOffset(jsonArray);
            this.dataSetFromQueue.add(dataSetFromBegginningOffset);
        }
    }

    private void process(ConsumerRecord<String, String> record) {

        //logger.info("CONSUME_DATA");
        //logger.info("RECORD_OFFSET: "+record.offset());
        //logger.info("RECORD_KEY: "+record.key());
        //logger.info("RECORD_VALUE: "+record.value());
        //logger.info("....");
        JsonArray jsonArray = new JsonArray();
        jsonArray.add(record.value());
        DataSetFromBegginningOffset dataSetFromBegginningOffset = new DataSetFromBegginningOffset(jsonArray);
        this.dataSetFromQueue.add(dataSetFromBegginningOffset);
    }

    synchronized void printNotificationsQueue()
    {
        logger.info("******************");
        logger.info("Queue Size: "+readNotificationsQueue.size());
        Iterator<NotificationContext> iterator = readNotificationsQueue.iterator();
        while(iterator.hasNext())
        {
            NotificationContext notificationContext = iterator.next();
            logger.info("NotificationContextId: "+notificationContext.getMessageWindow().getStart());
        }
        logger.info("**********************");
    }
}
