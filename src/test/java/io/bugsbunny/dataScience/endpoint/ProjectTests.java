package io.bugsbunny.dataScience.endpoint;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.bugsbunny.dataScience.model.*;
import io.bugsbunny.dataScience.service.ProjectService;
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
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertFalse;

@QuarkusTest
public class ProjectTests extends BaseTest
{
    private static Logger logger = LoggerFactory.getLogger(ProjectTests.class);

    @Inject
    private ProjectService projectService;

    @Test
    public void readProjects() throws Exception
    {
        String url = "/projects/";
        Response response = given().get(url).andReturn();
        response.getBody().prettyPrint();
    }

    @Test
    public void updateProject() throws Exception
    {
        Project project = AllModelTests.mockProject();
        this.projectService.addProject(project);

        String url = "/projects/";
        Response response = given().get(url).andReturn();
        response.getBody().prettyPrint();
        JsonObject projectsJson = JsonParser.parseString(response.getBody().asString()).getAsJsonObject();
        JsonArray array = projectsJson.get("projects").getAsJsonArray();
        project = Project.parse(array.get(0).getAsJsonObject().toString());
        Scientist scientist = AllModelTests.mockScientist();
        project.getTeam().addScientist(scientist);

        url = "/projects/updateProject/";
        JsonObject json = new JsonObject();
        json.add("project",project.toJson());
        response = given().body(json.toString()).
                post(url).andReturn();
        Project updated = Project.parse(response.getBody().asString());
        assertTrue(updated.getTeam().getScientists().contains(scientist));
    }

    @Test
    public void updateProjectValidationError() throws Exception
    {
        String url = "/projects/updateProject/";
        JsonObject json = new JsonObject();
        Response response = given().body(json.toString()).
                post(url).andReturn();
        assertEquals(403, response.getStatusCode());
        JsonObject error = JsonParser.parseString(response.getBody().asString()).getAsJsonObject();
        assertEquals("project_missing",error.get("project_missing").getAsString());
    }

    @Test
    public void updateProjectNotFound() throws Exception
    {
        Project project = AllModelTests.mockProject();
        this.projectService.addProject(project);

        String url = "/projects/";
        Response response = given().get(url).andReturn();
        response.getBody().prettyPrint();
        JsonObject projectsJson = JsonParser.parseString(response.getBody().asString()).getAsJsonObject();
        JsonArray array = projectsJson.get("projects").getAsJsonArray();
        project = Project.parse(array.get(0).getAsJsonObject().toString());
        Scientist scientist = AllModelTests.mockScientist();
        project.getTeam().addScientist(scientist);

        url = "/projects/updateProject/";
        JsonObject json = new JsonObject();
        project.setProjectId("blah");
        json.add("project",project.toJson());
        response = given().body(json.toString()).
                post(url).andReturn();
        assertEquals(404,response.getStatusCode());
        String message = JsonParser.parseString(response.getBody().asString()).getAsJsonObject().get("message").getAsString();
        assertEquals("PROJECT_NOT_FOUND",message);
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
        Response response = given().
                body(input.toString()).
                post(url).andReturn();
        //response.getBody().prettyPrint();
        assertEquals(200,response.getStatusCode());
        Project project = Project.parse(response.body().asString());
        JsonUtil.print(project.toJson());

        Artifact deser = project.getArtifacts().get(0);
        assertNotNull(deser.getArtifactId());
        assertEquals(artifact.getLabels(),deser.getLabels());
        assertEquals(artifact.getFeatures(),deser.getFeatures());
        assertEquals(artifact.getParameters(),deser.getParameters());
        assertFalse(artifact.getParameters().isEmpty());
        assertFalse(deser.getParameters().isEmpty());
        assertFalse(deser.isLive());
        assertEquals(scientist.getEmail(),deser.getScientist());
        assertTrue(project.getTeam().getScientists().contains(new Scientist(deser.getScientist())));
    }

