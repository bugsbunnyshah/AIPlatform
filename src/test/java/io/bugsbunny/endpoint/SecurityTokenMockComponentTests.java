package io.bugsbunny.endpoint;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.bugsbunny.data.history.service.DataReplayService;
import io.bugsbunny.dataScience.service.PackagingService;
import io.bugsbunny.preprocess.AITrafficAgent;
import io.bugsbunny.preprocess.SecurityTokenContainer;
import io.bugsbunny.test.components.BaseTest;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

//TODO: Add Tests for DataMapper and RemoteMOdel ObjectDiffs

@QuarkusTest
public class SecurityTokenMockComponentTests extends BaseTest {
    private static Logger logger = LoggerFactory.getLogger(SecurityTokenMockComponentTests.class);

    @Inject
    private DataReplayService dataReplayService;

    @Inject
    private AITrafficAgent aiTrafficAgent;

    @Inject
    private PackagingService packagingService;

    @Inject
    private SecurityTokenContainer securityTokenContainer;

    @Test
    public void testEvalLiveModelTrafficReplay() throws Exception
    {
        for(int i=0; i<3; i++)
        {
            String modelPackage = IOUtils.resourceToString("dataScience/aiplatform-model.json", StandardCharsets.UTF_8,
                    Thread.currentThread().getContextClassLoader());

            JsonObject input = this.packagingService.performPackaging(modelPackage);
            String modelId = input.get("modelId").getAsString();

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
            String dataSetId = returnValue.get("dataSetId").getAsString();
            JsonArray dataSetIdArray = new JsonArray();
            dataSetIdArray.add(dataSetId);
            input = new JsonObject();
            input.addProperty("modelId", modelId);
            input.add("dataSetIds", dataSetIdArray);

            //Deploy the model
            JsonObject deployModel = new JsonObject();
            deployModel.addProperty("modelId", modelId);
            given().body(deployModel.toString()).when().post("/liveModel/deployJavaModel").andReturn();


            response = given().body(input.toString()).when().post("/liveModel/evalJava").andReturn();
            logger.info("************************");
            logger.info(response.statusLine());
            logger.info("************************");
            assertEquals(200, response.getStatusCode());
        }

        String token = this.securityTokenContainer.getSecurityToken().getToken();


        //TODO: TURN_BACK_ON
        //String requestChainId = this.aiTrafficAgent.
        //List<JsonObject> traffic = this.dataReplayService.replayDiffChain(requestChainId);
        //logger.info(traffic.toString());

        //String responseChainId = this.aiTrafficAgent.findResponseChainId(token);
        //assertNotNull(responseChainId);
        //logger.info("ResponseChainId: "+responseChainId);
        //traffic = this.dataReplayService.replayDiffChain(responseChainId);
        //logger.info(traffic.toString());
    }
}