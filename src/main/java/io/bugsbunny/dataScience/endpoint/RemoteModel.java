package io.bugsbunny.dataScience.endpoint;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.bugsbunny.dataScience.service.AIModelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;

import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("remoteModel")
public class RemoteModel
{
    private static Logger logger = LoggerFactory.getLogger(RemoteModel.class);

    @Path("/mlflow/invocations")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response mlFlow(@RequestBody String input)
    {
        JsonObject returnValue = new JsonObject();
        Response response = Response.ok(returnValue.toString()).build();
        return response;
    }
}
