package io.bugsbunny.datascience.endpoint;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.restassured.response.Response;
import static io.restassured.RestAssured.given;

@QuarkusTest
public class LiveModelTests {
    private static Logger logger = LoggerFactory.getLogger(LiveModelTests.class);

    @Test
    public void testEval() throws Exception
    {
        Response response = given().body("{}").when().post("/liveModel/eval").andReturn();
        logger.info("************************");
        logger.info(response.statusLine());
        logger.info("************************");
        logger.info(response.body().asString());
        logger.info("************************");
    }
}