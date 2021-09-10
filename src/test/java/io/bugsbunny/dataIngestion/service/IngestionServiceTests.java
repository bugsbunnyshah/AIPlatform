package io.bugsbunny.dataIngestion.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.bugsbunny.dataScience.service.PackagingService;
import io.bugsbunny.preprocess.SecurityTokenContainer;
import io.bugsbunny.query.GraphData;
import io.bugsbunny.query.LocalGraphData;
import io.bugsbunny.query.ObjectGraphQueryService;
import io.bugsbunny.test.components.BaseTest;
import io.bugsbunny.util.BGNotificationReceiver;
import io.bugsbunny.util.BackgroundProcessListener;
import io.bugsbunny.util.JsonUtil;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import org.apache.commons.io.IOUtils;
import org.apache.tinkerpop.gremlin.sparql.process.traversal.dsl.sparql.SparqlTraversalSource;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.TimerTask;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

//TODO: FIX ALL THE TESTS

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class IngestionServiceTests extends BaseTest {
    private static Logger logger = LoggerFactory.getLogger(IngestionServiceTests.class);

    @Inject
    private IngestionService ingestionService;

    @Inject
    private PackagingService packagingService;

    @Inject
    private ObjectGraphQueryService objectGraphQueryService;

    @Inject
    private SecurityTokenContainer securityTokenContainer;

    //@Test
    @Order(1)
    public void testDataHistoryFromDataLake() throws Exception
    {
        try {
            this.startIngester();
            TinkerGraph graph = TinkerGraph.open();
            SparqlTraversalSource server = new SparqlTraversalSource(graph);
            GraphData graphData = new LocalGraphData(server);
            this.objectGraphQueryService.setGraphData(graphData);

            String modelPackage = IOUtils.resourceToString("dataScience/aiplatform-model.json", StandardCharsets.UTF_8,
                    Thread.currentThread().getContextClassLoader());

            JsonObject liveModelDeployedJson = this.packagingService.performPackaging(modelPackage);
            String modelId = liveModelDeployedJson.get("modelId").getAsString();

            String data = IOUtils.resourceToString("dataScience/saturn_data_train.csv", StandardCharsets.UTF_8,
                    Thread.currentThread().getContextClassLoader());
            JsonObject ingestion = new JsonObject();
            ingestion.addProperty("sourceData", data);
            ingestion.addProperty("hasHeader", false);
            ingestion.addProperty("entity", "saturn");

            String[] dataIds = new String[3];
            JsonArray dataLakeIdArray = new JsonArray();

            BGNotificationReceiver receiver = new BGNotificationReceiver();
            synchronized (receiver) {
                BackgroundProcessListener.getInstance().setThreshold(1);
                BackgroundProcessListener.getInstance().setReceiver(receiver);

                //Perform the test
                Response response = given().body(ingestion.toString()).when().post("/dataMapper/mapCsv/").andReturn();
                response.getBody().prettyPrint();
                assertEquals(200, response.getStatusCode());
                JsonObject returnValue = JsonParser.parseString(response.body().asString()).getAsJsonObject();
                String dataLakeId = returnValue.get("dataLakeId").getAsString();
                dataIds[0] = dataLakeId;
                dataLakeIdArray.add(dataLakeId);
                BackgroundProcessListener.getInstance().setDataLakeId(dataLakeId);


                receiver.wait();
                //JsonUtil.print(receiver.getData());
            }

            String dataLakeId = dataIds[0];
            logger.info("" + dataLakeId);
            Response response = given().when().get("/dataMapper/readDataLakeObject/?dataLakeId=" + dataLakeId).andReturn();
            response.getBody().prettyPrint();
            JsonArray result = JsonParser.parseString(response.body().asString()).getAsJsonArray();
            assertTrue(result.size()>0);

            //TODO: reactivate
            /*JsonObject input = new JsonObject();
            input.addProperty("modelId", modelId);
            input.add("dataLakeIds", dataLakeIdArray);
            response = given().body(input.toString()).when().post("/trainModel/trainJavaFromDataLake").andReturn();
            response.body().prettyPrint();
            assertEquals(200, response.getStatusCode());
            String dataHistoryId = JsonParser.parseString(response.body().asString()).getAsJsonObject().get("dataHistoryId").getAsString();
            logger.info("************************");
            logger.info(response.statusLine());
            logger.info("DATA_HISTORY_ID: " + dataHistoryId);
            logger.info("************************");
            assertNotNull(dataHistoryId);
            assertTrue(JsonParser.parseString(response.getBody().asString()).getAsJsonObject().has("result"));*/
        }
        finally {
            this.stopIngester();
        }
    }

    @Test
    @Order(2)
    public void diffSamePayload() throws Exception{
        try {
            this.startIngester();
            String data = IOUtils.toString(Thread.currentThread().
                            getContextClassLoader().
                            getResourceAsStream("aviation/flights0.json"),
                    StandardCharsets.UTF_8);

            JsonArray json = JsonParser.parseString(data).getAsJsonObject().get("data").getAsJsonArray();
            //JsonUtil.print(json);

            JsonArray ingestion1 = new JsonArray();
            for (int i = 0; i < 2; i++) {
                ingestion1.add(json.get(i));
            }

            //JsonUtil.print(ingestion1);
            //JsonUtil.print(ingestion2);

            //Ingest1
            JsonObject r1 = this.sendData(ingestion1);
            //JsonUtil.print(r1);

            String dataLakeId = r1.get("dataLakeId").getAsString();
            String chainId = r1.get("chainId").getAsString();
            assertNotNull(dataLakeId);
            assertNotNull(chainId);

            /*JsonArray ingestion2 = new JsonArray();
            for (int i = 0; i < 2; i++) {
                JsonObject local = json.get(i).getAsJsonObject();
                local.addProperty("braineous_datalakeid",dataLakeId);
                local.addProperty("chainId",chainId);
                ingestion2.add(local);
            }

            //Ingest2
            //JsonObject r2 = this.sendData(ingestion2);
            //JsonUtil.print(r2);*/

            //Check the Data Chain
            this.handle(r1);

            //System.out.println("***********R2*************");
            //this.handle(r2);
        }
        finally {
            this.stopIngester();
        }
    }

    private void handle(JsonObject result){
        String dataLakeId = result.get("dataLakeId").getAsString();
        logger.info("********************************************************************************************************");
        logger.info("DATALAKE_ID: " + dataLakeId);
        logger.info("************************************************************************************************************");
        Response response = given().when().get("/dataMapper/readDataLakeObject/?dataLakeId=" + dataLakeId).andReturn();
        response.getBody().prettyPrint();
        JsonArray json = JsonParser.parseString(response.getBody().asString()).getAsJsonArray();
        assertTrue(json.size()>0);
        JsonObject top = json.get(0).getAsJsonObject();
        JsonObject bottom = json.get(1).getAsJsonObject();
        assertEquals("204",top.get("flight").getAsJsonObject().get("number").getAsString());
        assertEquals("4086",bottom.get("flight").getAsJsonObject().get("number").getAsString());
    }

    private JsonObject sendData(JsonArray ingestion) throws Exception{
        JsonUtil.print(ingestion);

        JsonObject input = new JsonObject();
        input.addProperty("sourceData", ingestion.toString());
        input.addProperty("entity", "flight");

        Response response;
        BGNotificationReceiver receiver = new BGNotificationReceiver();
        synchronized (receiver) {
            BackgroundProcessListener.getInstance().setThreshold(ingestion.size());
            BackgroundProcessListener.getInstance().setReceiver(receiver);

            response = given().body(input.toString()).when().post("/dataMapper/map/").andReturn();
            response.getBody().prettyPrint();
            assertEquals(200, response.getStatusCode());
            JsonObject returnValue = JsonParser.parseString(response.body().asString()).getAsJsonObject();
            String dataLakeId = returnValue.get("dataLakeId").getAsString();
            BackgroundProcessListener.getInstance().setDataLakeId(dataLakeId);

            receiver.wait();
            //JsonUtil.print(receiver.getData());
        }

        return JsonParser.parseString(response.getBody().asString()).getAsJsonObject();
    }

    //@Test
    @Order(2)
    public void testTrainJavaFromDataLake() throws Exception
    {
        try {
            this.startIngester();
            TinkerGraph graph = TinkerGraph.open();
            SparqlTraversalSource server = new SparqlTraversalSource(graph);
            GraphData graphData = new LocalGraphData(server);
            this.objectGraphQueryService.setGraphData(graphData);
            String modelPackage = IOUtils.resourceToString("dataScience/aiplatform-model.json", StandardCharsets.UTF_8,
                    Thread.currentThread().getContextClassLoader());

            JsonObject input = this.packagingService.performPackaging(modelPackage);
            JsonObject trainingModelDeployedJson = this.packagingService.performPackaging(modelPackage);
            String modelId = trainingModelDeployedJson.get("modelId").getAsString();

            String xml = IOUtils.toString(Thread.currentThread().getContextClassLoader()
                            .getResourceAsStream("dataMapper/people.xml"),
                    StandardCharsets.UTF_8);

            JsonObject ingestedData;
            BGNotificationReceiver receiver = new BGNotificationReceiver();
            synchronized (receiver) {
                BackgroundProcessListener.getInstance().setThreshold(1);
                BackgroundProcessListener.getInstance().setReceiver(receiver);

                input = new JsonObject();
                input.addProperty("sourceData", xml);
                input.addProperty("entity", "saturn");

                Response ingestionResponse = given().body(input.toString()).when().post("/dataMapper/mapXml/")
                        .andReturn();

                String jsonResponse = ingestionResponse.getBody().prettyPrint();
                logger.info("****");
                logger.info(ingestionResponse.getStatusLine());
                logger.info(jsonResponse);
                logger.info("****");
                assertEquals(200, ingestionResponse.getStatusCode());

                //assert the body
                ingestedData = JsonParser.parseString(jsonResponse).getAsJsonObject();
                assertNotNull(ingestedData.get("dataLakeId"));
                String dataLakeId = ingestedData.get("dataLakeId").getAsString();
                BackgroundProcessListener.getInstance().setDataLakeId(dataLakeId);

                receiver.wait();
                JsonUtil.print(receiver.getData());
            }

            input = new JsonObject();
            JsonArray jsonArray = new JsonArray();
            jsonArray.add(ingestedData.get("dataLakeId").getAsString());
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
        finally {
            this.startIngester();
        }
    }

    //@Test
    //@Order(1)
    public void ingestAndEval() throws Exception
    {
        try {
            this.startIngester();

            String modelPackage = IOUtils.resourceToString("dataScience/aiplatform-model.json", StandardCharsets.UTF_8,
                    Thread.currentThread().getContextClassLoader());

            JsonObject input = this.packagingService.performPackaging(modelPackage);
            JsonObject trainingModelDeployedJson = this.packagingService.performPackaging(modelPackage);
            String modelId = trainingModelDeployedJson.get("modelId").getAsString();

            String xml = IOUtils.toString(Thread.currentThread().getContextClassLoader()
                            .getResourceAsStream("dataMapper/people.xml"),
                    StandardCharsets.UTF_8);

            JsonObject ingestedData = null;
            BGNotificationReceiver receiver = new BGNotificationReceiver();
            synchronized (receiver) {
                BackgroundProcessListener.getInstance().setThreshold(4);
                BackgroundProcessListener.getInstance().setReceiver(receiver);

                //Perform the test
                input = new JsonObject();
                input.addProperty("sourceSchema", xml);
                input.addProperty("destinationSchema", xml);
                input.addProperty("sourceData", xml);
                input.addProperty("entity", "saturn");

                Response ingestionResponse = given().body(input.toString()).when().post("/dataMapper/mapXml/")
                        .andReturn();

                String jsonResponse = ingestionResponse.getBody().prettyPrint();
                logger.info("****");
                logger.info(ingestionResponse.getStatusLine());
                logger.info(jsonResponse);
                logger.info("****");
                assertEquals(200, ingestionResponse.getStatusCode());
                ingestedData = JsonParser.parseString(jsonResponse).getAsJsonObject();
                assertNotNull(ingestedData.get("dataLakeId"));
                String dataLakeId = ingestedData.get("dataLakeId").getAsString();
                BackgroundProcessListener.getInstance().setDataLakeId(dataLakeId);

                receiver.wait();
                JsonArray data = receiver.getData();
                assertEquals(4, data.size());
                logger.info("*******RETURNED*******");
                JsonUtil.print(data);
            }

            input = new JsonObject();
            JsonArray jsonArray = new JsonArray();
            jsonArray.add(ingestedData.get("dataLakeId").getAsString());
            input.addProperty("modelId", modelId);
            input.add("dataLakeIds", jsonArray);
            //Deploy the model
            JsonObject deployModel = new JsonObject();
            deployModel.addProperty("modelId", modelId);
            given().body(deployModel.toString()).when().post("/liveModel/deployJavaModel").andReturn();

            JsonUtil.print(input);
            Response response = given().body(input.toString()).when().post("/liveModel/evalJavaFromDataLake")

                    .andReturn();
            logger.info("************************");
            logger.info(response.statusLine());
            response.body().prettyPrint();
            logger.info("************************");
            assertEquals(200, response.getStatusCode());
            assertNotNull(JsonParser.parseString(response.body().asString()).getAsJsonObject().get("dataHistoryId"));
        }
        finally {
            this.stopIngester();
        }
    }

    //TODO: Investigate why multiple fetch + map is not working
    //@Test
    //@Order(2)
    public void ingestFetchAndPush() throws Exception{
        try {
            this.startIngester();

            String agentId = "ian";

            BGNotificationReceiver receiver = new BGNotificationReceiver();
            synchronized (receiver) {
                BackgroundProcessListener.getInstance().setThreshold(2);
                BackgroundProcessListener.getInstance().setReceiver(receiver);

                for (int i = 0; i < 2; i++) {
                    DataFetchAgent flightAgent = new FlightAgent();
                    this.ingestionService.ingestData(agentId + i, "flight", flightAgent);
                }

                receiver.wait();
            }

            JsonArray data = receiver.getData();
            assertEquals(2, data.size());
            logger.info("*******RETURNED*******");
            JsonUtil.print(data);



            //Push Test
            String responseJson = IOUtils.resourceToString("aviation/flights0.json", StandardCharsets.UTF_8,
                    Thread.currentThread().getContextClassLoader());
            JsonArray jsonArray = JsonParser.parseString(responseJson).getAsJsonObject().getAsJsonArray("data");
            receiver = new BGNotificationReceiver();
            synchronized (receiver) {
                BackgroundProcessListener.getInstance().setThreshold(2);
                BackgroundProcessListener.getInstance().setReceiver(receiver);

                //Perform the test
                DataFetchAgent flightAgent = new FlightAgent();
                this.ingestionService.ingestData(agentId, "flight", (DataPushAgent) flightAgent, jsonArray);

                receiver.wait();
            }

            data = receiver.getData();
            assertEquals(2, data.size());
            logger.info("*******RETURNED*******");
            JsonUtil.print(data);
        }
        finally {
            this.stopIngester();
        }
    }

    //@Test
    //@Order(2)
    public void streamIngesterSubmit() throws Exception{
        String sourceData = IOUtils.toString(Thread.currentThread().getContextClassLoader().getResourceAsStream(
                "aviation/flights0.json"),
                StandardCharsets.UTF_8);
        JsonArray jsonArray = JsonParser.parseString(sourceData).getAsJsonObject().get("data").getAsJsonArray();


        System.out.println("*******************************");
        System.out.println("STARTING_INGESTION");
        System.out.println("*******************************");

        BGNotificationReceiver receiver = new BGNotificationReceiver();
        synchronized (receiver) {
            BackgroundProcessListener.getInstance().setThreshold(2);
            BackgroundProcessListener.getInstance().setReceiver(receiver);
            JsonObject input = new JsonObject();
            input.addProperty("sourceSchema", "");
            input.addProperty("destinationSchema", "");
            input.addProperty("sourceData", jsonArray.toString());
            input.addProperty("entity", "flight");
            Response response = given().body(input.toString()).when().post("/dataMapper/map")
                    .andReturn();
            response.getBody().prettyPrint();
            JsonObject json = JsonParser.parseString(response.getBody().asString()).getAsJsonObject();
            String dataLakeId = json.get("dataLakeId").getAsString();
            BackgroundProcessListener.getInstance().setDataLakeId(dataLakeId);

            receiver.wait();
        }

        JsonArray data = receiver.getData();
        assertEquals(2,data.size());
        logger.info("*******RETURNED*******");
        JsonUtil.print(data);
    }





    private static class FlightAgent implements DataFetchAgent,DataPushAgent{

        @Override
        public JsonArray fetchData() throws FetchException{
            try {
                String responseJson = IOUtils.resourceToString("aviation/flights0.json", StandardCharsets.UTF_8,
                        Thread.currentThread().getContextClassLoader());
                JsonArray jsonArray = JsonParser.parseString(responseJson).getAsJsonObject().getAsJsonArray("data");

                return jsonArray;
            }
            catch(Exception e){
                throw new FetchException(e);
            }
        }

        @Override
        public void receiveData(JsonArray json) throws FetchException {
            System.out.println("************PUSH_RECEIVED************");
        }
    }

    //@Test
    public void testGetIngestion() throws Exception
    {
        //TODO: temp code for debugging
        String dataLakeId = "-2586030430120757939";
        logger.info(this.ingestionService.readDataLakeData(dataLakeId).toString());
    }

    //@Test
    public void testDataHistoryFromDataSet() throws Exception
    {
        String modelPackage = IOUtils.resourceToString("dataScience/aiplatform-model.json", StandardCharsets.UTF_8,
                Thread.currentThread().getContextClassLoader());

        JsonObject liveModelDeployedJson = this.packagingService.performPackaging(modelPackage);
        String modelId = liveModelDeployedJson.get("modelId").getAsString();

        String data = IOUtils.resourceToString("dataScience/saturn_data_train.csv", StandardCharsets.UTF_8,
                Thread.currentThread().getContextClassLoader());
        JsonObject input = new JsonObject();
        input.addProperty("modelId", modelId);
        input.addProperty("format", "csv");
        input.addProperty("data", data);

        String[] dataIds = new String[3];
        String dataHistoryId=null;
        for(int i=0; i<dataIds.length; i++) {
            Response response = given().body(input.toString()).when().post("/dataset/storeEvalDataSet/").andReturn();
            logger.info("************************");
            logger.info(response.statusLine());
            logger.info("************************");
            assertEquals(200, response.getStatusCode());
            JsonObject returnValue = JsonParser.parseString(response.body().asString()).getAsJsonObject();
            String dataSetId = returnValue.get("dataSetId").getAsString();

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
                String dataSetId = cour.next().getAsString();

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