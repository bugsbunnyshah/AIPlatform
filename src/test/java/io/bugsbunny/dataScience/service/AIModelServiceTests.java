package io.bugsbunny.dataScience.service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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
    public void testStartEval() throws Exception
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
        String result = this.aiModelService.evalJava(modelId, dataSetId);
        logger.info("****************");
        logger.info("ModelId: "+modelId);
        logger.info("****************");
        assertNotNull(result);
    }
}
