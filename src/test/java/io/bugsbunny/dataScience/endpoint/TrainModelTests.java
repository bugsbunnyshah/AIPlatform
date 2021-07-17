package io.bugsbunny.dataScience.endpoint;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.bugsbunny.data.history.service.DataReplayService;
import io.bugsbunny.dataScience.service.PackagingService;
import io.bugsbunny.preprocess.AITrafficAgent;

import io.bugsbunny.query.GraphData;
import io.bugsbunny.query.LocalGraphData;
import io.bugsbunny.query.ObjectGraphQueryService;
import io.bugsbunny.test.components.BaseTest;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import org.apache.commons.io.IOUtils;
import org.apache.tinkerpop.gremlin.sparql.process.traversal.dsl.sparql.SparqlTraversalSource;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
public class TrainModelTests extends BaseTest
{
    private static Logger logger = LoggerFactory.getLogger(TrainModelTests.class);

    @Inject
    private DataReplayService dataReplayService;

    @Inject
    private AITrafficAgent aiTrafficAgent;

    @Inject
    private PackagingService packagingService;

    @Inject
    private ObjectGraphQueryService objectGraphQueryService;

    /*@BeforeEach
    public void setUp()
    {
        TinkerGraph graph = TinkerGraph.open();
        SparqlTraversalSource server = new SparqlTraversalSource(graph);
        GraphData graphData = new LocalGraphData(server);
        this.objectGraphQueryService.setGraphData(graphData);
    }*/

    @Test
    public void testTrainJava() throws Exception
    {
        String modelPackage = IOUtils.resourceToString("dataScience/aiplatform-model.json", StandardCharsets.UTF_8,
                Thread.currentThread().getContextClassLoader());

        JsonObject input = this.packagingService.performPackaging(modelPackage);
        JsonObject liveModelDeployedJson = this.packagingService.performPackaging(modelPackage);
        long modelId = liveModelDeployedJson.get("modelId").getAsLong();

        String data = IOUtils.resourceToString("dataScience/saturn_data_train.csv", StandardCharsets.UTF_8,
                Thread.currentThread().getContextClassLoader());
        input = new JsonObject();
        input.addProperty("modelId", modelId);
        input.addProperty("format", "csv");
        input.addProperty("data", data);

        Response response = given().body(input.toString()).when().post("/dataset/storeEvalDataSet/").andReturn();
        logger.info("************************");
        logger.info(response.statusLine());
        response.body().prettyPrint();
        logger.info("************************");
        assertEquals(200, response.getStatusCode());
        JsonObject returnValue = JsonParser.parseString(response.body().asString()).getAsJsonObject();
        long dataSetId = returnValue.get("dataSetId").getAsLong();
        input = new JsonObject();
        JsonArray dataSetIdArray = new JsonArray();
        dataSetIdArray.add(dataSetId);
        input.addProperty("modelId", modelId);
        input.add("dataSetIds", dataSetIdArray);

        response = given().body(input.toString()).when().post("/trainModel/trainJava").andReturn();
        logger.info("************************");
        logger.info(response.statusLine());
        response.body().prettyPrint();
        logger.info("************************");
        assertEquals(200, response.getStatusCode());
        assertNotNull(JsonParser.parseString(response.body().asString()).getAsJsonObject().get("dataHistoryId"));
    }

    @Test
    public void testTrainJavaFromDataLake() throws Exception
    {
        TinkerGraph graph = TinkerGraph.open();
        SparqlTraversalSource server = new SparqlTraversalSource(graph);
        GraphData graphData = new LocalGraphData(server);
        this.objectGraphQueryService.setGraphData(graphData);
        String modelPackage = IOUtils.resourceToString("dataScience/aiplatform-model.json", StandardCharsets.UTF_8,
                Thread.currentThread().getContextClassLoader());

        JsonObject input = this.packagingService.performPackaging(modelPackage);
        JsonObject trainingModelDeployedJson = this.packagingService.performPackaging(modelPackage);
        long modelId = trainingModelDeployedJson.get("modelId").getAsLong();

        String xml = IOUtils.toString(Thread.currentThread().getContextClassLoader()
                        .getResourceAsStream("dataMapper/people.xml"),
                StandardCharsets.UTF_8);

        input = new JsonObject();
        input.addProperty("sourceData", xml);
        input.addProperty("entity","saturn");

        Response ingestionResponse = given().body(input.toString()).when().post("/dataMapper/mapXml/")
                .andReturn();

        String jsonResponse = ingestionResponse.getBody().prettyPrint();
        logger.info("****");
        logger.info(ingestionResponse.getStatusLine());
        logger.info(jsonResponse);
        logger.info("****");
        assertEquals(200, ingestionResponse.getStatusCode());

        //assert the body
        JsonObject ingestedData = JsonParser.parseString(jsonResponse).getAsJsonObject();
        assertNotNull(ingestedData.get("dataLakeId"));
        input = new JsonObject();
        JsonArray jsonArray = new JsonArray();
        jsonArray.add(ingestedData.get("dataLakeId").getAsLong());
        input.addProperty("modelId", modelId);
        input.add("dataLakeIds", jsonArray);

        Response response = given().body(input.toString()).when().post("/trainModel/trainJavaFromDataLake")

                .andReturn();
        logger.info("************************");
        logger.info(response.statusLine());
        response.body().prettyPrint();
        logger.info("************************");
        assertEquals(200, response.getStatusCode());
        assertNotNull(JsonParser.parseString(response.body().asString()).getAsJsonObject().get("dataHistoryId"));
    }

