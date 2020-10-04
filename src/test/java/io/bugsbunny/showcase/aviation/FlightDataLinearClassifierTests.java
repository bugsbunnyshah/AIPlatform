package io.bugsbunny.showcase.aviation;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.bugsbunny.dataScience.dl4j.AIPlatformDataSetIteratorFactory;
import io.bugsbunny.dataScience.utils.PlotUtil;
import io.bugsbunny.restClient.OAuthClient;

import io.bugsbunny.test.components.BaseTest;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.TimeUnit;

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
import org.nd4j.linalg.learning.config.Sgd;
import org.nd4j.linalg.lossfunctions.LossFunctions;

@QuarkusTest
public class FlightDataLinearClassifierTests extends BaseTest
{
    private static Logger logger = LoggerFactory.getLogger(FlightDataLinearClassifierTests.class);

    public static String dataLocalPath;

    @Inject
    private AIPlatformDataSetIteratorFactory aiPlatformDataSetIteratorFactory;

    //@Test
    public void testClassifier() throws Exception
    {
        long dataSetId = 539862742631474360l;

        OAuthClient oAuthClient = new OAuthClient();
        String clientId = "PAlDekAoo0XWjAicU9SQDKgy7B0y2p2t";
        String clientSecret = "U2jMgxL8zJgYOMmHDYTe6-P9yO6Wq51VmixuZSRCaL-11EPE4WrQOWtGLVnQetdd";
        JsonObject securityToken = oAuthClient.getAccessToken(clientId,clientSecret);

        String token = securityToken.get("access_token").getAsString();

        //Create the Experiment
        HttpClient httpClient = HttpClient.newBuilder().build();
        String restUrl = "http://localhost:8080/dataset/readDataSet/?dataSetId="+dataSetId;

        HttpRequest.Builder httpRequestBuilder = HttpRequest.newBuilder();
        HttpRequest httpRequest = httpRequestBuilder.uri(new URI(restUrl))
                .header("Bearer",token)
                .header("Principal",clientId)
                .GET()
                .build();


        HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        String responseJson = httpResponse.body();
        int status = httpResponse.statusCode();
        if(status != 200)
        {
            return;
        }
        JsonObject dataSet = JsonParser.parseString(responseJson).getAsJsonObject();
        //logger.info(dataSet.get("data").getAsString());
        logger.info("ROWS: " +dataSet.get("rows").getAsLong()+"");
        logger.info("COLUMNS: "+dataSet.get("columns").getAsLong()+"");

        DataSetIterator iterator = this.aiPlatformDataSetIteratorFactory.getInstance(dataSetId);
        DataSet allData = iterator.next();

        allData.shuffle();
        DataSet trainingData = allData;
        DataSet testData = allData;

        int labelIndex = 4;
        int numClasses = 2;
        final int numInputs = 5;
        int outputNum = 5;
        long seed = 6;

        logger.info("Build model....");
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(seed)
                .activation(Activation.TANH)
                .weightInit(WeightInit.XAVIER)
                .updater(new Sgd(0.1))
                .l2(1e-4)
                .list()
                .layer(new DenseLayer.Builder().nIn(numInputs).nOut(outputNum)
                        .build())
                .layer(new DenseLayer.Builder().nIn(numInputs).nOut(outputNum)
                        .build())
                .layer( new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                        .activation(Activation.SOFTMAX) //Override the global TANH activation with softmax for this layer
                        .nIn(numInputs).nOut(outputNum).build())
                .build();

        //run the model
        MultiLayerNetwork model = new MultiLayerNetwork(conf);
        model.init();
        //record score once every 100 iterations
        model.setListeners(new ScoreIterationListener(100));
        for(int i=0; i<1000; i++ )
        {
            model.fit(trainingData);
        }

        //evaluate the model on the test set
        Evaluation eval = new Evaluation(numClasses);
        INDArray output = model.output(testData.getFeatures());
        eval.eval(testData.getLabels(), output);
        logger.info(eval.stats());

        this.generateSaturnVisuals(model, iterator, iterator);


        Thread.sleep(120000);
    }

    private void generateVisuals(MultiLayerNetwork model, DataSetIterator trainIter, DataSetIterator testIter) throws Exception {
        double xMin = 0;
        double xMax = 1.0;
        double yMin = -0.2;
        double yMax = 0.8;
        int nPointsPerAxis = 100;

        //Generate x,y points that span the whole range of features
        INDArray allXYPoints = PlotUtil.generatePointsOnGraph(xMin, xMax, yMin, yMax, nPointsPerAxis);
        //Get train data and plot with predictions
        PlotUtil.plotTrainingData(model, trainIter, allXYPoints, nPointsPerAxis);
        TimeUnit.SECONDS.sleep(3);
        //Get test data, run the test data through the network to generate predictions, and plot those predictions:
        PlotUtil.plotTestData(model, testIter, allXYPoints, nPointsPerAxis);
    }

    private void generateSaturnVisuals(MultiLayerNetwork model, DataSetIterator trainIter, DataSetIterator testIter) throws Exception {
        double xMin = -15;
        double xMax = 15;
        double yMin = -15;
        double yMax = 15;

        //Let's evaluate the predictions at every point in the x/y input space, and plot this in the background
        int nPointsPerAxis = 100;

        //Generate x,y points that span the whole range of features
        INDArray allXYPoints = PlotUtil.generatePointsOnGraph(xMin, xMax, yMin, yMax, nPointsPerAxis);

        logger.info("*******************");
        logger.info(allXYPoints.shapeInfoToString());

        //Get train data and plot with predictions
        PlotUtil.plotTrainingData(model, trainIter, allXYPoints, nPointsPerAxis);

        TimeUnit.SECONDS.sleep(3);
        //Get test data, run the test data through the network to generate predictions, and plot those predictions:
        PlotUtil.plotTestData(model, testIter, allXYPoints, nPointsPerAxis);
    }
}
