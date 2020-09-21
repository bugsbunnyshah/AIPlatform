package io.bugsbunny.restClient;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;


/**
 *
 * @author babyboy
 */
@ApplicationScoped
public class AzureMLClient
{
    private static Logger logger = LoggerFactory.getLogger(AzureMLClient.class);

    public JsonObject invokeAzureModel()
    {
        HttpClient httpClient = HttpClient.newBuilder().build();
        try
        {
            String requestUrl = "https://aiplatform.cognitiveservices.azure.com/vision/v3.0/analyze?visualFeatures=Categories,Description&details=Landmarks";

            JsonObject json = new JsonObject();
            json.addProperty("url","https://upload.wikimedia.org/wikipedia/commons/3/3c/Shaki_waterfall.jpg");
            HttpRequest.Builder httpRequestBuilder = HttpRequest.newBuilder();
            HttpRequest httpRequest = httpRequestBuilder.uri(new URI(requestUrl))
                    .setHeader("Content-Type", "application/json")
                    .setHeader("Ocp-Apim-Subscription-Key","33ac0f0aaf754d31a12163668944dfb6")
                    .POST(HttpRequest.BodyPublishers.ofString(json.toString()))
                    .build();
            HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            JsonObject jsonObject = JsonParser.parseString(httpResponse.body()).getAsJsonObject();
            return jsonObject;
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(),e);
            throw new RuntimeException(e);
        }
    }
}