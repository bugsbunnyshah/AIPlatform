package io.bugsbunny.showcase.aviation;

import io.bugsbunny.dataScience.utils.DownloaderUtility;
import io.bugsbunny.dataScience.utils.PlotUtil;
import io.bugsbunny.endpoint.SecurityToken;
import io.bugsbunny.endpoint.SecurityTokenContainer;
import io.bugsbunny.test.components.BaseTest;
import io.quarkus.test.junit.QuarkusTest;
import org.datavec.api.records.reader.RecordReader;
import org.datavec.api.records.reader.impl.csv.CSVRecordReader;
import org.datavec.api.split.FileSplit;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.deeplearning4j.util.ModelSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import io.restassured.response.Response;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.bugsbunny.dataIngestion.util.CSVDataUtil;
import io.bugsbunny.dataScience.dl4j.AIPlatformDataSetIteratorFactory;

import org.apache.commons.io.IOUtils;

import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.evaluation.classification.Evaluation;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.learning.config.Nesterovs;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@QuarkusTest
public class AIModelTests extends BaseTest
{
    private static Logger logger = LoggerFactory.getLogger(AIModelTests.class);

    public static boolean visualize = true;

    @Inject
    private AIPlatformDataSetIteratorFactory aiPlatformDataSetIteratorFactory;

    @Test
    public void testTraining() throws Exception
    {
        /*String flightsJson = IOUtils.resourceToString("aviation/flights0.json",
                StandardCharsets.UTF_8,
                Thread.currentThread().getContextClassLoader());
        JsonArray data = JsonParser.parseString(flightsJson).getAsJsonObject().getAsJsonArray("data");

        CSVDataUtil csvDataUtil = new CSVDataUtil();
        JsonArray csvData = new JsonArray();

        Random random = new Random();
        Iterator<JsonElement> itr = data.iterator();
        while(itr.hasNext())
        {
            JsonObject flightData = itr.next().getAsJsonObject();

            JsonObject arrival = flightData.get("arrival").getAsJsonObject();

            String scheduled = arrival.get("scheduled").getAsString();
            String estimated = arrival.get("estimated").getAsString();
            String actual = null;
            if (!arrival.get("actual").isJsonNull()) {
                actual = arrival.get("actual").getAsString();
            }

            JsonObject csvRow = new JsonObject();
            csvRow.addProperty("scheduled", random.nextDouble());
            csvRow.addProperty("estimated", random.nextDouble());

            csvData.add(csvRow);
        }*/
        JsonArray trainCsvData = new JsonArray();
        Random random = new Random();
        for(int i=0; i<100; i++)
        {
            JsonObject csvRow = new JsonObject();
            csvRow.addProperty("scheduled", random.nextDouble());
            csvRow.addProperty("estimated", random.nextDouble());
            trainCsvData.add(csvRow);
        }
        CSVDataUtil csvDataUtil = new CSVDataUtil();
        JsonObject csvJson = csvDataUtil.convert(trainCsvData);
        csvJson.addProperty("format", "csv");
        //String csv = csvJson.get("data").getAsString();

        String csv = IOUtils.resourceToString("dataScience/saturn_data_train.csv",
            StandardCharsets.UTF_8,
            Thread.currentThread().getContextClassLoader());
        logger.info(csv);
        Response response = given().body(csvJson.toString()).when().post("/dataset/storeTrainingDataSet/").andReturn();
        logger.info("************************");
        logger.info(response.statusLine());
        response.body().prettyPrint();
        logger.info("************************");
        assertEquals(200, response.getStatusCode());
        JsonObject returnValue = JsonParser.parseString(response.body().asString()).getAsJsonObject();
        long trainingDataSetId = returnValue.get("dataSetId").getAsLong();
        logger.info("TrainingDataSetId: "+trainingDataSetId);


        JsonArray testCsvData = new JsonArray();
        for(int i=0; i<100; i++)
        {
            JsonObject csvRow = new JsonObject();
            csvRow.addProperty("scheduled", random.nextDouble());
            csvRow.addProperty("estimated", random.nextDouble());
            testCsvData.add(csvRow);
        }
        csvJson = csvDataUtil.convert(testCsvData);
        csvJson.addProperty("format", "csv");
        //csv = csvJson.get("data").getAsString();
        csv = IOUtils.resourceToString("dataScience/saturn_data_eval.csv",
                StandardCharsets.UTF_8,
                Thread.currentThread().getContextClassLoader());
        logger.info(csv);
        response = given().body(csvJson.toString()).when().post("/dataset/storeEvalDataSet/").andReturn();
        logger.info("************************");
        logger.info(response.statusLine());
        response.body().prettyPrint();
        logger.info("************************");
        assertEquals(200, response.getStatusCode());
        returnValue = JsonParser.parseString(response.body().asString()).getAsJsonObject();
        long evalDataSetId = returnValue.get("dataSetId").getAsLong();
        logger.info("EvalDataSetId: "+evalDataSetId);

        int seed = 123;
        double learningRate = 0.01;
        int batchSize = 50;
        int nEpochs = 30;

        int numInputs = 2;
        int numOutputs = 100;
        int numHiddenNodes = 2;

        DataSetIterator trainIter = this.aiPlatformDataSetIteratorFactory.getInstance(trainingDataSetId);
        DataSetIterator testIter = this.aiPlatformDataSetIteratorFactory.getInstance(evalDataSetId);

        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(seed)
                .weightInit(WeightInit.XAVIER)
                .updater(new Nesterovs(learningRate, 0.9))
                .list()
                .layer(new DenseLayer.Builder().nIn(numInputs).nOut(numHiddenNodes)
                        .activation(Activation.RELU)
                        .build())
                .layer(new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                        .activation(Activation.SOFTMAX)
                        .nIn(numHiddenNodes).nOut(numOutputs).build())
                .build();


        MultiLayerNetwork model = new MultiLayerNetwork(conf);
        model.init();
        model.setListeners(new ScoreIterationListener(10));  //Print score every 10 parameter updates

        model.fit(trainIter, nEpochs);

        System.out.println("Evaluate model....");
        Evaluation eval = new Evaluation(numOutputs);
        while (testIter.hasNext()) {
            DataSet t = testIter.next();
            INDArray features = t.getFeatures();
            INDArray labels = t.getLabels();

            //logger.info("Features: "+features);
            logger.info("Label: "+labels);
            logger.info("BAH:"+t.getLabelNamesList().toString());

            INDArray predicted = model.output(features, false);
            eval.eval(labels, predicted);
        }

        //An alternate way to do the above loop
        Evaluation evalResults = model.evaluate(testIter);

        //Print the evaluation statistics
        System.out.println(evalResults.stats());
        System.out.println("\n****************Example finished********************");

        //Deploy the Model
        ByteArrayOutputStream modelBytes = new ByteArrayOutputStream();
        ModelSerializer.writeModel(model, modelBytes, false);
        String modelString = Base64.getEncoder().encodeToString(modelBytes.toByteArray());
        JsonObject modelPackage = new JsonObject();
        modelPackage.addProperty("name", "name");
        modelPackage.addProperty("model", modelString);
        response = given().body(modelPackage.toString()).when().post("/aimodel/performPackaging/").andReturn();
        logger.info("************************");
        logger.info(response.statusLine());
        response.body().prettyPrint();
        logger.info("************************");
        assertEquals(200, response.getStatusCode());
    }
}
