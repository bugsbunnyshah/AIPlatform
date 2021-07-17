package io.bugsbunny.dataIngestion.service;

import com.google.gson.JsonObject;
import io.bugsbunny.infrastructure.MongoDBJsonStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.Serializable;

@ApplicationScoped
public class IngestionService implements Serializable {
    private static Logger logger = LoggerFactory.getLogger(IngestionService.class);

    @Inject
    private MongoDBJsonStore mongoDBJsonStore;

    public JsonObject ingestDevModelData(String data)
    {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("data", data);

        //long dataLakeId = this.mongoDBJsonStore.storeIngestion(jsonObject);
        //jsonObject.addProperty("dataLakeId", dataLakeId);

        return jsonObject;
    }

    public JsonObject readDataLakeData(long dataLakeId)
    {
        JsonObject ingestion = this.mongoDBJsonStore.getIngestion(dataLakeId);
        return ingestion;
    }
}
