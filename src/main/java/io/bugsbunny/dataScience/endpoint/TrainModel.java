package io.bugsbunny.dataScience.endpoint;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.bugsbunny.dataScience.model.AIModel;
import io.bugsbunny.dataScience.model.Artifact;
import io.bugsbunny.dataScience.model.PortableAIModelInterface;
import io.bugsbunny.dataScience.service.*;
import io.bugsbunny.preprocess.AITrafficContainer;
import jep.Interpreter;
import jep.JepException;
import jep.MainInterpreter;
import jep.SharedInterpreter;
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
import java.util.Iterator;

@Path("trainModel")
public class TrainModel
{
    private static Logger logger = LoggerFactory.getLogger(TrainModel.class);

    private static boolean isPythonDetected = false;

    @Inject
    private AITrafficContainer aiTrafficContainer;

    @Inject
    private AIModelService trainingAIModelService;

    @Inject
    private ProjectService projectService;

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

    /*@Path("trainJava")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response trainJava(@RequestBody String input)
    {
        try {
            JsonObject jsonInput = JsonParser.parseString(input).getAsJsonObject();
            String modelId = jsonInput.get("modelId").getAsString();
            JsonArray dataSetIdArray = jsonInput.get("dataSetIds").getAsJsonArray();
            String[] dataSetIds = new String[dataSetIdArray.size()];
            Iterator<JsonElement> iterator = dataSetIdArray.iterator();
            int counter = 0;
            while(iterator.hasNext())
            {
                dataSetIds[counter] = iterator.next().getAsString();
                counter++;
            }

            Artifact artifact = new Artifact();
            artifact.setAiModel(new AIModel());
            artifact.getAiModel().setModelId(modelId);
            String eval = this.trainingAIModelService.trainJava(artifact, dataSetIds);

            JsonObject returnValue = new JsonObject();
            returnValue.add("result", JsonParser.parseString(eval));

            //TODO: use this as chain id but once concept of data history and training history
            //is created, this will have to change
            returnValue.addProperty("dataHistoryId", dataSetIds[0]);

            Response response = Response.ok(returnValue.toString()).build();
            return response;
        }
        catch(ModelNotFoundException modelNotFoundException)
        {
            logger.error(modelNotFoundException.getMessage(), modelNotFoundException);
            JsonObject error = new JsonObject();
            error.addProperty("exception", modelNotFoundException.getMessage());
            return Response.status(404).entity(error.toString()).build();
        }
        catch(ModelIsLive modelIsLive)
        {
            logger.error(modelIsLive.getMessage(), modelIsLive);
            JsonObject error = new JsonObject();
            error.addProperty("exception", modelIsLive.getMessage());
            return Response.status(422).entity(error.toString()).build();
        }
        catch(Exception e)
        {
            logger.error(e.getMessage(), e);
            JsonObject error = new JsonObject();
            error.addProperty("exception", e.getMessage());
            return Response.status(500).entity(error.toString()).build();
        }
    }*/

    @Path("trainPython")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response trainPython(@RequestBody String input)
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
            JsonObject jsonInput = JsonParser.parseString(input).getAsJsonObject();
            String modelId =  jsonInput.get("modelId").getAsString();
            JsonArray dataSetIdArray = jsonInput.get("dataSetIds").getAsJsonArray();
            String[] dataSetIds = new String[dataSetIdArray.size()];
            Iterator<JsonElement> iterator = dataSetIdArray.iterator();
            int counter = 0;
            while(iterator.hasNext())
            {
                dataSetIds[counter] = iterator.next().getAsString();
                counter++;
            }
            String eval = this.trainingAIModelService.evalPython(modelId, dataSetIds);


            JsonObject returnValue = new JsonObject();
            returnValue.add("result", JsonParser.parseString(eval));
            returnValue.addProperty("dataHistoryId", this.aiTrafficContainer.getChainId());

