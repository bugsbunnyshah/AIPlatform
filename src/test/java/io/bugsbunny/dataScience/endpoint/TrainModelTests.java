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
import io.bugsbunny.util.BGNotificationReceiver;
import io.bugsbunny.util.BackgroundProcessListener;
import io.bugsbunny.util.JsonUtil;
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
        String modelId = liveModelDeployedJson.get("modelId").getAsString();

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
        String dataSetId = returnValue.get("dataSetId").getAsString();
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
    public void testEvalPython() throws Exception
    {
        String modelPackage = IOUtils.resourceToString("dataScience/aiplatform-python-model.json", StandardCharsets.UTF_8,
                Thread.currentThread().getContextClassLoader());

        JsonObject modelDeployedJson = this.packagingService.performPackaging(modelPackage);
        String modelId = modelDeployedJson.get("modelId").getAsString();

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

        String dataSetId = JsonParser.parseString(response.body().asString()).getAsJsonObject().get("dataSetId").getAsString();
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
}