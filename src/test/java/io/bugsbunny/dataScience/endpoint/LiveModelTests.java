package io.bugsbunny.dataScience.endpoint;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.bugsbunny.data.history.service.DataReplayService;
import io.bugsbunny.dataScience.service.PackagingService;
import io.bugsbunny.preprocess.AITrafficAgent;
import io.bugsbunny.test.components.BaseTest;
import io.quarkus.test.junit.QuarkusTest;
import org.apache.commons.io.IOUtils;
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
    private DataReplayService dataReplayService;

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
        String modelId = input.get("modelId").getAsString();

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
        String dataSetId = returnValue.get("dataSetId").getAsString();
        input = new JsonObject();
        JsonArray dataSetIdArray = new JsonArray();
        dataSetIdArray.add(dataSetId);
        input.addProperty("modelId", modelId);
        input.add("dataSetIds", dataSetIdArray);

        //Deploy the model
        JsonObject deployModel = new JsonObject();
        deployModel.addProperty("modelId", modelId);
        given().body(deployModel.toString()).when().post("/liveModel/deployJavaModel").andReturn();

        response = given().body(input.toString()).when().post("/liveModel/evalJava").andReturn();
        logger.info("************************");
        logger.info(response.statusLine());
        response.body().prettyPrint();
        logger.info("************************");
        assertEquals(200, response.getStatusCode());
        assertNotNull(JsonParser.parseString(response.body().asString()).getAsJsonObject().get("dataHistoryId"));
    }

    @Test
    public void testEvalJavaFromDataLake() throws Exception
    {
        String modelPackage = IOUtils.resourceToString("dataScience/aiplatform-model.json", StandardCharsets.UTF_8,
                Thread.currentThread().getContextClassLoader());

        JsonObject input = this.packagingService.performPackaging(modelPackage);
        JsonObject trainingModelDeployedJson = this.packagingService.performPackaging(modelPackage);
        String modelId = trainingModelDeployedJson.get("modelId").getAsString();

        String xml = IOUtils.toString(Thread.currentThread().getContextClassLoader()
                        .getResourceAsStream("dataMapper/people.xml"),
                StandardCharsets.UTF_8);

        input = new JsonObject();
        input.addProperty("sourceSchema", xml);
        input.addProperty("destinationSchema", xml);
        input.addProperty("sourceData", xml);
        input.addProperty("entity","saturn");


        Response ingestionResponse = given().body(input.toString()).when().post("/dataMapper/mapXml/")
                .andReturn();

        String jsonResponse = ingestionResponse.getBody().prettyPrint();
        logger.info("****");
        logger.info(ingestionResponse.getStatusLine());
        logger.info(jsonResponse);
        logger.info("****");
        assertEquals(200, ingestionResponse.getStatusCode());

        //assert the body
        JsonObject ingestedData = JsonParser.parseString(jsonResponse).getAsJsonObject();
        assertNotNull(ingestedData.get("dataLakeId"));
        input = new JsonObject();
        JsonArray jsonArray = new JsonArray();
        jsonArray.add(ingestedData.get("dataLakeId").getAsString());
        input.addProperty("modelId", modelId);
        input.add("dataLakeIds", jsonArray);

        //Deploy the model
        JsonObject deployModel = new JsonObject();
        deployModel.addProperty("modelId", modelId);
        given().body(deployModel.toString()).when().post("/liveModel/deployJavaModel").andReturn();

        Response response = given().body(input.toString()).when().post("/liveModel/evalJavaFromDataLake")

                .andReturn();
        logger.info("************************");
        logger.info(response.statusLine());
        response.body().prettyPrint();
        logger.info("************************");
        assertEquals(200, response.getStatusCode());
        assertNotNull(JsonParser.parseString(response.body().asString()).getAsJsonObject().get("dataHistoryId"));
    }

    @Test
    public void testRetrain() throws Exception
    {
        String modelPackage = IOUtils.resourceToString("dataScience/aiplatform-model.json", StandardCharsets.UTF_8,
                Thread.currentThread().getContextClassLoader());

        JsonObject input = this.packagingService.performPackaging(modelPackage);
        String modelId = input.get("modelId").getAsString();

        String data = IOUtils.resourceToString("dataScience/saturn_data_eval.csv", StandardCharsets.UTF_8,
                Thread.currentThread().getContextClassLoader());
        JsonArray dataSetIdArray = new JsonArray();
        for(int i=0; i<3; i++)
        {
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
            String dataSetId = returnValue.get("dataSetId").getAsString();
            dataSetIdArray.add(dataSetId);
        }


        input = new JsonObject();
        input.addProperty("modelId", modelId);
        input.add("dataSetIds", dataSetIdArray);

        //Deploy the model
        JsonObject deployModel = new JsonObject();
        deployModel.addProperty("modelId", modelId);
        given().body(deployModel.toString()).when().post("/liveModel/deployJavaModel").andReturn();

        Response response = given().body(input.toString()).when().post("/liveModel/evalJava").andReturn();
        logger.info("************************");
        logger.info(response.statusLine());
        response.body().prettyPrint();
        logger.info("************************");
        assertEquals(200, response.getStatusCode());
        assertNotNull(JsonParser.parseString(response.body().asString()).getAsJsonObject().get("dataHistoryId"));

        response = given().body(input.toString()).when().post("/liveModel/retrain").andReturn();
        logger.info("************************");
        logger.info(response.statusLine());
        response.body().prettyPrint();
        logger.info("************************");
        assertEquals(200, response.getStatusCode());

        response = given().body(input.toString()).when().post("/liveModel/evalJava").andReturn();
        logger.info("************************");
        logger.info(response.statusLine());
        logger.info("************************");
        assertEquals(422, response.getStatusCode());
        response.body().prettyPrint();

        //Start the retraining
        response = given().body(input.toString()).when().post("/trainModel/trainJava").andReturn();
        logger.info("************************");
        logger.info(response.statusLine());
        logger.info("************************");
        assertEquals(200, response.getStatusCode());
        response.body().prettyPrint();
        assertNotNull(JsonParser.parseString(response.body().asString()).getAsJsonObject().get("dataHistoryId"));
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
        String modelId = modelDeployedJson.get("modelId").getAsString();

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

        String dataSetId = JsonParser.parseString(response.body().asString()).getAsJsonObject().get("dataSetId").getAsString();
        input = new JsonObject();
        input.addProperty("modelId", modelId);
        input.addProperty("dataSetId", dataSetId);
        response = given().body(input.toString()).when().post("/liveModel/evalPython/").andReturn();
        logger.info("************************");
        logger.info(response.statusLine());
        response.body().prettyPrint();
        logger.info("************************");
    }
}