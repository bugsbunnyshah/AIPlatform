package io.bugsbunny.dataScience.endpoint;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import com.google.gson.JsonParser;
import io.bugsbunny.dataScience.service.PackagingService;
import io.bugsbunny.infrastructure.MongoDBJsonStore;
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
public class RemoteModelTests extends BaseTest {
    private static Logger logger = LoggerFactory.getLogger(RemoteModelTests.class);

    @Inject
    private MongoDBJsonStore mongoDBJsonStore;

    @Inject
    private PackagingService packagingService;

    @Inject
    private SecurityTokenContainer securityTokenContainer;


    @Test
    public void testInvocations() throws Exception {
        JsonObject jsonObject = new JsonObject();
        JsonArray columns = new JsonArray();
        columns.add("x");
        JsonArray first = new JsonArray();
        first.add(1);
        JsonArray second = new JsonArray();
        second.add(-1);
        JsonArray data = new JsonArray();
        data.add(first);
        data.add(second);
        jsonObject.add("columns", columns);
        jsonObject.add("data", data);

        String modelPackage = IOUtils.resourceToString("dataScience/aiplatform-remote-model.json", StandardCharsets.UTF_8,
                Thread.currentThread().getContextClassLoader());

        JsonObject deploymentResponse = this.packagingService.performPackaging(modelPackage);
        long modelId = deploymentResponse.get("modelId").getAsLong();
        logger.info("modelId: " + modelId);

        JsonObject request = new JsonObject();
        request.add("payload", jsonObject);
        request.addProperty("modelId", modelId);
        for (int i = 0; i < 3; i++)
        {
            Response response = given().body(request.toString()).when().post("/remoteModel/mlflow/invocations").andReturn();
            logger.info("************************");
            logger.info(response.statusLine());
            response.body().prettyPrint();
            logger.info("************************");
            if(response.statusCode() == 500)
            {
                JsonObject error = JsonParser.parseString(response.body().asString()).getAsJsonObject();
                if(error.get("exception").getAsString().contains("Connection refused"))
                {
                    return;
                }
            }
            assertEquals(200, response.getStatusCode());
        }

        JsonObject rolledOverDataSetIds = this.mongoDBJsonStore.rollOverToTraningDataSets(this.securityTokenContainer.getTenant(),modelId);
        logger.info(rolledOverDataSetIds.toString());

        //TODO: Assert
        List<Long> dataSetIds = new ArrayList<>();
        JsonArray array = rolledOverDataSetIds.getAsJsonArray("rolledOverDataSetIds");
        assertTrue(array.size() > 0);
        Iterator<JsonElement> iterator = array.iterator();
        while(iterator.hasNext())
        {
            dataSetIds.add(iterator.next().getAsLong());
        }
    }
}