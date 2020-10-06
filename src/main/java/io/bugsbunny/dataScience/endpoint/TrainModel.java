package io.bugsbunny.dataScience.endpoint;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.bugsbunny.dataScience.service.AIModelService;
import jep.JepException;
import jep.MainInterpreter;
import org.eclipse.microprofile.config.ConfigProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;

import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;

@Path("trainModel")
public class TrainModel
{
    private static Logger logger = LoggerFactory.getLogger(TrainModel.class);

    private static boolean isPythonDetected = false;

    static
    {
        try
        {
            String jepLibraryPath = ConfigProvider.getConfig().getValue("jepLibraryPath", String.class);
            File file = new File(jepLibraryPath);
            TrainModel.isPythonDetected = file.exists();
            if(TrainModel.isPythonDetected) {
                MainInterpreter.setJepLibraryPath(jepLibraryPath);
            }
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    @Inject
    private AIModelService trainingAIModelService;

    @Path("trainJava")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response eval(@RequestBody String input)
    {
        JsonObject jsonInput = JsonParser.parseString(input).getAsJsonObject();
        long modelId =  jsonInput.get("modelId").getAsLong();
        long dataSetId =  jsonInput.get("dataSetId").getAsLong();
        String eval = this.trainingAIModelService.trainJava(modelId, dataSetId);
        Response response = Response.ok(eval).build();
        return response;
    }

    @Path("trainPython")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response python(@RequestBody String input)
    {
        try
        {

            if(!isPythonDetected)
            {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("message", "PYTHON_RUNTIME_NOT_DETECTED");
                return Response.status(404).entity(jsonObject.toString()).build();
            }

            logger.info("******************");
            logger.info("TRAIN_PYTHON_MODEL");
            logger.info("******************");
            JsonObject inputJson = JsonParser.parseString(input).getAsJsonObject();
            JsonObject jsonInput = JsonParser.parseString(input).getAsJsonObject();
            long modelId =  jsonInput.get("modelId").getAsLong();
            long dataSetId =  jsonInput.get("dataSetId").getAsLong();
            String output = this.trainingAIModelService.evalPython(modelId, dataSetId);


            JsonObject result = new JsonObject();
            result.addProperty("output", output);
            Response response = Response.ok(result.toString()).build();
            return response;
        }
        catch(JepException |UnsatisfiedLinkError jepError)
        {
            logger.error(jepError.getMessage(), jepError);
            return Response.serverError().build();
        }
    }
}
