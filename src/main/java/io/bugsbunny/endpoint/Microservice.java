package io.bugsbunny.endpoint;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.UUID;

@Path("/microservice")
public class Microservice
{
    private static Logger logger = LoggerFactory.getLogger(Microservice.class);

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public String microservice(@RequestBody String json)
    {
        JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
        logger.info(jsonObject.toString());

        jsonObject.addProperty("oid", UUID.randomUUID().toString());
        jsonObject.addProperty("message", "HELLO_TO_HUMANITY");

        return jsonObject.toString();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String microserviceGet()
    {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("oid", UUID.randomUUID().toString());
        jsonObject.addProperty("message", "HELLO_TO_HUMANITY");

        return jsonObject.toString();
    }
}