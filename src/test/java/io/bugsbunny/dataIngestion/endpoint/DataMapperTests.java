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
    public void setUp() throws Exception
    {
        String securityTokenJson = IOUtils.toString(Thread.currentThread().getContextClassLoader().
                        getResourceAsStream("oauthAgent/token.json"),
                StandardCharsets.UTF_8);
        SecurityToken securityToken = SecurityToken.fromJson(securityTokenJson);
        this.securityTokenContainer.getTokenContainer().set(securityToken);
    }

    @Test
    public void testMapWithOneToOneFields() throws Exception{
        String sourceSchema = IOUtils.toString(Thread.currentThread().getContextClassLoader().
                        getResourceAsStream("dataMapper/sourceSchema.json"),
                StandardCharsets.UTF_8);
        String sourceData = IOUtils.toString(Thread.currentThread().getContextClassLoader().
                        getResourceAsStream("dataMapper/sourceData.json"),
                StandardCharsets.UTF_8);
        logger.info("****************");
        logger.info(sourceSchema);
        logger.info(sourceData);
        logger.info("****************");

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
    public void testMapWithScatteredFields() throws Exception{
        String sourceSchema = IOUtils.toString(Thread.currentThread().getContextClassLoader().
                        getResourceAsStream("dataMapper/sourceSchema.json"),
                StandardCharsets.UTF_8);
        String sourceData = IOUtils.toString(Thread.currentThread().getContextClassLoader().
                        getResourceAsStream("dataMapper/sourceDataWithScatteredFields.json"),
                StandardCharsets.UTF_8);
        logger.info("****************");
        logger.info(sourceSchema);
        logger.info(sourceData);
        logger.info("****************");

        JsonObject input = new JsonObject();
        input.addProperty("sourceSchema", sourceSchema);
        input.addProperty("destinationSchema", sourceSchema);
        input.addProperty("sourceData", sourceData);


        Response response = given().body(input.toString()).when().post("/dataMapper/map")
                .andReturn();

        String jsonResponse = response.getBody().prettyPrint();
        logger.info("***************");
        logger.info(response.getStatusLine());
        logger.info(jsonResponse);
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
    public void testMapXmlSourceData() throws Exception
    {
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
        logger.info(jsonResponse);
        logger.info("****");

        JsonObject storedJson = this.mongoDBJsonStore.getIngestion("1");
        logger.info("*******");
        logger.info(storedJson.toString());
        logger.info("*******");
    }

    @Test
    public void testMapCsvSourceData() throws Exception
    {
        //Map<Integer,String> dataMap = this.readEnumCSV();
        //logger.info(dataMap.toString());

        //Second: the RecordReaderDataSetIterator handles conversion to DataSet objects, ready for use in neural network
        //int labelIndex = 4;     //5 values in each row of the iris.txt CSV: 4 input features followed by an integer label (class) index. Labels are the 5th value (index 4) in each row
        //int numClasses = 3;     //3 classes (types of iris flowers) in the iris data set. Classes have integer values 0, 1 or 2
        //int batchSizeTraining = 30;
        //DataSet dataSet = this.readCSVDataset(batchSizeTraining, labelIndex, numClasses);
        //logger.info(dataSet.toString());

        /*File file = new File("tmp/data");
        FileInputStream fis = new FileInputStream(file);
        String data = IOUtils.toString(fis, StandardCharsets.UTF_8);
        JsonObject input = new JsonObject();
        input.addProperty("sourceSchema", data);
        input.addProperty("destinationSchema", data);
        input.addProperty("sourceData", data);*/

        Response response = given().body("").when().post("/dataMapper/mapCsv")
                .andReturn();

        //String jsonResponse = response.getBody().prettyPrint();
        //logger.info("****");
        //logger.info(response.getStatusLine());
        //logger.info(jsonResponse);
        //logger.info("****");

        //Kickoff the Training
        //String runId = this.trainingWorkflow.startTraining();

        //logger.info("*******");
        //logger.info("RunId: "+runId);
        //logger.info("*******");
        //assertNotNull(runId);

        //String runJson = this.mlFlowRunClient.getRun(runId);
        //logger.info(runJson);
    }

    //-----------------------------------------------------------------------------
    private Map<Integer,String> readEnumCSV() throws Exception{
        File file = new File("tmp/data");
        FileInputStream fis = new FileInputStream(file);
        String data = IOUtils.toString(fis, StandardCharsets.UTF_8);

        InputStream is = new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8));
        List<String> lines = IOUtils.readLines(is);
        Map<Integer,String> enums = new HashMap<>();
        for(String line:lines){
            String[] parts = line.split(",");
            enums.put(Integer.parseInt(parts[0]),parts[1]);
        }
        return enums;
    }
}