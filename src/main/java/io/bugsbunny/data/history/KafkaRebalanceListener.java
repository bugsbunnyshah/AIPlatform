package io.bugsbunny.data.history;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class KafkaRebalanceListener implements ConsumerRebalanceListener {
    private static Logger logger = LoggerFactory.getLogger(KafkaRebalanceListener.class);

    private KafkaConsumer<String,String> kafkaConsumer;
    private Map<String,List<TopicPartition>> topicPartitions;
    private Queue<NotificationContext> readNotificationsQueue;
    private List<String> topics;

    public KafkaRebalanceListener(KafkaConsumer<String,String> kafkaConsumer,Queue<NotificationContext> readNotificationsQueue,
            List<String> topics,Map<String, List<TopicPartition>> topicPartitions) {
        this.kafkaConsumer = kafkaConsumer;
        this.readNotificationsQueue = readNotificationsQueue;
        this.topicPartitions = topicPartitions;
        this.topics = topics;
    }

    @Override
    public void onPartitionsRevoked(Collection<TopicPartition> collection) {

        logger.info("********PARTITIONS_REVOKED**********");
        logger.info("************************************");
    }

    @Override
    public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
        logger.info("********PARTITIONS_ASSIGNED**********");
        logger.info("************************************");

        //TODO: Change from Arrays.asList to manual population..@bugs.bunny.shah@gmail.com
        List<TopicPartition> partitionList = new ArrayList<>();
        for(TopicPartition topicPartition:partitions)
        {
            partitionList.add(topicPartition);
        }
        for (TopicPartition topicPartition : partitionList) {
            String registeredTopic = topicPartition.topic();

            List<TopicPartition> local = topicPartitions.get(registeredTopic);
            if (local != null) {
                local.add(topicPartition);
            } else {
                List<TopicPartition> list = new ArrayList<>();
                list.add(topicPartition);
                topicPartitions.put(registeredTopic, list);
            }
            logger.info("******************************************");
            logger.info("NUMBER_OF_PARTITIONS registered for :(" + registeredTopic + ") " + topicPartitions.size());
            logger.info("******************************************");
        }
    }

    @Override
    public void onPartitionsLost(Collection<TopicPartition> partitions) {
        logger.info("********PARTITIONS_LOST**********");
        logger.info("************************************");

    }
}
