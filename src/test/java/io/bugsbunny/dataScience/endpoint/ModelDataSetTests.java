package io.bugsbunny.dataScience.endpoint;

import com.google.gson.JsonParser;
import io.bugsbunny.dataScience.service.ModelDataSetService;
import io.bugsbunny.dataScience.service.PackagingService;

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

import com.google.gson.JsonObject;

@QuarkusTest
public class ModelDataSetTests extends BaseTest {
    private static Logger logger = LoggerFactory.getLogger(ModelDataSetTests.class);

    @Inject
    private ModelDataSetService modelDataSetService;

    @Inject
    private PackagingService packagingService;

    @Test
    public void testStoreTrainingDataSet() throws Exception
    {
        String data = IOUtils.resourceToString("dataScience/saturn_data_train.csv", StandardCharsets.UTF_8,
                Thread.currentThread().getContextClassLoader());
        JsonObject input = new JsonObject();
        input.addProperty("format", "csv");
        input.addProperty("data", data);

        Response response = given().body(input.toString()).when().post("/dataset/storeTrainingDataSet/").andReturn();
        logger.info("************************");
        logger.info(response.statusLine());
        response.body().prettyPrint();
        logger.info("************************");
        assertEquals(200, response.getStatusCode());

        JsonObject returnValue = JsonParser.parseString(response.body().asString()).getAsJsonObject();
        String dataSetId = returnValue.get("dataSetId").getAsString();
        response = given().get("/dataset/readDataSet/?dataSetId=" + dataSetId).andReturn();
        returnValue = JsonParser.parseString(response.body().asString()).getAsJsonObject();
        String storedData = returnValue.get("data").getAsString();
        assertEquals(data, storedData);
        assertEquals("training", returnValue.get("dataSetType").getAsString());
    }

    @Test
    public void testStoreEvalDataSet() throws Exception
    {
        String modelPackage = IOUtils.resourceToString("dataScience/aiplatform-model.json", StandardCharsets.UTF_8,
                Thread.currentThread().getContextClassLoader());

        JsonObject liveModelDeployedJson = this.packagingService.performPackagingForLiveDeployment(modelPackage);
        String modelId = liveModelDeployedJson.get("modelId").getAsString();

        String data = IOUtils.resourceToString("dataScience/saturn_data_eval.csv", StandardCharsets.UTF_8,
                Thread.currentThread().getContextClassLoader());
        JsonObject input = new JsonObject();
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
        response = given().get("/dataset/readDataSet/?dataSetId="+dataSetId).andReturn();
        returnValue = JsonParser.parseString(response.body().asString()).getAsJsonObject();
        String storedData = returnValue.get("data").getAsString();
        assertEquals(data, storedData);
        assertEquals("evaluation", returnValue.get("dataSetType").getAsString());
    }
}