    @Test
    public void createModelForTrainingValidationError() throws Exception {
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

        String url = "/projects/createModelForTraining/";
        Response response = given().
                body(input.toString()).
                post(url).andReturn();
        //response.getBody().prettyPrint();
        assertEquals(403,response.getStatusCode());
        JsonObject error = JsonParser.parseString(response.getBody().asString()).getAsJsonObject();
        assertEquals("scientist_missing",error.get("scientist_missing").getAsString());
    }

    @Test
    public void getArtifact() throws Exception {
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
        Response response = given().
                body(input.toString()).
                post(url).andReturn();
        //response.getBody().prettyPrint();
        assertEquals(200,response.getStatusCode());
        Project project = Project.parse(response.body().asString());
        JsonUtil.print(project.toJson());

        url = "/projects/project/artifact/?projectId="+project.getProjectId()+"&artifactId="+project.
                getArtifacts().get(0).getArtifactId();
        response = given().
                get(url).andReturn();
        //response.getBody().prettyPrint();
        assertEquals(200,response.getStatusCode());
        Artifact deser = Artifact.parse(response.body().asString());
        JsonUtil.print(deser.toJson());

        assertNotNull(deser.getArtifactId());
        assertEquals(artifact.getLabels(),deser.getLabels());
        assertEquals(artifact.getFeatures(),deser.getFeatures());
        assertEquals(artifact.getParameters(),deser.getParameters());
        assertFalse(artifact.getParameters().isEmpty());
        assertFalse(deser.getParameters().isEmpty());
        assertFalse(deser.isLive());
        assertEquals(scientist.getEmail(),deser.getScientist());
        assertTrue(project.getTeam().getScientists().contains(new Scientist(deser.getScientist())));
    }

    @Test
    public void getArtifactProjectNotFound() throws Exception {
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
        Response response = given().
                body(input.toString()).
                post(url).andReturn();
        //response.getBody().prettyPrint();
        assertEquals(200,response.getStatusCode());
        Project project = Project.parse(response.body().asString());
        JsonUtil.print(project.toJson());

        url = "/projects/project/artifact/?projectId=mock&artifactId="+project.
                getArtifacts().get(0).getArtifactId();
        response = given().
                get(url).andReturn();
        //response.getBody().prettyPrint();
        assertEquals(404,response.getStatusCode());
        String message = JsonParser.parseString(response.getBody().asString()).getAsJsonObject().get("message").getAsString();
        assertEquals("ARTIFACT_NOT_FOUND",message);
    }

    @Test
    public void getArtifactNotFound() throws Exception {
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
        Response response = given().
                body(input.toString()).
                post(url).andReturn();
        //response.getBody().prettyPrint();
        assertEquals(200,response.getStatusCode());
        Project project = Project.parse(response.body().asString());
        JsonUtil.print(project.toJson());

        url = "/projects/project/artifact/?projectId="+project.getProjectId()+"&artifactId=mock";
        response = given().
                get(url).andReturn();
        //response.getBody().prettyPrint();
        assertEquals(404,response.getStatusCode());
        String message = JsonParser.parseString(response.getBody().asString()).getAsJsonObject().get("message").getAsString();
        assertEquals("ARTIFACT_NOT_FOUND",message);
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
        input.addProperty("scientist",scientist.getEmail());

        String url = "/projects/createModelForTraining/";
        Response response = given().
                body(input.toString()).
                post(url).andReturn();
        //response.getBody().prettyPrint();
        assertEquals(200,response.getStatusCode());
        Project project = Project.parse(response.body().asString());
        JsonUtil.print(project.toJson());

        url = "/projects/project/artifact/?projectId="+project.getProjectId()+"&artifactId="+project.
                getArtifacts().get(0).getArtifactId();
        response = given().
                get(url).andReturn();
        //response.getBody().prettyPrint();
        assertEquals(200,response.getStatusCode());
        Artifact deser = Artifact.parse(response.body().asString());
        JsonUtil.print(deser.toJson());

        assertNotNull(deser.getArtifactId());
        assertEquals(artifact.getLabels(),deser.getLabels());
        assertEquals(artifact.getFeatures(),deser.getFeatures());
        assertEquals(artifact.getParameters(),deser.getParameters());
        assertFalse(artifact.getParameters().isEmpty());
        assertFalse(deser.getParameters().isEmpty());
        assertFalse(deser.isLive());
        assertEquals(scientist.getEmail(),deser.getScientist());
        assertTrue(project.getTeam().getScientists().contains(new Scientist(deser.getScientist())));

        //Update the artifact
        Label newLabel = new Label("newValue","newField");
        deser.addLabel(newLabel);
        JsonObject json = new JsonObject();
        json.addProperty("projectId",project.getProjectId());
        json.add("artifact",deser.toJson());
        url = "/projects/updateArtifact/";
        response = given().
                body(json.toString()).post(url)
                .andReturn();
        assertEquals(200, response.getStatusCode());
        Artifact updated = Artifact.parse(response.body().asString());
        assertTrue(updated.getLabels().contains(newLabel));
        JsonUtil.print(this.projectService.readProject(project.getProjectId()).toJson());
    }

