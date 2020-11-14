package io.bugsbunny.dataScience.service;

import com.google.gson.JsonObject;
import io.bugsbunny.infrastructure.MongoDBJsonStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class ModelDataSetService {
    private static Logger logger = LoggerFactory.getLogger(ModelDataSetService.class);

    @Inject
    private MongoDBJsonStore mongoDBJsonStore;

    public long storeTrainingDataSet(JsonObject dataSetJson)
    {

        return this.mongoDBJsonStore.storeTrainingDataSet(dataSetJson);
    }

    public long storeEvalDataSet(JsonObject dataSetJson)
    {
        return this.mongoDBJsonStore.storeEvalDataSet(dataSetJson);
    }

    public JsonObject readDataSet(long dataSetId)
    {
        return this.mongoDBJsonStore.readDataSet(dataSetId);
    }
}
