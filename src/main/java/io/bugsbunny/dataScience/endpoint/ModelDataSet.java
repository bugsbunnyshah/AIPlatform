package io.bugsbunny.dataScience.endpoint;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.bugsbunny.dataScience.service.AIModelService;
import io.bugsbunny.persistence.MongoDBJsonStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("dataset")
public class ModelDataSet
{
    private static Logger logger = LoggerFactory.getLogger(ModelDataSet.class);

    @Inject
    private MongoDBJsonStore mongoDBJsonStore;

    @Path("storeTrainingDataSet")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response storeTrainingDataSet(@RequestBody String input)
    {
        //TODO: FINISH_IMPL
        Response response = Response.ok("{}").build();
        return response;
    }

    @Path("storeEvalDataSet")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response storeEvalDataSet(@RequestBody String input)
    {
        //TODO: FINISH_IMPL
        Response response = Response.ok("{}").build();
        return response;
    }

    @Path("readForEval")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response readForEval(@QueryParam("dataSetId") long dataSetId)
    {
        //TODO: FINISH_IMPL
        JsonObject jsonInput = this.mongoDBJsonStore.readDataSet(dataSetId);
        Response response = Response.ok(jsonInput.toString()).build();
        return response;
    }
}
