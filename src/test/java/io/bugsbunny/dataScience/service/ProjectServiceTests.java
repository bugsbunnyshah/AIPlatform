package io.bugsbunny.dataScience.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.bugsbunny.dataScience.model.AllModelTests;
import io.bugsbunny.dataScience.model.Artifact;
import io.bugsbunny.dataScience.model.Project;
import io.bugsbunny.dataScience.model.Scientist;
import io.bugsbunny.infrastructure.MongoDBJsonStore;
import io.bugsbunny.infrastructure.Tenant;
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
import java.util.UUID;

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
    public void addArtifact() throws Exception{
        Artifact artifact = AllModelTests.mockArtifact();
        Project project = AllModelTests.mockProject();
        project.getArtifacts().clear();

        String modelPackage = IOUtils.resourceToString("dataScience/aiplatform-model.json", StandardCharsets.UTF_8,
                Thread.currentThread().getContextClassLoader());
        JsonObject modelJson = JsonParser.parseString(modelPackage).getAsJsonObject();

        String projectId = project.getProjectId();
        this.projectService.addProject(project);
        String modelId = this.projectService.addDataArtifact(projectId,modelJson,new JsonArray(),artifact);
        logger.info("MODEL_ID: "+modelId);

        Project stored = this.projectService.readProject(projectId);
        JsonUtil.print(stored.toJson());
        assertEquals(stored.getProjectId(),project.getProjectId());
        assertTrue(stored.getArtifacts().contains(artifact));
        assertTrue(stored.containsModel(modelId));
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
    public void evalModelFromData() throws Exception{
        Project project = AllModelTests.mockProject();
        project.getArtifacts().clear();
        this.projectService.addProject(project);

        Artifact artifact = AllModelTests.mockArtifact();

        String modelPackage = IOUtils.resourceToString("dataScience/aiplatform-model.json", StandardCharsets.UTF_8,
                Thread.currentThread().getContextClassLoader());
        JsonObject modelJson = JsonParser.parseString(modelPackage).getAsJsonObject();

        String data = IOUtils.resourceToString("dataScience/saturn_data_train.csv", StandardCharsets.UTF_8,
                Thread.currentThread().getContextClassLoader());
        JsonObject input = new JsonObject();
        input.addProperty("format", "csv");
        input.addProperty("data", data);
        JsonArray modelInput = new JsonArray();
        modelInput.add(input);

        String modelId = this.projectService.addDataArtifact(project.getProjectId(),modelJson,modelInput,artifact);
        project = this.projectService.readProject(project.getProjectId());
        logger.info("MODEL_ID: "+modelId);
        assertTrue(project.containsModel(modelId));

        this.projectService.evalModelFromData(project.getProjectId(),artifact.getArtifactId());
    }

    @Test
    public void evalModelFromLake() throws Exception{
        Project project = AllModelTests.mockProject();
        project.getArtifacts().clear();
        this.projectService.addProject(project);

        Artifact artifact = AllModelTests.mockArtifact();

        String modelPackage = IOUtils.resourceToString("dataScience/aiplatform-model.json", StandardCharsets.UTF_8,
                Thread.currentThread().getContextClassLoader());
        JsonObject modelJson = JsonParser.parseString(modelPackage).getAsJsonObject();

        String data = IOUtils.toString(Thread.currentThread().
                        getContextClassLoader().
                        getResourceAsStream("aviation/flights0.json"),
                StandardCharsets.UTF_8);

        JsonArray json = JsonParser.parseString(data).getAsJsonObject().get("data").getAsJsonArray();
        String dataLakeId = UUID.randomUUID().toString();
        Tenant tenant = this.securityTokenContainer.getTenant();
        String chainId = "/" + tenant.getPrincipal() + "/" + dataLakeId;
        for(int i=0; i<10;i++)
        {
            JsonObject cour = json.get(i).getAsJsonObject();
            cour.addProperty("braineous_datalakeid",dataLakeId);
            cour.addProperty("tenant",tenant.getPrincipal());
            cour.addProperty("data", cour.toString());
            cour.addProperty("chainId",chainId);
            this.mongoDBJsonStore.storeIngestion(tenant,cour);
        }

        JsonArray ingestion = this.mongoDBJsonStore.getIngestion(tenant,dataLakeId);
        //JsonUtil.print(ingestion);

        data = IOUtils.resourceToString("dataScience/saturn_data_train.csv", StandardCharsets.UTF_8,
                Thread.currentThread().getContextClassLoader());
        JsonObject input = new JsonObject();
        input.addProperty("format", "csv");
        input.addProperty("data", data);
        JsonArray modelInput = new JsonArray();
        modelInput.add(input);

        String modelId = this.projectService.addLakeArtifact(project.getProjectId(),modelJson,modelInput,artifact);
        project = this.projectService.readProject(project.getProjectId());
        logger.info("MODEL_ID: "+modelId);
        assertTrue(project.containsModel(modelId));

        this.projectService.evalModelDataFromLake(project.getProjectId(),artifact.getArtifactId());
    }
}
