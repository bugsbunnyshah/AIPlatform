package io.bugsbunny.restClient;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.bugsbunny.dataScience.model.DataBricksProcessException;
import io.bugsbunny.persistence.MongoDBJsonStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;

@ApplicationScoped
public class DataBricksClient
{
    private static Logger logger = LoggerFactory.getLogger(DataBricksClient.class);

    @Inject
    private MongoDBJsonStore mongoDBJsonStore;

    /*public String createDevExperiment(String experiment) throws DataBricksProcessException
    {
        String experimentId = null;
        try
        {
            //Create the Experiment
            HttpClient httpClient = HttpClient.newBuilder().build();
            String restUrl = "http://127.0.0.1:5000/api/2.0/mlflow/experiments/create";

            JsonObject json = new JsonObject();
            json.addProperty("name", experiment);
            HttpRequest.Builder httpRequestBuilder = HttpRequest.newBuilder();
            HttpRequest httpRequest = httpRequestBuilder.uri(new URI(restUrl))
                    .POST(HttpRequest.BodyPublishers.ofString(json.toString()))
                    .build();


            HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            String responseJson = httpResponse.body();
            int status = httpResponse.statusCode();
            if(status != 200)
            {
                throw new DataBricksProcessException("CREATE_EXPERIMENT_FAIL: "+httpResponse.toString());
            }

            //Create Experiment Meta Data
            JsonObject responseJsonObject = JsonParser.parseString(responseJson).getAsJsonObject();
            experimentId = responseJsonObject.get("experiment_id").getAsString();
            json = new JsonObject();
            json.addProperty("experiment_id", experimentId);
            json.addProperty("key", "deploymentStatus");
            json.addProperty("value", "DEV");
            restUrl = "http://127.0.0.1:5000/api/2.0/mlflow/experiments/set-experiment-tag";
            httpRequest = httpRequestBuilder.uri(new URI(restUrl))
                    .POST(HttpRequest.BodyPublishers.ofString(json.toString()))
                    .build();
            httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            if(status != 200)
            {
                throw new DataBricksProcessException("EXPERIMENT_TAGGING_FAIL: "+httpResponse.toString());
            }

            return experimentId;
        }
        catch(Exception e)
        {
            throw new DataBricksProcessException(e);
        }
    }

    public JsonObject getExperiments() throws DataBricksProcessException
    {
        try
        {
            HttpClient httpClient = HttpClient.newBuilder().build();
            String restUrl = "http://127.0.0.1:5000/api/2.0/mlflow/experiments/list";

            HttpRequest.Builder httpRequestBuilder = HttpRequest.newBuilder();
            HttpRequest httpRequest = httpRequestBuilder.uri(new URI(restUrl))
                    .GET()
                    .build();


            HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            String responseJson = httpResponse.body();
            int status = httpResponse.statusCode();
            if(status != 200)
            {
                throw new DataBricksProcessException("GET_EXPERIMENTS_FAIL: "+httpResponse.toString());
            }

            return JsonParser.parseString(responseJson).getAsJsonObject();
        }
        catch(Exception e)
        {
            throw new DataBricksProcessException(e);
        }
    }

    public void logModel(String runId, String modelJson, String model) throws DataBricksProcessException
    {
        try
        {
            HttpClient httpClient = HttpClient.newBuilder().build();
            String restUrl = "http://127.0.0.1:5000/api/2.0/mlflow/runs/log-model";

            JsonObject json = new JsonObject();
            json.addProperty("run_id", runId);
            json.addProperty("model_json", modelJson);
            HttpRequest.Builder httpRequestBuilder = HttpRequest.newBuilder();
            HttpRequest httpRequest = httpRequestBuilder.uri(new URI(restUrl))
                    .POST(HttpRequest.BodyPublishers.ofString(json.toString()))
                    .build();


            HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            int status = httpResponse.statusCode();
            if(status != 200)
            {
                throw new DataBricksProcessException("LOG_MODEL_FAIL: "+httpResponse.toString());
            }

            json.addProperty("model", model);
            this.mongoDBJsonStore.storeLiveModel(json);
        }
        catch(Exception e)
        {
            throw new DataBricksProcessException(e);
        }
    }

    public String createRun(String experimentId) throws DataBricksProcessException
    {
        String runId = null;
        try
        {
            HttpClient httpClient = HttpClient.newBuilder().build();
            String restUrl = "http://127.0.0.1:5000/api/2.0/mlflow/runs/create";

            JsonObject json = new JsonObject();
            long startTime = Instant.now().toEpochMilli();
            json.addProperty("experiment_id", experimentId);
            json.addProperty("start_time", startTime);
            HttpRequest.Builder httpRequestBuilder = HttpRequest.newBuilder();
            HttpRequest httpRequest = httpRequestBuilder.uri(new URI(restUrl))
                    .POST(HttpRequest.BodyPublishers.ofString(json.toString()))
                    .build();

            HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            int status = httpResponse.statusCode();
            if(status != 200)
            {
                throw new DataBricksProcessException("CREATE_RUN_FAIL: "+httpResponse.toString());
            }

            JsonObject jsonObject = JsonParser.parseString(httpResponse.body()).getAsJsonObject();
            runId = jsonObject.get("run").getAsJsonObject().get("info").getAsJsonObject().get("run_id").getAsString();

            return runId;
        }
        catch(Exception e)
        {
            throw new DataBricksProcessException(e);
        }
    }*/

    public JsonElement invokeDatabricksModel(JsonObject payload, JsonObject modelPackage) throws DataBricksProcessException
    {
        try
        {
            //Create the Experiment
            HttpClient httpClient = HttpClient.newBuilder().build();
            String url = modelPackage.get("url").getAsString();
            String restUrl = url;

            HttpRequest.Builder httpRequestBuilder = HttpRequest.newBuilder();
            HttpRequest httpRequest = httpRequestBuilder.uri(new URI(restUrl))
                    .header("Content-Type", "application/json; format=pandas-split")
                    .POST(HttpRequest.BodyPublishers.ofString(payload.toString()))
                    .build();


            HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            String responseJson = httpResponse.body();
            int status = httpResponse.statusCode();
            if(status != 200)
            {
                throw new DataBricksProcessException("DATABRICKS_MODEL_INVOCATION_FAILED: "+responseJson);
            }

            return JsonParser.parseString(responseJson);
        }
        catch(Exception e)
        {
            throw new DataBricksProcessException(e);
        }
    }
}