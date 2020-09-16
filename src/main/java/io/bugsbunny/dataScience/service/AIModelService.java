package io.bugsbunny.dataScience.service;

import io.bugsbunny.persistence.MongoDBJsonStore;

import org.deeplearning4j.datasets.iterator.impl.MnistDataSetIterator;
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

@ApplicationScoped
public class AIModelService
{
    private static Logger logger = LoggerFactory.getLogger(AIModelService.class);

    @Inject
    private MongoDBJsonStore mongoDBJsonStore;

    private MultiLayerNetwork network;

    public String eval(long modelId)
    {
        try
        {
            if(this.network == null)
            {
                logger.info("******************************************");
                logger.info("DESERIALZING_THE_MODEL");
                logger.info("******************************************");
                String modelString = this.mongoDBJsonStore.getModel(modelId);
                ByteArrayInputStream restoreStream = new ByteArrayInputStream(Base64.getDecoder().decode(modelString));
                this.network = ModelSerializer.restoreMultiLayerNetwork(restoreStream, true);
            }

            DataSetIterator mnistTest = new MnistDataSetIterator(10000, false, 12345);
            Evaluation evaluation = this.network.evaluate(mnistTest);

            return evaluation.toJson();
        }
        catch(Exception e)
        {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
