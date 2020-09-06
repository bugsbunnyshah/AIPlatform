package io.bugsbunny.restClient;

import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@ApplicationScoped
public class DataBricksClient
{
    private static Logger logger = LoggerFactory.getLogger(DataBricksClient.class);

    public void createExperiment()
    {
        HttpClient httpClient = HttpClient.newBuilder().build();
        try
        {
            String name = "appgal0";
            //name = URLEncoder.encode(name,"UTF-8");
            String requestUrl = "http://localhost:5000/mlflow/experiments/list/";
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("name",name);
            String inputJson = jsonObject.toString();

            HttpRequest.Builder httpRequestBuilder = HttpRequest.newBuilder();
            HttpRequest httpRequest = httpRequestBuilder.uri(new URI(requestUrl))
                    //.setHeader("Content-Type", "application/json")
                    //.setHeader("Ocp-Apim-Subscription-Key","42f903592a524b86949d9324e02a99ce")
                    //.POST(HttpRequest.BodyPublishers.ofString(inputJson))
                    .GET()
                    .build();
            HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            System.out.println("**************************************************************************");
            System.out.println(""+httpResponse.statusCode());
            System.out.println(httpResponse.body());
            System.out.println("**************************************************************************");
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
    }
}