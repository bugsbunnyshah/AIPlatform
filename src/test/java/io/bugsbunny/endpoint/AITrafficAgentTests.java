package io.bugsbunny.endpoint;

import com.google.gson.JsonObject;
import io.bugsbunny.data.history.service.PayloadReplayService;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
public class AITrafficAgentTests {
    private static Logger logger = LoggerFactory.getLogger(AITrafficAgentTests.class);

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
    public void testEvalWithTrafficReplay() throws Exception
    {
        for(int i=0; i<3; i++)
        {
            Response response = given().body("{\"oid\":\"" + UUID.randomUUID() + "\"}").when().post("/liveModel/eval").andReturn();
            logger.info("************************");
            logger.info(response.statusLine());
            logger.info("************************");
            //logger.info(response.body().asString());
            //logger.info("************************");
        }

        String token = this.securityTokenContainer.getTokenContainer().get().getToken();


        String requestChainId = this.aiTrafficAgent.findRequestChainId(token);
        List<JsonObject> traffic = this.payloadReplayService.replayDiffChain(requestChainId);
        logger.info(traffic.toString());

        String responseChainId = this.aiTrafficAgent.findResponseChainId(token);
        assertNotNull(responseChainId);
        //traffic = this.payloadReplayService.replayDiffChain(responseChainId);
        //logger.info(traffic.toString());
    }
}