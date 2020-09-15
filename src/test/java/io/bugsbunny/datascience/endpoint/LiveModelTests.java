package io.bugsbunny.datascience.endpoint;

import io.bugsbunny.data.history.service.PayloadReplayService;
import io.bugsbunny.endpoint.AITrafficAgent;
import io.bugsbunny.endpoint.SecurityToken;
import io.bugsbunny.endpoint.SecurityTokenContainer;
import io.quarkus.test.junit.QuarkusTest;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.restassured.response.Response;

import javax.inject.Inject;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
public class LiveModelTests {
    private static Logger logger = LoggerFactory.getLogger(LiveModelTests.class);

    @Inject
    private PayloadReplayService payloadReplayService;

    @Inject
    private SecurityTokenContainer securityTokenContainer;

    @Inject
    private AITrafficAgent aiTrafficAgent;

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
    public void testEval() throws Exception
    {
        Response response = given().body("{}").when().post("/liveModel/eval").andReturn();
        logger.info("************************");
        logger.info(response.statusLine());
        logger.info("************************");
        assertEquals(200, response.getStatusCode());
    }
}