package io.bugsbunny.dataScience;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.bugsbunny.endpoint.SecurityToken;
import io.bugsbunny.endpoint.SecurityTokenContainer;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import io.restassured.response.Response;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.FileUtils;

import org.datavec.api.records.reader.RecordReader;
import org.datavec.api.records.reader.impl.csv.CSVRecordReader;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.evaluation.classification.Evaluation;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.learning.config.Nesterovs;
import org.nd4j.linalg.lossfunctions.LossFunctions.LossFunction;
import org.datavec.api.split.FileSplit;
import org.deeplearning4j.util.ModelSerializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static java.nio.charset.StandardCharsets.UTF_8;

@QuarkusTest
public class ModelTrainingProcessTests
{
    private static Logger logger = LoggerFactory.getLogger(ModelTrainingProcessTests.class);

    public static String dataLocalPath;
    public static boolean visualize = true;

    @Inject
    private SecurityTokenContainer securityTokenContainer;

    @BeforeEach
    public void setUp() throws Exception
    {
        String securityTokenJson = IOUtils.toString(Thread.currentThread().getContextClassLoader().
                        getResourceAsStream("oauthAgent/token.json"),
                UTF_8);
        SecurityToken securityToken = SecurityToken.fromJson(securityTokenJson);
        this.securityTokenContainer.getTokenContainer().set(securityToken);
    }

    @Test
    public void testModel() throws Exception
    {
        int batchSize = 50;
        int seed = 123;
        double learningRate = 0.005;
        int nEpochs = 30;

        int numInputs = 2;
        int numOutputs = 2;
        int numHiddenNodes = 20;

        String tmp = "tmp";

        //Load the test/evaluation data:
        String data = IOUtils.resourceToString("dataScience/saturn_data_eval.csv", StandardCharsets.UTF_8,
                Thread.currentThread().getContextClassLoader());
        JsonObject input = new JsonObject();
        input.addProperty("format", "csv");
        input.addProperty("data", data);
        Response response = given().body(input.toString()).when().post("/dataset/storeEvalDataSet/").andReturn();
        JsonObject returnValue = JsonParser.parseString(response.body().asString()).getAsJsonObject();
        long dataSetId = returnValue.get("dataSetId").getAsLong();
        response = given().get("/dataset/readDataSet/?dataSetId="+dataSetId).andReturn();
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

        data = IOUtils.resourceToString("dataScience/saturn_data_train.csv", StandardCharsets.UTF_8,
                Thread.currentThread().getContextClassLoader());
        input = new JsonObject();
        input.addProperty("format", "csv");
        input.addProperty("data", data);
        response = given().body(input.toString()).when().post("/dataset/storeTrainingDataSet/").andReturn();
        returnValue = JsonParser.parseString(response.body().asString()).getAsJsonObject();
        dataSetId = returnValue.get("dataSetId").getAsLong();
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
        DataSetIterator trainIter = new RecordReaderDataSetIterator(rrTrain, batchSize, 0, 2);

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
        model.setListeners(new ScoreIterationListener(10));    //Print score every 10 parameter updates

        model.fit(trainIter, nEpochs);

        System.out.println("Evaluate model....");
        Evaluation eval = model.evaluate(testIter);
        System.out.println(eval.stats());

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

        //Run the Model in the Cloud
        response = given().body(packageResponse.body().asString()).when().post("/liveModel/eval").andReturn();
        response.body().prettyPrint();
        assertEquals(200, response.getStatusCode());
    }

    /*@Test
    public void testDataSetIteratorGeneric() throws Exception
    {
        //logger.info("*********DATA_SET_DATA********************");
        //logger.info(this.aiPlatformDataSetIterator.getLabels().toString());
        //logger.info("*****************************");

        int batchSize = 50;
        int seed = 123;
        double learningRate = 0.005;
        //Number of epochs (full passes of the data)
        int nEpochs = 30;

        int numInputs = 2;
        int numOutputs = 2;
        int numHiddenNodes = 20;

        dataLocalPath = DownloaderUtility.CLASSIFICATIONDATA.Download();
        //Load the training data:
        RecordReader rr = new CSVRecordReader();
        rr.initialize(new FileSplit(new File(dataLocalPath, "saturn_data_train.csv")));
        DataSetIterator trainIter = new RecordReaderDataSetIterator(rr, batchSize, 0, 2);
        //log.info("Build model....");
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
        model.setListeners(new ScoreIterationListener(10));    //Print score every 10 parameter updates

        model.fit(trainIter, nEpochs);

        Evaluation eval = model.evaluate(this.aiPlatformDataSetIterator);
        logger.info(eval.toJson());
    }*/
}
