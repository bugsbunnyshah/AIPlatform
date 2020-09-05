package io.bugsbunny.data.history.service;

import com.google.gson.JsonObject;
import io.bugsbunny.data.history.ObjectDiffAlgorithm;
import io.bugsbunny.persistence.MongoDBJsonStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

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

    public void addToDiffChain(String chainId, JsonObject payload)
    {
        JsonObject lastPayload = this.mongoDBJsonStore.getLastPayload(chainId);
        JsonObject objectDiff = this.objectDiffAlgorithm.diff(lastPayload,payload);

        this.mongoDBJsonStore.addToDiffChain(chainId, payload);
        this.mongoDBJsonStore.addToDiff(chainId, objectDiff);
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
}
