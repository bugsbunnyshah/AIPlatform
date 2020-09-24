package io.bugsbunny.data.history.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.bugsbunny.data.history.ObjectDiffAlgorithm;
import io.bugsbunny.persistence.MongoDBJsonStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.*;

@ApplicationScoped
public class PayloadReplayService {
    private static Logger logger = LoggerFactory.getLogger(PayloadReplayService.class);

    @Inject
    private ObjectDiffAlgorithm objectDiffAlgorithm;

    @Inject
    private MongoDBJsonStore mongoDBJsonStore;

    public String generateDiffChain(JsonObject payload)
    {
        String chainId = this.mongoDBJsonStore.startDiffChain(payload);
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
        /*logger.info("**********DEBUG************");
        logger.info(top.toString());
        logger.info("***************************");*/
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
        else
        {
            Iterator<JsonElement> itr = top.getAsJsonArray().iterator();
            while(itr.hasNext())
            {
                JsonElement local = itr.next();
                if(local.isJsonObject()) {
                    JsonObject localAsJsonObject = local.getAsJsonObject();
                    JsonObject lastPayload = this.mongoDBJsonStore.getLastPayload(chainId);
                    JsonObject objectDiff = this.objectDiffAlgorithm.diff(lastPayload, localAsJsonObject);
                    this.mongoDBJsonStore.addToDiffChain(chainId, localAsJsonObject);
                    this.mongoDBJsonStore.addToDiff(chainId, objectDiff);
                }
                else if(local.isJsonArray())
                {
                    //TODO: DEAL_WITH_ARRAY
                }
                else if(local.isJsonPrimitive())
                {
                    JsonObject localAsJsonObject = new JsonObject();
                    localAsJsonObject.addProperty("value", local.getAsJsonPrimitive().toString());
                    JsonObject lastPayload = this.mongoDBJsonStore.getLastPayload(chainId);
                    JsonObject objectDiff = this.objectDiffAlgorithm.diff(lastPayload, localAsJsonObject);
                    this.mongoDBJsonStore.addToDiffChain(chainId, localAsJsonObject);
                    this.mongoDBJsonStore.addToDiff(chainId, objectDiff);
                }
            }
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
                JsonObject localAsJsonObject = new JsonObject();
                localAsJsonObject.addProperty("value", localAsJsonObject.getAsJsonPrimitive().toString());
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
            this.addToDiffChain(chainId, top.getAsJsonObject());
        }
        else
        {
            Iterator<JsonElement> itr = top.getAsJsonArray().iterator();
            while(itr.hasNext())
            {
                JsonElement local = itr.next();
                if(local.isJsonObject()) {
                    JsonObject localAsJsonObject = local.getAsJsonObject();
                    JsonObject lastPayload = this.mongoDBJsonStore.getLastPayload(chainId);
                    JsonObject objectDiff = this.objectDiffAlgorithm.diff(lastPayload, localAsJsonObject);
                    this.mongoDBJsonStore.addToDiffChain(requestChainId, chainId, localAsJsonObject);
                    this.mongoDBJsonStore.addToDiff(requestChainId, chainId, objectDiff);
                }
                else if(local.isJsonArray())
                {
                    //TODO: DEAL_WITH_ARRAY
                }
                else if(local.isJsonPrimitive())
                {
                    JsonObject localAsJsonObject = new JsonObject();
                    localAsJsonObject.addProperty("value", local.getAsJsonPrimitive().toString());
                    JsonObject lastPayload = this.mongoDBJsonStore.getLastPayload(chainId);
                    JsonObject objectDiff = this.objectDiffAlgorithm.diff(lastPayload, localAsJsonObject);
                    this.mongoDBJsonStore.addToDiffChain(chainId, localAsJsonObject);
                    this.mongoDBJsonStore.addToDiff(chainId, objectDiff);
                }
            }
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
                JsonObject localAsJsonObject = new JsonObject();
                localAsJsonObject.addProperty("value", localAsJsonObject.getAsJsonPrimitive().toString());
                this.addToDiffChain(chainId, localAsJsonObject);
            }
        }
    }

    public void addToDiffChain(String chainId, JsonObject payload)
    {
        JsonObject lastPayload = this.mongoDBJsonStore.getLastPayload(chainId);
        JsonObject objectDiff = this.objectDiffAlgorithm.diff(lastPayload,payload);

        this.mongoDBJsonStore.addToDiffChain(chainId, payload);
        this.mongoDBJsonStore.addToDiff(chainId, objectDiff);
    }

    public void addToDiffChain(String requestChainId, String chainId, JsonObject payload)
    {
        JsonObject lastPayload = this.mongoDBJsonStore.getLastPayload(chainId);
        JsonObject objectDiff = this.objectDiffAlgorithm.diff(lastPayload,payload);

        this.mongoDBJsonStore.addToDiffChain(requestChainId, chainId, payload);
        this.mongoDBJsonStore.addToDiff(requestChainId, chainId, objectDiff);
    }

    public List<JsonObject> replayDiffChain(String chainId)
    {
        java.util.List<JsonObject> replayChain = new ArrayList<>();

        List<JsonObject> diffChain = this.mongoDBJsonStore.readDiffChain(chainId);
        List<JsonObject> objectDiffs = this.mongoDBJsonStore.readDiffs(chainId);

        replayChain.add(diffChain.get(0).getAsJsonObject("payload"));
        int length = objectDiffs.size();
        for(int i=0; i<length; i++)
        {
            JsonObject objectDiff = objectDiffs.get(i).getAsJsonObject("objectDiff");
            JsonObject payload = diffChain.get(i+1).getAsJsonObject("payload");;
            replayChain.add(this.objectDiffAlgorithm.merge(payload, objectDiff));
        }

        return replayChain;
    }

    public Map<String,List<JsonObject>> replayDiffChain(String region, String principal)
    {
        Map<String, List<JsonObject>> replayChainMap = new HashMap<>();

        List<JsonObject> diffChain = this.mongoDBJsonStore.readDiffChain(region, principal);
        List<JsonObject> objectDiffs = this.mongoDBJsonStore.readDiffs(region, principal);


        List<JsonObject> replayChain = new ArrayList<>();
        JsonObject top = diffChain.get(0).getAsJsonObject("payload");
        String chainId = diffChain.get(0).get("chainId").getAsString();
        replayChain.add(top);
        replayChainMap.put(chainId, replayChain);
        int length = objectDiffs.size();
        for(int i=0; i<length; i++)
        {
            JsonObject objectDiff = objectDiffs.get(i).getAsJsonObject("objectDiff");
            JsonObject payload = diffChain.get(i+1).getAsJsonObject("payload");
            String currentChainId = diffChain.get(i+1).get("chainId").getAsString();
            if(!chainId.equals(currentChainId))
            {
                chainId = currentChainId;
                replayChain = new ArrayList<>();
                replayChainMap.put(chainId, replayChain);
                replayChain.add(payload);
            }
            else
            {
                replayChain.add(this.objectDiffAlgorithm.merge(payload, objectDiff));
            }
        }

        return replayChainMap;
    }
}
