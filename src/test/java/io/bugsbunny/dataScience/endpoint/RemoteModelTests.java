package io.bugsbunny.dataScience.endpoint;

import com.google.gson.JsonObject;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
public class RemoteModelTests {
    private static Logger logger = LoggerFactory.getLogger(RemoteModelTests.class);

    @Test
    public void testInvocations() throws Exception
    {
        JsonObject input = new JsonObject();

        Response response = given().body(input.toString()).when().post("/remoteModel/mlflow/invocations").andReturn();
        logger.info("************************");
        logger.info(response.statusLine());
        logger.info("************************");
        assertEquals(200, response.getStatusCode());
    }
}