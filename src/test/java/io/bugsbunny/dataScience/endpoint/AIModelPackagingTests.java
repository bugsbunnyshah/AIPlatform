package io.bugsbunny.dataScience.endpoint;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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

import io.bugsbunny.endpoint.SecurityToken;
import io.bugsbunny.endpoint.SecurityTokenContainer;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
public class AIModelPackagingTests extends BaseTest
{
    private static Logger logger = LoggerFactory.getLogger(AIModelPackagingTests.class);

    @Test
    public void testGetModel() throws Exception
    {
        String modelPackage = IOUtils.resourceToString("dataScience/aiplatform-model.json", StandardCharsets.UTF_8,
                Thread.currentThread().getContextClassLoader());

        JsonObject input = JsonParser.parseString(modelPackage).getAsJsonObject();
        Response response = given().body(input.toString()).when().post("/aimodel/performPackaging/").andReturn();
        logger.info("************************");
        logger.info(response.statusLine());
        response.body().prettyPrint();
        logger.info("************************");
        assertEquals(200, response.getStatusCode());

        JsonObject responseJson = JsonParser.parseString(response.body().asString()).getAsJsonObject();
        long modelId = responseJson.get("modelId").getAsLong();
        Response modelResponse = given().get("/aimodel/model/?modelId="+modelId).andReturn();
        logger.info(modelResponse.body().asString());
    }
}