            Response response = Response.ok(returnValue.toString()).build();
            return response;
        }
        catch(JepException | UnsatisfiedLinkError pythonError)
        {
            logger.error(pythonError.getMessage(), pythonError);
            JsonObject error = new JsonObject();
            error.addProperty("exception", "PYTHON_RUNTIME_NOT_DETECTED");
            return Response.status(500).entity(error.toString()).build();
        }
        catch(Exception e)
        {
            logger.error(e.getMessage(), e);
            JsonObject error = new JsonObject();
            error.addProperty("exception", e.getMessage());
            return Response.status(500).entity(error.toString()).build();
        }
    }

    @Path("trainModelFromDataLake")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response trainModelFromDataLake(@RequestBody String input)
    {
        try {
            JsonObject jsonInput = JsonParser.parseString(input).getAsJsonObject();
            String projectId = null;
            if(jsonInput.has("projectId"))
            {
                projectId = jsonInput.get("projectId").getAsString();
            }
            String artifactId = null;
            if(jsonInput.has("artifactId")){
                artifactId = jsonInput.get("artifactId").getAsString();
            }
            JsonArray dataLakeIdsArray = null;
            if(jsonInput.has("dataLakeIds")){
                dataLakeIdsArray = jsonInput.get("dataLakeIds").getAsJsonArray();
            }

            if(projectId == null || artifactId == null || dataLakeIdsArray == null){
                JsonObject response = new JsonObject();
                if(projectId == null){
                    response.addProperty("project_id_missing","project_id_missing");
                }
                if(artifactId == null){
                    response.addProperty("artifact_id_missing","artifact_id_missing");
                }
                if(dataLakeIdsArray == null){
                    response.addProperty("data_missing","data_missing");
                }
                return Response.status(403).entity(response.toString()).build();
            }

            String[] dataLakeIds = new String[dataLakeIdsArray.size()];
            Iterator<JsonElement> iterator = dataLakeIdsArray.iterator();
            int counter = 0;
            while(iterator.hasNext())
            {
                dataLakeIds[counter] = iterator.next().getAsString();
                counter++;
            }
            JsonObject evalJson = this.projectService.trainModelFromDataLake(projectId,artifactId,dataLakeIds);

            JsonObject returnValue = new JsonObject();
            returnValue.add("result", evalJson);

            //TODO: use this as chain id but once concept of data history and training history
            //is created, this will have to change
            returnValue.addProperty("dataHistoryId", dataLakeIds[0]);

            Response response = Response.ok(returnValue.toString()).build();
            return response;
        }
        catch(ModelNotFoundException modelNotFoundException)
        {
            logger.error(modelNotFoundException.getMessage(), modelNotFoundException);
            JsonObject error = new JsonObject();
            error.addProperty("exception", modelNotFoundException.getMessage());
            return Response.status(404).entity(error.toString()).build();
        }
        catch(ModelIsLive modelIsLive)
        {
            logger.error(modelIsLive.getMessage(), modelIsLive);
            JsonObject error = new JsonObject();
            error.addProperty("exception", modelIsLive.getMessage());
            return Response.status(422).entity(error.toString()).build();
        }
        catch(ArtifactNotFoundException artifactNotFoundException){
            JsonObject error = new JsonObject();
            error.addProperty("message", "ARTIFACT_NOT_FOUND");
            return Response.status(404).entity(error.toString()).build();
        }
        catch(DataNotFoundException dataNotFoundException){
            JsonObject error = new JsonObject();
            error.addProperty("message", "DATA_NOT_FOUND");
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

    @Path("trainModelFromDataSet")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response trainModelFromDataSet(@RequestBody String input)
    {
        try {
            JsonObject jsonInput = JsonParser.parseString(input).getAsJsonObject();
            String projectId = null;
            if(jsonInput.has("projectId"))
            {
                projectId = jsonInput.get("projectId").getAsString();
            }
            String artifactId = null;
            if(jsonInput.has("artifactId")){
                artifactId = jsonInput.get("artifactId").getAsString();
            }
            JsonArray dataSetIdsArray = null;
            if(jsonInput.has("dataSetIds")){
                dataSetIdsArray = jsonInput.get("dataSetIds").getAsJsonArray();
            }

            if(projectId == null || artifactId == null || dataSetIdsArray == null){
                JsonObject response = new JsonObject();
                if(projectId == null){
                    response.addProperty("project_id_missing","project_id_missing");
                }
                if(artifactId == null){
                    response.addProperty("artifact_id_missing","artifact_id_missing");
                }
                if(dataSetIdsArray == null){
                    response.addProperty("data_missing","data_missing");
                }
                return Response.status(403).entity(response.toString()).build();
            }

            String[] dataSetIds = new String[dataSetIdsArray.size()];
            Iterator<JsonElement> iterator = dataSetIdsArray.iterator();
            int counter = 0;
            while(iterator.hasNext())
            {
                dataSetIds[counter] = iterator.next().getAsString();
                counter++;
            }
            JsonObject evalJson = this.projectService.trainModelFromDataSet(projectId,artifactId,dataSetIds);

            JsonObject returnValue = new JsonObject();
            returnValue.add("result", evalJson);

            //TODO: use this as chain id but once concept of data history and training history
            //is created, this will have to change
            //returnValue.addProperty("dataHistoryId", dataLakeIds[0]);

            Response response = Response.ok(returnValue.toString()).build();
            return response;
        }
        catch(ModelNotFoundException modelNotFoundException)
        {
            logger.error(modelNotFoundException.getMessage(), modelNotFoundException);
            JsonObject error = new JsonObject();
            error.addProperty("exception", modelNotFoundException.getMessage());
            return Response.status(404).entity(error.toString()).build();
        }
        catch(ModelIsLive modelIsLive)
        {
            logger.error(modelIsLive.getMessage(), modelIsLive);
            JsonObject error = new JsonObject();
            error.addProperty("exception", modelIsLive.getMessage());
            return Response.status(422).entity(error.toString()).build();
        }
        catch(ArtifactNotFoundException artifactNotFoundException){
            JsonObject error = new JsonObject();
            error.addProperty("message", "ARTIFACT_NOT_FOUND");
            return Response.status(404).entity(error.toString()).build();
        }
        catch(DataNotFoundException dataNotFoundException){
            JsonObject error = new JsonObject();
            error.addProperty("message", "DATA_NOT_FOUND");
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
}
