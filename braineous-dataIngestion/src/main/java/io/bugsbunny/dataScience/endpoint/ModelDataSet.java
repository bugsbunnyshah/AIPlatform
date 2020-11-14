package io.bugsbunny.dataScience.endpoint;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.bugsbunny.dataScience.service.ModelDataSetService;
import io.bugsbunny.infrastructure.MongoDBJsonStore;
import io.bugsbunny.preprocess.SecurityTokenContainer;
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

    @Inject
    private ModelDataSetService modelDataSetService;

    @Inject
    private SecurityTokenContainer securityTokenContainer;

    @Path("storeTrainingDataSet")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response storeTrainingDataSet(@RequestBody String input)
    {
        try {
            JsonObject dataSetJson = JsonParser.parseString(input).getAsJsonObject();
            long dataSetId = this.modelDataSetService.storeTrainingDataSet(dataSetJson);

            JsonObject returnValue = new JsonObject();
            returnValue.addProperty("dataSetId", dataSetId);
            Response response = Response.ok(returnValue.toString()).build();
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

    @Path("storeEvalDataSet")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response storeEvalDataSet(@RequestBody String input)
    {
        try {
            JsonObject dataSetJson = JsonParser.parseString(input).getAsJsonObject();
            long dataSetId = this.modelDataSetService.storeEvalDataSet(dataSetJson);

            JsonObject returnValue = new JsonObject();
            returnValue.addProperty("dataSetId", dataSetId);
            Response response = Response.ok(returnValue.toString()).build();
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

    @Path("readDataSet")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response readDataSet(@QueryParam("dataSetId") long dataSetId)
    {
        try {
            JsonObject jsonInput = this.modelDataSetService.readDataSet(dataSetId);

            Response response = null;
            if(jsonInput != null) {
                response = Response.ok(jsonInput.toString()).build();
            }
            else
            {
                JsonObject error = new JsonObject();
                error.addProperty("dataSetId", dataSetId+": NOT_FOUND");
                response = Response.status(404).entity(error.toString()).build();
            }
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
