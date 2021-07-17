import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.IOUtils;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import java.io.IOException;
import java.net.Authenticator;
import java.net.CookieHandler;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class Temp {
    public static void main(String[] args) throws Exception
    {
        String sourceData = IOUtils.toString(Thread.currentThread().getContextClassLoader().getResourceAsStream(
                "aviation/flights0.json"),
                StandardCharsets.UTF_8);
        JsonArray jsonArray = JsonParser.parseString(sourceData).getAsJsonObject().get("data").getAsJsonArray();
        String payload = jsonArray.toString();

        JsonObject json = new JsonObject();
        json.addProperty("sourceSchema","sourceSchema");
        json.addProperty("destinationSchema","destinationSchema");
        json.addProperty("sourceData",payload);
        //System.out.println(json);

        //Create the Experiment
        HttpClient httpClient = HttpClient.newBuilder().build();
        String url = "http://localhost:8080/dataMapper/map";
        String restUrl = url;

        HttpRequest.Builder httpRequestBuilder = HttpRequest.newBuilder();
        HttpRequest httpRequest = httpRequestBuilder.uri(new URI(restUrl))
                .POST(HttpRequest.BodyPublishers.ofString(json.toString()))
                .build();


        HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        String responseJson = httpResponse.body();
        System.out.println(responseJson);
    }
}
