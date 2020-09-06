package io.bugsbunny.dataScience.endpoint;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.bugsbunny.dataScience.service.TensorFlowTrainingWorkflow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("trainModel")
public class TrainModel {
    private Logger logger = LoggerFactory.getLogger(LiveModel.class);

    @Inject
    private TensorFlowTrainingWorkflow tensorFlowTrainingWorkflow;

    @Path("train")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response train(@RequestBody String trainingInput) throws Exception
    {
        try {
            JsonObject inputJson = JsonParser.parseString(trainingInput).getAsJsonObject();
            String runId = tensorFlowTrainingWorkflow.startTraining(inputJson);

            JsonObject result = new JsonObject();
            result.addProperty("mlPlatform", inputJson.get("mlPlatform").getAsString());
            result.addProperty("runId", runId);
            result.addProperty("success", 200);
            return Response.ok(result.toString()).build();
        }
        catch(Exception e)
        {
            logger.error(e.getMessage(), e);
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("error", e.getMessage());
            return Response.status(500).entity(jsonObject.toString()).build();
        }
    }

    @Path("data/{runId}")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response getData(@PathParam("runId") String runId) throws Exception
    {
        String message = "I_USED_TO_THINK_I_AM_GOD_BUT_I_KNOW_NOW_I_DONT_BELONG_HERE_I_AM_NOT_HIP_ENOUGH_FOR_APP_GAL_AKA_MOTHER_EARTH";
        String json = "{message=\"I_USED_TO_THINK_I_AM_GOD_BUT_I_KNOW_NOW_I_DONT_BELONG_HERE_I_AM_NOT_HIP_ENOUGH_FOR_APP_GAL_AKA_MOTHER_EARTH\"}";
        JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
        boolean flag = true;
        while(flag)
        {
            logger.info("****************");
            //logger.info("I_USED_TO_THINK_I_AM_GOD_BUT_I_KNOW_NOW_I_DONT_BELONG_HERE_I_AM_NOT_HIP_ENOUGH_FOR_APP_GAL_AKA_MOTHER_EARTH");
            logger.info(jsonObject.toString());
            logger.info("****************");
        }
        
        String data = this.tensorFlowTrainingWorkflow.getData(runId);
        if(data == null)
        {
            return Response.status(404).build();
        }
        return Response.ok(data).build();
    }
}