    @Test
    public void testEvalPython() throws Exception
    {
        String modelPackage = IOUtils.resourceToString("dataScience/aiplatform-python-model.json", StandardCharsets.UTF_8,
                Thread.currentThread().getContextClassLoader());

        JsonObject modelDeployedJson = this.packagingService.performPackaging(modelPackage);
        long modelId = modelDeployedJson.get("modelId").getAsLong();

        String data = IOUtils.resourceToString("dataScience/saturn_data_train.csv", StandardCharsets.UTF_8,
                Thread.currentThread().getContextClassLoader());
        JsonObject input = new JsonObject();
        input.addProperty("modelId", modelId);
        input.addProperty("format", "csv");
        input.addProperty("data", data);

        Response response = given().body(input.toString()).when().post("/dataset/storeTrainingDataSet/").andReturn();
        logger.info("************************");
        logger.info(response.statusLine());
        response.body().prettyPrint();
        logger.info("modelId: "+modelId);
        logger.info("************************");
        assertEquals(200, response.getStatusCode());

        long dataSetId = JsonParser.parseString(response.body().asString()).getAsJsonObject().get("dataSetId").getAsLong();
        JsonObject training = new JsonObject();
        JsonArray dataSetIdArray = new JsonArray();
        dataSetIdArray.add(dataSetId);
        training.addProperty("modelId", modelId);
        training.add("dataSetIds", dataSetIdArray);
        logger.info(input.toString());
        response = given().body(training.toString()).when().post("/trainModel/trainPython/").andReturn();
        logger.info("************************");
        logger.info(response.statusLine());
        response.body().prettyPrint();
        logger.info("************************");
    }

    @Test
    public void testDataHistoryFromDataLake() throws Exception
    {
        TinkerGraph graph = TinkerGraph.open();
        SparqlTraversalSource server = new SparqlTraversalSource(graph);
        GraphData graphData = new LocalGraphData(server);
        this.objectGraphQueryService.setGraphData(graphData);

        String modelPackage = IOUtils.resourceToString("dataScience/aiplatform-model.json", StandardCharsets.UTF_8,
                Thread.currentThread().getContextClassLoader());

        JsonObject liveModelDeployedJson = this.packagingService.performPackaging(modelPackage);
        long modelId = liveModelDeployedJson.get("modelId").getAsLong();

        String data = IOUtils.resourceToString("dataScience/saturn_data_train.csv", StandardCharsets.UTF_8,
                Thread.currentThread().getContextClassLoader());
        JsonObject ingestion = new JsonObject();
        ingestion.addProperty("sourceData", data);
        ingestion.addProperty("hasHeader", false);
        ingestion.addProperty("entity","saturn");

        String[] dataIds = new String[3];
        String dataHistoryId=null;
        for(int i=0; i<dataIds.length; i++) {
            Response response = given().body(ingestion.toString()).when().post("/dataMapper/mapCsv/").andReturn();
            logger.info("************************");
            logger.info(response.statusLine());
            logger.info("************************");
            assertEquals(200, response.getStatusCode());

            JsonObject returnValue = JsonParser.parseString(response.body().asString()).getAsJsonObject();
            String dataLakeId = returnValue.get("dataLakeId").getAsString();
            JsonObject input = new JsonObject();
            JsonArray dataLakeIdArray = new JsonArray();
            dataLakeIdArray.add(dataLakeId);
            input.addProperty("modelId", modelId);
            input.add("dataLakeIds", dataLakeIdArray);

            response = given().body(input.toString()).when().post("/trainModel/trainJavaFromDataLake").andReturn();
            response.body().prettyPrint();
            dataHistoryId = JsonParser.parseString(response.body().asString()).getAsJsonObject().get("dataHistoryId").getAsString();
            logger.info("************************");
            logger.info(response.statusLine());
            logger.info("DATA_HISTORY_ID: " + dataHistoryId);
            logger.info("DATA_LAKE_ID: " + dataLakeId);
            logger.info("************************");
            assertEquals(200, response.getStatusCode());
            assertNotNull(dataHistoryId);

            dataIds[i] = dataLakeId;
        }

        logger.info("*******");
        logger.info("DATA_HISTORY_ID: " + dataHistoryId);
        logger.info("*******");
        String dataHistoryUrl = "/replay/chain/?oid=" + dataHistoryId;
        Response response = given().when().get(dataHistoryUrl).andReturn();
        logger.info("************************");
        logger.info(response.statusLine());
        response.prettyPrint();
        assertEquals(200, response.getStatusCode());

        JsonArray dataArray = JsonParser.parseString(response.body().asString()).getAsJsonArray();
        Iterator<JsonElement> itr = dataArray.iterator();
        while(itr.hasNext())
        {
            JsonObject object = itr.next().getAsJsonObject();
            JsonArray dataLakeIds = object.getAsJsonArray("dataLakeIds");
            if(dataLakeIds == null)
            {
                continue;
            }
            Iterator<JsonElement> cour = dataLakeIds.iterator();
            while(cour.hasNext())
            {
                long dataLakeId = cour.next().getAsLong();

                logger.info(""+dataLakeId);
                response = given().when().get("/dataMapper/readDataLakeObject/?dataLakeId="+dataLakeId).andReturn();
                logger.info(response.statusLine());
                JsonObject result = JsonParser.parseString(response.body().asString()).getAsJsonObject();
                String storedData = result.get("data").getAsString();
                logger.info(storedData);
                assertNotNull(storedData);
            }
        }
    }

