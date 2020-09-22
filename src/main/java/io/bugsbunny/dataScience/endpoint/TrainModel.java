package io.bugsbunny.dataScience.endpoint;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.bugsbunny.dataScience.service.AIModelService;
import io.bugsbunny.dataScience.service.TrainingAIModelService;
import jep.MainInterpreter;
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

@Path("trainModel")
public class TrainModel
{
    private static Logger logger = LoggerFactory.getLogger(TrainModel.class);

    static
    {
        try
        {
            MainInterpreter.setJepLibraryPath("/Users/babyboy/opt/anaconda3/lib/python3.8/site-packages/jep/jep.cpython-38-darwin.so");
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    @Inject
    private TrainingAIModelService trainingAIModelService;

    @Path("evalJava")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response eval(@RequestBody String input)
    {
        JsonObject jsonInput = JsonParser.parseString(input).getAsJsonObject();
        long modelId =  jsonInput.get("modelId").getAsLong();
        long dataSetId =  jsonInput.get("dataSetId").getAsLong();
        String eval = this.trainingAIModelService.evalJava(modelId, dataSetId);
        Response response = Response.ok(eval).build();
        return response;
    }

    @Path("evalPython")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response python()
    {
        try
        {
            logger.info("******************");
            logger.info("TRAIN_PYTHON_MODEL");
            logger.info("******************");
            String eval = this.trainingAIModelService.evalPython(0l,0l);
            Response response = Response.ok(eval).build();
            return response;
        }
        catch(Exception e)
        {
            throw new RuntimeException(e);
        }
    }
}
