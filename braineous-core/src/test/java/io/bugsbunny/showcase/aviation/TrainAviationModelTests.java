package io.bugsbunny.showcase.aviation;

import io.bugsbunny.test.components.BaseTest;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class TrainAviationModelTests extends BaseTest
{
    private static Logger logger = LoggerFactory.getLogger(TrainAviationModelTests.class);

    @Test
    public void testTrainAviationAIModel() throws Exception
    {
        Response response = given()
                .get("/aviation/train")
                .andReturn();
        response.body().prettyPrint();
        assertEquals(200, response.statusCode());
    }
}
