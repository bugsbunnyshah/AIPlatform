package io.bugsbunny.restClient;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.MessageFormat;

@ApplicationScoped
public class ElasticSearchClient {
    private static Logger logger = LoggerFactory.getLogger(ElasticSearchClient.class);

    public String updateIndex(JsonArray jsonDocs)
    {
        try
        {
            StringBuilder dataBuilder = new StringBuilder();

            int length = jsonDocs.size();
            for(int i=0; i<length; i++)
            {
                JsonObject jsonObject = jsonDocs.get(i).getAsJsonObject();
                String indexString = MessageFormat.format("{0}\"index\":{0}\"_id\":\"{2}\"{1}{1}",
                        "{", "}", jsonObject.get("ingestionId").getAsString());
                String dataString = jsonObject.toString();
                dataBuilder.append(indexString+"\n"+dataString+"\n");
            }

            String data = dataBuilder.toString();
            String requestUrl = "http://localhost:9200/alz@cat/_bulk?pretty&refresh";
            HttpClient httpClient = HttpClient.newBuilder().build();
            HttpRequest.Builder httpRequestBuilder = HttpRequest.newBuilder();
            HttpRequest httpRequest = httpRequestBuilder.uri(new URI(requestUrl))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(data))
                    .build();
            HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            return httpResponse.body();
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
