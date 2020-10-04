package io.bugsbunny.showcase.aviation.service;


import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.bugsbunny.dataIngestion.endpoint.DataMapper;
import io.bugsbunny.dataIngestion.util.CSVDataUtil;
import io.bugsbunny.dataScience.endpoint.ModelDataSet;
import io.bugsbunny.endpoint.SecurityTokenContainer;
import io.bugsbunny.restClient.OAuthClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.OffsetDateTime;
import java.util.*;

public class IngestData extends TimerTask {
    private static Logger logger = LoggerFactory.getLogger(IngestData.class);

    private Timer timer;

    private CSVDataUtil csvDataUtil;

    private OAuthClient oAuthClient;

    private SecurityTokenContainer securityTokenContainer;

    public IngestData(OAuthClient oAuthClient, SecurityTokenContainer securityTokenContainer) {
        this.oAuthClient = oAuthClient;
        this.securityTokenContainer = securityTokenContainer;
        this.csvDataUtil = new CSVDataUtil();
    }

    public void start()
    {
        this.timer = new Timer(true);
        this.timer.schedule(this, new Date(), 5000);
    }

    @Override
    public void run()
    {
        try
        {
            //Create the Experiment
            HttpClient httpClient = HttpClient.newBuilder().build();
            String restUrl = "https://api.aviationstack.com/v1/flights/?access_key=680da0736176cb1218acdb0d6e1cc10e";

            HttpRequest.Builder httpRequestBuilder = HttpRequest.newBuilder();
            HttpRequest httpRequest = httpRequestBuilder.uri(new URI(restUrl))
                    .GET()
                    .build();


            HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            String responseJson = httpResponse.body();
            int status = httpResponse.statusCode();
            if(status != 200)
            {
               return;
            }

            Random random = new Random();
            CSVDataUtil csvDataUtil = new CSVDataUtil();

            JsonArray trainCsvData = new JsonArray();
            Iterator<JsonElement> itr = JsonParser.parseString(responseJson).getAsJsonObject().get("data")
                    .getAsJsonArray().iterator();
            int counter = 0;
            while(itr.hasNext())
            {
                JsonObject flightData = itr.next().getAsJsonObject();

                JsonObject arrival = flightData.get("arrival").getAsJsonObject();

                String scheduled = arrival.get("scheduled").getAsString();
                String estimated = arrival.get("estimated").getAsString();

                JsonObject csvRow = new JsonObject();
                float scheduledEpochSecond = OffsetDateTime.parse(scheduled).toEpochSecond();
                float estimatedEpochSecond = OffsetDateTime.now().toEpochSecond();
                float difference = Math.abs(scheduledEpochSecond-estimatedEpochSecond);
                //csvRow.addProperty("scheduled", OffsetDateTime.parse(scheduled).toEpochSecond());
                //csvRow.addProperty("estimated", OffsetDateTime.now().toEpochSecond());
                csvRow.addProperty("scheduled", random.nextFloat());
                csvRow.addProperty("estimated", random.nextFloat());
                csvRow.addProperty("difference", random.nextFloat());
                csvRow.addProperty("na", Math.abs(random.nextInt(3)));

                if(counter < 33) {
                    csvRow.addProperty("labelIndex", 0);
                }
                else if(counter < 66)
                {
                    csvRow.addProperty("labelIndex", 1);
                }
                else
                {
                    csvRow.addProperty("labelIndex", 2);
                }

                trainCsvData.add(csvRow);

                counter++;
            }
            JsonObject csvJson = csvDataUtil.convert(trainCsvData);
            csvJson.addProperty("format", "csv");
            //String csv = csvJson.get("data").getAsString();
            //long rows = csvJson.get("rows").getAsLong();
            //long columns = csvJson.get("columns").getAsLong();
            logger.info(csvJson.toString());

            String clientId = "PAlDekAoo0XWjAicU9SQDKgy7B0y2p2t";
            String clientSecret = "U2jMgxL8zJgYOMmHDYTe6-P9yO6Wq51VmixuZSRCaL-11EPE4WrQOWtGLVnQetdd";
            JsonObject securityToken = this.oAuthClient.getAccessToken(clientId,clientSecret);

            String token = securityToken.get("access_token").getAsString();
            restUrl = "http://localhost:8080/dataset/storeTrainingDataSet/";
            httpRequestBuilder = HttpRequest.newBuilder();
            httpRequest = httpRequestBuilder.uri(new URI(restUrl)).
                    header("Bearer", token).
                    header("Principal", clientId)
                    .POST(HttpRequest.BodyPublishers.ofString(csvJson.toString()))
                    .build();


            httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            responseJson = httpResponse.body();
            status = httpResponse.statusCode();
            if(status != 200)
            {
                return;
            }
            logger.info(responseJson);
        }
        catch(Exception e)
        {
            logger.error(e.getMessage(), e);
        }
    }
}
