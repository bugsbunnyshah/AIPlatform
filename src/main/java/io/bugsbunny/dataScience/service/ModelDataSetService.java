package io.bugsbunny.dataScience.service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.bugsbunny.persistence.MongoDBJsonStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class ModelDataSetService {
    private static Logger logger = LoggerFactory.getLogger(ModelDataSetService.class);

    @Inject
    private MongoDBJsonStore mongoDBJsonStore;

    public long storeTrainingDataSet(String dataFormat, String data)
    {
        return this.mongoDBJsonStore.storeDataSet(dataFormat, "training", data);
    }

    public long storeEvalDataSet(String dataFormat, String data)
    {
        return this.mongoDBJsonStore.storeDataSet(dataFormat, "eval", data);
    }
}
