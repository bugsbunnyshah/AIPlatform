package io.bugsbunny.dataIngestion.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import io.bugsbunny.data.history.service.DataReplayService;
import io.bugsbunny.infrastructure.MongoDBJsonStore;
import io.bugsbunny.infrastructure.Tenant;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.Serializable;

public class StreamIngesterContext implements Serializable {
    private static Logger logger = LoggerFactory.getLogger(StreamIngesterContext.class);

    private static StreamIngester streamIngester = new StreamIngester();
    private static StreamIngesterContext streamIngesterContext = new StreamIngesterContext();

    private StreamIngesterQueue streamIngesterQueue;

    private MongoDBJsonStore mongoDBJsonStore;

    private DataReplayService dataReplayService;


    private StreamIngesterContext()
    {
        this.streamIngesterQueue = new StreamIngesterQueue();
    }

    public static StreamIngester getStreamIngester()
    {
        if(StreamIngesterContext.streamIngester == null){
            StreamIngesterContext.streamIngester = new StreamIngester();
        }
        return StreamIngesterContext.streamIngester;
    }

    public static StreamIngesterContext getStreamIngesterContext()
    {
        if(StreamIngesterContext.streamIngesterContext == null){
            StreamIngesterContext.streamIngesterContext = new StreamIngesterContext();
        }
        return StreamIngesterContext.streamIngesterContext;
    }

    public void addStreamObject(StreamObject streamObject)
    {
        this.streamIngesterQueue.add(streamObject);
    }

    public StreamObject getLatest(){
        return this.streamIngesterQueue.latest();
    }


    public void ingestData(String principal, JsonObject jsonObject)
    {
        logger.info("********************************************");
        logger.info(jsonObject.toString());
        logger.info("********************************************");

        Tenant tenant = new Tenant();
        tenant.setPrincipal(principal);
        this.mongoDBJsonStore.storeIngestion(tenant,jsonObject);

        //Add for DataReplay
        String chainId = this.dataReplayService.generateDiffChain(jsonObject);
        logger.info("CHAIN_ID: "+chainId);
    }

    public void setDataReplayService(DataReplayService dataReplayService){
        this.dataReplayService = dataReplayService;
    }

    public void setMongoDBJsonStore(MongoDBJsonStore mongoDBJsonStore) {
        this.mongoDBJsonStore = mongoDBJsonStore;
    }
}
