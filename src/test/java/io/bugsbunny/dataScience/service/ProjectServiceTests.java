package io.bugsbunny.dataScience.service;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.bugsbunny.dataScience.model.*;
import io.bugsbunny.infrastructure.MongoDBJsonStore;
import io.bugsbunny.preprocess.SecurityTokenContainer;
import io.bugsbunny.test.components.BaseTest;
import io.bugsbunny.util.JsonUtil;
import io.quarkus.test.junit.QuarkusTest;

import org.apache.commons.io.IOUtils;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.nio.charset.StandardCharsets;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.bugsbunny.dataScience.dl4j.AIPlatformDataSetIteratorFactory;
import io.bugsbunny.preprocess.SecurityTokenContainer;

import io.bugsbunny.test.components.BaseTest;
import io.quarkus.test.junit.QuarkusTest;
import org.deeplearning4j.datasets.iterator.impl.MnistDataSetIterator;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.layers.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import io.restassured.response.Response;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.FileUtils;

import org.datavec.api.records.reader.RecordReader;
import org.datavec.api.records.reader.impl.csv.CSVRecordReader;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.common.primitives.Pair;
import org.nd4j.evaluation.classification.Evaluation;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.learning.config.Nesterovs;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.nd4j.linalg.lossfunctions.LossFunctions.LossFunction;
import org.datavec.api.split.FileSplit;
import org.deeplearning4j.util.ModelSerializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import static io.restassured.RestAssured.given;

@QuarkusTest
public class ProjectServiceTests extends BaseTest {
    private static Logger logger = LoggerFactory.getLogger(ProjectServiceTests.class);

    @Inject
    private ProjectService projectService;

    @Inject
    private MongoDBJsonStore mongoDBJsonStore;

    @Inject
    private SecurityTokenContainer securityTokenContainer;

    @Test
    public void addScientist() throws Exception{
        Project project = AllModelTests.mockProject();
        String projectId = project.getProjectId();
        Scientist scientist = AllModelTests.mockScientist();
        this.projectService.addProject(project);

        this.projectService.addScientist(project.getProjectId(),scientist);

        Project stored = this.projectService.readProject(projectId);
        JsonUtil.print(ProjectServiceTests.class,stored.toJson());
        assertTrue(stored.getTeam().getScientists().contains(scientist));
    }

    @Test
    public void createArtifactForTraining() throws Exception{
        String modelPackage = IOUtils.resourceToString("dataScience/aiplatform-model.json", StandardCharsets.UTF_8,
                Thread.currentThread().getContextClassLoader());

        Artifact artifact = AllModelTests.mockArtifact();
        JsonElement labels = artifact.toJson().get("labels");
        JsonElement features = artifact.toJson().get("features");
        JsonElement parameters = artifact.toJson().get("parameters");

        JsonObject input = JsonParser.parseString(modelPackage).getAsJsonObject();
        input.add("labels",labels);
        input.add("features",features);
        input.add("parameters",parameters);

        Scientist scientist = AllModelTests.mockScientist();

        Project project = this.projectService.createArtifactForTraining(scientist.getEmail(),input);
        JsonUtil.print(project.toJson());
        Artifact deser = project.getArtifacts().get(0);
        assertNotNull(deser.getArtifactId());
        assertNotNull(deser.getAiModel().getModelId());
        assertEquals(artifact.getLabels(),deser.getLabels());
        assertEquals(artifact.getFeatures(),deser.getFeatures());
        assertEquals(artifact.getParameters(),deser.getParameters());
        assertFalse(artifact.getParameters().isEmpty());
        assertFalse(deser.getParameters().isEmpty());
        assertFalse(deser.isLive());
        assertEquals(scientist.getEmail(),deser.getScientist());
        assertTrue(project.getTeam().getScientists().contains(new Scientist(deser.getScientist())));

        //Assert the actual model was stored
        String model = this.projectService.getAiModel(project.getProjectId(), deser.getArtifactId());
        assertNotNull(model);
        assertTrue(model.length()>0);
    }

