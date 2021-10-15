package io.bugsbunny.dataScience.endpoint;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.bugsbunny.dataScience.model.AllModelTests;
import io.bugsbunny.dataScience.model.Artifact;
import io.bugsbunny.dataScience.model.Project;
import io.bugsbunny.dataScience.model.Scientist;
import io.bugsbunny.dataScience.service.ProjectService;
import io.bugsbunny.infrastructure.MongoDBJsonStore;
import io.bugsbunny.preprocess.SecurityTokenContainer;
import io.bugsbunny.test.components.BaseTest;
import io.bugsbunny.util.JsonUtil;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import org.apache.commons.io.IOUtils;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.deeplearning4j.util.ModelSerializer;
import org.junit.jupiter.api.Test;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.learning.config.Nesterovs;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class TrainModelTests extends BaseTest {
    private static Logger logger = LoggerFactory.getLogger(TrainModelTests.class);

    @Inject
    private ProjectService projectService;

    @Inject
    private MongoDBJsonStore mongoDBJsonStore;

    @Inject
    private SecurityTokenContainer securityTokenContainer;

    @Test
    public void trainModelFromDataLake() throws Exception{
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
        MultiLayerNetwork network = new MultiLayerNetwork(conf);
        network.init();
        network.setListeners(new ScoreIterationListener(10));

        ByteArrayOutputStream modelBytes = new ByteArrayOutputStream();
        ModelSerializer.writeModel(network, modelBytes, false);
        String modelString = Base64.getEncoder().encodeToString(modelBytes.toByteArray());

        //Deploy the Artifact with the Model
        Artifact artifact = AllModelTests.mockArtifact();
        JsonElement labels = artifact.toJson().get("labels");
        JsonElement features = artifact.toJson().get("features");
        JsonElement parameters = artifact.toJson().get("parameters");

        JsonObject input = new JsonObject();
        input.addProperty("name","model");
        input.addProperty("model",modelString);
        input.add("labels",labels);
        input.add("features",features);
        input.add("parameters",parameters);

        Scientist scientist = AllModelTests.mockScientist();
        Project project = this.projectService.createArtifactForTraining(scientist.getEmail(),input);

        String data = IOUtils.resourceToString("dataScience/saturn_data_train.csv", StandardCharsets.UTF_8,
                Thread.currentThread().getContextClassLoader());

        JsonArray dataLakeIds = new JsonArray();

        for(int i=0; i< 3; i++) {
            String dataLakeId = UUID.randomUUID().toString();
            String chainId = "/" + this.securityTokenContainer.getTenant().getPrincipal() + "/" + dataLakeId;
            JsonObject dataJson = new JsonObject();
            dataJson.addProperty("braineous_datalakeid", dataLakeId);
            dataJson.addProperty("tenant", this.securityTokenContainer.getTenant().getPrincipal());
            dataJson.addProperty("data", data);
            dataJson.addProperty("chainId", chainId);
            logger.info("************PERSISTING-" + dataLakeId + "******************");
            logger.info(dataJson.toString());
            logger.info("****************************************");
            this.mongoDBJsonStore.storeIngestion(this.securityTokenContainer.getTenant(), dataJson);
            dataLakeIds.add(dataLakeId);
        }

        String url = "/trainModel/trainModelFromDataLake/";
        JsonObject json = new JsonObject();
        json.addProperty("projectId",project.getProjectId());
        json.addProperty("artifactId",project.getArtifacts().get(0).getArtifactId());
        json.add("dataLakeIds",dataLakeIds);
        Response response = given().body(json.toString()).
                post(url).andReturn();
        response.getBody().prettyPrint();
        assertEquals(200, response.getStatusCode());
        JsonObject responseJson = JsonParser.parseString(response.getBody().asString()).getAsJsonObject();
        JsonObject confusion = responseJson.get("result").getAsJsonObject().get("confusion").getAsJsonObject();
        JsonUtil.print(confusion);
        assertNotNull(confusion);
    }

    @Test
    public void trainModelFromDataLakeExceptions() throws Exception{
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
        MultiLayerNetwork network = new MultiLayerNetwork(conf);
        network.init();
        network.setListeners(new ScoreIterationListener(10));

        ByteArrayOutputStream modelBytes = new ByteArrayOutputStream();
        ModelSerializer.writeModel(network, modelBytes, false);
        String modelString = Base64.getEncoder().encodeToString(modelBytes.toByteArray());

        Artifact artifact = AllModelTests.mockArtifact();
        JsonElement labels = artifact.toJson().get("labels");
        JsonElement features = artifact.toJson().get("features");
        JsonElement parameters = artifact.toJson().get("parameters");

        JsonObject input = new JsonObject();
        input.addProperty("name","model");
        input.addProperty("model",modelString);
        input.add("labels",labels);
        input.add("features",features);
        input.add("parameters",parameters);

        Scientist scientist = AllModelTests.mockScientist();
        Project project = this.projectService.createArtifactForTraining(scientist.getEmail(),input);

        //Validation Errors
        String url = "/trainModel/trainModelFromDataLake/";
        JsonObject json = new JsonObject();
        Response response = given().body(json.toString()).
                post(url).andReturn();
        response.getBody().prettyPrint();
        assertEquals(403, response.getStatusCode());
        JsonObject responseJson = JsonParser.parseString(response.getBody().asString()).getAsJsonObject();
        assertEquals("project_id_missing",responseJson.get("project_id_missing").getAsString());
        assertEquals("artifact_id_missing",responseJson.get("artifact_id_missing").getAsString());
        assertEquals("data_missing",responseJson.get("data_missing").getAsString());

        //Artifact_Not_Found
        url = "/trainModel/trainModelFromDataLake/";
        json = new JsonObject();
        json.addProperty("projectId","blah");
        json.addProperty("artifactId",project.getArtifacts().get(0).getArtifactId());
        json.add("dataLakeIds",new JsonArray());
        response = given().body(json.toString()).
                post(url).andReturn();
        response.getBody().prettyPrint();
        assertEquals(404, response.getStatusCode());
        responseJson = JsonParser.parseString(response.getBody().asString()).getAsJsonObject();
        assertEquals("ARTIFACT_NOT_FOUND",responseJson.get("message").getAsString());

        //Artifact_Not_Found
        url = "/trainModel/trainModelFromDataLake/";
        json = new JsonObject();
        json.addProperty("projectId",project.getProjectId());
        json.addProperty("artifactId","blah");
        json.add("dataLakeIds",new JsonArray());
        response = given().body(json.toString()).
                post(url).andReturn();
        response.getBody().prettyPrint();
        assertEquals(404, response.getStatusCode());
        responseJson = JsonParser.parseString(response.getBody().asString()).getAsJsonObject();
        assertEquals("ARTIFACT_NOT_FOUND",responseJson.get("message").getAsString());

        //Data_Not_Found
        url = "/trainModel/trainModelFromDataLake/";
        json = new JsonObject();
        json.addProperty("projectId",project.getProjectId());
        json.addProperty("artifactId",project.getArtifacts().get(0).getArtifactId());
        json.add("dataLakeIds",new JsonArray());
        response = given().body(json.toString()).
                post(url).andReturn();
        response.getBody().prettyPrint();
        assertEquals(404, response.getStatusCode());
        responseJson = JsonParser.parseString(response.getBody().asString()).getAsJsonObject();
        assertEquals("DATA_NOT_FOUND",responseJson.get("message").getAsString());
    }

    @Test
    public void trainModelFromDataSet() throws Exception{
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
        MultiLayerNetwork network = new MultiLayerNetwork(conf);
        network.init();
        network.setListeners(new ScoreIterationListener(10));

        ByteArrayOutputStream modelBytes = new ByteArrayOutputStream();
        ModelSerializer.writeModel(network, modelBytes, false);
        String modelString = Base64.getEncoder().encodeToString(modelBytes.toByteArray());

        //Deploy the Artifact with the Model
        Artifact artifact = AllModelTests.mockArtifact();
        JsonElement labels = artifact.toJson().get("labels");
        JsonElement features = artifact.toJson().get("features");
        JsonElement parameters = artifact.toJson().get("parameters");

        JsonObject input = new JsonObject();
        input.addProperty("name","model");
        input.addProperty("model",modelString);
        input.add("labels",labels);
        input.add("features",features);
        input.add("parameters",parameters);

        Scientist scientist = AllModelTests.mockScientist();
        Project project = this.projectService.createArtifactForTraining(scientist.getEmail(),input);


        String data = IOUtils.resourceToString("dataScience/saturn_data_train.csv", StandardCharsets.UTF_8,
                Thread.currentThread().getContextClassLoader());

        JsonArray dataSetIds = new JsonArray();
        for(int i=0; i< 3; i++) {
            input = new JsonObject();
            input.addProperty("format", "csv");
            input.addProperty("data", data);
            Response response = given().body(input.toString()).when().post("/dataset/storeTrainingDataSet/").andReturn();
            JsonObject returnValue = JsonParser.parseString(response.body().asString()).getAsJsonObject();
            String dataSetId = returnValue.get("dataSetId").getAsString();
            dataSetIds.add(dataSetId);
        }

        String url = "/trainModel/trainModelFromDataSet/";
        JsonObject json = new JsonObject();
        json.addProperty("projectId",project.getProjectId());
        json.addProperty("artifactId",project.getArtifacts().get(0).getArtifactId());
        json.add("dataSetIds",dataSetIds);
        Response response = given().body(json.toString()).
                post(url).andReturn();
        response.getBody().prettyPrint();
        assertEquals(200, response.getStatusCode());
        JsonObject responseJson = JsonParser.parseString(response.getBody().asString()).getAsJsonObject();
        JsonObject confusion = responseJson.get("result").getAsJsonObject().get("confusion").getAsJsonObject();
        JsonUtil.print(confusion);
        assertNotNull(confusion);
    }

    @Test
    public void trainModelFromDataSetExceptions() throws Exception{
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
        MultiLayerNetwork network = new MultiLayerNetwork(conf);
        network.init();
        network.setListeners(new ScoreIterationListener(10));

        ByteArrayOutputStream modelBytes = new ByteArrayOutputStream();
        ModelSerializer.writeModel(network, modelBytes, false);
        String modelString = Base64.getEncoder().encodeToString(modelBytes.toByteArray());

        Artifact artifact = AllModelTests.mockArtifact();
        JsonElement labels = artifact.toJson().get("labels");
        JsonElement features = artifact.toJson().get("features");
        JsonElement parameters = artifact.toJson().get("parameters");

        JsonObject input = new JsonObject();
        input.addProperty("name","model");
        input.addProperty("model",modelString);
        input.add("labels",labels);
        input.add("features",features);
        input.add("parameters",parameters);

        Scientist scientist = AllModelTests.mockScientist();
        Project project = this.projectService.createArtifactForTraining(scientist.getEmail(),input);

        //Validation Errors
        String url = "/trainModel/trainModelFromDataSet/";
        JsonObject json = new JsonObject();
        Response response = given().body(json.toString()).
                post(url).andReturn();
        response.getBody().prettyPrint();
        assertEquals(403, response.getStatusCode());
        JsonObject responseJson = JsonParser.parseString(response.getBody().asString()).getAsJsonObject();
        assertEquals("project_id_missing",responseJson.get("project_id_missing").getAsString());
        assertEquals("artifact_id_missing",responseJson.get("artifact_id_missing").getAsString());
        assertEquals("data_missing",responseJson.get("data_missing").getAsString());

        //Artifact_Not_Found
        url = "/trainModel/trainModelFromDataSet/";
        json = new JsonObject();
        json.addProperty("projectId","blah");
        json.addProperty("artifactId",project.getArtifacts().get(0).getArtifactId());
        json.add("dataSetIds",new JsonArray());
        response = given().body(json.toString()).
                post(url).andReturn();
        response.getBody().prettyPrint();
        assertEquals(404, response.getStatusCode());
        responseJson = JsonParser.parseString(response.getBody().asString()).getAsJsonObject();
        assertEquals("ARTIFACT_NOT_FOUND",responseJson.get("message").getAsString());

        //Artifact_Not_Found
        url = "/trainModel/trainModelFromDataSet/";
        json = new JsonObject();
        json.addProperty("projectId",project.getProjectId());
        json.addProperty("artifactId","blah");
        json.add("dataSetIds",new JsonArray());
        response = given().body(json.toString()).
                post(url).andReturn();
        response.getBody().prettyPrint();
        assertEquals(404, response.getStatusCode());
        responseJson = JsonParser.parseString(response.getBody().asString()).getAsJsonObject();
        assertEquals("ARTIFACT_NOT_FOUND",responseJson.get("message").getAsString());

        //Data_Not_Found
        url = "/trainModel/trainModelFromDataSet/";
        json = new JsonObject();
        json.addProperty("projectId",project.getProjectId());
        json.addProperty("artifactId",project.getArtifacts().get(0).getArtifactId());
        json.add("dataSetIds",new JsonArray());
        response = given().body(json.toString()).
                post(url).andReturn();
        response.getBody().prettyPrint();
        assertEquals(404, response.getStatusCode());
        responseJson = JsonParser.parseString(response.getBody().asString()).getAsJsonObject();
        assertEquals("DATA_NOT_FOUND",responseJson.get("message").getAsString());
    }
}
