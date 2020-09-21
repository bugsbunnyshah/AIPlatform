package io.bugsbunny.dataScience.endpoint;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.bugsbunny.dataScience.service.AIModelService;
import io.bugsbunny.dataScience.service.ModelDataSetService;
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

    @Inject
    private ModelDataSetService modelDataSetService;

    @Path("storeTrainingDataSet")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response storeTrainingDataSet(@RequestBody String input)
    {
        JsonObject jsonObject = JsonParser.parseString(input).getAsJsonObject();
        String dataFormat = jsonObject.get("format").getAsString();
        String data = jsonObject.get("data").getAsString();
        long dataSetId = this.modelDataSetService.storeTrainingDataSet(dataFormat, data);

        JsonObject returnValue = new JsonObject();
        returnValue.addProperty("dataSetId", dataSetId);
        Response response = Response.ok(returnValue.toString()).build();
        return response;
    }

    @Path("storeEvalDataSet")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response storeEvalDataSet(@RequestBody String input)
    {
        JsonObject jsonObject = JsonParser.parseString(input).getAsJsonObject();
        String dataFormat = jsonObject.get("format").getAsString();
        String data = jsonObject.get("data").getAsString();
        long modelId = jsonObject.get("modelId").getAsLong();
        long dataSetId = this.modelDataSetService.storeEvalDataSet(modelId, dataFormat, data);

        JsonObject returnValue = new JsonObject();
        returnValue.addProperty("dataSetId", dataSetId);
        Response response = Response.ok(returnValue.toString()).build();
        return response;
    }

    @Path("readDataSet")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response readDataSet(@QueryParam("dataSetId") long dataSetId)
    {
        JsonObject jsonInput = this.modelDataSetService.readDataSet(dataSetId);
        Response response = Response.ok(jsonInput.toString()).build();
        return response;
    }
}
