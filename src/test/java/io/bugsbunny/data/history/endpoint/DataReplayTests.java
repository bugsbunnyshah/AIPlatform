package io.bugsbunny.data.history.endpoint;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.bugsbunny.persistence.MongoDBJsonStore;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
public class DataReplayTests {
    private static Logger logger = LoggerFactory.getLogger(DataReplayTests.class);

    @Test
    public void testChain() throws Exception
    {
        String json = "[{\"payload\" : { \"Id\" : 7777777, \"Rcvr\" : 77777, \"HasSig\" : true }},{\"payload\" : { \"Id\" : 7777777, \"Rcvr\" : 77777, \"HasSig\" : false }}]";
        Response response = given().body(json).when().post("/replay/map").andReturn();
        logger.info("************************");
        logger.info(response.statusLine());
        logger.info("************************");
        logger.info(response.body().asString());
        logger.info("************************");

        JsonObject jsonObject = JsonParser.parseString(response.body().asString()).getAsJsonObject();
        String oid = jsonObject.get("oid").getAsString();
        response = given().get("/replay/chain/?oid="+oid).andReturn();
        logger.info("************************");
        logger.info(response.statusLine());
        logger.info("************************");
        logger.info(response.body().asString());
        logger.info("************************");
    }
}