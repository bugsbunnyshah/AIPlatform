package io.bugsbunny.dataScience.endpoint;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.bugsbunny.dataScience.model.AllModelTests;
import io.bugsbunny.dataScience.model.Artifact;
import io.bugsbunny.dataScience.model.Project;
import io.bugsbunny.dataScience.model.Scientist;
import io.bugsbunny.preprocess.SecurityTokenContainer;
import io.bugsbunny.test.components.BaseTest;
import io.bugsbunny.util.JsonUtil;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.nio.charset.StandardCharsets;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertFalse;

@QuarkusTest
public class ProjectTests extends BaseTest
{
    private static Logger logger = LoggerFactory.getLogger(ProjectTests.class);

    @Inject
    private SecurityTokenContainer securityTokenContainer;

    @Test
    public void readProjects() throws Exception
    {
        String url = "/projects/";
        String principal = this.securityTokenContainer.getSecurityToken().getPrincipal();
        String token = this.securityTokenContainer.getSecurityToken().getToken();
        Response response = given().header("Principal",principal).header("Bearer",token).get(url).andReturn();
        response.getBody().prettyPrint();
    }

    @Test
    public void createModelForTraining() throws Exception {
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
        input.addProperty("scientist",scientist.getEmail());

        String url = "/projects/createModelForTraining/";
        String principal = this.securityTokenContainer.getSecurityToken().getPrincipal();
        String token = this.securityTokenContainer.getSecurityToken().getToken();
        Response response = given().header("Principal",principal).header("Bearer",token).
                body(input.toString()).
                post(url).andReturn();
        //response.getBody().prettyPrint();
        assertEquals(200,response.getStatusCode());
        Project project = Project.parse(response.body().asString());
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
        JsonObject json = new JsonObject();
        json.addProperty("projectId",project.getProjectId());
        json.addProperty("artifactId", deser.getArtifactId());
        url = "/projects/model/";
        response = given().header("Principal",principal).header("Bearer",token).
                body(json.toString()).post(url)
                .andReturn();
        assertEquals(200, response.getStatusCode());
        String model = response.body().asString();
        //logger.info(model);
        assertTrue(model.length() > 0);
    }
}