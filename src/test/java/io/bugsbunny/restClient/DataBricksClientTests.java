package io.bugsbunny.restClient;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.bugsbunny.persistence.MongoDBJsonStore;
import io.quarkus.test.junit.QuarkusTest;
import net.minidev.json.JSONValue;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
public class DataBricksClientTests {
    private static Logger logger = LoggerFactory.getLogger(DataBricksClientTests.class);

    @Inject
    private DataBricksClient dataBricksClient;

    @Inject
    private MongoDBJsonStore mongoDBJsonStore;

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
        String runId = this.dataBricksClient.createRun(experimentId);
        String model = this.getModel();
        this.dataBricksClient.logModel(runId, json, model);
        logger.info(this.mongoDBJsonStore.getLiveModel(runId).toString());
    }
    //-------------------------------------------------------------------------------------------------------
    private String getModel()
    {
        ByteArrayOutputStream modelStream = null;
        ObjectOutputStream out = null;
        try
        {
            String model = IOUtils.toString(Thread.currentThread().
                            getContextClassLoader().getResourceAsStream("databricks/model.pkl"),
                    StandardCharsets.UTF_8);

            //Register the Trained Model with the DataBricks Repository
            modelStream = new ByteArrayOutputStream();
            out = new ObjectOutputStream(modelStream);
            out.writeObject(model);

            return Base64.getEncoder().encodeToString(modelStream.toByteArray());
        }
        catch(Exception e)
        {
            logger.info(e.getMessage(), e);
            throw new RuntimeException(e);
        }
        finally
        {
            if(out != null)
            {
                try
                {
                    out.close();
                }
                catch(IOException ioe)
                {
                    //Tried to cleanup..no biggie no biggie if still problemo time (lol)
                }
            }
            if(modelStream != null)
            {
                try
                {
                    modelStream.close();
                }
                catch(IOException ioe)
                {
                    //Tried to cleanup..no biggie no biggie if still problemo time (lol)
                }
            }
        }
    }
}
