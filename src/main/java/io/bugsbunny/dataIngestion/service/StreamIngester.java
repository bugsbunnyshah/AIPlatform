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

public class StreamIngester implements Serializable{
    private static Logger logger = LoggerFactory.getLogger(StreamIngester.class);

    private SparkConf sparkConf;
    private JavaStreamingContext streamingContext;
    private StreamReceiver streamReceiver;

    private void startIngester()
    {
        try {
            JavaDStream<String> dataStream = this.streamingContext.receiverStream(this.streamReceiver);
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

            Thread t = new Thread(()->{
                try {
                    this.streamingContext.start();
                    this.streamingContext.awaitTermination();
                }
                catch (Exception e)
                {
                    throw new RuntimeException(e);
                }
            });
            t.start();
        }
        catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    public JsonObject submit(Tenant tenant, SecurityTokenContainer securityTokenContainer,
                             MongoDBJsonStore mongoDBJsonStore,
                             DataReplayService dataReplayService,
                             JsonArray sourceData)
    {
        JsonObject json = new JsonObject();

        if(this.streamingContext == null){
            try {
                // Create a local StreamingContext with two working thread and batch interval of 1 second
                sparkConf = new SparkConf().setAppName("StreamIngester").setMaster("local[20]");
                streamingContext = new JavaStreamingContext(sparkConf, new Duration(1000));
                streamReceiver = new StreamReceiver(StorageLevels.MEMORY_AND_DISK_2);
                startIngester();
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        }

        if(StreamIngesterContext.getStreamIngesterContext() != null){
            StreamIngesterContext streamIngesterContext = StreamIngesterContext.getStreamIngesterContext();
            streamIngesterContext.setSecurityTokenContainer(securityTokenContainer);
            streamIngesterContext.setDataReplayService(dataReplayService);
            streamIngesterContext.setMongoDBJsonStore(mongoDBJsonStore);
        }

        String dataLakeId = UUID.randomUUID().toString();
        this.streamReceiver.receiveData(tenant.getPrincipal(),dataLakeId, sourceData.toString());

        json.addProperty("dataLakeId", dataLakeId);
        return json;
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

        public void receiveData(String principal,String dataLakeId,String data)
        {
            StreamObject streamObject = new StreamObject();
            streamObject.setDataLakeId(dataLakeId);
            streamObject.setData(data);
            streamObject.setPrincipal(principal);
            StreamIngesterContext.getStreamIngesterContext().addStreamObject(streamObject);
        }
    }

    private static class DataProcessor implements Runnable, Serializable
    {
        private StreamReceiver streamReceiver;

        private DataProcessor(StreamReceiver streamReceiver)
        {
            this.streamReceiver = streamReceiver;
        }


        @Override
        public void run() {
            try {
                // Until stopped or connection broken continue reading
                while (!this.streamReceiver.isStopped()) {
                    StreamObject streamObject = StreamIngesterContext.getStreamIngesterContext().getLatest();
                    if(streamObject != null) {
                        String data = streamObject.getData();
                        String dataLakeId = streamObject.getDataLakeId();
                        String principal = streamObject.getPrincipal();
                        JsonArray jsonArray = JsonParser.parseString(data).getAsJsonArray();
                        Iterator<JsonElement> iterator = jsonArray.iterator();
                        while (iterator.hasNext()) {
                            JsonObject jsonObject = iterator.next().getAsJsonObject();
                            jsonObject.addProperty("principal",principal);
                            jsonObject.addProperty("dataLakeId",dataLakeId);
                            this.streamReceiver.store(jsonObject.toString());
                        }
                    }
                }
                this.streamReceiver.restart("RESTARTING.......");
            } catch(Throwable t) {
                // restart if there is any other error
                t.printStackTrace();
                this.streamReceiver.restart("Error receiving data", t);
            }
        }
    }
}