    @Test
    public void updateArtifactValidationError() throws Exception{
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
        Response response = given().
                body(input.toString()).
                post(url).andReturn();
        //response.getBody().prettyPrint();
        assertEquals(200,response.getStatusCode());
        Project project = Project.parse(response.body().asString());
        JsonUtil.print(project.toJson());

        url = "/projects/project/artifact/?projectId="+project.getProjectId()+"&artifactId="+project.
                getArtifacts().get(0).getArtifactId();
        response = given().
                get(url).andReturn();
        //response.getBody().prettyPrint();
        assertEquals(200,response.getStatusCode());
        Artifact deser = Artifact.parse(response.body().asString());
        JsonUtil.print(deser.toJson());

        assertNotNull(deser.getArtifactId());
        assertEquals(artifact.getLabels(),deser.getLabels());
        assertEquals(artifact.getFeatures(),deser.getFeatures());
        assertEquals(artifact.getParameters(),deser.getParameters());
        assertFalse(artifact.getParameters().isEmpty());
        assertFalse(deser.getParameters().isEmpty());
        assertFalse(deser.isLive());
        assertEquals(scientist.getEmail(),deser.getScientist());
        assertTrue(project.getTeam().getScientists().contains(new Scientist(deser.getScientist())));

        //Update the artifact
        Label newLabel = new Label("newValue","newField");
        deser.addLabel(newLabel);
        JsonObject json = new JsonObject();
        url = "/projects/updateArtifact/";
        response = given().
                body(json.toString()).post(url)
                .andReturn();
        assertEquals(403, response.getStatusCode());
        JsonObject error = JsonParser.parseString(response.getBody().asString()).getAsJsonObject();
        assertEquals("project_id_missing",error.get("project_id_missing").getAsString());
        assertEquals("artifact_missing",error.get("artifact_missing").getAsString());
    }

