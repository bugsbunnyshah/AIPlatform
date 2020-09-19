package io.bugsbunny.dataScience.endpoint;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.bugsbunny.dataScience.service.AIModelService;
import io.bugsbunny.persistence.MongoDBJsonStore;
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

@Path("dataset")
public class ModelDataSet
{
    private static Logger logger = LoggerFactory.getLogger(ModelDataSet.class);

    @Inject
    private MongoDBJsonStore mongoDBJsonStore;

    @Path("readForEval")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response readForEval()
    {
        //TODO: FINISH_IMPL
        JsonObject jsonInput = this.mongoDBJsonStore.readDataSet(-3077535194297214256l);
        Response response = Response.ok(jsonInput.toString()).build();
        return response;
    }
}
