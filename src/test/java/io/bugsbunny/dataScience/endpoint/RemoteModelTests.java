package io.bugsbunny.dataScience.endpoint;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import io.bugsbunny.dataScience.service.PackagingService;
import io.bugsbunny.endpoint.SecurityToken;
import io.bugsbunny.endpoint.SecurityTokenContainer;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.nio.charset.StandardCharsets;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
public class RemoteModelTests {
    private static Logger logger = LoggerFactory.getLogger(RemoteModelTests.class);

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
    public void testInvocations() throws Exception
    {
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
        logger.info("modelId: "+modelId);

        JsonObject request = new JsonObject();
        request.add("payload", jsonObject);
        request.addProperty("modelId", modelId);
        Response response = given().body(request.toString()).when().post("/remoteModel/mlflow/invocations").andReturn();
        logger.info("************************");
        logger.info(response.statusLine());
        response.body().prettyPrint();
        logger.info("************************");
        assertEquals(200, response.getStatusCode());
    }
}