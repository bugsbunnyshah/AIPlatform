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
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.nio.charset.StandardCharsets;

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
        deser = this.projectService.updateArtifact(project.getProjectId(),deser);
        assertTrue(deser.getLabels().contains(newLabel));
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
}
