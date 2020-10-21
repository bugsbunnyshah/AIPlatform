package io.appgallabs.showcase.aviation;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.appgallabs.showcase.aviation.service.AviationDataIngestionService;
import io.bugsbunny.dataScience.dl4j.AIPlatformDataSetIteratorFactory;
import io.bugsbunny.dataScience.dl4j.AIPlatformDataSetLoader;
import io.bugsbunny.endpoint.SecurityToken;
import io.bugsbunny.endpoint.SecurityTokenContainer;
import io.bugsbunny.persistence.MongoDBJsonStore;
import io.bugsbunny.restClient.OAuthClient;

import org.deeplearning4j.util.ModelSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
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

import io.appgallabs.showcase.aviation.utils.*;

public class FlightDataClassifier
{
    private static Logger logger = LoggerFactory.getLogger(FlightDataClassifier.class);

    public static String dataLocalPath;

    private AIPlatformDataSetIteratorFactory aiPlatformDataSetIteratorFactory;
    private SecurityTokenContainer securityTokenContainer;
    private AIPlatformDataSetLoader aiPlatformDataSetLoader;
    private MongoDBJsonStore mongoDBJsonStore;

    public void testClassifier() throws Exception
    {
        OAuthClient oAuthClient = new OAuthClient();
        String clientId = "PAlDekAoo0XWjAicU9SQDKgy7B0y2p2t";
        String clientSecret = "U2jMgxL8zJgYOMmHDYTe6-P9yO6Wq51VmixuZSRCaL-11EPE4WrQOWtGLVnQetdd";
        JsonObject securityToken = oAuthClient.getAccessToken(clientId,clientSecret);
        String token = securityToken.get("access_token").getAsString();

        JsonObject securityTokenJson = new JsonObject();
        securityTokenJson.addProperty("access_token", securityToken.get("access_token").getAsString());
        securityTokenJson.addProperty("principal", clientId);

        this.securityTokenContainer = new SecurityTokenContainer();
        this.securityTokenContainer.setSecurityToken(SecurityToken.fromJson(securityTokenJson.toString()));
        this.mongoDBJsonStore = new MongoDBJsonStore();
        this.mongoDBJsonStore.setSecurityTokenContainer(this.securityTokenContainer);
        this.aiPlatformDataSetLoader = new AIPlatformDataSetLoader();
        this.aiPlatformDataSetLoader.setMongoDBJsonStore(this.mongoDBJsonStore);
        this.aiPlatformDataSetLoader.setSecurityTokenContainer(this.securityTokenContainer);
        this.aiPlatformDataSetIteratorFactory = new AIPlatformDataSetIteratorFactory();
        this.aiPlatformDataSetIteratorFactory.setSecurityTokenContainer(this.securityTokenContainer);
        this.aiPlatformDataSetIteratorFactory.setAiPlatformDataSetLoader(this.aiPlatformDataSetLoader);

        //Create the Experiment
        long dataSetId = 539862742631474360l;
        HttpClient httpClient = HttpClient.newBuilder().build();
        String restUrl = "http://localhost:8080/dataset/readDataSet/?dataSetId="+dataSetId;

        HttpRequest.Builder httpRequestBuilder = HttpRequest.newBuilder();
        HttpRequest httpRequest = httpRequestBuilder.uri(new URI(restUrl))
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

        DataSetIterator iterator = this.aiPlatformDataSetIteratorFactory.getInstance(new long[]{dataSetId});
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

        //Deploy the Model
        ByteArrayOutputStream modelBytes = new ByteArrayOutputStream();
        ModelSerializer.writeModel(model, modelBytes, false);
        String modelString = Base64.getEncoder().encodeToString(modelBytes.toByteArray());

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("name", UUID.randomUUID().toString());
        jsonObject.addProperty("model", modelString);
        logger.info(jsonObject.toString());

        httpClient = HttpClient.newBuilder().build();
        restUrl = "http://localhost:8080/aimodel/performPackaging/";

        httpRequestBuilder = HttpRequest.newBuilder();
        httpRequest = httpRequestBuilder.uri(new URI(restUrl))
                .header("Bearer",token)
                .header("Principal",clientId)
                .POST(HttpRequest.BodyPublishers.ofString(jsonObject.toString()))
                .build();


        httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        status = httpResponse.statusCode();
        if(status != 200)
        {
            return;
        }
        JsonObject deployedModel = JsonParser.parseString(httpResponse.body()).getAsJsonObject();
        long modelId = deployedModel.get("modelId").getAsLong();
        logger.info("MODEL_ID: "+modelId);

        //Eval
        long testDataSetId = dataSetId;
        JsonObject evalRequest = new JsonObject();
        evalRequest.addProperty("modelId", modelId);
        evalRequest.addProperty("dataSetId", dataSetId);
        restUrl = "http://localhost:8080/liveModel/evalJava/";

        httpRequestBuilder = HttpRequest.newBuilder();
        httpRequest = httpRequestBuilder.uri(new URI(restUrl))
                .header("Bearer",token)
                .header("Principal",clientId)
                .POST(HttpRequest.BodyPublishers.ofString(evalRequest.toString()))
                .build();


        httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        logger.info(httpResponse.body().toString());
    }

