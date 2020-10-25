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

public class ThirdPartyModelSupport {
    private static Logger logger = LoggerFactory.getLogger(ThirdPartyModelSupport.class);

    private static String principal = "PAlDekAoo0XWjAicU9SQDKgy7B0y2p2t";
    private static String token = "";

    public static void main(String[] args) throws Exception
    {
        //Register the third party remote model
        String modelPackage = IOUtils.toString(Thread.currentThread().
                getContextClassLoader().getResourceAsStream("dataScience/aiplatform-remote-model.json"), StandardCharsets.UTF_8);

        JsonObject register = JsonParser.parseString(modelPackage).getAsJsonObject();
        JsonObject response = registerThirdPartyModel(register.toString());
        long modelId = response.get("modelId").getAsLong();
        logger.info("modelId: " + modelId);

        //Prepare the payload
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

        //Invoke the third party remote model
        JsonObject remoteModelPayload = new JsonObject();
        remoteModelPayload.addProperty("modelId", modelId);
        remoteModelPayload.add("payload", jsonObject);
        logger.info(remoteModelPayload.toString());
        JsonArray responseArray = invokeRemoteModel(remoteModelPayload.toString());
        logger.info(responseArray.toString());
    }

    private static JsonObject registerThirdPartyModel(String payload) throws Exception
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

    private static JsonArray invokeRemoteModel(String payload) throws Exception
    {
        //Create the Experiment
        HttpClient httpClient = HttpClient.newBuilder().build();
        String restUrl = "http://localhost:8080/remoteModel/mlflow/invocations/";

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
