package io.bugsbunny.dataIngestion.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.bugsbunny.infrastructure.MongoDBJsonStore;
import io.bugsbunny.preprocess.SecurityTokenContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.*;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.TimerTask;

@Singleton
public class IngestionService implements Serializable {
    private static Logger logger = LoggerFactory.getLogger(IngestionService.class);

    @Inject
    private SecurityTokenContainer securityTokenContainer;

    @Inject
    private MongoDBJsonStore mongoDBJsonStore;

    private Map<String,IngestionAgent> ingestionAgents;

    public IngestionService(){
        this.ingestionAgents = new HashMap<>();
    }

    public JsonObject ingestDevModelData(String data)
    {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("data", data);

        //long dataLakeId = this.mongoDBJsonStore.storeIngestion(jsonObject);
        //jsonObject.addProperty("dataLakeId", dataLakeId);

        return jsonObject;
    }

    public JsonObject readDataLakeData(String dataLakeId)
    {
        JsonObject ingestion = this.mongoDBJsonStore.getIngestion(this.securityTokenContainer.getTenant(), dataLakeId);
        return ingestion;
    }

    public void ingestData(String agentId, String entity, DataFetchAgent dataFetchAgent){
        if(this.ingestionAgents.get(agentId)==null){
            this.ingestionAgents.put(agentId, new IngestionAgent(entity,dataFetchAgent));
            this.ingestionAgents.get(agentId).start();
        }
    }

    public void ingestData(String agentId, String entity, DataPushAgent dataPushAgent,JsonArray data){
        if(this.ingestionAgents.get(agentId)==null){
            this.ingestionAgents.put(agentId, new IngestionAgent(entity,dataPushAgent));
        }
        this.ingestionAgents.get(agentId).receiveData(data);
    }
}
