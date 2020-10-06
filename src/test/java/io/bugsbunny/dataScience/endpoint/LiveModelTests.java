package io.bugsbunny.dataScience.endpoint;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.bugsbunny.data.history.service.PayloadReplayService;
import io.bugsbunny.dataScience.service.AIModelService;
import io.bugsbunny.dataScience.service.PackagingService;
import io.bugsbunny.endpoint.AITrafficAgent;
import io.bugsbunny.endpoint.SecurityToken;
import io.bugsbunny.endpoint.SecurityTokenContainer;
import io.bugsbunny.test.components.BaseTest;
import io.bugsbunny.test.components.SecurityTokenMockComponent;
import io.quarkus.test.junit.QuarkusTest;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.restassured.response.Response;

import javax.inject.Inject;

import java.nio.charset.StandardCharsets;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
public class LiveModelTests extends BaseTest {
    private static Logger logger = LoggerFactory.getLogger(LiveModelTests.class);

    @Inject
    private PayloadReplayService payloadReplayService;

    @Inject
    private AITrafficAgent aiTrafficAgent;

    @Inject
    private PackagingService packagingService;

    @Test
    public void testEvalJava() throws Exception
    {
        String modelPackage = IOUtils.resourceToString("dataScience/aiplatform-model.json", StandardCharsets.UTF_8,
                Thread.currentThread().getContextClassLoader());

        JsonObject input = this.packagingService.performPackaging(modelPackage);
        long modelId = input.get("modelId").getAsLong();

        String data = IOUtils.resourceToString("dataScience/saturn_data_eval.csv", StandardCharsets.UTF_8,
                Thread.currentThread().getContextClassLoader());
        input = new JsonObject();
        input.addProperty("modelId", modelId);
        input.addProperty("format", "csv");
        input.addProperty("data", data);

        Response response = given().body(input.toString()).when().post("/dataset/storeEvalDataSet/").andReturn();
        logger.info("************************");
        logger.info(response.statusLine());
        response.body().prettyPrint();
        logger.info("************************");
        assertEquals(200, response.getStatusCode());
        JsonObject returnValue = JsonParser.parseString(response.body().asString()).getAsJsonObject();
        long dataSetId = returnValue.get("dataSetId").getAsLong();
        input = new JsonObject();
        JsonArray dataSetIdArray = new JsonArray();
        dataSetIdArray.add(dataSetId);
        input.addProperty("modelId", modelId);
        input.add("dataSetIds", dataSetIdArray);

        response = given().body(input.toString()).when().post("/liveModel/evalJava").andReturn();
        logger.info("************************");
        logger.info(response.statusLine());
        logger.info("************************");
        assertEquals(200, response.getStatusCode());
    }

    @Test
    public void testEvalPython() throws Exception
    {
        String pythonScript = IOUtils.resourceToString("dataScience/train.py", StandardCharsets.UTF_8,
                Thread.currentThread().getContextClassLoader());

        String modelPackage = IOUtils.resourceToString("dataScience/aiplatform-python-model.json", StandardCharsets.UTF_8,
                Thread.currentThread().getContextClassLoader());
        JsonObject modelPackageJson = JsonParser.parseString(modelPackage).getAsJsonObject();
        modelPackageJson.addProperty("script", pythonScript);

        JsonObject modelDeployedJson = this.packagingService.performPackaging(modelPackageJson.toString());
        long modelId = modelDeployedJson.get("modelId").getAsLong();

        String data = IOUtils.resourceToString("dataScience/numpyTest.csv", StandardCharsets.UTF_8,
                Thread.currentThread().getContextClassLoader());
        JsonObject input = new JsonObject();
        input.addProperty("modelId", modelId);
        input.addProperty("format", "csv");
        input.addProperty("data", data);

        Response response = given().body(input.toString()).when().post("/dataset/storeEvalDataSet/").andReturn();
        logger.info("************************");
        logger.info(response.statusLine());
        response.body().prettyPrint();
        logger.info("modelId: "+modelId);
        logger.info("************************");
        assertEquals(200, response.getStatusCode());

        long dataSetId = JsonParser.parseString(response.body().asString()).getAsJsonObject().get("dataSetId").getAsLong();
        input = new JsonObject();
        input.addProperty("modelId", modelId);
        input.addProperty("dataSetId", dataSetId);
        response = given().body(input.toString()).when().post("/liveModel/evalPython/").andReturn();
        logger.info("************************");
        logger.info(response.statusLine());
        response.body().prettyPrint();
        logger.info("************************");
        //assertEquals(200, response.getStatusCode());

        //Assert
        //String output = JsonParser.parseString(response.body().asString()).getAsJsonObject().get("output").getAsString();
        //assertNotNull(output);
    }
}