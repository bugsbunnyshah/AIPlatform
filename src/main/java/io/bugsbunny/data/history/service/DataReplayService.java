package io.bugsbunny.data.history.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.bugsbunny.data.history.ObjectDiffAlgorithm;
import io.bugsbunny.dataIngestion.service.ChainNotFoundException;
import io.bugsbunny.infrastructure.MongoDBJsonStore;
import io.bugsbunny.preprocess.SecurityTokenContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.*;

@ApplicationScoped
public class DataReplayService {
    private static Logger logger = LoggerFactory.getLogger(DataReplayService.class);

    @Inject
    private ObjectDiffAlgorithm objectDiffAlgorithm;

    @Inject
    private MongoDBJsonStore mongoDBJsonStore;

    @Inject
    private SecurityTokenContainer securityTokenContainer;

    public String generateDiffChain(JsonObject payload)
    {
        String chainId = this.mongoDBJsonStore.startDiffChain(this.securityTokenContainer.getTenant(),payload);
        return chainId;
    }

    public String generateDiffChain(JsonArray payload)
    {
        //Validation
        if(payload == null || payload.size() == 0)
        {
            return null;
        }

        JsonElement top = payload.get(0);
        String chainId = null;
        if(top.isJsonObject())
        {
            chainId = this.generateDiffChain(top.getAsJsonObject());
        }
        else if(top.isJsonArray())
        {
            Iterator<JsonElement> itr = top.getAsJsonArray().iterator();
            while(itr.hasNext())
            {
                JsonElement local = itr.next();
                if(local.isJsonObject())
                {
                    chainId = this.generateDiffChain(local.getAsJsonObject());
                }
                else
                {
                    //TODO: DEAL_WITH_ARRAY
                }
            }
        }
        else if(top.isJsonPrimitive())
        {
            JsonObject value = new JsonObject();
            value.addProperty("value", top.getAsJsonPrimitive().toString());
            chainId = this.generateDiffChain(value.getAsJsonObject());
        }
        int length = payload.size();
        for(int i=1; i<length; i++)
        {
            JsonElement local = payload.get(i);
            if(local.isJsonObject()) {
                this.addToDiffChain(chainId, payload.get(i).getAsJsonObject());
            }
            else if(local.isJsonArray())
            {
                this.addToDiffChain(chainId, payload.get(i).getAsJsonArray());
            }
            else if(local.isJsonPrimitive())
            {
                JsonObject value = new JsonObject();
                value.addProperty("value", local.getAsJsonPrimitive().toString());
                chainId = this.generateDiffChain(value.getAsJsonObject());
            }
        }
        return chainId;
    }

    public void addToDiffChain(String chainId, JsonArray payload)
    {
        //Validation
        if(payload == null || payload.size() == 0)
        {
            return;
        }

        JsonElement top = payload.get(0);
        if(top.isJsonObject()) {
            this.addToDiffChain(chainId, top.getAsJsonObject());
        }
        else if(top.isJsonArray())
        {
            Iterator<JsonElement> itr = top.getAsJsonArray().iterator();
            while(itr.hasNext())
            {
                JsonElement local = itr.next();
                if(local.isJsonObject()) {
                    JsonObject localAsJsonObject = local.getAsJsonObject();
                    JsonObject lastPayload = this.mongoDBJsonStore.getLastPayload(this.securityTokenContainer.getTenant(),chainId);
                    JsonObject objectDiff = this.objectDiffAlgorithm.diff(lastPayload, localAsJsonObject);
                    this.mongoDBJsonStore.addToDiffChain(this.securityTokenContainer.getTenant(),chainId, localAsJsonObject);
                    this.mongoDBJsonStore.addToDiff(this.securityTokenContainer.getTenant(),chainId, objectDiff);
                }
                else if(local.isJsonArray())
                {
                    //TODO: DEAL_WITH_ARRAY
                }
                else if(local.isJsonPrimitive())
                {
                    JsonObject localAsJsonObject = new JsonObject();
                    localAsJsonObject.addProperty("value", local.getAsJsonPrimitive().toString());
                    JsonObject lastPayload = this.mongoDBJsonStore.getLastPayload(this.securityTokenContainer.getTenant(),chainId);
                    JsonObject objectDiff = this.objectDiffAlgorithm.diff(lastPayload, localAsJsonObject);
                    this.mongoDBJsonStore.addToDiffChain(this.securityTokenContainer.getTenant(),chainId, localAsJsonObject);
                    this.mongoDBJsonStore.addToDiff(this.securityTokenContainer.getTenant(),chainId, objectDiff);
                }
            }
        }
        else if(top.isJsonPrimitive())
        {
            JsonObject value = new JsonObject();
            value.addProperty("value", top.getAsJsonPrimitive().toString());
            this.addToDiffChain(chainId, value.getAsJsonObject());
        }

        //Rest of the Array
        int length = payload.size();
        for(int i=1; i<length; i++)
        {
            JsonElement local = payload.get(i);
            if(local.isJsonObject()) {
                this.addToDiffChain(chainId, local.getAsJsonObject());
            }
            else if(local.isJsonArray())
            {
                //TODO: DEAL_WITH_ARRAY
            }
            else if(local.isJsonPrimitive())
            {
                JsonObject localAsJsonObject = new JsonObject();
                localAsJsonObject.addProperty("value", local.getAsJsonPrimitive().toString());
                this.addToDiffChain(chainId, localAsJsonObject);
            }
        }
    }

