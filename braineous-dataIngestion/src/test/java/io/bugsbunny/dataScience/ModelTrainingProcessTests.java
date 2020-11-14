package io.bugsbunny.dataScience;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.bugsbunny.dataScience.dl4j.AIPlatformDataSetIteratorFactory;
import io.bugsbunny.preprocess.SecurityTokenContainer;
import io.bugsbunny.test.components.BaseTest;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.datavec.api.records.reader.RecordReader;
import org.datavec.api.records.reader.impl.csv.CSVRecordReader;
import org.datavec.api.split.FileSplit;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.deeplearning4j.datasets.iterator.impl.MnistDataSetIterator;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.layers.*;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.deeplearning4j.util.ModelSerializer;
import org.junit.jupiter.api.Test;
import org.nd4j.common.primitives.Pair;
import org.nd4j.evaluation.classification.Evaluation;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.learning.config.Nesterovs;
import org.nd4j.linalg.lossfunctions.LossFunctions.LossFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
public class ModelTrainingProcessTests extends BaseTest
{
    private static Logger logger = LoggerFactory.getLogger(ModelTrainingProcessTests.class);

    public static String dataLocalPath;
    public static boolean visualize = true;

    @Inject
    private AIPlatformDataSetIteratorFactory aiPlatformDataSetIteratorFactory;

    @Inject
    private SecurityTokenContainer securityTokenContainer;

    //@Test
    public void testSaturnClassifierModel() throws Exception
    {
        int batchSize = 50;
        int seed = 123;
        double learningRate = 0.005;
        int nEpochs = 30;

        int numInputs = 2;
        int numOutputs = 2;
        int numHiddenNodes = 20;

        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(seed)
                .weightInit(WeightInit.XAVIER)
                .updater(new Nesterovs(learningRate, 0.9))
                .list()
                .layer(new DenseLayer.Builder().nIn(numInputs).nOut(numHiddenNodes)
                        .activation(Activation.RELU)
                        .build())
                .layer(new OutputLayer.Builder(LossFunction.NEGATIVELOGLIKELIHOOD)
                        .activation(Activation.SOFTMAX)
                        .nIn(numHiddenNodes).nOut(numOutputs).build())
                .build();


        MultiLayerNetwork model = new MultiLayerNetwork(conf);
        model.init();
        model.setListeners(new ScoreIterationListener(10));

        //Deploy the Model
        ByteArrayOutputStream modelBytes = new ByteArrayOutputStream();
        ModelSerializer.writeModel(model, modelBytes, false);
        String modelString = Base64.getEncoder().encodeToString(modelBytes.toByteArray());

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("name", UUID.randomUUID().toString());
        jsonObject.addProperty("model", modelString);
        logger.info(jsonObject.toString());

        Response packageResponse = given().body(jsonObject.toString()).when().post("/aimodel/performPackaging/")
                .andReturn();
        packageResponse.body().prettyPrint();
        JsonObject deployedModel = JsonParser.parseString(packageResponse.body().asString()).getAsJsonObject();
        long modelId = deployedModel.get("modelId").getAsLong();

        String tmp = "tmp";

        //Load the test/evaluation data:
        String data = IOUtils.resourceToString("dataScience/saturn_data_eval.csv", StandardCharsets.UTF_8,
                Thread.currentThread().getContextClassLoader());
        JsonObject input = new JsonObject();
        input.addProperty("modelId", modelId);
        input.addProperty("format", "csv");
        input.addProperty("data", data);
        Response response = given().body(input.toString()).when().post("/dataset/storeEvalDataSet/").andReturn();
        JsonObject returnValue = JsonParser.parseString(response.body().asString()).getAsJsonObject();
        long evalDataSetId = returnValue.get("dataSetId").getAsLong();
        response = given().get("/dataset/readDataSet/?dataSetId="+evalDataSetId).andReturn();
        returnValue = JsonParser.parseString(response.body().asString()).getAsJsonObject();
        String storedData = returnValue.get("data").getAsString();
        String testFileName = UUID.randomUUID().toString();
        File testFile = new File(tmp+"/"+testFileName);
        FileUtils.writeStringToFile(testFile, storedData);
        RecordReader rrTest = new CSVRecordReader();
        //InputStreamInputSplit testSplit = new InputStreamInputSplit(new ByteArrayInputStream(
        //        storedData.getBytes(UTF_8)));
        rrTest.initialize(new FileSplit(testFile));
        DataSetIterator testIter = new RecordReaderDataSetIterator(rrTest, batchSize, 0, 2);
        //DataSetIterator testIter = AIPlatformDataSetIteratorFactory.getInstance();

        data = IOUtils.resourceToString("dataScience/saturn_data_train.csv", StandardCharsets.UTF_8,
                Thread.currentThread().getContextClassLoader());
        input = new JsonObject();
        input.addProperty("format", "csv");
        input.addProperty("data", data);
        response = given().body(input.toString()).when().post("/dataset/storeTrainingDataSet/").andReturn();
        returnValue = JsonParser.parseString(response.body().asString()).getAsJsonObject();
        long dataSetId = returnValue.get("dataSetId").getAsLong();
        response = given().get("/dataset/readDataSet/?dataSetId="+dataSetId).andReturn();
        returnValue = JsonParser.parseString(response.body().asString()).getAsJsonObject();
        storedData = returnValue.get("data").getAsString();
        String trainFileName = UUID.randomUUID().toString();
        File trainFile = new File(tmp+"/"+trainFileName);
        FileUtils.writeStringToFile(trainFile, storedData);
        RecordReader rrTrain = new CSVRecordReader();
        //InputStreamInputSplit trainSplit = new InputStreamInputSplit(new ByteArrayInputStream(
        //        storedData.getBytes(UTF_8)));
        rrTrain.initialize(new FileSplit(trainFile));
        //DataSetIterator trainIter = new RecordReaderDataSetIterator(rrTrain, batchSize, 0, 2);
        DataSetIterator trainIter = this.aiPlatformDataSetIteratorFactory.getInstance(
                new long[]{8262950843826255554l});

        model.fit(trainIter, nEpochs);

        logger.info("Evaluate model....");
        Evaluation eval = model.evaluate(testIter);
        logger.info(eval.stats());

        //Deploy the model
        JsonObject deployModel = new JsonObject();
        deployModel.addProperty("modelId", modelId);
        given().body(deployModel.toString()).when().post("/liveModel/deployJavaModel").andReturn();

        //Run the Model in the Cloud
        JsonObject deployResult = JsonParser.parseString(packageResponse.body().asString()).getAsJsonObject();
        JsonArray dataSetIdArray = new JsonArray();
        dataSetIdArray.add(dataSetId);
        deployResult.add("dataSetIds",dataSetIdArray);
        response = given().body(deployResult.toString()).when().post("/liveModel/evalJava").andReturn();
        response.body().prettyPrint();
        assertEquals(200, response.getStatusCode());
    }