    @Test
    public void testDataHistoryFromDataSet() throws Exception
    {
        String modelPackage = IOUtils.resourceToString("dataScience/aiplatform-model.json", StandardCharsets.UTF_8,
                Thread.currentThread().getContextClassLoader());

        JsonObject liveModelDeployedJson = this.packagingService.performPackaging(modelPackage);
        long modelId = liveModelDeployedJson.get("modelId").getAsLong();

        String data = IOUtils.resourceToString("dataScience/saturn_data_train.csv", StandardCharsets.UTF_8,
                Thread.currentThread().getContextClassLoader());
        JsonObject input = new JsonObject();
        input.addProperty("modelId", modelId);
        input.addProperty("format", "csv");
        input.addProperty("data", data);

        long[] dataIds = new long[3];
        String dataHistoryId=null;
        for(int i=0; i<dataIds.length; i++) {
            Response response = given().body(input.toString()).when().post("/dataset/storeEvalDataSet/").andReturn();
            logger.info("************************");
            logger.info(response.statusLine());
            logger.info("************************");
            assertEquals(200, response.getStatusCode());
            JsonObject returnValue = JsonParser.parseString(response.body().asString()).getAsJsonObject();
            long dataSetId = returnValue.get("dataSetId").getAsLong();

            JsonObject training = new JsonObject();
            JsonArray dataSetIdArray = new JsonArray();
            dataSetIdArray.add(dataSetId);
            training.addProperty("modelId", modelId);
            training.add("dataSetIds", dataSetIdArray);
            response = given().body(training.toString()).when().post("/trainModel/trainJava").andReturn();
            dataHistoryId = JsonParser.parseString(response.body().asString()).getAsJsonObject().get("dataHistoryId").getAsString();
            logger.info("************************");
            logger.info(response.statusLine());
            logger.info("DATA_HISTORY_ID: "+dataHistoryId);
            logger.info("DATA_SET_ID: " + dataSetId);
            logger.info("************************");
            assertEquals(200, response.getStatusCode());
            assertNotNull(dataHistoryId);

            dataIds[i] = dataSetId;
        }

        logger.info("*******");
        logger.info("DATA_HISTORY_ID: " + dataHistoryId);
        logger.info("*******");
        String dataHistoryUrl = "/replay/chain/?oid=" + dataHistoryId;
        Response response = given().when().get(dataHistoryUrl).andReturn();
        logger.info("************************");
        logger.info(response.statusLine());
        response.prettyPrint();
        assertEquals(200, response.getStatusCode());

        JsonArray dataArray = JsonParser.parseString(response.body().asString()).getAsJsonArray();
        Iterator<JsonElement> itr = dataArray.iterator();
        while(itr.hasNext())
        {
            JsonObject object = itr.next().getAsJsonObject();
            JsonArray dataSetIds = object.getAsJsonArray("dataSetIds");
            if(dataSetIds == null)
            {
                continue;
            }
            Iterator<JsonElement> cour = dataSetIds.iterator();
            while(cour.hasNext())
            {
                long dataSetId = cour.next().getAsLong();

                logger.info(""+dataSetId);
                response = given().when().get("/dataset/readDataSet/?dataSetId="+dataSetId).andReturn();
                logger.info(response.statusLine());
                JsonObject result = JsonParser.parseString(response.body().asString()).getAsJsonObject();
                String storedData = result.get("data").getAsString();
                logger.info(storedData);
                assertNotNull(storedData);
            }
        }
    }
}