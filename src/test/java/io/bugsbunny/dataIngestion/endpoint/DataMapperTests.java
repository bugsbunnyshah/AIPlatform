package io.bugsbunny.dataIngestion.endpoint;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.bugsbunny.endpoint.SecurityToken;
import io.bugsbunny.endpoint.SecurityTokenContainer;
import io.bugsbunny.persistence.MongoDBJsonStore;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
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
public class DataMapperTests {
    private static Logger logger = LoggerFactory.getLogger(DataMapperTests.class);

    @Inject
    private MongoDBJsonStore mongoDBJsonStore;

    @Inject
    private SecurityTokenContainer securityTokenContainer;

    @BeforeEach
    public void setUp() throws Exception {
        String securityTokenJson = IOUtils.toString(Thread.currentThread().getContextClassLoader().
                        getResourceAsStream("oauthAgent/token.json"),
                StandardCharsets.UTF_8);
        SecurityToken securityToken = SecurityToken.fromJson(securityTokenJson);
        this.securityTokenContainer.getTokenContainer().set(securityToken);
    }

    @Test
    public void testMapWithOneToOneFields() throws Exception {
        String sourceSchema = IOUtils.toString(Thread.currentThread().getContextClassLoader().
                        getResourceAsStream("dataMapper/sourceSchema.json"),
                StandardCharsets.UTF_8);
        String sourceData = IOUtils.toString(Thread.currentThread().getContextClassLoader().
                        getResourceAsStream("dataMapper/sourceData.json"),
                StandardCharsets.UTF_8);
        JsonObject input = new JsonObject();
        input.addProperty("sourceSchema", sourceSchema);
        input.addProperty("destinationSchema", sourceSchema);
        input.addProperty("sourceData", sourceData);


        Response response = given().body(input.toString()).when().post("/dataMapper/map")
                .andReturn();

        String jsonResponse = response.getBody().prettyPrint();
        logger.info("**************");
        logger.info(response.getStatusLine());
        logger.info("***************");

        //assert the body
        JsonArray array = JsonParser.parseString(jsonResponse).getAsJsonArray();
        JsonObject jsonObject = array.get(0).getAsJsonObject();
        int statusCode = response.getStatusCode();
        assertEquals(200, statusCode);
        assertEquals("123456789", jsonObject.get("Id").getAsString());
        assertEquals("1234567", jsonObject.get("Rcvr").getAsString());
        assertEquals(Boolean.TRUE, jsonObject.get("HasSig").getAsBoolean());

        jsonObject = array.get(1).getAsJsonObject();
        assertEquals("7777777", jsonObject.get("Id").getAsString());
        assertEquals("77777", jsonObject.get("Rcvr").getAsString());
        assertEquals(Boolean.FALSE, jsonObject.get("HasSig").getAsBoolean());
    }

    @Test
    public void testMapWithScatteredFields() throws Exception {
        String sourceSchema = IOUtils.toString(Thread.currentThread().getContextClassLoader().
                        getResourceAsStream("dataMapper/sourceSchema.json"),
                StandardCharsets.UTF_8);
        String sourceData = IOUtils.toString(Thread.currentThread().getContextClassLoader().
                        getResourceAsStream("dataMapper/sourceDataWithScatteredFields.json"),
                StandardCharsets.UTF_8);

        JsonObject input = new JsonObject();
        input.addProperty("sourceSchema", sourceSchema);
        input.addProperty("destinationSchema", sourceSchema);
        input.addProperty("sourceData", sourceData);


        Response response = given().body(input.toString()).when().post("/dataMapper/map")
                .andReturn();

        String jsonResponse = response.getBody().prettyPrint();
        logger.info("***************");
        logger.info(response.getStatusLine());
        logger.info("***************");

        //assert the body
        JsonArray array = JsonParser.parseString(jsonResponse).getAsJsonArray();
        JsonObject jsonObject = array.get(0).getAsJsonObject();
        int statusCode = response.getStatusCode();
        assertEquals(200, statusCode);
        assertEquals("123456789", jsonObject.get("Id").getAsString());
        assertEquals("1234567", jsonObject.get("Rcvr").getAsString());
        assertEquals(Boolean.TRUE, jsonObject.get("HasSig").getAsBoolean());
    }

    @Test
    public void testMapXmlSourceData() throws Exception {
        String xml = IOUtils.toString(Thread.currentThread().getContextClassLoader().getResourceAsStream("people.xml"),
                StandardCharsets.UTF_8);

        JsonObject input = new JsonObject();
        input.addProperty("sourceSchema", xml);
        input.addProperty("destinationSchema", xml);
        input.addProperty("sourceData", xml);


        Response response = given().body(input.toString()).when().post("/dataMapper/mapXml")
                .andReturn();

        String jsonResponse = response.getBody().prettyPrint();
        logger.info("****");
        logger.info(response.getStatusLine());
        logger.info("****");
        assertEquals(200, response.getStatusCode());

        JsonObject storedJson = this.mongoDBJsonStore.getIngestion("1");
        logger.info("*******");
        logger.info(storedJson.toString());
        logger.info("*******");

        //TODO:JSon compare
    }

    @Test
    public void testMapCsvSourceData() throws Exception
    {
        String spaceData = IOUtils.toString(Thread.currentThread().getContextClassLoader().getResourceAsStream(
                "dataMapper/data.csv"),
                StandardCharsets.UTF_8);
        JsonObject input = new JsonObject();
        input.addProperty("sourceSchema", "");
        input.addProperty("destinationSchema", "");
        input.addProperty("sourceData", spaceData);
        Response response = given().body(input.toString()).when().post("/dataMapper/mapCsv")
                .andReturn();

        logger.info("****");
        logger.info(response.getStatusLine());
        logger.info("****");
        assertEquals(200, response.getStatusCode());
    }
}