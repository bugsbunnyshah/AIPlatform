package io.bugsbunny.dataScience.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import io.bugsbunny.dataScience.endpoint.ModelIsLive;
import io.bugsbunny.dataScience.endpoint.ModelNotFoundException;
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
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import io.bugsbunny.dataScience.dl4j.AIPlatformDataSetIteratorFactory;
import io.bugsbunny.persistence.MongoDBJsonStore;

@ApplicationScoped
public class AIModelService
{
    private static Logger logger = LoggerFactory.getLogger(AIModelService.class);

    @Inject
    private MongoDBJsonStore mongoDBJsonStore;

    @Inject
    private PackagingService packagingService;

    @Inject
    private AIPlatformDataSetIteratorFactory aiPlatformDataSetIteratorFactory;

    private Map<Long, MultiLayerNetwork> activeModels;

    public AIModelService()
    {
        this.activeModels = new HashMap<>();
    }

    public String trainJava(long modelId, long[] dataSetIds) throws ModelNotFoundException, ModelIsLive
    {
        JsonObject modelPackage = this.mongoDBJsonStore.getModelPackage(modelId);
        if(modelPackage == null)
        {
            throw new ModelNotFoundException("MODEL_NOT_FOUND:"+modelId);
        }
        if(modelPackage.get("live").getAsBoolean() == true)
        {
            throw new ModelIsLive("LIVE_MODEL_TRAINING_DOESNOT_MAKE_SENSE:"+modelId);
        }
        String modelString = this.mongoDBJsonStore.getModel(modelId);
        try
        {
            MultiLayerNetwork network = this.activeModels.get(modelId);

            if(network == null)
            {
                //logger.info("******************************************");
                //logger.info("DESERIALZING_THE_MODEL: "+modelId);
                //logger.info("******************************************");
                ByteArrayInputStream restoreStream = new ByteArrayInputStream(Base64.getDecoder().decode(modelString));
                network = ModelSerializer.restoreMultiLayerNetwork(restoreStream, true);
                this.activeModels.put(modelId, network);
            }

            DataSetIterator dataSetIterator = this.aiPlatformDataSetIteratorFactory.
                    getInstance(dataSetIds);
            network.fit(dataSetIterator);

            Evaluation evaluation = network.evaluate(dataSetIterator);

            this.activeModels.put(modelId, network);

            return evaluation.toJson();
        }
        catch(Exception e)
        {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public void deployModel(long modelId) throws ModelNotFoundException
    {
        String modelString = this.mongoDBJsonStore.getModel(modelId);
        if(modelString == null)
        {
            throw new ModelNotFoundException("MODEL_NOT_FOUND:"+modelId);
        }

        try {
            //Taking care of idempotency, whatever that means...I know and understand..but what a freaking word lol
            if(this.activeModels.get(modelId) != null)
            {
                return;
            }

            JsonObject currentModel = this.mongoDBJsonStore.getModelPackage(modelId);
            currentModel.addProperty("model", modelString);
            this.mongoDBJsonStore.deployModel(modelId);


            ByteArrayInputStream restoreStream = new ByteArrayInputStream(Base64.getDecoder().decode(modelString));
            MultiLayerNetwork network = ModelSerializer.restoreMultiLayerNetwork(restoreStream, true);
            this.activeModels.put(modelId, network);
        }
        catch(Exception e)
        {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public String evalJava(long modelId, long[] dataSetIds) throws ModelNotFoundException
    {
        String modelString = this.mongoDBJsonStore.getModel(modelId);
        if(modelString == null)
        {
            throw new ModelNotFoundException("MODEL_NOT_FOUND:"+modelId);
        }
        try
        {
            MultiLayerNetwork network = this.activeModels.get(modelId);

            if(network == null)
            {
                //logger.info("******************************************");
                //logger.info("DESERIALZING_THE_MODEL: "+modelId);
                //logger.info("******************************************");
                ByteArrayInputStream restoreStream = new ByteArrayInputStream(Base64.getDecoder().decode(modelString));
                network = ModelSerializer.restoreMultiLayerNetwork(restoreStream, true);
                this.activeModels.put(modelId, network);
            }

            DataSetIterator dataSetIterator = this.aiPlatformDataSetIteratorFactory.
                    getInstance(dataSetIds);
            Evaluation evaluation = network.evaluate(dataSetIterator);

            return evaluation.toJson();
        }
        catch(Exception e)
        {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public String evalPython(long modelId, long[] dataSetIds) throws JepException
    {
        String output;
        JsonObject modelPackage = this.packagingService.getModelPackage(modelId);
        String pythonScript = modelPackage.get("script").getAsString();
        try (Interpreter interp = new SharedInterpreter())
        {
            JsonArray dataSetIdArray = new JsonArray();
            for(long dataSetId:dataSetIds)
            {
                dataSetIdArray.add(dataSetId);
            }
            interp.set("dataSetId", dataSetIdArray.toString());
            interp.set("modelId", modelId);
            interp.exec(pythonScript);
            output = interp.getValue("output", String.class);
        }
        return output;
    }
}
