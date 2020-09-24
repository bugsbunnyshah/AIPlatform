package io.bugsbunny.dataScience.service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.bugsbunny.endpoint.SecurityToken;
import jep.Interpreter;
import jep.JepException;
import jep.SharedInterpreter;

import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.evaluation.classification.Evaluation;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import java.io.ByteArrayInputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import io.bugsbunny.dataScience.dl4j.AIPlatformDataSetIteratorFactory;
import io.bugsbunny.persistence.MongoDBJsonStore;

@ApplicationScoped
public class AIModelService
{
    private static Logger logger = LoggerFactory.getLogger(AIModelService.class);

    private static ThreadLocal<Boolean> testInvocation;
    public static void activateTest()
    {
        AIModelService.testInvocation = new ThreadLocal<>();
    }
    public static boolean isTest()
    {
        return (AIModelService.testInvocation != null);
    }

    @Inject
    private MongoDBJsonStore mongoDBJsonStore;

    @Inject
    private AIPlatformDataSetIteratorFactory aiPlatformDataSetIteratorFactory;

    private Map<Long, MultiLayerNetwork> activeModels;

    public AIModelService()
    {
        this.activeModels = new HashMap<>();
    }

    public String evalJava(long modelId, long dataSetId)
    {
        try
        {
            MultiLayerNetwork network = this.activeModels.get(modelId);

            if(network == null)
            {
                //logger.info("******************************************");
                //logger.info("DESERIALZING_THE_MODEL: "+modelId);
                //logger.info("******************************************");
                String modelString = this.mongoDBJsonStore.getModel(modelId);
                ByteArrayInputStream restoreStream = new ByteArrayInputStream(Base64.getDecoder().decode(modelString));
                network = ModelSerializer.restoreMultiLayerNetwork(restoreStream, true);
                this.activeModels.put(modelId, network);
            }

            DataSetIterator dataSetIterator = this.aiPlatformDataSetIteratorFactory.getInstance(dataSetId);
            Evaluation evaluation = network.evaluate(dataSetIterator);

            return evaluation.toJson();
        }
        catch(Exception e)
        {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public String evalPython(long modelId, long dataSetId) throws JepException
    {
        if(AIModelService.isTest())
        {
            JsonObject testResponse = new JsonObject();
            testResponse.addProperty("output", "test");
            return testResponse.toString();
        }
        String output = (new JsonObject()).toString();
        JsonObject modelPackage = this.mongoDBJsonStore.getModelPackage(modelId);
        String pythonScript = modelPackage.get("script").getAsString();
        try (Interpreter interp = new SharedInterpreter())
        {
            interp.exec(pythonScript);
            output = interp.getValue("output", String.class);
        }
        return output;
    }
}