    public void addToDiffChain(String requestChainId, String chainId, JsonArray payload)
    {
        //Validation
        if(payload == null || payload.size() == 0)
        {
            return;
        }

        JsonElement top = payload.get(0);
        if(top.isJsonObject()) {
            this.addToDiffChain(requestChainId, chainId, top.getAsJsonObject());
        }
        else if(top.isJsonArray())
        {
            Iterator<JsonElement> itr = top.getAsJsonArray().iterator();
            while(itr.hasNext())
            {
                JsonElement local = itr.next();
                if(local.isJsonObject()) {
                    JsonObject localAsJsonObject = local.getAsJsonObject();
                    JsonObject lastPayload = this.mongoDBJsonStore.getLastPayload(this.securityTokenContainer.getTenant(),chainId);
                    JsonObject objectDiff = this.objectDiffAlgorithm.diff(lastPayload, localAsJsonObject);
                    this.mongoDBJsonStore.addToDiffChain(this.securityTokenContainer.getTenant(),requestChainId, chainId, localAsJsonObject);
                    this.mongoDBJsonStore.addToDiff(this.securityTokenContainer.getTenant(),requestChainId, chainId, objectDiff);
                }
                else if(local.isJsonArray())
                {
                    //TODO: DEAL_WITH_ARRAY
                }
                else if(local.isJsonPrimitive())
                {
                    JsonObject localAsJsonObject = new JsonObject();
                    localAsJsonObject.addProperty("value", local.getAsJsonPrimitive().toString());
                    JsonObject lastPayload = this.mongoDBJsonStore.getLastPayload(this.securityTokenContainer.getTenant(),chainId);
                    JsonObject objectDiff = this.objectDiffAlgorithm.diff(lastPayload, localAsJsonObject);
                    this.mongoDBJsonStore.addToDiffChain(this.securityTokenContainer.getTenant(),requestChainId, chainId, localAsJsonObject);
                    this.mongoDBJsonStore.addToDiff(this.securityTokenContainer.getTenant(),requestChainId, chainId, objectDiff);
                }
            }
        }
        else if(top.isJsonPrimitive())
        {
            JsonObject value = new JsonObject();
            value.addProperty("value", top.getAsJsonPrimitive().toString());
            this.addToDiffChain(requestChainId, chainId, value.getAsJsonObject());
        }

        //Rest of the Array
        int length = payload.size();
        for(int i=1; i<length; i++)
        {
            JsonElement local = payload.get(i);
            if(local.isJsonObject()) {
                this.addToDiffChain(requestChainId, chainId, local.getAsJsonObject());
            }
            else if(local.isJsonArray())
            {
                //TODO: DEAL_WITH_ARRAY
            }
            else if(local.isJsonPrimitive())
            {
                JsonObject localAsJsonObject = new JsonObject();
                localAsJsonObject.addProperty("value", local.getAsJsonPrimitive().toString());
                this.addToDiffChain(requestChainId, chainId, localAsJsonObject);
            }
        }
    }

    public void addToDiffChain(String chainId, JsonObject payload)
    {
        JsonObject lastPayload = this.mongoDBJsonStore.getLastPayload(this.securityTokenContainer.getTenant(),chainId);
        JsonObject objectDiff = this.objectDiffAlgorithm.diff(lastPayload,payload);

        this.mongoDBJsonStore.addToDiffChain(this.securityTokenContainer.getTenant(),chainId, payload);
        this.mongoDBJsonStore.addToDiff(this.securityTokenContainer.getTenant(),chainId, objectDiff);
    }

    public void addToDiffChain(String requestChainId, String chainId, JsonObject payload)
    {
        JsonObject lastPayload = this.mongoDBJsonStore.getLastPayload(this.securityTokenContainer.getTenant(),chainId);
        JsonObject objectDiff = this.objectDiffAlgorithm.diff(lastPayload,payload);

        this.mongoDBJsonStore.addToDiffChain(this.securityTokenContainer.getTenant(),requestChainId, chainId, payload);
        this.mongoDBJsonStore.addToDiff(this.securityTokenContainer.getTenant(),requestChainId, chainId, objectDiff);
    }

    public List<JsonObject> replayDiffChain(String chainId) throws ChainNotFoundException
    {
        java.util.List<JsonObject> replayChain = new ArrayList<>();

        List<JsonObject> diffChain = this.mongoDBJsonStore.readDiffChain(this.securityTokenContainer.getTenant(),chainId);
        List<JsonObject> objectDiffs = this.mongoDBJsonStore.readDiffs(this.securityTokenContainer.getTenant(),chainId);
        if (diffChain.size() == 0)
        {
            throw new ChainNotFoundException("CHAIN_NOT_FOUND: "+chainId);
        }

        replayChain.add(diffChain.get(0).getAsJsonObject("payload"));
        int length = objectDiffs.size();
        for(int i=0; i<length; i++)
        {
            JsonObject objectDiff = objectDiffs.get(i).getAsJsonObject("objectDiff");
            JsonObject payload = diffChain.get(i+1).getAsJsonObject("payload");
            JsonObject merge = this.objectDiffAlgorithm.merge(payload, objectDiff);
            replayChain.add(merge);
        }

        return replayChain;
    }

    public ObjectDiffAlgorithm getObjectDiffAlgorithm() {
        return objectDiffAlgorithm;
    }

    public void setObjectDiffAlgorithm(ObjectDiffAlgorithm objectDiffAlgorithm) {
        this.objectDiffAlgorithm = objectDiffAlgorithm;
    }

    public MongoDBJsonStore getMongoDBJsonStore() {
        return mongoDBJsonStore;
    }

    public void setMongoDBJsonStore(MongoDBJsonStore mongoDBJsonStore) {
        this.mongoDBJsonStore = mongoDBJsonStore;
    }
}
