package io.bugsbunny.dataScience.endpoint;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.bugsbunny.dataScience.service.PackagingService;
import io.bugsbunny.persistence.MongoDBJsonStore;
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
        JsonObject result = this.packagingService.performPackaging(input);
        Response response = Response.ok(result.toString()).build();
        return response;
    }

    @Path("model")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getModel(@QueryParam("modelId") long modelId)
    {
        String result = this.packagingService.getModel(modelId);

        Response response = Response.ok(result).build();
        return response;
    }
}
