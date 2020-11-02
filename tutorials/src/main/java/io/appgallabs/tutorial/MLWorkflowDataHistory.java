package io.appgallabs.tutorial;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
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
import java.util.Iterator;
import java.util.UUID;

public class MLWorkflowDataHistory {
    private static Logger logger = LoggerFactory.getLogger(MLWorkflowDataHistory.class);

    private static String principal = "PAlDekAoo0XWjAicU9SQDKgy7B0y2p2t";
    private static String token = "";

    public static void main(String[] args) throws Exception
    {
        int seed = 123;
        double learningRate = 0.005;

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

        JsonObject response = saveModelToCloud(jsonObject.toString());
        long modelId = response.get("modelId").getAsLong();

        //Ingest the data into the dataLake that can be used for ML components,
        //And available for building Applications
        String data = IOUtils.toString(Thread.currentThread().
                        getContextClassLoader().
                        getResourceAsStream("dataScience/saturn_data_train.csv"),
                StandardCharsets.UTF_8
                );
        JsonObject input = new JsonObject();
        input.addProperty("sourceData", data);
        input.addProperty("hasHeader", false);

        //Launch training in the Cloud
        String dataHistoryId=null;
        for(int i=0; i<3; i++) {
            JsonObject ingestion = ingestDataIntoDataLake(input.toString());
            String dataLakeId = ingestion.getAsJsonObject().get("dataLakeId").getAsString();
            logger.info("DataLakeId: "+dataLakeId);

            JsonObject trainingInput = new JsonObject();
            JsonArray trainingDataLakeIds = new JsonArray();
            trainingDataLakeIds.add(dataLakeId);
            trainingInput.addProperty("modelId", modelId);
            trainingInput.add("dataLakeIds", trainingDataLakeIds);
            response = trainModelInCloud(trainingInput.toString());
            logger.info(trainingInput.toString());
            dataHistoryId = response.get("dataHistoryId").getAsString();
            logger.info("DataHistoryId: " + dataHistoryId);
        }

        //Get the data
        JsonArray history = getDataHistory(dataHistoryId);
        Iterator<JsonElement> itr = history.iterator();
        while(itr.hasNext())
        {
            JsonObject object = itr.next().getAsJsonObject();
            JsonArray dataLakeIds = object.getAsJsonArray("dataLakeIds");
            if(dataLakeIds == null)
            {
                continue;
            }
            Iterator<JsonElement> cour = dataLakeIds.iterator();
            while(cour.hasNext())
            {
                long oid = cour.next().getAsLong();
                JsonObject content = readDataLakeObject(oid);
                logger.info(content.toString());
            }
        }
    }

    private static JsonArray getDataHistory(String oid) throws Exception
    {
        //Create the Experiment
        HttpClient httpClient = HttpClient.newBuilder().build();
        String restUrl = "http://localhost:8080/replay/chain/?oid="+oid;

        HttpRequest.Builder httpRequestBuilder = HttpRequest.newBuilder();
        HttpRequest httpRequest = httpRequestBuilder.uri(new URI(restUrl))
                .header("Content-Type", "application/json")
                .header("Principal", principal)
                .header("Bearer","")
                .GET()
                .build();

        HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        String responseJson = httpResponse.body();
        logger.info(httpResponse.statusCode()+"");

        return JsonParser.parseString(responseJson).getAsJsonArray();
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

    private static JsonObject ingestDataIntoDataLake(String payload) throws Exception
    {
        //Create the Experiment
        HttpClient httpClient = HttpClient.newBuilder().build();
        String restUrl = "http://localhost:8080/dataMapper/mapCsv/";

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
        String restUrl = "http://localhost:8080/trainModel/trainJavaFromDataLake/";

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

    private static JsonObject readDataLakeObject(long dataLakeId) throws Exception
    {
        //Create the Experiment
        HttpClient httpClient = HttpClient.newBuilder().build();
        String restUrl = "http://localhost:8080/dataMapper/readDataLakeObject/?dataLakeId="+dataLakeId;

        HttpRequest.Builder httpRequestBuilder = HttpRequest.newBuilder();
        HttpRequest httpRequest = httpRequestBuilder.uri(new URI(restUrl))
                .header("Content-Type", "application/json")
                .header("Principal", principal)
                .header("Bearer","")
                .GET()
                .build();

        HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        String responseJson = httpResponse.body();
        logger.info(httpResponse.statusCode()+"");

        return JsonParser.parseString(responseJson).getAsJsonObject();
    }
}
