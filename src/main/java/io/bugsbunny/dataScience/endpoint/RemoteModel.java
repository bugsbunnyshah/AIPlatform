package io.bugsbunny.dataScience.endpoint;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.bugsbunny.dataScience.service.ModelDataSetService;
import io.bugsbunny.restClient.DataBricksClient;

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

    @Inject
    private DataBricksClient dataBricksClient;


    @Inject
    private ModelDataSetService modelDataSetService;

    @Path("/mlflow/invocations")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response mlFlow(@RequestBody String input)
    {
        try
        {
            //TODO
            /*JsonObject request = JsonParser.parseString(input).getAsJsonObject();
            String modelId = request.get("modelId").getAsString();
            JsonObject remoteModelPackage = this.packagingService.getModelPackage(modelId);

            JsonObject data = request.get("payload").getAsJsonObject();
            JsonElement result = this.dataBricksClient.invokeDatabricksModel(data,
                    remoteModelPackage);

            JsonObject jsonObject = JsonParser.parseString(input).getAsJsonObject();
            String dataFormat = "remoteModelFormat";
            jsonObject.addProperty("format", dataFormat);
            this.modelDataSetService.storeEvalDataSet(jsonObject);

            Response response = Response.ok(result.toString()).build();
            return response;*/
            return null;
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
