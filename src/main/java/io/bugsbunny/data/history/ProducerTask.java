package io.bugsbunny.data.history;

import com.google.gson.JsonObject;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.RecursiveAction;

public class ProducerTask extends RecursiveAction {
    private static Logger logger = LoggerFactory.getLogger(ProducerTask.class);

    private KafkaProducer<String,String> kafkaProducer;
    private String topic;
    private JsonObject jsonObject;

    public ProducerTask(KafkaProducer<String,String> kafkaProducer, String topic, JsonObject jsonObject)
    {
        this.kafkaProducer = kafkaProducer;
        this.topic = topic;
        this.jsonObject = jsonObject;
    }

    @Override
    protected void compute() {

        final ProducerRecord<String, String> record = new ProducerRecord<>(topic,
                topic, jsonObject.toString());

        this.kafkaProducer.send(record, new Callback() {
            public void onCompletion(RecordMetadata metadata, Exception e) {
                if (e != null) {
                    logger.error("Send failed for record {}", record, e);
                }
                else
                {
                    logger.info("******************************************");
                    logger.info("PRODUCE_DATA");
                    logger.info("RECORD_META_DATA: "+metadata.toString());
                    logger.info("******************************************");
                }
            }
        });
    }
}
