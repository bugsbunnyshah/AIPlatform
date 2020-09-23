package io.bugsbunny.dataScience.service;

import io.bugsbunny.dataScience.dl4j.AIPlatformDataSetIteratorFactory;
import io.bugsbunny.persistence.MongoDBJsonStore;

import jep.Interpreter;
import jep.SharedInterpreter;

import org.apache.commons.io.IOUtils;

import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.evaluation.classification.Evaluation;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@RequestScoped
public class AIModelService
{
    private static Logger logger = LoggerFactory.getLogger(AIModelService.class);

    @Inject
    private MongoDBJsonStore mongoDBJsonStore;

    @Inject
    private AIPlatformDataSetIteratorFactory aiPlatformDataSetIteratorFactory;

    private MultiLayerNetwork network;

    public String evalJava(long modelId, long dataSetId)
    {
        try
        {
            if(this.network == null)
            {
                logger.info("******************************************");
                logger.info("DESERIALZING_THE_MODEL: "+modelId);
                logger.info("******************************************");
                String modelString = this.mongoDBJsonStore.getModel(modelId);
                ByteArrayInputStream restoreStream = new ByteArrayInputStream(Base64.getDecoder().decode(modelString));
                this.network = ModelSerializer.restoreMultiLayerNetwork(restoreStream, true);
            }

            DataSetIterator dataSetIterator = this.aiPlatformDataSetIteratorFactory.getInstance(dataSetId);
            Evaluation evaluation = this.network.evaluate(dataSetIterator);

            return evaluation.toJson();
        }
        catch(Exception e)
        {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public String evalPython(long modelId, long dataSetId)
    {
        try
        {
            String score;
            String pythonScript = "score=0.0";
            try (Interpreter interp = new SharedInterpreter()) {
                interp.exec(pythonScript);
                score = ""+ interp.getValue("score", Float.class);
            }
            return score;
        }
        catch(Exception e)
        {
            throw new RuntimeException(e);
        }
    }
}
