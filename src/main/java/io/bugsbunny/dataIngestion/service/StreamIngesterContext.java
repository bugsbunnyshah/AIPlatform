package io.bugsbunny.dataIngestion.service;


import com.google.gson.JsonObject;

import io.bugsbunny.data.history.service.DataReplayService;
import io.bugsbunny.infrastructure.MongoDBJsonStore;
import io.bugsbunny.infrastructure.Tenant;
import io.bugsbunny.preprocess.SecurityToken;
import io.bugsbunny.preprocess.SecurityTokenContainer;
import io.bugsbunny.util.BackgroundProcessListener;
import io.bugsbunny.util.JsonUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.*;

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

    public void clear(){
        StreamIngesterContext.streamIngester = null;
        StreamIngesterContext.streamIngesterContext = null;
    }

    public void addStreamObject(StreamObject streamObject)
    {
        //System.out.println("********ADDING_STREAM_OBJECT_FOR_STORAGE*******");
        //System.out.println(streamObject.getData());
        //System.out.println("************************************************");


        this.streamIngesterQueue.add(streamObject);
    }


    public Queue<StreamObject> getDataLakeQueue(String dataLakeId){
        return this.streamIngesterQueue.getDataLakeQueue(dataLakeId);
    }

    public Set<String> activeDataLakeIds()
    {
        return this.streamIngesterQueue.getActiveDataLakeIds();
    }

    public Map<String, String> getChainIds() {
        return chainIds;
    }

    public void ingestData(String principal,String dataLakeId, String chainId, JsonObject jsonObject)
    {
        //JsonUtil.print(jsonObject);

        Tenant tenant = new Tenant();
        tenant.setPrincipal(principal);
        SecurityToken securityToken = new SecurityToken();
        securityToken.setPrincipal(principal);
        this.securityTokenContainer.setSecurityToken(securityToken);


        JsonObject data = new JsonObject();
        data.addProperty("braineous_datalakeid",dataLakeId);
        data.addProperty("tenant",tenant.getPrincipal());
        data.addProperty("data", jsonObject.toString());
        data.addProperty("chainId",chainId);
        logger.info("************PERSISTING-"+dataLakeId+"******************");
        logger.info(data.toString());
        logger.info("****************************************");
        this.mongoDBJsonStore.storeIngestion(tenant,data);
        this.chainIds.put(dataLakeId,chainId);

        BackgroundProcessListener.getInstance().decreaseThreshold(data);
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
