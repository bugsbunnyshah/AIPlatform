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
    public void readProjects() throws Exception{
        System.out.println(this.projectService.readProjects());
    }


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
    public void verifyDeployment() throws Exception {
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

        //Assert the actual model was stored
        String model = this.projectService.getAiModel(project.getProjectId(), deser.getArtifactId());
        assertNotNull(model);
        assertTrue(model.length()>0);
    }
}
