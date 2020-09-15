package io.bugsbunny.data.history.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.bugsbunny.endpoint.SecurityToken;
import io.bugsbunny.endpoint.SecurityTokenContainer;
import org.apache.commons.io.IOUtils;

import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class PayloadReplayServiceTests {
    private static Logger logger = LoggerFactory.getLogger(PayloadReplayServiceTests.class);

    @Inject
    private PayloadReplayService payloadReplayService;

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
    public void testDiffChainProcess() throws Exception
    {
        String email0 = IOUtils.toString(
                Thread.currentThread().getContextClassLoader().getResourceAsStream("historyEngine/email0.json"),
                StandardCharsets.UTF_8);

        String email1 = IOUtils.toString(
                Thread.currentThread().getContextClassLoader().getResourceAsStream("historyEngine/diffChain/email1.json"),
                StandardCharsets.UTF_8);

        String email2 = IOUtils.toString(
                Thread.currentThread().getContextClassLoader().getResourceAsStream("historyEngine/diffChain/email2.json"),
                StandardCharsets.UTF_8);

        JsonObject top = JsonParser.parseString(email0).getAsJsonObject();
        JsonObject middle = JsonParser.parseString(email1).getAsJsonObject();
        JsonObject next = JsonParser.parseString(email2).getAsJsonObject();

        JsonArray jsonArray = new JsonArray();
        jsonArray.add(top);
        jsonArray.add(middle);
        jsonArray.add(next);

        String chainId = this.payloadReplayService.generateDiffChain(jsonArray);
        logger.info("************************");
        logger.info("ChainId: "+chainId);

        //Assert
        List<JsonObject> diffChain = this.payloadReplayService.replayDiffChain(chainId);
        logger.info("********REPLAY_CHAIN****************");
        logger.info(diffChain.toString());
        logger.info("************************");

        //Assert the stored data
        assertEquals(diffChain.get(0).hashCode(), top.hashCode());
        assertEquals(diffChain.get(1).hashCode(), middle.hashCode());
        assertEquals(diffChain.get(2).hashCode(), next.hashCode());
    }

    @Test
    public void testGenerateDiffChainRealData() throws Exception
    {
        String spaceData = IOUtils.toString(Thread.currentThread().getContextClassLoader().getResourceAsStream(
                "dataMapper/data.csv"),
                StandardCharsets.UTF_8);

        String[] lines = spaceData.split("\n");
        String header = lines[0];
        String[] columns = header.split(",");
        JsonArray array = new JsonArray();
        int length = lines.length;
        for(int i=1; i<length; i++)
        {
            String line = lines[i];
            String[] data = line.split(",");
            JsonObject jsonObject = new JsonObject();
            for(int j=0; j<data.length; j++)
            {
                jsonObject.addProperty(columns[j],data[j]);
            }
            array.add(jsonObject);
        }

        String chainId = this.payloadReplayService.generateDiffChain(array);
        logger.info("************************");
        logger.info("ChainId: "+chainId);
        logger.info("************************");

        //Assert
        List<JsonObject> diffChain = this.payloadReplayService.replayDiffChain(chainId);
        assertNotNull(diffChain);
        logger.info("************************");
        logger.info(diffChain.toString());
        logger.info("************************");
    }
}
