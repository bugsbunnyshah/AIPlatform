package io.bugsbunny.restClient;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.quarkus.test.junit.QuarkusTest;
import net.minidev.json.JSONValue;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import javax.inject.Inject;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
public class DataBricksClientTests {
    private static Logger logger = LoggerFactory.getLogger(DataBricksClientTests.class);

    @Inject
    private DataBricksClient dataBricksClient;

    @Test
    public void testCreateDevExperiment() throws Exception
    {
        String experiment = "appgal-"+UUID.randomUUID().toString();
        String experimentId = this.dataBricksClient.createDevExperiment(experiment);
        logger.info("****************");
        logger.info("ExperimentId: "+experimentId);
        logger.info("****************");
    }

    @Test
    public void testGetExperiments() throws Exception
    {
        JsonObject jsonObject = this.dataBricksClient.getExperiments();
        logger.info("****************");
        logger.info(jsonObject.toString());
        logger.info("****************");
    }

    @Test
    public void testLogModel() throws Exception
    {
        String mlModel = IOUtils.toString(Thread.currentThread().getContextClassLoader().getResourceAsStream("MLmodel"),
                StandardCharsets.UTF_8);
        Yaml yaml= new Yaml();
        Object obj = yaml.load(mlModel);
        String json = JSONValue.toJSONString(obj);

        String experiment = "appgal-"+UUID.randomUUID().toString();
        String experimentId = this.dataBricksClient.createDevExperiment(experiment);
        String runId = this.dataBricksClient.createRun(experimentId);;
        this.dataBricksClient.logModel(runId, json);
    }
}