    @Test
    public void getArtifact() throws Exception{
        String modelPackage = IOUtils.resourceToString("dataScience/aiplatform-model.json", StandardCharsets.UTF_8,
                Thread.currentThread().getContextClassLoader());

        Artifact artifact = AllModelTests.mockArtifact();
        JsonElement labels = artifact.toJson().get("labels");
        JsonElement features = artifact.toJson().get("features");
        JsonElement parameters = artifact.toJson().get("parameters");

        JsonObject input = JsonParser.parseString(modelPackage).getAsJsonObject();
        input.add("labels",labels);
        input.add("features",features);
        input.add("parameters",parameters);

        Scientist scientist = AllModelTests.mockScientist();

        Project project = this.projectService.createArtifactForTraining(scientist.getEmail(),input);
        JsonUtil.print(project.toJson());
        Artifact deser = this.projectService.getArtifact(project.getProjectId(),
                project.getArtifacts().get(0).getArtifactId());
        assertNotNull(deser.getArtifactId());
        assertNotNull(deser.getAiModel().getModelId());
        assertEquals(artifact.getLabels(),deser.getLabels());
        assertEquals(artifact.getFeatures(),deser.getFeatures());
        assertEquals(artifact.getParameters(),deser.getParameters());
        assertFalse(artifact.getParameters().isEmpty());
        assertFalse(deser.getParameters().isEmpty());
        assertFalse(deser.isLive());
        assertEquals(scientist.getEmail(),deser.getScientist());
        assertTrue(project.getTeam().getScientists().contains(new Scientist(deser.getScientist())));

        //Assert the actual model was stored
        String model = this.projectService.getAiModel(project.getProjectId(), deser.getArtifactId());
        assertNotNull(model);
        assertTrue(model.length()>0);
    }

    @Test
    public void getArtifactProjectNotFound() throws Exception{
        String modelPackage = IOUtils.resourceToString("dataScience/aiplatform-model.json", StandardCharsets.UTF_8,
                Thread.currentThread().getContextClassLoader());

        Artifact artifact = AllModelTests.mockArtifact();
        JsonElement labels = artifact.toJson().get("labels");
        JsonElement features = artifact.toJson().get("features");
        JsonElement parameters = artifact.toJson().get("parameters");

        JsonObject input = JsonParser.parseString(modelPackage).getAsJsonObject();
        input.add("labels",labels);
        input.add("features",features);
        input.add("parameters",parameters);

        Scientist scientist = AllModelTests.mockScientist();

        Project project = this.projectService.createArtifactForTraining(scientist.getEmail(),input);
        JsonUtil.print(project.toJson());
        Artifact deser = this.projectService.getArtifact("mock",
                project.getArtifacts().get(0).getArtifactId());
        assertNull(deser);
    }

    @Test
    public void getArtifactNotFound() throws Exception{
        String modelPackage = IOUtils.resourceToString("dataScience/aiplatform-model.json", StandardCharsets.UTF_8,
                Thread.currentThread().getContextClassLoader());

        Artifact artifact = AllModelTests.mockArtifact();
        JsonElement labels = artifact.toJson().get("labels");
        JsonElement features = artifact.toJson().get("features");
        JsonElement parameters = artifact.toJson().get("parameters");

        JsonObject input = JsonParser.parseString(modelPackage).getAsJsonObject();
        input.add("labels",labels);
        input.add("features",features);
        input.add("parameters",parameters);

        Scientist scientist = AllModelTests.mockScientist();

        Project project = this.projectService.createArtifactForTraining(scientist.getEmail(),input);
        JsonUtil.print(project.toJson());
        Artifact deser = this.projectService.getArtifact(project.getProjectId(),
                "mock");
        assertNull(deser);
    }

    @Test
    public void updateArtifact() throws Exception{
        String modelPackage = IOUtils.resourceToString("dataScience/aiplatform-model.json", StandardCharsets.UTF_8,
                Thread.currentThread().getContextClassLoader());

        Artifact artifact = AllModelTests.mockArtifact();
        JsonElement labels = artifact.toJson().get("labels");
        JsonElement features = artifact.toJson().get("features");
        JsonElement parameters = artifact.toJson().get("parameters");

        JsonObject input = JsonParser.parseString(modelPackage).getAsJsonObject();
        input.add("labels",labels);
        input.add("features",features);
        input.add("parameters",parameters);

        Scientist scientist = AllModelTests.mockScientist();

        Project project = this.projectService.createArtifactForTraining(scientist.getEmail(),input);
        JsonUtil.print(project.toJson());
        Artifact deser = this.projectService.getArtifact(project.getProjectId(),
                project.getArtifacts().get(0).getArtifactId());
        assertNotNull(deser.getArtifactId());
        assertNotNull(deser.getAiModel().getModelId());
        assertEquals(artifact.getLabels(),deser.getLabels());
        assertEquals(artifact.getFeatures(),deser.getFeatures());
        assertEquals(artifact.getParameters(),deser.getParameters());
        assertFalse(artifact.getParameters().isEmpty());
        assertFalse(deser.getParameters().isEmpty());
        assertFalse(deser.isLive());
        assertEquals(scientist.getEmail(),deser.getScientist());
        assertTrue(project.getTeam().getScientists().contains(new Scientist(deser.getScientist())));

        Label newLabel = new Label("newValue","newField");
        deser.addLabel(newLabel);
        Artifact updated = this.projectService.updateArtifact(project.getProjectId(),deser);
        assertTrue(updated.getLabels().contains(newLabel));
        JsonUtil.print(this.projectService.readProject(project.getProjectId()).toJson());

        //Assert the actual model was stored
        String model = this.projectService.getAiModel(project.getProjectId(), deser.getArtifactId());
        assertNotNull(model);
        assertTrue(model.length()>0);
    }

