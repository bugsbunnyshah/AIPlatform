package io.appgallabs.tutorial;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.commons.io.IOUtils;

import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.learning.config.Nesterovs;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

public class MLWorkflowRetraining {
    private static Logger logger = LoggerFactory.getLogger(MLWorkflowRetraining.class);

    private static String principal = "PAlDekAoo0XWjAicU9SQDKgy7B0y2p2t";
    private static String token = "";

    public static void main(String[] args) throws Exception
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
                .layer(new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                        .activation(Activation.SOFTMAX)
                        .nIn(numHiddenNodes).nOut(numOutputs).build())
                .build();


        MultiLayerNetwork model = new MultiLayerNetwork(conf);
        model.init();
        model.setListeners(new ScoreIterationListener(10));

        //Deploy the Model, for training in the Cloud
        ByteArrayOutputStream modelBytes = new ByteArrayOutputStream();
        ModelSerializer.writeModel(model, modelBytes, false);
        String modelString = Base64.getEncoder().encodeToString(modelBytes.toByteArray());

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("name", UUID.randomUUID().toString());
        jsonObject.addProperty("model", modelString);
        logger.info(jsonObject.toString());

        JsonObject response = saveModelToCloud(jsonObject.toString());
        logger.info(response.toString());
        long modelId = response.get("modelId").getAsLong();

        //Ingest the data that will train the model
        String data = IOUtils.toString(Thread.currentThread().
                        getContextClassLoader().
                        getResourceAsStream("dataScience/saturn_data_train.csv"),
                StandardCharsets.UTF_8
                );
        JsonObject input = new JsonObject();
        input.addProperty("modelId", modelId);
        input.addProperty("format", "csv");
        input.addProperty("data", data);
        logger.info(input.toString());
        response = saveTrainingDataSet(input.toString());
        logger.info(response.toString());

        //Launch training in the Cloud
        JsonObject trainingInput = new JsonObject();
        JsonArray trainingDataSetIds = new JsonArray();
        trainingDataSetIds.add(response.get("dataSetId").getAsString());
        trainingInput.addProperty("modelId", modelId);
        trainingInput.add("dataSetIds", trainingDataSetIds);
        logger.info(trainingInput.toString());
        response = trainModelInCloud(trainingInput.toString());
        logger.info(response.toString());

        //Deploy the trained model as a live model
        JsonObject deploy = new JsonObject();
        deploy.addProperty("modelId", modelId);
        logger.info("DEPLOY_REQUEST: "+deploy.toString());
        response = deployLiveModelInCloud(deploy.toString());
        logger.info("DEPLOY_RESPONSE: "+response.toString());

        //Ingest the data that will call the live model
        data = IOUtils.toString(Thread.currentThread().
                        getContextClassLoader().
                        getResourceAsStream("dataScience/saturn_data_eval.csv"),
                StandardCharsets.UTF_8
        );
        input = new JsonObject();
        input.addProperty("modelId", modelId);
        input.addProperty("format", "csv");
        input.addProperty("data", data);
        logger.info(input.toString());
        response = saveLiveDataSet(input.toString());
        logger.info(response.toString());

        //Evaluate the Live Model in the Cloud
        JsonObject liveInput = new JsonObject();
        JsonArray liveDataSetIds = new JsonArray();
        liveDataSetIds.add(response.get("dataSetId").getAsString());
        liveInput.addProperty("modelId", modelId);
        liveInput.add("dataSetIds", liveDataSetIds);
        logger.info(liveInput.toString());
        response = evaluateLiveModelInCloud(liveInput.toString());
        logger.info(response.toString());

        //Bring down a Live Model for Training
        JsonObject undeploy = new JsonObject();
        undeploy.addProperty("modelId", modelId);
        logger.info(liveInput.toString());
        JsonArray undeployResponse = undeployLiveModel(undeploy.toString());
        logger.info(undeployResponse.toString());

        //Train the Newly Undeployed Model
        logger.info(trainingInput.toString());
        response = trainModelInCloud(trainingInput.toString());
        logger.info(response.toString());

        //Deploy the newly Trained Model
        response = deployLiveModelInCloud(deploy.toString());
        logger.info("DEPLOY_RESPONSE: "+response.toString());

