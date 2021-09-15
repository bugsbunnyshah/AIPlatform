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

    public JsonObject performPackagingForLiveDeployment(String packageString)
    {
        JsonObject jsonObject = new JsonObject();

        JsonObject modelPackage = JsonParser.parseString(packageString).getAsJsonObject();
        modelPackage.addProperty("live", false);
        String modelId = this.mongoDBJsonStore.storeModel(this.securityTokenContainer.getTenant(),modelPackage);

        jsonObject.addProperty("modelId", modelId);
        return jsonObject;
    }

    public JsonObject performPackagingForDevelopment(String packageString)
    {
        JsonObject modelPackage = JsonParser.parseString(packageString).getAsJsonObject();
        modelPackage.addProperty("live", false);
        modelPackage.addProperty("development",true);
        String modelId = this.mongoDBJsonStore.storeModel(this.securityTokenContainer.getTenant(),modelPackage);


        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("modelId", modelId);

        return jsonObject;
    }

    public JsonObject getModelPackage(String modelId)
    {
        return this.mongoDBJsonStore.getModelPackage(this.securityTokenContainer.getTenant(),modelId);
    }

    public String getModel(String modelId)
    {
        return this.mongoDBJsonStore.getModel(this.securityTokenContainer.getTenant(),modelId);
    }
}
