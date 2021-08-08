package io.bugsbunny.dataIngestion.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.bugsbunny.data.history.service.DataReplayService;
import io.bugsbunny.infrastructure.MongoDBJsonStore;
import io.bugsbunny.infrastructure.Tenant;
import io.bugsbunny.preprocess.SecurityTokenContainer;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.StorageLevels;
import org.apache.spark.api.java.function.VoidFunction;
import org.apache.spark.storage.StorageLevel;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.receiver.Receiver;

import org.mitre.schemastore.model.Entity;
import org.mitre.schemastore.model.SchemaElement;
import org.mitre.schemastore.model.schemaInfo.FilteredSchemaInfo;
import org.mitre.schemastore.model.schemaInfo.HierarchicalSchemaInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StreamIngester implements Serializable{
    private static Logger logger = LoggerFactory.getLogger(StreamIngester.class);

    private SparkConf sparkConf;
    private JavaStreamingContext streamingContext;
    private StreamReceiver streamReceiver;
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private boolean isReceiverStarted = false;

    public JsonObject submit(Tenant tenant, SecurityTokenContainer securityTokenContainer,
                             MongoDBJsonStore mongoDBJsonStore,
                             DataReplayService dataReplayService,
                             JsonArray sourceData)
    {
        JsonObject json = new JsonObject();

        if(this.streamingContext == null){
            synchronized (this) {
                if(this.streamingContext == null) {
                    // Create a local StreamingContext with two working thread and batch interval of 1 second
                    this.sparkConf = new SparkConf().setAppName("StreamIngester")
                            .set("hostname", "localhost").setMaster("local[20]");
                    this.streamingContext = new JavaStreamingContext(sparkConf, new Duration(1000));
                    this.streamReceiver = new StreamReceiver(StorageLevels.MEMORY_AND_DISK_2);
                    startIngestion();
                }
            }
        }

        if(securityTokenContainer != null) {
            if (StreamIngesterContext.getStreamIngesterContext() != null) {
                StreamIngesterContext streamIngesterContext = StreamIngesterContext.getStreamIngesterContext();
                streamIngesterContext.setSecurityTokenContainer(securityTokenContainer);
                streamIngesterContext.setDataReplayService(dataReplayService);
                streamIngesterContext.setMongoDBJsonStore(mongoDBJsonStore);
            }


            String dataLakeId = UUID.randomUUID().toString();
            String chainId = "/" + tenant.getPrincipal() + "/" + dataLakeId;
            StreamObject streamObject = new StreamObject();
            streamObject.setDataLakeId(dataLakeId);
            streamObject.setData(sourceData.toString());
            streamObject.setPrincipal(tenant.getPrincipal());
            StreamIngesterContext.getStreamIngesterContext().addStreamObject(streamObject);

            json.addProperty("dataLakeId", dataLakeId);
            json.addProperty("chainId",chainId);
            json.addProperty("tenant",tenant.getPrincipal());
            return json;
        }
        else {
            return new JsonObject();
        }
    }

    private void startIngestion()
    {
        try {
            JavaDStream<String> dataStream = this.streamingContext.receiverStream(streamReceiver);
            dataStream.foreachRDD(new VoidFunction<JavaRDD<String>>() {
                @Override
                public void call(JavaRDD<String> stringJavaRDD) throws Exception {
                    stringJavaRDD.foreach(s -> {
                        JsonElement root = JsonParser.parseString(s);
                        if (root.isJsonPrimitive()) {
                            return;
                        }

                        String dataLakeId = root.getAsJsonObject().get("dataLakeId").getAsString();
                        String principal = root.getAsJsonObject().get("principal").getAsString();
                        root.getAsJsonObject().remove("dataLakeId");
                        root.getAsJsonObject().remove("principal");

                        HierarchicalSchemaInfo sourceSchemaInfo = MapperService.populateHierarchialSchema(root.toString(),
                                root.toString(), null);

                        HierarchicalSchemaInfo destinationSchemaInfo = MapperService.populateHierarchialSchema(root.toString(),
                                root.toString(), null);


                        FilteredSchemaInfo f1 = new FilteredSchemaInfo(sourceSchemaInfo);
                        f1.addElements(sourceSchemaInfo.getElements(Entity.class));
                        FilteredSchemaInfo f2 = new FilteredSchemaInfo(destinationSchemaInfo);
                        f2.addElements(destinationSchemaInfo.getElements(Entity.class));
                        Map<SchemaElement, Double> scores = MapperService.findMatches(f1, f2, sourceSchemaInfo.getElements(Entity.class));
                        //logger.info("*************************************");
                        //logger.info(scores.toString());
                        //logger.info("*************************************");

                        JsonObject local = MapperService.performMapping(scores, root.toString());
                        local.addProperty("braineous_datalakeid",dataLakeId);
                        StreamIngesterContext.getStreamIngesterContext().ingestData(principal,local);
                    });
                }
            });

            if(!this.isReceiverStarted) {
                Thread t = new Thread(() -> {
                    try {
                        this.streamingContext.start();
                        this.streamingContext.awaitTermination();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
                t.start();
                this.isReceiverStarted = true;
            }
        }
        catch(Exception e){
            logger.error(e.getMessage(),e);
            throw new RuntimeException(e);
        }
    }

    private static class StreamReceiver extends Receiver<String> {
        private DataProcessor dataProcessor;

        public StreamReceiver(StorageLevel storageLevel) {
            super(storageLevel);
            this.dataProcessor = new DataProcessor(this);
        }

        @Override
        public void onStart() {
            try {
                // Start the thread that receives data over a connection
                Thread t = new Thread(this.dataProcessor);
                t.start();
            }
            catch (Exception e){
                throw new RuntimeException(e);
            }
        }

        @Override
        public void onStop() {
            // There is nothing much to do as the thread calling receive()
            // is designed to stop by itself if isStopped() returns false
        }
    }

    private static class DataProcessor implements Runnable, Serializable
    {
        private StreamReceiver streamReceiver;
        private List<String> activeQueueProcessors;
        private DataProcessor(StreamReceiver streamReceiver)
        {
            this.streamReceiver = streamReceiver;
            this.activeQueueProcessors = new ArrayList<>();
        }


        @Override
        public void run() {
            try {
                while(true) {
                    Set<String> activeDataLakeIds = StreamIngesterContext.getStreamIngesterContext().activeDataLakeIds();
                    //System.out.println(activeDataLakeIds);

                    for(String activeDataLakeId:activeDataLakeIds) {
                        if(!this.activeQueueProcessors.contains(activeDataLakeId)) {
                            Queue<StreamObject> queue = StreamIngesterContext.getStreamIngesterContext().getDataLakeQueue(activeDataLakeId);
                            if(!queue.isEmpty()) {
                                this.activeQueueProcessors.add(activeDataLakeId);
                                QueueProcessor queueProcessor = new QueueProcessor(activeDataLakeId, this.activeQueueProcessors, this.streamReceiver,
                                        queue);
                                Thread t = new Thread(queueProcessor);
                                t.start();
                            }
                        }
                    }
                }
            } catch(Throwable t) {
                // restart if there is any other error
                logger.error(t.getMessage(),t);
                this.streamReceiver.restart(t.getMessage());
            }
        }
    }

    private static class QueueProcessor implements Runnable{
        private String dataLakeId;
        private Queue<StreamObject> queue;
        private StreamReceiver streamReceiver;
        private List<String> activeProcessors;

        private QueueProcessor(String dataLakeId,List<String> activeProcessors,StreamReceiver streamReceiver,Queue<StreamObject> queue){
            this.dataLakeId = dataLakeId;
            this.activeProcessors = activeProcessors;
            this.streamReceiver = streamReceiver;
            this.queue = queue;
        }

        @Override
        public void run() {
            try {
                if (this.queue.isEmpty()) {
                    throw new IllegalStateException("DATA_NOT_FOUND");
                }
                //System.out.println("*******QUEUE_PROCESSOR********");
                //System.out.println("DataLakeId: "+dataLakeId);
                //System.out.println(this.queue);
                //System.out.println(StreamIngesterContext.getStreamIngesterContext().activeDataLakeIds());
                //System.out.println("*******************************");
                while (!this.queue.isEmpty()) {
                    StreamObject streamObject = this.queue.poll();
                    //StreamObject streamObject = this.queue.peek();
                    String data = streamObject.getData();
                    String dataLakeId = streamObject.getDataLakeId();
                    String principal = streamObject.getPrincipal();
                    JsonObject jsonObject = JsonParser.parseString(data).getAsJsonObject();
                    jsonObject.addProperty("principal", principal);
                    jsonObject.addProperty("dataLakeId", dataLakeId);
                    this.streamReceiver.store(jsonObject.toString());
                }
            }
            finally {
                this.activeProcessors.remove(this.dataLakeId);
            }
        }
    }
}
