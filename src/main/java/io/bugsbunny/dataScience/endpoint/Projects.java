package io.bugsbunny.dataScience.endpoint;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.bugsbunny.dataScience.model.Project;
import io.bugsbunny.dataScience.service.AIModelService;
import io.bugsbunny.dataScience.service.ModelIsLive;
import io.bugsbunny.dataScience.service.ModelNotFoundException;
import io.bugsbunny.dataScience.service.ProjectService;
import io.bugsbunny.preprocess.AITrafficContainer;
import jep.Interpreter;
import jep.JepException;
import jep.MainInterpreter;
import jep.SharedInterpreter;
import org.eclipse.microprofile.config.ConfigProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

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

}
