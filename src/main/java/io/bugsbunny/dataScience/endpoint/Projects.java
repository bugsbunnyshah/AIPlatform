package io.bugsbunny.dataScience.endpoint;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.bugsbunny.dataScience.model.Artifact;
import io.bugsbunny.dataScience.model.Project;
import io.bugsbunny.dataScience.service.ArtifactNotFoundException;
import io.bugsbunny.dataScience.service.ProjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("projects")
public class Projects
{
    private static Logger logger = LoggerFactory.getLogger(Projects.class);

    private static boolean isPythonDetected = false;

    @Inject
    private ProjectService projectService;


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response readProjects()
    {
        try {
            List<Project> projects = this.projectService.readProjects();
            JsonArray array = JsonParser.parseString(projects.toString()).getAsJsonArray();

            JsonObject json = new JsonObject();
            json.add("projects",array);
            Response response = Response.ok(json.toString()).build();
            return response;
        }
        catch(Exception e)
        {
            logger.error(e.getMessage(), e);
            JsonObject error = new JsonObject();
            error.addProperty("exception", e.getMessage());
            return Response.status(500).entity(error.toString()).build();
        }
    }

    @Path("updateProject")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateProject(@RequestBody String input){
        try {
            JsonObject json = JsonParser.parseString(input).getAsJsonObject();
            JsonObject projectJson = null;
            if(json.has("project")){
                projectJson = json.get("project").getAsJsonObject();
            }

            //Validate
            if(projectJson == null){
                JsonObject response = new JsonObject();
                if(projectJson == null){
                    response.addProperty("project_missing","project_missing");
                }
                return Response.status(403).entity(response.toString()).build();
            }

            Project project = Project.parse(projectJson.toString());

            project = this.projectService.updateProject(project);
            if(project == null){
                JsonObject error = new JsonObject();
                error.addProperty("message", "PROJECT_NOT_FOUND");
                return Response.status(404).entity(error.toString()).build();
            }

            return Response.ok(project.toJson().toString()).build();
        }
        catch(Exception e)
        {
            logger.error(e.getMessage(), e);
            JsonObject error = new JsonObject();
            error.addProperty("exception", e.getMessage());
            return Response.status(500).entity(error.toString()).build();
        }
    }

    @Path("createModelForTraining")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response createModelForTraining(@RequestBody String input){
        try {
            JsonObject json = JsonParser.parseString(input).getAsJsonObject();
            String scientist = null;
            if(json.has("scientist")){
                scientist = json.get("scientist").getAsString();
            }

            //Validate
            if(scientist == null) {
                JsonObject response = new JsonObject();
                if (scientist == null) {
                    response.addProperty("scientist_missing", "scientist_missing");
                }
                return Response.status(403).entity(response.toString()).build();
            }

            Project project = this.projectService.createArtifactForTraining(scientist,json);
            return Response.ok(project.toJson().toString()).build();
        }
        catch(Exception e)
        {
            logger.error(e.getMessage(), e);
            JsonObject error = new JsonObject();
            error.addProperty("exception", e.getMessage());
            return Response.status(500).entity(error.toString()).build();
        }
    }

    @Path("/project/artifact")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getArtifact(@QueryParam("projectId") String projectId, @QueryParam("artifactId") String artifactId)
    {
        try {
            Artifact artifact = this.projectService.getArtifact(projectId,artifactId);
            if(artifact == null){
                JsonObject error = new JsonObject();
                error.addProperty("message", "ARTIFACT_NOT_FOUND");
                return Response.status(404).entity(error.toString()).build();
            }

            Response response = Response.ok(artifact.toJson().toString()).build();
            return response;
        }
        catch(Exception e)
        {
            logger.error(e.getMessage(), e);
            JsonObject error = new JsonObject();
            error.addProperty("exception", e.getMessage());
            return Response.status(500).entity(error.toString()).build();
        }
    }

    @Path("updateArtifact")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateArtifact(@RequestBody String input){
        try {
            JsonObject json = JsonParser.parseString(input).getAsJsonObject();
            String projectId = null;
            if(json.has("projectId")){
                projectId = json.get("projectId").getAsString();
            }
            JsonObject artifactJson = null;
            if(json.has("artifact")){
                artifactJson = json.get("artifact").getAsJsonObject();
            }

            //Validate
            if(projectId == null || artifactJson == null){
                JsonObject response = new JsonObject();
                if(projectId == null){
                    response.addProperty("project_id_missing","project_id_missing");
                }
                if(artifactJson == null){
                    response.addProperty("artifact_missing","artifact_missing");
                }
                return Response.status(403).entity(response.toString()).build();
            }

            Artifact artifact = Artifact.parse(artifactJson.toString());

            artifact = this.projectService.updateArtifact(projectId,artifact);
            if(artifact == null){
                JsonObject error = new JsonObject();
                error.addProperty("message", "ARTIFACT_NOT_FOUND");
                return Response.status(404).entity(error.toString()).build();
            }

            return Response.ok(artifact.toJson().toString()).build();
        }
        catch(Exception e)
        {
            logger.error(e.getMessage(), e);
            JsonObject error = new JsonObject();
            error.addProperty("exception", e.getMessage());
            return Response.status(500).entity(error.toString()).build();
        }
    }

