package io.bugsbunny.data.history.endpoint;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.bugsbunny.data.history.service.DataReplayService;
import io.bugsbunny.infrastructure.MongoDBJsonStore;
import io.bugsbunny.infrastructure.Tenant;
import io.bugsbunny.preprocess.SecurityTokenContainer;
import io.bugsbunny.test.components.BaseTest;
import io.bugsbunny.util.JsonUtil;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import java.util.List;
import java.util.Random;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
public class DataReplayTests extends BaseTest {
    private static Logger logger = LoggerFactory.getLogger(DataReplayTests.class);

    @Inject
    private DataReplayService dataReplayService;

    @Inject
    private MongoDBJsonStore mongoDBJsonStore;

    @Inject
    private SecurityTokenContainer securityTokenContainer;

    @Test
    public void testChain() throws Exception
    {
        String json = "[{\"payload\" : { \"Id\" : 7777777, \"Rcvr\" : 77777, \"HasSig\" : true }},{\"payload\" : { \"Id\" : 7777777, \"Rcvr\" : 77777, \"HasSig\" : false }}]";
        JsonArray jsonArray = JsonParser.parseString(json).getAsJsonArray();
        JsonObject modelChain = new JsonObject();
        Random random = new Random();
        modelChain.addProperty("modelId", random.nextLong());
        modelChain.add("payload",jsonArray);
        String oid = this.dataReplayService.generateDiffChain(modelChain);
        Tenant tenant = this.securityTokenContainer.getTenant();
        List<JsonObject> diffChain = this.mongoDBJsonStore.readDiffChain(tenant,oid);
        logger.info("CHAIN_ID: "+oid);
        JsonUtil.print(JsonParser.parseString(diffChain.toString()));

        Response response = given().get("/replay/chain/?oid="+oid).andReturn();
        logger.info("************************");
        logger.info(response.statusLine());
        response.body().prettyPrint();
        logger.info("************************");
        assertEquals(200, response.getStatusCode());
    }
}