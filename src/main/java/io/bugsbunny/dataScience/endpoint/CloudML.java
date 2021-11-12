package io.bugsbunny.dataScience.endpoint;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.bugsbunny.dataScience.service.CloudMLService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("cloudml")
public class CloudML
{
    private static Logger logger = LoggerFactory.getLogger(CloudML.class);

    private static boolean isPythonDetected = false;

    @Inject
    private CloudMLService cloudMLService;


    @Path("executeScript")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response executeScript(@RequestBody String input){
        try {
            JsonObject json = JsonParser.parseString(input).getAsJsonObject();
            String script = null;
            if(json.has("script")){
               script = json.get("script").getAsString();
            }

            //Validate
            if(script == null){
                JsonObject response = new JsonObject();
                response.addProperty("missing_script","missing_script");
                return Response.status(403).entity(response.toString()).build();
            }

            this.cloudMLService.executeScript(script);

            JsonObject success = new JsonObject();
            success.addProperty("message","script_execution_success");

            return Response.ok(success.toString()).build();
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
