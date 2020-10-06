package io.bugsbunny.dataScience.service;

import com.google.gson.JsonArray;
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

    public String evalJava(long modelId, long[] dataSetIds)
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

    public String trainJava(long modelId, long[] dataSetIds)
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

            DataSetIterator dataSetIterator = this.aiPlatformDataSetIteratorFactory.
                    getInstance(dataSetIds);
            network.fit(dataSetIterator);

            //Deploy the Model
            ByteArrayOutputStream modelBytes = new ByteArrayOutputStream();
            ModelSerializer.writeModel(network, modelBytes, false);
            String modelString = Base64.getEncoder().encodeToString(modelBytes.toByteArray());

            JsonObject currentModel = this.mongoDBJsonStore.getModelPackage(modelId);
            currentModel.addProperty("model", modelString);
            this.mongoDBJsonStore.updateModel(modelId, currentModel);

            this.activeModels.put(modelId, network);

            return new JsonObject().toString();
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
