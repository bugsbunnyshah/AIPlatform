package io.bugsbunny.dataIngestion.endpoint;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.bugsbunny.dataIngestion.util.CSVDataUtil;
import io.bugsbunny.persistence.MongoDBJsonStore;
import io.bugsbunny.test.components.BaseTest;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.json.XML;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class DataMapperTests extends BaseTest
{
    private static Logger logger = LoggerFactory.getLogger(DataMapperTests.class);

    @Inject
    private MongoDBJsonStore mongoDBJsonStore;

    private CSVDataUtil csvDataUtil = new CSVDataUtil();

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
        logger.info(jsonResponse);
        logger.info("***************");

        //assert the body
        JsonObject ingestedData = JsonParser.parseString(jsonResponse).getAsJsonObject();
        assertNotNull(ingestedData.get("dataSetId"));
        JsonArray array = JsonParser.parseString(ingestedData.get("data").getAsString()).getAsJsonArray();
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
        JsonObject ingestedData = JsonParser.parseString(jsonResponse).getAsJsonObject();
        assertNotNull(ingestedData.get("dataSetId"));
        JsonArray array = JsonParser.parseString(ingestedData.get("data").getAsString()).getAsJsonArray();
        JsonObject jsonObject = array.get(0).getAsJsonObject();
        int statusCode = response.getStatusCode();
        assertEquals(200, statusCode);
        assertEquals("123456789", jsonObject.get("Id").getAsString());
        assertEquals("1234567", jsonObject.get("Rcvr").getAsString());
        assertEquals(Boolean.TRUE, jsonObject.get("HasSig").getAsBoolean());
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

        String jsonResponse = response.getBody().prettyPrint();
        logger.info("****");
        logger.info(response.getStatusLine());
        logger.info(jsonResponse);
        logger.info("****");
        assertEquals(200, response.getStatusCode());

        //assert the body
        JsonObject ingestedData = JsonParser.parseString(jsonResponse).getAsJsonObject();
        assertNotNull(ingestedData.get("dataSetId"));
        JsonArray array = JsonParser.parseString(ingestedData.get("data").getAsString()).getAsJsonArray();
        int statusCode = response.getStatusCode();
        assertEquals(200, statusCode);
        assertEquals(5, array.size());
    }

    @Test
    public void testMapXmlSourceData() throws Exception {
        String xml = IOUtils.toString(Thread.currentThread().getContextClassLoader()
                        .getResourceAsStream("dataMapper/people.xml"),
                StandardCharsets.UTF_8);

        JsonObject input = new JsonObject();
        input.addProperty("sourceSchema", xml);
        input.addProperty("destinationSchema", xml);
        input.addProperty("sourceData", xml);


        Response response = given().body(input.toString()).when().post("/dataMapper/mapXml/")
                .andReturn();

        String jsonResponse = response.getBody().prettyPrint();
        logger.info("****");
        logger.info(response.getStatusLine());
        //logger.info(jsonResponse);
        logger.info("****");
        assertEquals(200, response.getStatusCode());

        //assert the body
    }

    @Test
    public void testXmlTraversal() throws Exception
    {
        String xml = IOUtils.toString(Thread.currentThread().getContextClassLoader().getResourceAsStream("dataMapper/people.xml"),
                StandardCharsets.UTF_8);

        JSONObject sourceJson = XML.toJSONObject(xml);
        String json = sourceJson.toString(4);


        JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
        this.traverse(jsonObject);
    }

    private void traverse(JsonObject currentObject)
    {
        Iterator<String> allProps = currentObject.keySet().iterator();
        while(allProps.hasNext())
        {
            String nextObject = allProps.next();
            JsonElement resolve = currentObject.get(nextObject);
            if(resolve.isJsonObject())
            {
                JsonObject resolveJson = resolve.getAsJsonObject();
                if(resolveJson.keySet().size()==1) {
                    this.resolve(resolveJson);
                }
                else
                {
                    this.traverse(resolveJson);
                }
            }
            else
            {
                //resolve is an array, means its a leaf
                JsonObject csv = csvDataUtil.convert(resolve.getAsJsonArray());
                logger.info(csv.get("data").getAsString());
            }
        }
    }

    private void resolve(JsonObject leaf)
    {
        JsonArray finalResult;
        if (leaf.isJsonObject()) {
            finalResult = leaf.get(leaf.keySet().iterator().next()).getAsJsonArray();
        } else {
            finalResult = leaf.getAsJsonArray();
        }
        JsonObject csv = csvDataUtil.convert(finalResult);
        logger.info(csv.get("data").getAsString());
    }
}