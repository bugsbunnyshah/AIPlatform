package io.bugsbunny.endpoint;

import com.google.gson.JsonObject;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.UUID;

@Path("/microservice")
public class Microservice {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String hello()
    {
        System.out.println("BLAH");

        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("oid", UUID.randomUUID().toString());
        jsonObject.addProperty("message", "HELLO_TO_HUMANITY");

        return jsonObject.toString();
    }
}