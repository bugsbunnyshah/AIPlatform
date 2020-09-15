package io.bugsbunny.data.history.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.bugsbunny.data.history.ObjectDiffAlgorithm;
import io.bugsbunny.persistence.MongoDBJsonStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class PayloadReplayService {
    private static Logger logger = LoggerFactory.getLogger(PayloadReplayService.class);

    @Inject
    private ObjectDiffAlgorithm objectDiffAlgorithm;

    @Inject
    private MongoDBJsonStore mongoDBJsonStore;

    public String generateDiffChain(JsonArray payload)
    {
        //Validation
        if(payload == null || payload.size() == 0)
        {
            return null;
        }

        String chainId = this.generateDiffChain(payload.get(0).getAsJsonObject());
        int length = payload.size();
        for(int i=1; i<length; i++)
        {
            this.addToDiffChain(chainId, payload.get(i).getAsJsonObject());
        }
        return chainId;
    }

    public String generateDiffChain(JsonObject payload)
    {
        String chainId = this.mongoDBJsonStore.startDiffChain(payload);
        return chainId;
    }

    public void addToDiffChain(String chainId, JsonArray payload)
    {
        //Validation
        if(payload == null || payload.size() == 0)
        {
            return;
        }

        this.addToDiffChain(chainId, payload.get(0).getAsJsonObject());
        int length = payload.size();
        for(int i=1; i<length; i++)
        {
            this.addToDiffChain(chainId, payload.get(i).getAsJsonObject());
        }
    }

    public void addToDiffChain(String chainId, JsonObject payload)
    {
        JsonObject lastPayload = this.mongoDBJsonStore.getLastPayload(chainId);
        JsonObject objectDiff = this.objectDiffAlgorithm.diff(lastPayload,payload);

        this.mongoDBJsonStore.addToDiffChain(chainId, payload);
        this.mongoDBJsonStore.addToDiff(chainId, objectDiff);
    }

    public void addToDiffChain(String requestChainId, String chainId, JsonArray payload)
    {
        //Validation
        if(payload == null || payload.size() == 0)
        {
            return;
        }

        this.addToDiffChain(requestChainId, chainId, payload.get(0).getAsJsonObject());
        int length = payload.size();
        for(int i=1; i<length; i++)
        {
            this.addToDiffChain(requestChainId, chainId, payload.get(i).getAsJsonObject());
        }
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
