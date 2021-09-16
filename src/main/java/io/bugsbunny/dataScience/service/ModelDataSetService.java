package io.bugsbunny.dataScience.service;

import com.google.gson.JsonObject;
import io.bugsbunny.infrastructure.MongoDBJsonStore;
import io.bugsbunny.preprocess.SecurityTokenContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class ModelDataSetService {
    private static Logger logger = LoggerFactory.getLogger(ModelDataSetService.class);

    @Inject
    private MongoDBJsonStore mongoDBJsonStore;

    @Inject
    private SecurityTokenContainer securityTokenContainer;

    public String storeTrainingDataSet(JsonObject dataSetJson)
    {

        return this.mongoDBJsonStore.storeTrainingDataSet(this.securityTokenContainer.getTenant(),dataSetJson);
    }

    public String storeTrainingDataSetInLake(JsonObject dataSetJson)
    {
        return this.mongoDBJsonStore.storeTrainingDataSetInLake(this.securityTokenContainer.getTenant(),dataSetJson);
    }

    public String storeEvalDataSet(JsonObject dataSetJson)
    {
        return this.mongoDBJsonStore.storeEvalDataSet(this.securityTokenContainer.getTenant(),dataSetJson);
    }

    public JsonObject readDataSet(String dataSetId)
    {
        return this.mongoDBJsonStore.readDataSet(this.securityTokenContainer.getTenant(),dataSetId);
    }
}