    @Test
    public void deleteArtifact() throws Exception{
        String modelPackage = IOUtils.resourceToString("dataScience/aiplatform-model.json", StandardCharsets.UTF_8,
                Thread.currentThread().getContextClassLoader());

        Artifact artifact = AllModelTests.mockArtifact();
        JsonElement labels = artifact.toJson().get("labels");
        JsonElement features = artifact.toJson().get("features");
        JsonElement parameters = artifact.toJson().get("parameters");

        JsonObject input = JsonParser.parseString(modelPackage).getAsJsonObject();
        input.add("labels",labels);
        input.add("features",features);
        input.add("parameters",parameters);

        Scientist scientist = AllModelTests.mockScientist();

        Project project = this.projectService.createArtifactForTraining(scientist.getEmail(),input);
        JsonUtil.print(project.toJson());
        Artifact deser = this.projectService.getArtifact(project.getProjectId(),
                project.getArtifacts().get(0).getArtifactId());
        assertNotNull(deser.getArtifactId());
        assertNotNull(deser.getAiModel().getModelId());
        assertEquals(artifact.getLabels(),deser.getLabels());
        assertEquals(artifact.getFeatures(),deser.getFeatures());
        assertEquals(artifact.getParameters(),deser.getParameters());
        assertFalse(artifact.getParameters().isEmpty());
        assertFalse(deser.getParameters().isEmpty());
        assertFalse(deser.isLive());
        assertEquals(scientist.getEmail(),deser.getScientist());
        assertTrue(project.getTeam().getScientists().contains(new Scientist(deser.getScientist())));

        Project updatedProject = this.projectService.deleteArtifact(project.getProjectId(), deser.getArtifactId());
        JsonUtil.print(updatedProject.toJson());
        assertFalse(updatedProject.getArtifacts().contains(deser));

        //Assert the actual model was stored
        String model = this.projectService.getAiModel(project.getProjectId(), deser.getArtifactId());
        assertNull(model);
    }

    @Test
    public void updateProject() throws Exception{
        Project project = AllModelTests.mockProject();
        String projectId = project.getProjectId();
        Scientist scientist = AllModelTests.mockScientist();
        project.getTeam().addScientist(scientist);
        this.projectService.addProject(project);

        Project stored = this.projectService.readProject(projectId);
        JsonUtil.print(ProjectServiceTests.class,stored.toJson());
        assertTrue(stored.getTeam().getScientists().contains(scientist));

        stored.getTeam().getScientists().remove(scientist);
        stored = this.projectService.updateProject(stored);
        JsonUtil.print(ProjectServiceTests.class,stored.toJson());
        assertFalse(stored.getTeam().getScientists().contains(scientist));
    }

    @Test
    public void verifyDeployment() throws Exception {
    }

    @Test
    public void trainModelFromDataSet() throws Exception
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
                .layer(new OutputLayer.Builder(LossFunction.NEGATIVELOGLIKELIHOOD)
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

        String[] dataSetIds = new String[3];
        for(int i=0; i< dataSetIds.length; i++) {
            input = new JsonObject();
            input.addProperty("format", "csv");
            input.addProperty("data", data);
            Response response = given().body(input.toString()).when().post("/dataset/storeTrainingDataSet/").andReturn();
            JsonObject returnValue = JsonParser.parseString(response.body().asString()).getAsJsonObject();
            String dataSetId = returnValue.get("dataSetId").getAsString();
            dataSetIds[i] = dataSetId;
        }

        JsonObject trainingResult = this.projectService.trainModelFromDataSet(project.getProjectId(),
                project.getArtifacts().get(0).getArtifactId(), dataSetIds);
        JsonObject confusion = trainingResult.get("confusion").getAsJsonObject();
        JsonUtil.print(confusion);
    }

    @Test
    public void trainModelFromDataLake() throws Exception
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
                .layer(new OutputLayer.Builder(LossFunction.NEGATIVELOGLIKELIHOOD)
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

        String[] dataLakeIds = new String[3];

        for(int i=0; i< dataLakeIds.length; i++) {
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
            dataLakeIds[i] = dataLakeId;
        }

        JsonObject trainingResult = this.projectService.trainModelFromDataLake(project.getProjectId(),
                project.getArtifacts().get(0).getArtifactId(), dataLakeIds);
        JsonObject confusion = trainingResult.get("confusion").getAsJsonObject();
        JsonUtil.print(confusion);
        assertNotNull(confusion);
    }
}
