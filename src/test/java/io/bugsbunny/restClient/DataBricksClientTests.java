package io.bugsbunny.restClient;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.bugsbunny.endpoint.SecurityToken;
import io.bugsbunny.endpoint.SecurityTokenContainer;
import io.bugsbunny.persistence.MongoDBJsonStore;
import io.quarkus.test.junit.QuarkusTest;
import net.minidev.json.JSONValue;
import org.apache.commons.io.IOUtils;
import org.deeplearning4j.datasets.iterator.impl.MnistDataSetIterator;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.layers.CenterLossOutputLayer;
import org.deeplearning4j.nn.conf.layers.ConvolutionLayer;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.SubsamplingLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.util.ModelSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.nd4j.evaluation.classification.Evaluation;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import javax.inject.Inject;
import java.io.*;
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

    @Inject
    private SecurityTokenContainer securityTokenContainer;

    @BeforeEach
    public void setUp() throws Exception
    {
        String securityTokenJson = IOUtils.toString(Thread.currentThread().getContextClassLoader().
                        getResourceAsStream("oauthAgent/token.json"),
                StandardCharsets.UTF_8);
        SecurityToken securityToken = SecurityToken.fromJson(securityTokenJson);
        this.securityTokenContainer.setSecurityToken(securityToken);
    }

    /*@Test
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
        JsonObject liveModel = this.mongoDBJsonStore.getLiveModel(runId);

        //Test Evaluating the Model
        String modelSer = liveModel.get("model").getAsString();
        ByteArrayInputStream restoreStream = new ByteArrayInputStream(Base64.getDecoder().decode(modelSer));
        MultiLayerNetwork network = ModelSerializer.restoreMultiLayerNetwork(restoreStream, true);
        DataSetIterator mnistTest = new MnistDataSetIterator(10000, false, 12345);
        Evaluation evaluation = network.evaluate(mnistTest);
        logger.info(evaluation.toJson());
    }
    //-------------------------------------------------------------------------------------------------------
    private String getModel()
    {
        try
        {
            int outputNum = 10; // The number of possible outcomes
            int batchSize = 64; // Test batch size
            int nEpochs = 1;   // Number of training epochs
            int seed = 123;

            //Lambda defines the relative strength of the center loss component.
            //lambda = 0.0 is equivalent to training with standard softmax only
            double lambda = 1.0;

            //Alpha can be thought of as the learning rate for the centers for each class
            double alpha = 0.1;

            MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                    .seed(seed)
                    .l2(0.0005)
                    .activation(Activation.LEAKYRELU)
                    .weightInit(WeightInit.RELU)
                    .updater(new Adam(0.01))
                    .list()
                    .layer(new ConvolutionLayer.Builder(5, 5).stride(1, 1).nOut(32).activation(Activation.LEAKYRELU).build())
                    .layer(new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX).kernelSize(2, 2).stride(2, 2).build())
                    .layer(new ConvolutionLayer.Builder(5, 5).stride(1, 1).nOut(64).build())
                    .layer(new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX).kernelSize(2, 2).stride(2, 2).build())
                    .layer(new DenseLayer.Builder().nOut(256).build())
                    //Layer 5 is our embedding layer: 2 dimensions, just so we can plot it on X/Y grid. Usually use more in practice
                    .layer(new DenseLayer.Builder().activation(Activation.IDENTITY).weightInit(WeightInit.XAVIER).nOut(2)
                            //Larger L2 value on the embedding layer: can help to stop the embedding layer weights
                            // (and hence activations) from getting too large. This is especially problematic with small values of
                            // lambda such as 0.0
                            .l2(0.1).build())
                    .layer(new CenterLossOutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                            .nIn(2).nOut(outputNum)
                            .weightInit(WeightInit.XAVIER).activation(Activation.SOFTMAX)
                            //Alpha and lambda hyperparameters are specific to center loss model: see comments above and paper
                            .alpha(alpha).lambda(lambda)
                            .build())
                    .setInputType(InputType.convolutionalFlat(28, 28, 1))
                    .build();

            MultiLayerNetwork orig = new MultiLayerNetwork(conf);
            orig.init();

            ByteArrayOutputStream modelStream = new ByteArrayOutputStream();
            ModelSerializer.writeModel(orig, modelStream, true);
            String model = Base64.getEncoder().encodeToString(modelStream.toByteArray());

            return model;
        }
        catch(Exception e)
        {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }*/

    //TODO: Re-activate Later...Redundant
    //@Test
    /*public void testInvokeDatabricksModel() throws Exception
    {
        JsonObject jsonObject = new JsonObject();
        JsonArray columns = new JsonArray();
        columns.add("x");
        JsonArray first = new JsonArray();
        first.add(1);
        JsonArray second = new JsonArray();
        second.add(-1);
        JsonArray data = new JsonArray();
        data.add(first);
        data.add(second);
        jsonObject.add("columns", columns);
        jsonObject.add("data", data);
        JsonElement response = this.dataBricksClient.invokeDatabricksModel(jsonObject);
        logger.info("****************");
        logger.info(response.toString());
        logger.info("****************");

        //TODO: ASSERT
    }*/
}
