package io.bugsbunny.dataScience.endpoint;

import com.google.gson.JsonObject;
import io.bugsbunny.dataScience.service.PackagingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("aimodel")
public class AIModelPackaging
{
    private static Logger logger = LoggerFactory.getLogger(AIModelPackaging.class);

    @Inject
    private PackagingService packagingService;

    @Path("performPackaging")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response performPackaging(@RequestBody String input)
    {
        try {
            JsonObject result = this.packagingService.performPackaging(input);
            Response response = Response.ok(result.toString()).build();
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
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getModel(@QueryParam("modelId") String modelId)
    {
        try {
            String result = this.packagingService.getModel(modelId);

            Response response = Response.ok(result).build();
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
