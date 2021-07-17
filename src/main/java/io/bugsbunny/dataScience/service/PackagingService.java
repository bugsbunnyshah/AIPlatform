package io.bugsbunny.dataScience.service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.bugsbunny.infrastructure.MongoDBJsonStore;
import io.bugsbunny.preprocess.SecurityTokenContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class PackagingService {
    private static Logger logger = LoggerFactory.getLogger(PackagingService.class);

    @Inject
    private MongoDBJsonStore mongoDBJsonStore;

    @Inject
    private SecurityTokenContainer securityTokenContainer;

    public JsonObject performPackaging(String packageString)
    {
        JsonObject jsonObject = new JsonObject();

        JsonObject modelPackage = JsonParser.parseString(packageString).getAsJsonObject();
        modelPackage.addProperty("live", false);
        long modelId = this.mongoDBJsonStore.storeModel(this.securityTokenContainer.getTenant(),modelPackage);

        jsonObject.addProperty("modelId", modelId);
        return jsonObject;
    }

    public JsonObject getModelPackage(long modelId)
    {
        return this.mongoDBJsonStore.getModelPackage(this.securityTokenContainer.getTenant(),modelId);
    }

    public String getModel(long modelId)
    {
        return this.mongoDBJsonStore.getModel(this.securityTokenContainer.getTenant(),modelId);
    }
}
