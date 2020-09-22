package io.bugsbunny.dataIngestion.service;

import com.google.gson.JsonObject;
import io.bugsbunny.persistence.MongoDBJsonStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.UUID;

@ApplicationScoped
public class IngestionService {
    private static Logger logger = LoggerFactory.getLogger(IngestionService.class);

    @Inject
    private MongoDBJsonStore mongoDBJsonStore;

    public void ingestDevModelData(String data)
    {
        JsonObject jsonObject = new JsonObject();

        String oid = UUID.randomUUID().toString();
        int offset = 10;
        jsonObject.addProperty("oid", oid);
        jsonObject.addProperty("offset", offset);
        jsonObject.addProperty("data", data);

        //TODO: Reconcile
        //this.mongoDBJsonStore.storeDevModels(jsonObject);
    }
}
