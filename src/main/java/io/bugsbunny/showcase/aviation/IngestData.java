package io.bugsbunny.showcase.aviation;


import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.bugsbunny.dataScience.model.DataBricksProcessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class IngestData extends TimerTask {
    private static Logger logger = LoggerFactory.getLogger(IngestData.class);

    private Timer timer;

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
            String url = "https://api.aviationstack.com/v1/flights/?access_key=680da0736176cb1218acdb0d6e1cc10e";
            String restUrl = url;

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

            logger.info(responseJson);
        }
        catch(Exception e)
        {
            logger.error(e.getMessage(), e);
        }
    }
}
