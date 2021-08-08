package io.bugsbunny.dataIngestion.endpoint;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.bugsbunny.dataIngestion.service.MapperService;
import io.bugsbunny.dataIngestion.util.CSVDataUtil;
import io.bugsbunny.infrastructure.MongoDBJsonStore;
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

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
public class DataIngesterTests extends BaseTest
{
    private static Logger logger = LoggerFactory.getLogger(DataIngesterTests.class);

    @BeforeEach
    public void setUp()
    {
    }

    @Test
    public void testFetch() throws Exception {
        //TODO: Add sourceSchema and destinationSchema concepts
        JsonObject input = new JsonObject();
        input.addProperty("agentId", "ian");
        input.addProperty("entity", "flight");


        Response response = given().body(input.toString()).when().post("/dataIngester/fetch")
                .andReturn();

        String jsonResponse = response.getBody().prettyPrint();

        //assert the body
        JsonObject ingestedData = JsonParser.parseString(jsonResponse).getAsJsonObject();
        assertTrue(ingestedData.get("success").getAsBoolean());
        int statusCode = response.getStatusCode();
        assertEquals(200, statusCode);

        Thread.sleep(20000);
    }
}