    @Path("deleteArtifact")
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteArtifact(@QueryParam("projectId") String projectId, @QueryParam("artifactId") String artifactId)
    {
        try {
            //Validate
            if(projectId == null || artifactId == null){
                JsonObject response = new JsonObject();

                if(projectId == null){
                    response.addProperty("project_id_missing","project_id_missing");
                }
                else{
                    response.addProperty("artifact_id_missing","artifact_id_missing");
                }

                return Response.status(403).entity(response).build();
            }

            Project project = this.projectService.deleteArtifact(projectId,artifactId);
            if(project == null){
                JsonObject error = new JsonObject();
                error.addProperty("message", "ARTIFACT_NOT_FOUND");
                return Response.status(404).entity(error.toString()).build();
            }

            Response response = Response.ok(project.toJson().toString()).build();
            return response;
        }
        catch(Exception e)
        {
            logger.error(e.getMessage(), e);
            JsonObject error = new JsonObject();
            error.addProperty("exception", e.getMessage());
            return Response.status(500).entity(error.toString()).build();
        }
    }

    @Path("model")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response getModel(@RequestBody String input)
    {
        try {
            JsonObject json = JsonParser.parseString(input).getAsJsonObject();

            String projectId = json.get("projectId").getAsString();
            String artifactId = json.get("artifactId").getAsString();

            String model = this.projectService.getAiModel(projectId,artifactId);
            if(model == null){
                JsonObject response = new JsonObject();
                response.addProperty("message","AI_MODEL_NOT_FOUND");
                return Response.status(404).entity(response.toString()).build();
            }

            Response response = Response.ok(model).build();
            return response;
        }
        catch(Exception e)
        {
            logger.error(e.getMessage(), e);
            JsonObject error = new JsonObject();
            error.addProperty("exception", e.getMessage());
            return Response.status(500).entity(error.toString()).build();
        }
    }

    @Path("storeModel")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response storeModel(@RequestBody String input)
    {
        try {
            JsonObject json = JsonParser.parseString(input).getAsJsonObject();

            String projectId = null;
            if(json.has("projectId")){
                projectId = json.get("projectId").getAsString();
            }

            String artifactId = null;
            if(json.has("artifactId")){
                artifactId = json.get("artifactId").getAsString();
            }

            JsonObject modelJson = null;
            if(json.has("modelPackage")){
                modelJson = json.get("modelPackage").getAsJsonObject();
            }

            if(projectId == null || artifactId == null || modelJson == null){
                JsonObject error = new JsonObject();
                if(projectId == null)
                {
                    error.addProperty("project_id_missing","project_id_missing");
                }
                if(artifactId == null){
                    error.addProperty("artifact_id_missing","artifact_id_missing");
                }
                if(modelJson == null){
                    error.addProperty("model_package_missing","model_package_missing");
                }
                return Response.status(403).entity(error.toString()).build();
            }

            String modelName = null;
            if(modelJson.has("name")){
                modelName = modelJson.get("name").getAsString();
            }

            String language = null;
            if(modelJson.has("language")){
                language = modelJson.get("language").getAsString();
            }

            String modelString = null;
            if(modelJson.has("model")){
                modelString = modelJson.get("model").getAsString();
            }

            //Validate
            if(modelName == null || language == null || modelString == null){
                JsonObject error = new JsonObject();
                if(modelName == null)
                {
                    error.addProperty("model_name_missing","model_name_missing");
                }
                if(language == null){
                    error.addProperty("language_missing","language_missing");
                }
                if(modelString == null){
                    error.addProperty("model_missing","model_missing");
                }
                return Response.status(403).entity(error.toString()).build();
            }

            JsonObject modelMetaDataJson = this.projectService.storeAiModel(projectId,artifactId,
                    modelName,language,modelString);

            Response response = Response.ok(modelMetaDataJson.toString()).build();
            return response;
        }
        catch(ArtifactNotFoundException artifactNotFoundException){
            JsonObject error = new JsonObject();
            error.addProperty("message", "ARTIFACT_NOT_FOUND");
            return Response.status(404).entity(error.toString()).build();
        }
        catch(Exception e)
        {
            logger.error(e.getMessage(), e);
            JsonObject error = new JsonObject();
            error.addProperty("exception", e.getMessage());
            return Response.status(500).entity(error.toString()).build();
        }
    }

}
