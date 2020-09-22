package io.bugsbunny.endpoint;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.bugsbunny.data.history.service.PayloadReplayService;
import io.bugsbunny.dataScience.service.PackagingService;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

//TODO: Add Tests for DataMapper and RemoteMOdel ObjectDiffs

@QuarkusTest
public class AITrafficAgentTests {
    private static Logger logger = LoggerFactory.getLogger(AITrafficAgentTests.class);

    @Inject
    private PayloadReplayService payloadReplayService;

    @Inject
    private AITrafficAgent aiTrafficAgent;

    @Inject
    private PackagingService packagingService;

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
    public void testEvalLiveModelTrafficReplay() throws Exception
    {
        for(int i=0; i<3; i++)
        {
            String modelPackage = IOUtils.resourceToString("dataScience/aiplatform-model.json", StandardCharsets.UTF_8,
                    Thread.currentThread().getContextClassLoader());

            JsonObject input = this.packagingService.performPackaging(modelPackage);
            long modelId = input.get("modelId").getAsLong();

            String data = IOUtils.resourceToString("dataScience/saturn_data_eval.csv", StandardCharsets.UTF_8,
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
            input.addProperty("modelId", modelId);
            input.addProperty("dataSetId", dataSetId);

            response = given().body(input.toString()).when().post("/liveModel/evalJava").andReturn();
            logger.info("************************");
            logger.info(response.statusLine());
            logger.info("************************");
            assertEquals(200, response.getStatusCode());
        }

        String token = this.securityTokenContainer.getTokenContainer().get().getToken();

        String requestChainId = this.aiTrafficAgent.findRequestChainId(token);
        List<JsonObject> traffic = this.payloadReplayService.replayDiffChain(requestChainId);
        logger.info(traffic.toString());

        String responseChainId = this.aiTrafficAgent.findResponseChainId(token);
        assertNotNull(responseChainId);
        logger.info("ResponseChainId: "+responseChainId);
        //traffic = this.payloadReplayService.replayDiffChain(responseChainId);
        //logger.info(traffic.toString());
    }
}