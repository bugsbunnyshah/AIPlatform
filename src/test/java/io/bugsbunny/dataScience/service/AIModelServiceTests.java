package io.bugsbunny.dataScience.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.bugsbunny.dataScience.model.*;
import io.bugsbunny.infrastructure.MongoDBJsonStore;
import io.bugsbunny.preprocess.SecurityTokenContainer;
import io.bugsbunny.test.components.BaseTest;
import io.bugsbunny.util.JsonUtil;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import org.apache.commons.io.IOUtils;
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
import org.deeplearning4j.util.ModelSerializer;
import org.junit.jupiter.api.Test;
import org.nd4j.evaluation.classification.Evaluation;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.learning.config.Nesterovs;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
public class AIModelServiceTests extends BaseTest {
    private static Logger logger = LoggerFactory.getLogger(AIModelServiceTests.class);

    @Inject
    private ProjectService projectService;

    @Inject
    private AIModelService aiModelService;

    @Inject
    private MongoDBJsonStore mongoDBJsonStore;

    @Inject
    private SecurityTokenContainer securityTokenContainer;

    @Test
    public void trainModelFromDataLakeSyntheticData() throws Exception{
        int seed = 123;
        double learningRate = 0.008;
        double momentum = 0.9;
        int numInputs = 1;
        int numOutputs = numInputs+1;
        int numHiddenNodes = 20;
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(seed)
                .weightInit(WeightInit.XAVIER)
                .updater(new Nesterovs(learningRate, momentum))
                .list()
                .layer(new DenseLayer.Builder().nIn(numInputs).nOut(numHiddenNodes)
                        .activation(Activation.RELU)
                        .build())
                .layer(new OutputLayer.Builder(LossFunctions.LossFunction.SQUARED_LOSS)
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
        artifact.setNumberOfLabels(numOutputs);

        JsonElement labels = artifact.toJson().get("labels");
        JsonElement features = artifact.toJson().get("features");
        JsonElement parameters = artifact.toJson().get("parameters");
        JsonObject input = new JsonObject();
        input.addProperty("name","model");
        input.addProperty("model",modelString);
        input.add("labels",labels);
        input.add("features",features);
        input.add("parameters",parameters);
        input.addProperty("numberOfLabels",artifact.getNumberOfLabels());
        input.addProperty("labelIndex",artifact.getLabelIndex());

        Scientist scientist = AllModelTests.mockScientist();
        Project project = this.projectService.createArtifactForTraining(scientist.getEmail(),input);
        JsonObject json = this.projectService.storeAiModel(project.getProjectId(),project.getArtifacts().get(0).getArtifactId(),
                "trainModel","java",
                modelString);

        String storedData = IOUtils.resourceToString("dataScience/syntheticData.csv", StandardCharsets.UTF_8,
                Thread.currentThread().getContextClassLoader());

        Artifact trainingArtifact = this.projectService.getArtifact(project.getProjectId(),project.getArtifacts().get(0).getArtifactId());
        for(int i=0; i< 3; i++) {
            String dataLakeId = UUID.randomUUID().toString();
            String chainId = "/" + this.securityTokenContainer.getTenant().getPrincipal() + "/" + dataLakeId;
            JsonObject dataJson = new JsonObject();
            dataJson.addProperty("braineous_datalakeid", dataLakeId);
            dataJson.addProperty("tenant", this.securityTokenContainer.getTenant().getPrincipal());
            dataJson.addProperty("data", storedData);
            dataJson.addProperty("chainId", chainId);
            logger.info("************PERSISTING-" + dataLakeId + "******************");
            logger.info(dataJson.toString());
            logger.info("****************************************");
            this.mongoDBJsonStore.storeIngestion(this.securityTokenContainer.getTenant(), dataJson);

            DataItem dataItem = new DataItem();
            dataItem.setDataLakeId(dataLakeId);
            trainingArtifact.getDataSet().addDataItem(dataItem);
        }

        JsonObject result = this.aiModelService.trainModelFromDataLake(trainingArtifact,30);
        JsonUtil.print(result);
        JsonObject confusion = result.get("confusion").getAsJsonObject();
        JsonUtil.print(confusion);
        assertNotNull(confusion);
    }

    @Test
    public void trainModelFromDataLakeRealData() throws Exception{
        int seed = 123;
        double learningRate = 0.008;
        double momentum = 0.9;
        int numInputs = 9;
        int numOutputs = numInputs+1;
        int numHiddenNodes = 20;
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(seed)
                .weightInit(WeightInit.XAVIER)
                .updater(new Nesterovs(learningRate, momentum))
                .list()
                .layer(new DenseLayer.Builder().nIn(numInputs).nOut(numHiddenNodes)
                        .activation(Activation.RELU)
                        .build())
                .layer(new OutputLayer.Builder(LossFunctions.LossFunction.SQUARED_LOSS)
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
        artifact.setNumberOfLabels(numOutputs);

        JsonElement labels = artifact.toJson().get("labels");
        JsonElement features = artifact.toJson().get("features");
        JsonElement parameters = artifact.toJson().get("parameters");
        JsonObject input = new JsonObject();
        input.addProperty("name","model");
        input.addProperty("model",modelString);
        input.add("labels",labels);
        input.add("features",features);
        input.add("parameters",parameters);
        input.addProperty("numberOfLabels",artifact.getNumberOfLabels());
        input.addProperty("labelIndex",artifact.getLabelIndex());

        Scientist scientist = AllModelTests.mockScientist();
        Project project = this.projectService.createArtifactForTraining(scientist.getEmail(),input);
        JsonObject json = this.projectService.storeAiModel(project.getProjectId(),project.getArtifacts().get(0).getArtifactId(),
                "trainModel","java",
                modelString);

        String storedData = IOUtils.resourceToString("dataScience/aiModelService_california_housing_train.csv", StandardCharsets.UTF_8,
                Thread.currentThread().getContextClassLoader());

        Artifact trainingArtifact = this.projectService.getArtifact(project.getProjectId(),project.getArtifacts().get(0).getArtifactId());
        for(int i=0; i< 1; i++) {
            String dataLakeId = UUID.randomUUID().toString();
            String chainId = "/" + this.securityTokenContainer.getTenant().getPrincipal() + "/" + dataLakeId;
            JsonObject dataJson = new JsonObject();
            dataJson.addProperty("braineous_datalakeid", dataLakeId);
            dataJson.addProperty("tenant", this.securityTokenContainer.getTenant().getPrincipal());
            dataJson.addProperty("data", storedData);
            dataJson.addProperty("chainId", chainId);
            logger.info("************PERSISTING-" + dataLakeId + "******************");
            logger.info(dataJson.toString());
            logger.info("****************************************");
            this.mongoDBJsonStore.storeIngestion(this.securityTokenContainer.getTenant(), dataJson);

            DataItem dataItem = new DataItem();
            dataItem.setDataLakeId(dataLakeId);
            trainingArtifact.getDataSet().addDataItem(dataItem);
        }

        JsonObject result = this.aiModelService.trainModelFromDataLake(trainingArtifact,30);
        JsonUtil.print(result);
        JsonObject confusion = result.get("confusion").getAsJsonObject();
        JsonUtil.print(confusion);
        assertNotNull(confusion);
    }

    @Test
    public void trainModelFromDataSetRealData() throws Exception{
        int seed = 123;
        double learningRate = 0.008;
        double momentum = 0.9;
        int numInputs = 9;
        int numOutputs = numInputs+1;
        int numHiddenNodes = 20;
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(seed)
                .weightInit(WeightInit.XAVIER)
                .updater(new Nesterovs(learningRate, momentum))
                .list()
                .layer(new DenseLayer.Builder().nIn(numInputs).nOut(numHiddenNodes)
                        .activation(Activation.RELU)
                        .build())
                .layer(new OutputLayer.Builder(LossFunctions.LossFunction.SQUARED_LOSS)
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
        artifact.setNumberOfLabels(numOutputs);

        JsonElement labels = artifact.toJson().get("labels");
        JsonElement features = artifact.toJson().get("features");
        JsonElement parameters = artifact.toJson().get("parameters");
        JsonObject input = new JsonObject();
        input.addProperty("name","model");
        input.addProperty("model",modelString);
        input.add("labels",labels);
        input.add("features",features);
        input.add("parameters",parameters);
        input.addProperty("numberOfLabels",artifact.getNumberOfLabels());
        input.addProperty("labelIndex",artifact.getLabelIndex());

        Scientist scientist = AllModelTests.mockScientist();
        Project project = this.projectService.createArtifactForTraining(scientist.getEmail(),input);
        JsonObject json = this.projectService.storeAiModel(project.getProjectId(),project.getArtifacts().get(0).getArtifactId(),
                "trainModel","java",
                modelString);

        String data = IOUtils.resourceToString("dataScience/aiModelService_california_housing_train.csv", StandardCharsets.UTF_8,
                Thread.currentThread().getContextClassLoader());

        String[] dataSetIds = new String[1];
        Artifact trainingArtifact = this.projectService.getArtifact(project.getProjectId(),project.getArtifacts().get(0).getArtifactId());
        for(int i=0; i< dataSetIds.length; i++) {
            input = new JsonObject();
            input.addProperty("format", "csv");
            input.addProperty("data", data);
            Response response = given().body(input.toString()).when().post("/dataset/storeTrainingDataSet/").andReturn();
            JsonObject returnValue = JsonParser.parseString(response.body().asString()).getAsJsonObject();
            String dataSetId = returnValue.get("dataSetId").getAsString();
            dataSetIds[i] = dataSetId;

            DataItem dataItem = new DataItem();
            dataItem.setDataSetId(dataSetId);
            trainingArtifact.getDataSet().addDataItem(dataItem);
        }

        JsonObject result = this.aiModelService.trainModelFromDataSet(trainingArtifact,30);
        JsonUtil.print(result);
        JsonObject confusion = result.get("confusion").getAsJsonObject();
        JsonUtil.print(confusion);
        assertNotNull(confusion);
    }
}
