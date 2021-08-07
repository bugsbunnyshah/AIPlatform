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
import io.bugsbunny.preprocess.SecurityToken;
import io.bugsbunny.preprocess.SecurityTokenContainer;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class StreamIngesterContext implements Serializable {
    private static Logger logger = LoggerFactory.getLogger(StreamIngesterContext.class);

    private static StreamIngester streamIngester = new StreamIngester();
    private static StreamIngesterContext streamIngesterContext = new StreamIngesterContext();

    private StreamIngesterQueue streamIngesterQueue;

    private MongoDBJsonStore mongoDBJsonStore;

    private DataReplayService dataReplayService;

    private SecurityTokenContainer securityTokenContainer;

    private Map<String,String> chainIds;


    private StreamIngesterContext()
    {
        this.streamIngesterQueue = new StreamIngesterQueue();
        this.chainIds = new HashMap<>();
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

    public Map<String, String> getChainIds() {
        return chainIds;
    }

    public void ingestData(String principal, JsonObject jsonObject)
    {

        Tenant tenant = new Tenant();
        tenant.setPrincipal(principal);
        SecurityToken securityToken = new SecurityToken();
        securityToken.setPrincipal(principal);
        this.securityTokenContainer.setSecurityToken(securityToken);

        //Store in the DataLake
        String dataLakeId = jsonObject.get("braineous_datalakeid").getAsString();

        //Add for DataReplay
        String chainId = this.dataReplayService.generateDiffChain(jsonObject);
        this.chainIds.put(dataLakeId,chainId);

        JsonObject data = new JsonObject();
        data.addProperty("braineous_datalakeid",jsonObject.get("braineous_datalakeid").getAsString());
        data.addProperty("tenant",tenant.getPrincipal());
        data.addProperty("data", jsonObject.toString());
        data.addProperty("chainId",chainId);
        logger.info("************PERSISTING******************");
        logger.info(data.toString());
        logger.info("****************************************");
        this.mongoDBJsonStore.storeIngestion(tenant,data);
    }

    public void setDataReplayService(DataReplayService dataReplayService){
        this.dataReplayService = dataReplayService;
    }

    public void setMongoDBJsonStore(MongoDBJsonStore mongoDBJsonStore) {
        this.mongoDBJsonStore = mongoDBJsonStore;
    }

    public void setSecurityTokenContainer(SecurityTokenContainer securityTokenContainer) {
        this.securityTokenContainer = securityTokenContainer;
    }
}
