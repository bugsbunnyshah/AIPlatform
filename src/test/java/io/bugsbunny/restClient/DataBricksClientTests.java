package io.bugsbunny.restClient;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.quarkus.test.junit.QuarkusTest;
import net.minidev.json.JSONValue;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import javax.inject.Inject;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
public class DataBricksClientTests {
    private static Logger logger = LoggerFactory.getLogger(DataBricksClientTests.class);

    @Inject
    private DataBricksClient dataBricksClient;

    @Test
    public void testCreateExperiment()
    {
        this.dataBricksClient.createExperiment();
    }

    @Test
    public void testGetExperiments()
    {
        this.dataBricksClient.getExperiments();
    }

    @Test
    public void testCreateRun()
    {
        String runId = this.dataBricksClient.createRun();
        logger.info("*******");
        logger.info("RunId: "+runId);
        logger.info("*******");
        assertNotNull(runId);
    }

    @Test
    public void testGetRun()
    {
        String runId = "1b117ece479c47aca912feb75bc55b0a";
        this.dataBricksClient.getRun(runId);
    }

    @Test
    public void testLogModel() throws Exception
    {
        //String yamlString = IOUtils.toString(Thread.currentThread().getContextClassLoader().getResourceAsStream("mlmodel")
        //        , StandardCharsets.UTF_8);
        String yamlString = "artifact_path: model\n" +
                "flavors:\n" +
                "  python_function:\n" +
                "    data: model.pkl\n" +
                "    env: conda.yaml\n" +
                "    loader_module: mlflow.sklearn\n" +
                "    python_version: 3.6.10\n" +
                "  sklearn:\n" +
                "    pickled_model: model.pkl\n" +
                "    serialization_format: cloudpickle\n" +
                "    sklearn_version: 0.19.1\n" +
                "run_id: 1b117ece479c47aca912feb75bc55b0a\n" +
                "utc_time_created: '2020-06-26 18:00:56.056775'";
        Yaml yaml= new Yaml();
        Object obj = yaml.load(yamlString);

        String json = JSONValue.toJSONString(obj);
        JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
        jsonObject.addProperty("modelSer", UUID.randomUUID().toString());
        json = jsonObject.toString();
        logger.info(json);

        String runId = "1b117ece479c47aca912feb75bc55b0a";
        this.dataBricksClient.logModel(runId, json);
        this.dataBricksClient.getRun(runId);
    }
}
