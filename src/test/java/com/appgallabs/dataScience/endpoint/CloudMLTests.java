package com.appgallabs.dataScience.endpoint;


import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import com.appgallabs.test.components.BaseTest;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class CloudMLTests extends BaseTest
{
    private static Logger logger = LoggerFactory.getLogger(CloudMLTests.class);

    @Test
    public void executeScript() throws Exception
    {
        String script = IOUtils.toString(Thread.currentThread().getContextClassLoader().
                        getResourceAsStream("cloudml/createModel.py"),
                StandardCharsets.UTF_8);
        JsonObject json = new JsonObject();
        json.addProperty("script",script);

        String url = "/cloudml/executeScript/";
        Response response = given().body(json.toString()).post(url).andReturn();
        response.getBody().prettyPrint();
        JsonObject result = JsonParser.parseString(response.getBody().asString()).getAsJsonObject();
        assertEquals(result.get("message").getAsString(),"script_execution_success");
    }
}