    //@Test
    public void testMnistModel() throws Exception
    {
        int outputNum = 10; // The number of possible outcomes
        int batchSize = 64; // Test batch size
        int nEpochs = 10;   // Number of training epochs
        int seed = 123;

        nEpochs = 1;

        //Lambda defines the relative strength of the center loss component.
        //lambda = 0.0 is equivalent to training with standard softmax only
        double lambda = 1.0;

        //Alpha can be thought of as the learning rate for the centers for each class
        double alpha = 0.1;

        logger.info("Load data....");
        DataSetIterator mnistTrain = new MnistDataSetIterator(batchSize, true, 12345);
        //DataSetIterator mnistTrain = this.aiPlatformDataSetIteratorFactory.getInstance(
        //        new long[]{8262950843826255554l});
        DataSetIterator mnistTest = new MnistDataSetIterator(10000, false, 12345);

        logger.info("Build model....");
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
                .layer(new CenterLossOutputLayer.Builder(LossFunction.NEGATIVELOGLIKELIHOOD)
                        .nIn(2).nOut(outputNum)
                        .weightInit(WeightInit.XAVIER).activation(Activation.SOFTMAX)
                        //Alpha and lambda hyperparameters are specific to center loss model: see comments above and paper
                        .alpha(alpha).lambda(lambda)
                        .build())
                .setInputType(InputType.convolutionalFlat(28, 28, 1))
                .build();

        MultiLayerNetwork model = new MultiLayerNetwork(conf);
        model.init();


        logger.info("Train model....");
        model.setListeners(new ScoreIterationListener(100));

        List<Pair<INDArray, INDArray>> embeddingByEpoch = new ArrayList<>();
        List<Integer> epochNum = new ArrayList<>();

        DataSet testData = mnistTest.next();
        for (int i = 0; i < nEpochs; i++) {
            model.fit(mnistTrain);

            logger.info("*** Completed epoch {} ***", i);

            //Feed forward to the embedding layer (layer 5) to get the 2d embedding to plot later
            INDArray embedding = model.feedForwardToLayer(5, testData.getFeatures()).get(6);

            embeddingByEpoch.add(new Pair<>(embedding, testData.getLabels()));
            epochNum.add(i);
        }

        System.out.println("Evaluate model....");
        Evaluation eval = model.evaluate(mnistTest);
        System.out.println(eval.stats());
    }
}
