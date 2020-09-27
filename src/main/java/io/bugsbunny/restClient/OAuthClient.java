package io.bugsbunny.restClient;

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

@ApplicationScoped
public class OAuthClient
{
    private static Logger logger = LoggerFactory.getLogger(OAuthClient.class);

    public JsonObject getAccessToken(String clientId, String clientSecret) throws DataBricksProcessException
    {
        try
        {
            //Create the Experiment
            HttpClient httpClient = HttpClient.newBuilder().build();
            String restUrl = "https://appgallabs.us.auth0.com/oauth/token/";

            JsonObject payload = new JsonObject();
            payload.addProperty("client_id", clientId);
            payload.addProperty("client_secret", clientSecret);
            payload.addProperty("audience", "https://appgallabs.us.auth0.com/api/v2/");
            payload.addProperty("grant_type", "client_credentials");
            HttpRequest.Builder httpRequestBuilder = HttpRequest.newBuilder();
            HttpRequest httpRequest = httpRequestBuilder.uri(new URI(restUrl))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(payload.toString()))
                    .build();


            HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            String responseJson = httpResponse.body();
            int status = httpResponse.statusCode();
            if(status != 200)
            {
                throw new DataBricksProcessException("OAUTH_AUTHENTICATION_FAILURE: "+responseJson);
            }

            return JsonParser.parseString(responseJson).getAsJsonObject();
        }
        catch(Exception e)
        {
            throw new DataBricksProcessException(e);
        }
    }
}