        //Verify we are back online
        JsonObject newLiveInput = new JsonObject();
        newLiveInput.addProperty("modelId", modelId);
        newLiveInput.add("dataSetIds", undeployResponse);
        logger.info(newLiveInput.toString());
        response = evaluateLiveModelInCloud(newLiveInput.toString());
        logger.info(response.toString());
    }

    private static JsonObject saveModelToCloud(String payload) throws Exception
    {
        //Create the Experiment
        HttpClient httpClient = HttpClient.newBuilder().build();
        String restUrl = "http://localhost:8080/aimodel/performPackaging/";

        HttpRequest.Builder httpRequestBuilder = HttpRequest.newBuilder();
        HttpRequest httpRequest = httpRequestBuilder.uri(new URI(restUrl))
                .header("Content-Type", "application/json")
                .header("Principal", principal)
                .header("Bearer","")
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();

        HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        String responseJson = httpResponse.body();

        return JsonParser.parseString(responseJson).getAsJsonObject();
    }

    private static JsonObject saveTrainingDataSet(String payload) throws Exception
    {
        //Create the Experiment
        HttpClient httpClient = HttpClient.newBuilder().build();
        String restUrl = "http://localhost:8080/dataset/storeTrainingDataSet/";

        HttpRequest.Builder httpRequestBuilder = HttpRequest.newBuilder();
        HttpRequest httpRequest = httpRequestBuilder.uri(new URI(restUrl))
                .header("Content-Type", "application/json")
                .header("Principal", principal)
                .header("Bearer","")
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();

        HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        String responseJson = httpResponse.body();

        return JsonParser.parseString(responseJson).getAsJsonObject();
    }

    private static JsonObject saveLiveDataSet(String payload) throws Exception
    {
        //Create the Experiment
        HttpClient httpClient = HttpClient.newBuilder().build();
        String restUrl = "http://localhost:8080/dataset/storeEvalDataSet/";

        HttpRequest.Builder httpRequestBuilder = HttpRequest.newBuilder();
        HttpRequest httpRequest = httpRequestBuilder.uri(new URI(restUrl))
                .header("Content-Type", "application/json")
                .header("Principal", principal)
                .header("Bearer","")
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();

        HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        String responseJson = httpResponse.body();

        return JsonParser.parseString(responseJson).getAsJsonObject();
    }

    private static JsonObject trainModelInCloud(String payload) throws Exception
    {
        //Create the Experiment
        HttpClient httpClient = HttpClient.newBuilder().build();
        String restUrl = "http://localhost:8080/trainModel/trainJava/";

        HttpRequest.Builder httpRequestBuilder = HttpRequest.newBuilder();
        HttpRequest httpRequest = httpRequestBuilder.uri(new URI(restUrl))
                .header("Content-Type", "application/json")
                .header("Principal", principal)
                .header("Bearer","")
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();

        HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        String responseJson = httpResponse.body();

        return JsonParser.parseString(responseJson).getAsJsonObject();
    }

    private static JsonObject deployLiveModelInCloud(String payload) throws Exception
    {
        //Create the Experiment
        HttpClient httpClient = HttpClient.newBuilder().build();
        String restUrl = "http://localhost:8080/liveModel/deployJavaModel/";

        HttpRequest.Builder httpRequestBuilder = HttpRequest.newBuilder();
        HttpRequest httpRequest = httpRequestBuilder.uri(new URI(restUrl))
                .header("Content-Type", "application/json")
                .header("Principal", principal)
                .header("Bearer","")
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();

        HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        String responseJson = httpResponse.body();

        return JsonParser.parseString(responseJson).getAsJsonObject();
    }

    private static JsonObject evaluateLiveModelInCloud(String payload) throws Exception
    {
        //Create the Experiment
        HttpClient httpClient = HttpClient.newBuilder().build();
        String restUrl = "http://localhost:8080/liveModel/evalJava/";

        HttpRequest.Builder httpRequestBuilder = HttpRequest.newBuilder();
        HttpRequest httpRequest = httpRequestBuilder.uri(new URI(restUrl))
                .header("Content-Type", "application/json")
                .header("Principal", principal)
                .header("Bearer","")
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();

        HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        String responseJson = httpResponse.body();

        return JsonParser.parseString(responseJson).getAsJsonObject();
    }

    private static JsonArray undeployLiveModel(String payload) throws Exception
    {
        //Create the Experiment
        HttpClient httpClient = HttpClient.newBuilder().build();
        String restUrl = "http://localhost:8080/liveModel/retrain/";

        HttpRequest.Builder httpRequestBuilder = HttpRequest.newBuilder();
        HttpRequest httpRequest = httpRequestBuilder.uri(new URI(restUrl))
                .header("Content-Type", "application/json")
                .header("Principal", principal)
                .header("Bearer","")
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();

        HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        String responseJson = httpResponse.body();

        return JsonParser.parseString(responseJson).getAsJsonArray();
    }
}
