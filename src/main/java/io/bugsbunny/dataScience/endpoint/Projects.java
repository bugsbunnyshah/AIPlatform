package io.bugsbunny.dataScience.endpoint;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.bugsbunny.dataScience.model.Artifact;
import io.bugsbunny.dataScience.model.Project;
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

    @Path("createModelForTraining")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response createModelForTraining(@RequestBody String input){
        try {
            JsonObject json = JsonParser.parseString(input).getAsJsonObject();
            String scientist = json.get("scientist").getAsString();

            //TODO: Validate

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
    public Response readArtifactId(@QueryParam("projectId") String projectId, @QueryParam("artifactId") String artifactId)
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

}
