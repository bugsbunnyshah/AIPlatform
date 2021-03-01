package io.bugsbunny;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.bugsbunny.configuration.AIPlatformConfig;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.File;
import java.io.FileInputStream;
import java.lang.annotation.Annotation;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Path("/microservice")
public class Microservice
{
    private static Logger logger = LoggerFactory.getLogger(Microservice.class);

    @Inject
    private AIPlatformConfig aiPlatformConfig;

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response microservice(@RequestBody String json)
    {
        try {
            JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
            logger.info(jsonObject.toString());

            jsonObject.addProperty("product", "braineous");
            jsonObject.addProperty("oid", UUID.randomUUID().toString());
            jsonObject.addProperty("message", "HELLO_TO_HUMANITY");

            return Response.ok(jsonObject.toString()).build();
        }
        catch(Exception e)
        {
            logger.error(e.getMessage(), e);
            JsonObject error = new JsonObject();
            error.addProperty("exception", e.getMessage());
            return Response.status(500).entity(error.toString()).build();
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response microserviceGet()
    {
        try {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("product", "braineous");
            jsonObject.addProperty("oid", UUID.randomUUID().toString());
            jsonObject.addProperty("message", "HELLO_TO_HUMANITY");

            return Response.ok(jsonObject.toString()).build();
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