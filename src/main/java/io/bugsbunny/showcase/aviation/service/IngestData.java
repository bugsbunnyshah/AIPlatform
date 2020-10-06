package io.bugsbunny.showcase.aviation.service;


import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.bugsbunny.dataIngestion.util.CSVDataUtil;
import io.bugsbunny.dataScience.service.ModelDataSetService;
import io.bugsbunny.endpoint.SecurityToken;
import io.bugsbunny.endpoint.SecurityTokenContainer;
import io.bugsbunny.restClient.OAuthClient;
import org.eclipse.microprofile.config.ConfigProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private AviationDataIngestionService aviationDataIngestionService;

    private ModelDataSetService modelDataSetService;

    private SecurityToken securityToken;

    public IngestData(OAuthClient oAuthClient, SecurityTokenContainer securityTokenContainer,
                      AviationDataIngestionService aviationDataIngestionService, ModelDataSetService modelDataSetService) {
        this.oAuthClient = oAuthClient;
        this.securityTokenContainer = securityTokenContainer;
        this.csvDataUtil = new CSVDataUtil();
        this.aviationDataIngestionService = aviationDataIngestionService;
        this.modelDataSetService = modelDataSetService;
        this.securityToken = this.securityTokenContainer.getSecurityToken();
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
            String aviationDataSource = ConfigProvider.getConfig().getValue("aviationDataSource", String.class);
            String aviationDataSourceAccessKey = ConfigProvider.getConfig().getValue("aviationDataSourceAccessKey", String.class);
            HttpClient httpClient = HttpClient.newBuilder().build();
            String restUrl = "https://"+aviationDataSource+"/v1/flights/?access_key="+aviationDataSourceAccessKey;

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


            this.securityTokenContainer.setSecurityToken(this.securityToken);
            long dataSetId = this.modelDataSetService.storeTrainingDataSet(csvJson);
            this.aviationDataIngestionService.registerDataSetId(dataSetId);
        }
        catch(Exception e)
        {
            logger.error(e.getMessage(), e);
        }
    }
}
