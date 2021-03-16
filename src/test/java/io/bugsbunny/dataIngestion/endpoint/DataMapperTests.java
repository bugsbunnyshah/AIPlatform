package io.bugsbunny.dataIngestion.endpoint;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.bugsbunny.dataIngestion.service.MapperService;
import io.bugsbunny.dataIngestion.util.CSVDataUtil;
import io.bugsbunny.infrastructure.MongoDBJsonStore;
import io.bugsbunny.test.components.BaseTest;

import io.bugsbunny.util.JsonUtil;
import io.quarkus.test.junit.QuarkusTest;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.jupiter.api.Test;
import io.restassured.response.Response;

import org.apache.commons.io.IOUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class DataMapperTests extends BaseTest
{
    private static Logger logger = LoggerFactory.getLogger(DataMapperTests.class);

    @Inject
    private MongoDBJsonStore mongoDBJsonStore;

    @Inject
    private MapperService mapperService;

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
        assertNotNull(ingestedData.get("dataLakeId"));
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
        assertNotNull(ingestedData.get("dataLakeId"));
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
        input.addProperty("hasHeader", true);
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
        assertNotNull(ingestedData.get("dataLakeId"));
        JsonArray array = JsonParser.parseString(ingestedData.get("data").getAsString()).getAsJsonArray();
        int statusCode = response.getStatusCode();
        assertEquals(200, statusCode);
        assertEquals(5, array.size());
    }

    @Test
    public void testMapCsvSourceDataWithoutHeaderForMLModel() throws Exception
    {
        String spaceData = IOUtils.toString(Thread.currentThread().getContextClassLoader().getResourceAsStream(
                "dataScience/saturn_data_train.csv"),
                StandardCharsets.UTF_8);
        JsonObject input = new JsonObject();
        input.addProperty("sourceSchema", "");
        input.addProperty("destinationSchema", "");
        input.addProperty("sourceData", spaceData);
        input.addProperty("hasHeader", false);
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
        assertNotNull(ingestedData.get("dataLakeId"));
        JsonArray array = JsonParser.parseString(ingestedData.get("data").getAsString()).getAsJsonArray();
        int statusCode = response.getStatusCode();
        assertEquals(200, statusCode);
        assertEquals(500, array.size());

        JsonObject top = array.get(0).getAsJsonObject();
        Set<String> fields = top.keySet();
        assertTrue(fields.contains("col1"));
        assertTrue(fields.contains("col2"));
        assertTrue(fields.contains("col3"));
    }

    @Test
    public void testMapCsvSourceDataWithHeaderForMLModel() throws Exception
    {
        String spaceData = IOUtils.toString(Thread.currentThread().getContextClassLoader().getResourceAsStream(
                "dataScience/saturn_data_train_with_header.csv"),
                StandardCharsets.UTF_8);
        JsonObject input = new JsonObject();
        input.addProperty("sourceSchema", "");
        input.addProperty("destinationSchema", "");
        input.addProperty("sourceData", spaceData);
        input.addProperty("hasHeader", true);
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
        assertNotNull(ingestedData.get("dataLakeId"));
        JsonArray array = JsonParser.parseString(ingestedData.get("data").getAsString()).getAsJsonArray();
        int statusCode = response.getStatusCode();
        assertEquals(200, statusCode);
        assertEquals(500, array.size());

        JsonObject top = array.get(0).getAsJsonObject();
        Set<String> fields = top.keySet();
        assertTrue(fields.contains("c1"));
        assertTrue(fields.contains("c2"));
        assertTrue(fields.contains("c3"));
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
        JsonObject ingestedData = JsonParser.parseString(jsonResponse).getAsJsonObject();
        assertNotNull(ingestedData.get("dataLakeId"));
    }

    @Test
    public void testEndToEndQueryByTraversal() throws Exception {
        String json = IOUtils.toString(Thread.currentThread().getContextClassLoader()
                        .getResourceAsStream("query/person.json"),
                StandardCharsets.UTF_8);

        JsonObject input = new JsonObject();
        input.addProperty("sourceData", json);

        logger.info(input.toString());


        Response response = given().body(input.toString()).when().post("/dataMapper/map/")
                .andReturn();

        String jsonResponse = response.getBody().prettyPrint();
        logger.info("****");
        logger.info(response.getStatusLine());
        logger.info(jsonResponse);
        logger.info("****");
        assertEquals(200, response.getStatusCode());

        //assert the body
        JsonObject ingestedData = JsonParser.parseString(jsonResponse).getAsJsonObject();
        assertNotNull(ingestedData.get("dataLakeId"));
        logger.info("DataLakeId: "+ingestedData.get("dataLakeId"));
    }

    @Test
    public void testObjectTraversal() throws Exception {
        String json = IOUtils.toString(Thread.currentThread().getContextClassLoader()
                        .getResourceAsStream("query/person.json"),
                StandardCharsets.UTF_8);

        JsonArray array = JsonParser.parseString(json).getAsJsonArray();
        this.traverse(array.get(0).getAsJsonObject(), new JsonArray());
    }

    private void traverse(JsonObject currentObject, JsonArray result)
    {
        String vertexId = UUID.randomUUID().toString();
        final GraphTraversal<Vertex, Vertex> currentVertex = this.mapperService.getG().addV();
        currentVertex.property("vertexId",vertexId);

        Iterator<String> allProps = currentObject.keySet().iterator();
        while(allProps.hasNext())
        {
            String nextObject = allProps.next();
            //logger.info("NEXT_OBJECT: "+nextObject);


            JsonElement resolve = currentObject.get(nextObject);
            if(resolve.isJsonObject())
            {
                JsonObject resolveJson = resolve.getAsJsonObject();
                if(resolveJson.keySet().size()==0)
                {
                    //EMPTY TAG...skip it
                    continue;
                }
                if(resolveJson.keySet().size()==1) {
                    //logger.info(nextObject+": RESOLVING");
                    this.resolve(nextObject, resolveJson, result);
                }
                else
                {
                    //logger.info(nextObject+": TRAVERSING");
                    this.traverse(resolveJson, result);
                }
            }
            else
            {
                if(resolve.isJsonPrimitive())
                {
                    //logger.info("PRIMITIVE_FOUND");
                    currentVertex.property(nextObject, resolve.getAsString());
                }
            }
        }

        logger.info(currentVertex.V().count().toList().toString());
        //JsonUtil.print(result);
    }

    private void resolve(String parent, JsonObject leaf, JsonArray result)
    {
        //logger.info("*********************************");
        //logger.info("PARENT: "+parent);
        //logger.info("*********************************");
        JsonArray finalResult=null;
        if (leaf.isJsonObject()) {
            String child = leaf.keySet().iterator().next();
            JsonElement childElement = leaf.get(child);
            if(childElement.isJsonArray()) {
                //logger.info(parent+": CHILD_ARRAY");
                finalResult = childElement.getAsJsonArray();
            }
            else
            {
                //logger.info(parent+": CHILD_OBJECT");
                finalResult = new JsonArray();
                finalResult.add(childElement);
                //this.traverse(childElement.getAsJsonObject(), result);
            }
        } else {
            //logger.info(parent+": LEAF_ARRAY");
            finalResult = leaf.getAsJsonArray();
        }


        if(finalResult != null) {
            //logger.info(parent+": CALCULATING");
            Iterator<JsonElement> itr = finalResult.iterator();
            JsonArray jsonArray = new JsonArray();
            while (itr.hasNext())
            {
                JsonElement jsonElement = itr.next();
                if(jsonElement.isJsonPrimitive())
                {
                    JsonObject primitive = new JsonObject();
                    primitive.addProperty(parent,jsonElement.toString());
                    jsonArray.add(primitive);
                }
                else {
                    jsonArray.add(jsonElement);
                }
            }
            JsonObject jsonObject = new JsonObject();
            jsonObject.add(parent,jsonArray);
            result.add(jsonObject);
        }
    }
}