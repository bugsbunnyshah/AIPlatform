package io.bugsbunny.endpoint;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.restassured.RestAssured.given;
import io.restassured.response.Response;

@QuarkusTest
public class OAuthAgentTests
{
    private static Logger logger = LoggerFactory.getLogger(OAuthAgentTests.class);

    @Test
    public void testOAuthGetStarted() throws Exception
    {
        String json = "[{\"payload\" : { \"Id\" : 7777777, \"Rcvr\" : 77777, \"HasSig\" : true }},{\"payload\" : { \"Id\" : 7777777, \"Rcvr\" : 77777, \"HasSig\" : false }}]";
        Response response = given().body(json).when().post("/replay/map").andReturn();
        logger.info("************************");
        logger.info(response.statusLine());
        logger.info("************************");

        JsonObject jsonObject = JsonParser.parseString(response.body().asString()).getAsJsonObject();
        String oid = jsonObject.get("oid").getAsString();
        response = given().get("/replay/chain/?oid="+oid).andReturn();
        logger.info("************************");
        logger.info(response.statusLine());
        logger.info("************************");
    }
}
