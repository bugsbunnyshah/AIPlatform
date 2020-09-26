package io.bugsbunny.showcase.aviation;

import io.bugsbunny.dataScience.utils.DownloaderUtility;
import io.bugsbunny.endpoint.SecurityToken;
import io.bugsbunny.endpoint.SecurityTokenContainer;
import io.quarkus.test.junit.QuarkusTest;
import org.datavec.api.records.reader.RecordReader;
import org.datavec.api.records.reader.impl.csv.CSVRecordReader;
import org.datavec.api.split.FileSplit;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
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
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Random;

@QuarkusTest
public class AIModelTests
{
    private static Logger logger = LoggerFactory.getLogger(AIModelTests.class);

    @Inject
    private AIPlatformDataSetIteratorFactory aiPlatformDataSetIteratorFactory;

    @Inject
    private SecurityTokenContainer securityTokenContainer;
    @BeforeEach
    public void setUp() throws Exception
    {
        String securityTokenJson = IOUtils.toString(Thread.currentThread().getContextClassLoader().
                        getResourceAsStream("oauthAgent/token.json"),
                StandardCharsets.UTF_8);
        SecurityToken securityToken = SecurityToken.fromJson(securityTokenJson);
        this.securityTokenContainer.getTokenContainer().set(securityToken);
    }

    @Test
    public void testTraining() throws Exception
    {
        String flightsJson = IOUtils.resourceToString("aviation/flights0.json",
                StandardCharsets.UTF_8,
                Thread.currentThread().getContextClassLoader());
        JsonArray data = JsonParser.parseString(flightsJson).getAsJsonObject().getAsJsonArray("data");

        CSVDataUtil csvDataUtil = new CSVDataUtil();
        JsonArray csvData = new JsonArray();

        Random random = new Random();
        double val = random.nextDouble();
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
            csvRow.addProperty("tag", 1);
            csvRow.addProperty("scheduled", val);
            csvRow.addProperty("estimated", val);
            //csvRow.addProperty("actual", actual);

            csvData.add(csvRow);
        }
        String csv = csvDataUtil.convert(csvData);
        //logger.info(csv);

        JsonObject input = new JsonObject();
        input.addProperty("format", "csv");
        input.addProperty("data", csv);

        Response response = given().body(input.toString()).when().post("/dataset/storeTrainingDataSet/").andReturn();
        logger.info("************************");
        logger.info(response.statusLine());
        response.body().prettyPrint();
        logger.info("************************");
        assertEquals(200, response.getStatusCode());
        JsonObject returnValue = JsonParser.parseString(response.body().asString()).getAsJsonObject();
        long dataSetId = returnValue.get("dataSetId").getAsLong();
        logger.info(""+dataSetId);

        int seed = 123;
        double learningRate = 0.01;
        int batchSize = 50;
        int nEpochs = 30;

        int numInputs = 3;
        int numOutputs = 100;
        int numHiddenNodes = 3;

        DataSetIterator trainIter = this.aiPlatformDataSetIteratorFactory.getInstance(dataSetId);
        DataSetIterator testIter = this.aiPlatformDataSetIteratorFactory.getInstance(dataSetId);

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
    }
}
