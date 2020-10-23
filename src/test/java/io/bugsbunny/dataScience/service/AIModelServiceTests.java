package io.bugsbunny.dataScience.service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.bugsbunny.dataScience.endpoint.ModelIsLive;
import io.bugsbunny.dataScience.endpoint.ModelNotFoundException;
import io.bugsbunny.endpoint.SecurityToken;
import io.bugsbunny.endpoint.SecurityTokenContainer;
import io.bugsbunny.test.components.BaseTest;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import java.nio.charset.StandardCharsets;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class AIModelServiceTests extends BaseTest {
    private static Logger logger = LoggerFactory.getLogger(AIModelServiceTests.class);

    @Inject
    private AIModelService aiModelService;

    @Inject
    private PackagingService packagingService;


    @Test
    public void testTrainJava() throws Exception
    {
        String data = IOUtils.resourceToString("dataScience/saturn_data_train.csv", StandardCharsets.UTF_8,
                Thread.currentThread().getContextClassLoader());
        JsonObject input = new JsonObject();
        input.addProperty("format", "csv");
        input.addProperty("data", data);

        Response dataSetResponse = given().body(input.toString()).when().post("/dataset/storeTrainingDataSet/").andReturn();
        logger.info("************************");
        logger.info(dataSetResponse.statusLine());
        dataSetResponse.body().prettyPrint();
        logger.info("************************");
        assertEquals(200, dataSetResponse.getStatusCode());

        JsonObject returnValue = JsonParser.parseString(dataSetResponse.body().asString()).getAsJsonObject();
        long dataSetId = returnValue.get("dataSetId").getAsLong();

        String modelPackage = IOUtils.resourceToString("dataScience/aiplatform-model.json", StandardCharsets.UTF_8,
                Thread.currentThread().getContextClassLoader());

        JsonObject response = this.packagingService.performPackaging(modelPackage);

        long modelId = response.get("modelId").getAsLong();
        String result = this.aiModelService.trainJava(modelId, new long[]{dataSetId});
        logger.info("****************");
        logger.info("ModelId: "+modelId);
        logger.info("****************");
        assertNotNull(result);
        logger.info(result.toString());
    }



    @Test
    public void testCannotTrainLiveModel() throws Exception
    {
        String modelPackage = IOUtils.resourceToString("dataScience/aiplatform-model.json", StandardCharsets.UTF_8,
                Thread.currentThread().getContextClassLoader());
        JsonObject response = this.packagingService.performPackaging(modelPackage);
        long modelId = response.get("modelId").getAsLong();
        logger.info("****************");
        logger.info("ModelId: "+modelId);
        logger.info("****************");

        //Make sure model is not deployed
        response = this.packagingService.getModelPackage(modelId);
        response.remove("model");
        logger.info(response.toString());
        assertFalse(response.get("live").getAsBoolean());

        this.aiModelService.deployModel(modelId);
        response = this.packagingService.getModelPackage(modelId);
        response.remove("model");
        logger.info(response.toString());
        assertTrue(response.get("live").getAsBoolean());

        boolean isModelLive = false;
        try {
            this.aiModelService.trainJava(modelId, null);
        }
        catch(ModelIsLive modelIsLive)
        {
            logger.info(modelIsLive.getMessage());
            isModelLive = true;
        }
        assertTrue(isModelLive);
    }

    @Test
    public void testTrainingModelNotFound() throws Exception
    {
        long modelId = 0l;
        boolean modelNotFound = false;
        try {
            this.aiModelService.trainJava(modelId, null);
        }
        catch(ModelNotFoundException modelNotFoundException)
        {
            logger.info(modelNotFoundException.getMessage());
            modelNotFound = true;
        }
        assertTrue(modelNotFound);
    }

    @Test
    public void testDeployModel() throws Exception
    {
        String modelPackage = IOUtils.resourceToString("dataScience/aiplatform-model.json", StandardCharsets.UTF_8,
                Thread.currentThread().getContextClassLoader());
        JsonObject response = this.packagingService.performPackaging(modelPackage);
        long modelId = response.get("modelId").getAsLong();
        logger.info("****************");
        logger.info("ModelId: "+modelId);
        logger.info("****************");

        //Make sure model is not deployed
        response = this.packagingService.getModelPackage(modelId);
        response.remove("model");
        logger.info(response.toString());
        assertFalse(response.get("live").getAsBoolean());

        this.aiModelService.deployModel(modelId);
        response = this.packagingService.getModelPackage(modelId);
        response.remove("model");
        logger.info(response.toString());
        assertTrue(response.get("live").getAsBoolean());
    }

    @Test
    public void testDeployModelNotFound() throws Exception
    {
        long modelId = 0l;
        boolean modelNotFound = false;
        try {
            this.aiModelService.deployModel(modelId);
        }
        catch(ModelNotFoundException modelNotFoundException)
        {
            logger.info(modelNotFoundException.getMessage());
            modelNotFound = true;
        }
        assertTrue(modelNotFound);
    }

    @Test
    public void testEvalJava() throws Exception
    {
        String data = IOUtils.resourceToString("dataScience/saturn_data_train.csv", StandardCharsets.UTF_8,
                Thread.currentThread().getContextClassLoader());
        JsonObject input = new JsonObject();
        input.addProperty("format", "csv");
        input.addProperty("data", data);

        Response dataSetResponse = given().body(input.toString()).when().post("/dataset/storeTrainingDataSet/").andReturn();
        logger.info("************************");
        logger.info(dataSetResponse.statusLine());
        dataSetResponse.body().prettyPrint();
        logger.info("************************");
        assertEquals(200, dataSetResponse.getStatusCode());

        JsonObject returnValue = JsonParser.parseString(dataSetResponse.body().asString()).getAsJsonObject();
        long dataSetId = returnValue.get("dataSetId").getAsLong();

        String modelPackage = IOUtils.resourceToString("dataScience/aiplatform-model.json", StandardCharsets.UTF_8,
                Thread.currentThread().getContextClassLoader());

        JsonObject response = this.packagingService.performPackaging(modelPackage);

        long modelId = response.get("modelId").getAsLong();
        this.aiModelService.deployModel(modelId);
        String result = this.aiModelService.evalJava(modelId, new long[]{dataSetId});
        logger.info("****************");
        logger.info("ModelId: "+modelId);
        logger.info("****************");
        assertNotNull(result);
        logger.info(result.toString());
    }
}
