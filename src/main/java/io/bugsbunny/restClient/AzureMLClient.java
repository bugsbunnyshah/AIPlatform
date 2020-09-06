package io.bugsbunny.restClient;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import java.net.URI;
import java.net.URLEncoder;
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

    public JsonObject search(String query)
    {
        HttpClient httpClient = HttpClient.newBuilder().build();
        try
        {
            query = URLEncoder.encode(query,"UTF-8");
            String requestUrl = "https://appgalentitysearch.cognitiveservices.azure.com/bing/v7.0/entities/?mkt=en-us&count=10&offset=0&safesearch=Moderate&q="+query;

            HttpRequest.Builder httpRequestBuilder = HttpRequest.newBuilder();
            HttpRequest httpRequest = httpRequestBuilder.uri(new URI(requestUrl))
                    .setHeader("Content-Type", "application/json")
                    .setHeader("Ocp-Apim-Subscription-Key","42f903592a524b86949d9324e02a99ce")
                    .GET()
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