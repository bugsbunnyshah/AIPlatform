package io.bugsbunny.dataScience.endpoint;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.bugsbunny.data.history.service.DataReplayService;
import io.bugsbunny.dataScience.service.PackagingService;
import io.bugsbunny.preprocess.AITrafficAgent;

import io.bugsbunny.test.components.BaseTest;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.nio.charset.StandardCharsets;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
public class TrainModelTests extends BaseTest
{
    private static Logger logger = LoggerFactory.getLogger(TrainModelTests.class);

    @Inject
    private DataReplayService dataReplayService;

    @Inject
    private AITrafficAgent aiTrafficAgent;

    @Inject
    private PackagingService packagingService;

    @Test
    public void testTrainJava() throws Exception
    {
        String modelPackage = IOUtils.resourceToString("dataScience/aiplatform-model.json", StandardCharsets.UTF_8,
                Thread.currentThread().getContextClassLoader());

        JsonObject input = this.packagingService.performPackaging(modelPackage);
        JsonObject liveModelDeployedJson = this.packagingService.performPackaging(modelPackage);
        long modelId = liveModelDeployedJson.get("modelId").getAsLong();

        String data = IOUtils.resourceToString("dataScience/saturn_data_train.csv", StandardCharsets.UTF_8,
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

        response = given().body(input.toString()).when().post("/trainModel/trainJava").andReturn();
        logger.info("************************");
        logger.info(response.statusLine());
        response.body().prettyPrint();
        logger.info("************************");
        assertEquals(200, response.getStatusCode());
        assertNotNull(JsonParser.parseString(response.body().asString()).getAsJsonObject().get("dataHistoryId"));
    }

    @Test
    public void testTrainJavaFromDataLake() throws Exception
    {
        String modelPackage = IOUtils.resourceToString("dataScience/aiplatform-model.json", StandardCharsets.UTF_8,
                Thread.currentThread().getContextClassLoader());

        JsonObject input = this.packagingService.performPackaging(modelPackage);
        JsonObject trainingModelDeployedJson = this.packagingService.performPackaging(modelPackage);
        long modelId = trainingModelDeployedJson.get("modelId").getAsLong();

        String xml = IOUtils.toString(Thread.currentThread().getContextClassLoader()
                        .getResourceAsStream("dataMapper/people.xml"),
                StandardCharsets.UTF_8);

        input = new JsonObject();
        input.addProperty("sourceData", xml);


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
        jsonArray.add(ingestedData.get("dataLakeId").getAsLong());
        input.addProperty("modelId", modelId);
        input.add("dataLakeIds", jsonArray);

        Response response = given().body(input.toString()).when().post("/trainModel/trainJavaFromDataLake")

                .andReturn();
        logger.info("************************");
        logger.info(response.statusLine());
        response.body().prettyPrint();
        logger.info("************************");
        assertEquals(200, response.getStatusCode());
        assertNotNull(JsonParser.parseString(response.body().asString()).getAsJsonObject().get("dataHistoryId"));
    }

    @Test
    public void testEvalPython() throws Exception
    {
        String modelPackage = IOUtils.resourceToString("dataScience/aiplatform-python-model.json", StandardCharsets.UTF_8,
                Thread.currentThread().getContextClassLoader());

        JsonObject modelDeployedJson = this.packagingService.performPackaging(modelPackage);
        long modelId = modelDeployedJson.get("modelId").getAsLong();

        String data = IOUtils.resourceToString("dataScience/saturn_data_train.csv", StandardCharsets.UTF_8,
                Thread.currentThread().getContextClassLoader());
        JsonObject input = new JsonObject();
        input.addProperty("modelId", modelId);
        input.addProperty("format", "csv");
        input.addProperty("data", data);

        Response response = given().body(input.toString()).when().post("/dataset/storeTrainingDataSet/").andReturn();
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
        logger.info(input.toString());
        response = given().body(input.toString()).when().post("/trainModel/trainPython/").andReturn();
        logger.info("************************");
        logger.info(response.statusLine());
        response.body().prettyPrint();
        logger.info("************************");
    }

    @Test
    public void testDataHistoryFromDataLake() throws Exception
    {
        String modelPackage = IOUtils.resourceToString("dataScience/aiplatform-model.json", StandardCharsets.UTF_8,
                Thread.currentThread().getContextClassLoader());

        JsonObject input = this.packagingService.performPackaging(modelPackage);
        JsonObject liveModelDeployedJson = this.packagingService.performPackaging(modelPackage);
        long modelId = liveModelDeployedJson.get("modelId").getAsLong();

        String data = IOUtils.resourceToString("dataScience/saturn_data_train.csv", StandardCharsets.UTF_8,
                Thread.currentThread().getContextClassLoader());
        input = new JsonObject();
        input = new JsonObject();
        input.addProperty("sourceData", data);
        input.addProperty("hasHeader", false);

        Response response = given().body(input.toString()).when().post("/dataMapper/mapCsv/").andReturn();
        logger.info("************************");
        logger.info(response.statusLine());
        response.body().prettyPrint();
        logger.info("************************");
        assertEquals(200, response.getStatusCode());
        JsonObject returnValue = JsonParser.parseString(response.body().asString()).getAsJsonObject();
        long dataLakeId = returnValue.get("dataLakeId").getAsLong();
        input = new JsonObject();
        JsonArray dataLakeIdArray = new JsonArray();
        dataLakeIdArray.add(dataLakeId);
        input.addProperty("modelId", modelId);
        input.add("dataLakeIds", dataLakeIdArray);

        response = given().body(input.toString()).when().post("/trainModel/trainJavaFromDataLake").andReturn();
        String dataHistoryId = JsonParser.parseString(response.body().asString()).getAsJsonObject().get("dataHistoryId").getAsString();
        logger.info("************************");
        logger.info(response.statusLine());
        response.body().prettyPrint();
        logger.info("DATA_HISTORY_ID: "+dataHistoryId);
        logger.info("************************");
        assertEquals(200, response.getStatusCode());
        assertNotNull(dataHistoryId);

        String dataHistoryUrl = "/replay/chain/?oid="+dataHistoryId;
        response =  given().when().get(dataHistoryUrl).andReturn();
        logger.info("************************");
        logger.info(response.statusLine());
        response.body().prettyPrint();
        assertEquals(200, response.getStatusCode());
    }

    @Test
    public void testDataHistoryFromDataSet() throws Exception
    {
        String modelPackage = IOUtils.resourceToString("dataScience/aiplatform-model.json", StandardCharsets.UTF_8,
                Thread.currentThread().getContextClassLoader());

        JsonObject input = this.packagingService.performPackaging(modelPackage);
        JsonObject liveModelDeployedJson = this.packagingService.performPackaging(modelPackage);
        long modelId = liveModelDeployedJson.get("modelId").getAsLong();

        String data = IOUtils.resourceToString("dataScience/saturn_data_train.csv", StandardCharsets.UTF_8,
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

        response = given().body(input.toString()).when().post("/trainModel/trainJava").andReturn();
        String dataHistoryId = JsonParser.parseString(response.body().asString()).getAsJsonObject().get("dataHistoryId").getAsString();
        logger.info("************************");
        logger.info(response.statusLine());
        response.body().prettyPrint();
        logger.info("DATA_HISTORY_ID: "+dataHistoryId);
        logger.info("************************");
        assertEquals(200, response.getStatusCode());
        assertNotNull(dataHistoryId);

        String dataHistoryUrl = "/replay/chain/?oid="+dataHistoryId;
        response =  given().when().get(dataHistoryUrl).andReturn();
        logger.info("************************");
        logger.info(response.statusLine());
        response.body().prettyPrint();
        assertEquals(200, response.getStatusCode());
    }
}