    @Test
    public void updateArtifactNotFound() throws Exception{
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
        Response response = given().
                body(input.toString()).
                post(url).andReturn();
        //response.getBody().prettyPrint();
        assertEquals(200,response.getStatusCode());
        Project project = Project.parse(response.body().asString());
        JsonUtil.print(project.toJson());

        url = "/projects/project/artifact/?projectId="+project.getProjectId()+"&artifactId="+project.
                getArtifacts().get(0).getArtifactId();
        response = given().
                get(url).andReturn();
        //response.getBody().prettyPrint();
        assertEquals(200,response.getStatusCode());
        Artifact deser = Artifact.parse(response.body().asString());
        JsonUtil.print(deser.toJson());

        assertNotNull(deser.getArtifactId());
        assertEquals(artifact.getLabels(),deser.getLabels());
        assertEquals(artifact.getFeatures(),deser.getFeatures());
        assertEquals(artifact.getParameters(),deser.getParameters());
        assertFalse(artifact.getParameters().isEmpty());
        assertFalse(deser.getParameters().isEmpty());
        assertFalse(deser.isLive());
        assertEquals(scientist.getEmail(),deser.getScientist());
        assertTrue(project.getTeam().getScientists().contains(new Scientist(deser.getScientist())));

        //Update the artifact
        Label newLabel = new Label("newValue","newField");
        deser.addLabel(newLabel);
        JsonObject json = new JsonObject();
        json.addProperty("projectId","blah");
        json.add("artifact", deser.toJson());
        url = "/projects/updateArtifact/";
        response = given().
                body(json.toString()).post(url)
                .andReturn();
        assertEquals(404, response.getStatusCode());
        JsonObject error = JsonParser.parseString(response.getBody().asString()).getAsJsonObject();
        assertEquals("ARTIFACT_NOT_FOUND",error.get("message").getAsString());
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
        input.addProperty("scientist",scientist.getEmail());

        String url = "/projects/createModelForTraining/";
        Response response = given().
                body(input.toString()).
                post(url).andReturn();
        //response.getBody().prettyPrint();
        assertEquals(200,response.getStatusCode());
        Project project = Project.parse(response.body().asString());
        JsonUtil.print(project.toJson());

        url = "/projects/project/artifact/?projectId="+project.getProjectId()+"&artifactId="+project.
                getArtifacts().get(0).getArtifactId();
        response = given().
                get(url).andReturn();
        //response.getBody().prettyPrint();
        assertEquals(200,response.getStatusCode());
        Artifact deser = Artifact.parse(response.body().asString());
        JsonUtil.print(deser.toJson());

        assertNotNull(deser.getArtifactId());
        assertEquals(artifact.getLabels(),deser.getLabels());
        assertEquals(artifact.getFeatures(),deser.getFeatures());
        assertEquals(artifact.getParameters(),deser.getParameters());
        assertFalse(artifact.getParameters().isEmpty());
        assertFalse(deser.getParameters().isEmpty());
        assertFalse(deser.isLive());
        assertEquals(scientist.getEmail(),deser.getScientist());
        assertTrue(project.getTeam().getScientists().contains(new Scientist(deser.getScientist())));

        //Delete the artifact
        url = "/projects/deleteArtifact/?projectId="+project.getProjectId()+"&artifactId="+deser.getArtifactId();
        response = given().delete(url)
                .andReturn();
        response.getBody().prettyPrint();
        assertEquals(200, response.getStatusCode());
        Project updatedProject = Project.parse(response.body().asString());
        JsonUtil.print(this.projectService.readProject(project.getProjectId()).toJson());
        assertFalse(updatedProject.getArtifacts().contains(deser));
    }

    @Test
    public void deleteArtifactNotFound() throws Exception{
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
        Response response = given().
                body(input.toString()).
                post(url).andReturn();
        //response.getBody().prettyPrint();
        assertEquals(200,response.getStatusCode());
        Project project = Project.parse(response.body().asString());
        JsonUtil.print(project.toJson());

        url = "/projects/project/artifact/?projectId="+project.getProjectId()+"&artifactId="+project.
                getArtifacts().get(0).getArtifactId();
        response = given().
                get(url).andReturn();
        //response.getBody().prettyPrint();
        assertEquals(200,response.getStatusCode());
        Artifact deser = Artifact.parse(response.body().asString());
        JsonUtil.print(deser.toJson());

        assertNotNull(deser.getArtifactId());
        assertEquals(artifact.getLabels(),deser.getLabels());
        assertEquals(artifact.getFeatures(),deser.getFeatures());
        assertEquals(artifact.getParameters(),deser.getParameters());
        assertFalse(artifact.getParameters().isEmpty());
        assertFalse(deser.getParameters().isEmpty());
        assertFalse(deser.isLive());
        assertEquals(scientist.getEmail(),deser.getScientist());
        assertTrue(project.getTeam().getScientists().contains(new Scientist(deser.getScientist())));

        //Delete the artifact
        url = "/projects/deleteArtifact/?projectId="+project.getProjectId()+"&artifactId=blah";
        response = given().delete(url)
                .andReturn();
        response.getBody().prettyPrint();
        assertEquals(404, response.getStatusCode());
        JsonObject error = JsonParser.parseString(response.getBody().asString()).getAsJsonObject();
        assertEquals("ARTIFACT_NOT_FOUND",error.get("message").getAsString());
    }
}