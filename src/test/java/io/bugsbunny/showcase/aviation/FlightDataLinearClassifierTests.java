package io.bugsbunny.showcase.aviation;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.bugsbunny.dataScience.dl4j.AIPlatformDataSetIteratorFactory;
import io.bugsbunny.restClient.OAuthClient;

import io.bugsbunny.showcase.aviation.service.AviationDataIngestionService;
import io.bugsbunny.test.components.BaseTest;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import org.deeplearning4j.util.ModelSerializer;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.learning.config.Sgd;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class FlightDataLinearClassifierTests extends BaseTest
{
    private static Logger logger = LoggerFactory.getLogger(FlightDataLinearClassifierTests.class);

    public static String dataLocalPath;

    @Inject
    private AIPlatformDataSetIteratorFactory aiPlatformDataSetIteratorFactory;

    @Inject
    private AviationDataIngestionService aviationDataIngestionService;

    //@Test
    public void testClassifier() throws Exception
    {
        this.aviationDataIngestionService.startIngestion();

        Thread.sleep(20000);

        List<Long> dataSetIds = this.aviationDataIngestionService.getDataSetIds();
        int counter = 100;
        while(dataSetIds.isEmpty() && counter > 0)
        {
            dataSetIds = this.aviationDataIngestionService.getDataSetIds();
            counter--;
        }
        assertFalse(dataSetIds.isEmpty());
        logger.info(dataSetIds.get(dataSetIds.size()-1)+"");

        long trainingDataSetId = dataSetIds.get(0);
        long testDataSetId = dataSetIds.get(dataSetIds.size()-1);
        logger.info("TrainingDataSetId: "+trainingDataSetId);

        OAuthClient oAuthClient = new OAuthClient();
        String clientId = "PAlDekAoo0XWjAicU9SQDKgy7B0y2p2t";
        String clientSecret = "U2jMgxL8zJgYOMmHDYTe6-P9yO6Wq51VmixuZSRCaL-11EPE4WrQOWtGLVnQetdd";
        JsonObject securityToken = oAuthClient.getAccessToken(clientId,clientSecret);

        String token = securityToken.get("access_token").getAsString();

        //Create the Experiment
        HttpClient httpClient = HttpClient.newBuilder().build();
        String restUrl = "http://localhost:8080/dataset/readDataSet/?dataSetId="+trainingDataSetId;

        HttpRequest httpRequest = HttpRequest.newBuilder().uri(new URI(restUrl))
                .header("Bearer",token)
                .header("Principal",clientId)
                .GET()
                .build();


        HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        int status = httpResponse.statusCode();
        if(status != 200)
        {
            return;
        }

        DataSetIterator iterator = this.aiPlatformDataSetIteratorFactory.getInstance(trainingDataSetId);
        DataSet allData = iterator.next();

        allData.shuffle();
        DataSet trainingData = allData;

        //int labelIndex = 4;
        //int numClasses = 2;
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

        //Deploy the Model
        ByteArrayOutputStream modelBytes = new ByteArrayOutputStream();
        ModelSerializer.writeModel(model, modelBytes, false);
        String modelString = Base64.getEncoder().encodeToString(modelBytes.toByteArray());

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("name", UUID.randomUUID().toString());
        jsonObject.addProperty("model", modelString);
        logger.info(jsonObject.toString());

        restUrl = "http://localhost:8080/aimodel/performPackaging/";
        httpRequest = HttpRequest.newBuilder().uri(new URI(restUrl))
                .header("Bearer",token)
                .header("Principal",clientId)
                .POST(HttpRequest.BodyPublishers.ofString(jsonObject.toString()))
                .build();
        httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        JsonObject deployedModel = JsonParser.parseString(httpResponse.body()).getAsJsonObject();
        long modelId = deployedModel.get("modelId").getAsLong();
        logger.info("MODEL_ID: "+modelId);

        //Run the Model in the Cloud
        JsonObject deployResult = new JsonObject();
        deployResult.addProperty("dataSetId",testDataSetId);
        deployResult.addProperty("modelId",modelId);
        logger.info(deployResult.toString());
        restUrl = "http://localhost:8080/liveModel/evalJava/";
        httpRequest = HttpRequest.newBuilder().uri(new URI(restUrl))
                .header("Bearer",token)
                .header("Principal",clientId)
                .POST(HttpRequest.BodyPublishers.ofString(deployResult.toString()))
                .build();
        httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        logger.info(httpResponse.body());
        assertEquals(200, httpResponse.statusCode());
    }
}
