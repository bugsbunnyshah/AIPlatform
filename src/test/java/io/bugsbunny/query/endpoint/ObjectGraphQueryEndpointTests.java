package io.bugsbunny.query.endpoint;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.bugsbunny.util.JsonUtil;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
public class ObjectGraphQueryEndpointTests {
    private static Logger logger = LoggerFactory.getLogger(ObjectGraphQueryEndpointTests.class);


    @Test
    public void queryByCriteria() throws Exception
    {
        JsonObject criteria = new JsonObject();
        criteria.addProperty("size", 100);
        //criteria.addProperty("code", "aus");

        JsonObject json = new JsonObject();
        json.addProperty("entity","airport");
        json.add("criteria",criteria);

        Response response = given().body(json.toString()).when().post("/graph/query/criteria").andReturn();
        String jsonString = response.getBody().print();
        JsonElement responseJson = JsonParser.parseString(jsonString);
        assertEquals(200, response.getStatusCode());
        JsonUtil.print(responseJson);
    }

    @Test
    public void navigateByCriteria() throws Exception
    {
        JsonObject criteria = new JsonObject();
        criteria.addProperty("code","lax");

        JsonObject json = new JsonObject();
        json.addProperty("startEntity","airport");
        json.addProperty("destinationEntity","flight");
        json.addProperty("relationship","arrival");
        json.add("criteria",criteria);

        Response response = given().body(json.toString()).when().post("/graph/query/navigate").andReturn();
        String jsonString = response.getBody().print();
        JsonElement responseJson = JsonParser.parseString(jsonString);
        assertEquals(200, response.getStatusCode());
        JsonUtil.print(responseJson);
    }
}