    public void executeAviationDataScienceWorkflow() throws Exception
    {
        AviationDataIngestionService aviationDataIngestionService = new AviationDataIngestionService();
        aviationDataIngestionService.startIngestion();

        Thread.sleep(20000);

        List<Long> dataSetIds = aviationDataIngestionService.getDataSetIds();
        int counter = 100;
        while(dataSetIds.isEmpty() && counter > 0)
        {
            dataSetIds = aviationDataIngestionService.getDataSetIds();
            counter--;
        }
        logger.info(dataSetIds.get(dataSetIds.size()-1)+"");

        long trainingDataSetId = dataSetIds.get(0);
        long testDataSetId = dataSetIds.get(dataSetIds.size()-1);
        logger.info("TrainingDataSetId: "+trainingDataSetId);

        SecurityTokenContainer securityTokenContainer = aviationDataIngestionService.getSecurityTokenContainer();
        String token = securityTokenContainer.getSecurityToken().getToken();
        String clientId = aviationDataIngestionService.getAppConfig().get("client_id").getAsString();
        HttpClient httpClient = HttpClient.newBuilder().build();
        String restUrl = "http://localhost:8080/dataset/readDataSet/?dataSetId="+trainingDataSetId;

        HttpRequest.Builder httpRequestBuilder = HttpRequest.newBuilder();
        HttpRequest httpRequest = httpRequestBuilder.uri(new URI(restUrl))
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
        logger.info(httpResponse.body());

        //Setup the DataSetIterator to train the model locally
        MongoDBJsonStore mongoDBJsonStore = new MongoDBJsonStore();
        mongoDBJsonStore.setSecurityTokenContainer(securityTokenContainer);
        AIPlatformDataSetLoader aiPlatformDataSetLoader = new AIPlatformDataSetLoader();
        aiPlatformDataSetLoader.setMongoDBJsonStore(mongoDBJsonStore);
        aiPlatformDataSetLoader.setSecurityTokenContainer(securityTokenContainer);
        AIPlatformDataSetIteratorFactory aiPlatformDataSetIteratorFactory = new AIPlatformDataSetIteratorFactory();
        aiPlatformDataSetIteratorFactory.setSecurityTokenContainer(securityTokenContainer);
        aiPlatformDataSetIteratorFactory.setAiPlatformDataSetLoader(aiPlatformDataSetLoader);

        DataSetIterator iterator = aiPlatformDataSetIteratorFactory.getInstance(new long[]{trainingDataSetId});
        DataSet allData = iterator.next();
        allData.shuffle();
        DataSet trainingData = allData;

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

        restUrl = "http://localhost:8080/aimodel/performPackaging/";
        httpRequest = httpRequestBuilder.uri(new URI(restUrl))
                .header("Bearer",token)
                .header("Principal",clientId)
                .POST(HttpRequest.BodyPublishers.ofString(jsonObject.toString()))
                .build();
        httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        logger.info(httpResponse.body());
        JsonObject deployedModel = JsonParser.parseString(httpResponse.body()).getAsJsonObject();
        long modelId = deployedModel.get("modelId").getAsLong();


        logger.info("MODEL_ID: "+modelId);
        logger.info("TestDataSetId: "+testDataSetId);
        deployedModel.addProperty("dataSetId", testDataSetId);
        logger.info(deployedModel.toString());
        restUrl = "http://localhost:8080/liveModel/evalJava/";
        httpRequest = httpRequestBuilder.uri(new URI(restUrl))
                .header("Bearer",token)
                .header("Principal",clientId)
                .POST(HttpRequest.BodyPublishers.ofString(deployedModel.toString()))
                .build();
        httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        logger.info(httpResponse.body());
    }

    public static void main(String[] args) throws Exception
    {
        FlightDataClassifier flightDataClassifier = new FlightDataClassifier();
        flightDataClassifier.executeAviationDataScienceWorkflow();
    }
}
