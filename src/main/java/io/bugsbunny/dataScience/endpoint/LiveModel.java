package io.bugsbunny.dataScience.endpoint;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.bugsbunny.dataScience.service.AIModelService;

import io.bugsbunny.persistence.MongoDBJsonStore;
import jep.Interpreter;
import jep.JepException;
import jep.MainInterpreter;
import jep.SharedInterpreter;
import org.eclipse.microprofile.config.ConfigProvider;
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
import java.io.File;
import java.util.Iterator;

@Path("liveModel")
public class LiveModel
{
    private static Logger logger = LoggerFactory.getLogger(LiveModel.class);

    private static boolean isPythonDetected = false;

    static
    {
        try
        {
            String jepLibraryPath = ConfigProvider.getConfig().getValue("jepLibraryPath", String.class);
            File file = new File(jepLibraryPath);
            isPythonDetected = file.exists();
            if(isPythonDetected) {
                MainInterpreter.setJepLibraryPath(jepLibraryPath);

                String pythonScript = "print('PYTHON_LOADED')";
                try (Interpreter interp = new SharedInterpreter())
                {
                    interp.exec(pythonScript);
                }
            }
        }
        catch (Exception | UnsatisfiedLinkError e)
        {
            isPythonDetected = false;
            logger.info("*******************************************");
            logger.info("PYTHON_RUNTIME_WAS_NOT_DETECTED");
            logger.info("PYTHON_AIMODELS_CANNOT_BE_SUPPORTED_FOR_NOW");
            logger.info("*******************************************");
        }
    }

    @Inject
    private AIModelService aiModelService;

    @Inject
    private MongoDBJsonStore mongoDBJsonStore;

    @Path("evalJava")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response evalJava(@RequestBody String input)
    {
        try {
            JsonObject jsonInput = JsonParser.parseString(input).getAsJsonObject();
            long modelId = jsonInput.get("modelId").getAsLong();
            JsonArray dataSetIdArray = jsonInput.get("dataSetIds").getAsJsonArray();
            long[] dataSetIds = new long[dataSetIdArray.size()];
            Iterator<JsonElement> iterator = dataSetIdArray.iterator();
            int counter = 0;
            while(iterator.hasNext())
            {
                dataSetIds[counter] = iterator.next().getAsLong();
                counter++;
            }
            String eval = this.aiModelService.evalJava(modelId, dataSetIds);
            Response response = Response.ok(eval).build();
            return response;
        }
        catch(ModelNotFoundException modelNotFoundException)
        {
            logger.error(modelNotFoundException.getMessage(), modelNotFoundException);
            JsonObject error = new JsonObject();
            error.addProperty("exception", modelNotFoundException.getMessage());
            return Response.status(404).entity(error.toString()).build();
        }
        catch(ModelIsNotLive modelIsNotLive)
        {
            logger.error(modelIsNotLive.getMessage(), modelIsNotLive);
            JsonObject error = new JsonObject();
            error.addProperty("exception", modelIsNotLive.getMessage());
            return Response.status(422).entity(error.toString()).build();
        }
        catch(Exception e)
        {
            logger.error(e.getMessage(), e);
            JsonObject error = new JsonObject();
            error.addProperty("exception", e.getMessage());
            return Response.status(500).entity(error.toString()).build();
        }
    }

    @Path("deployJavaModel")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response deployJavaModel(@RequestBody String input)
    {
        try {
            JsonObject jsonInput = JsonParser.parseString(input).getAsJsonObject();
            long modelId = jsonInput.get("modelId").getAsLong();

            this.aiModelService.deployModel(modelId);

            JsonObject modelPackage = this.mongoDBJsonStore.getModelPackage(modelId);
            modelPackage.remove("_id");
            modelPackage.remove("model");

            JsonObject result = new JsonObject();
            result.addProperty("success", true);
            result.addProperty("liveModelId", modelId);
            Response response = Response.ok(modelPackage.toString()).build();
            return response;
        }
        catch(ModelNotFoundException modelNotFoundException)
        {
            logger.error(modelNotFoundException.getMessage(), modelNotFoundException);
            JsonObject error = new JsonObject();
            error.addProperty("exception", modelNotFoundException.getMessage());
            return Response.status(404).entity(error.toString()).build();
        }
        catch(Exception e)
        {
            logger.error(e.getMessage(), e);
            JsonObject error = new JsonObject();
            error.addProperty("exception", e.getMessage());
            return Response.status(500).entity(error.toString()).build();
        }
    }

    @Path("evalPython")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response evalPython(@RequestBody String input)
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
            logger.info("EVAL_PYTHON_MODEL");
            logger.info("******************");
            JsonObject jsonInput = JsonParser.parseString(input).getAsJsonObject();
            long modelId =  jsonInput.get("modelId").getAsLong();
            JsonArray dataSetIdArray = jsonInput.get("dataSetIds").getAsJsonArray();
            long[] dataSetIds = new long[dataSetIdArray.size()];
            Iterator<JsonElement> iterator = dataSetIdArray.iterator();
            int counter = 0;
            while(iterator.hasNext())
            {
                dataSetIds[counter] = iterator.next().getAsLong();
                counter++;
            }
            String output = this.aiModelService.evalPython(modelId, dataSetIds);

            JsonObject result = new JsonObject();
            result.addProperty("output", output);
            Response response = Response.ok(result.toString()).build();
            return response;
        }
        catch(JepException|UnsatisfiedLinkError jepError)
        {
            logger.error(jepError.getMessage(), jepError);
            JsonObject error = new JsonObject();
            error.addProperty("exception", jepError.getMessage());
            return Response.status(500).entity(error.toString()).build();
        }
    }
}
