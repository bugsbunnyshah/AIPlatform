package io.bugsbunny.dataScience.endpoint;

import com.google.gson.JsonObject;
import io.bugsbunny.data.history.service.PayloadReplayService;
import io.bugsbunny.dataScience.service.PackagingService;
import io.bugsbunny.endpoint.AITrafficAgent;
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
public class TrainModelTests {
    private static Logger logger = LoggerFactory.getLogger(TrainModelTests.class);

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
    public void testEvalJava() throws Exception
    {
        String modelPackage = IOUtils.resourceToString("dataScience/aiplatform-model.json", StandardCharsets.UTF_8,
                Thread.currentThread().getContextClassLoader());

        JsonObject input = this.packagingService.performPackaging(modelPackage);

        Response response = given().body(input.toString()).when().post("/trainModel/evalJava").andReturn();
        logger.info("************************");
        logger.info(response.statusLine());
        logger.info("************************");
        assertEquals(200, response.getStatusCode());
    }
}