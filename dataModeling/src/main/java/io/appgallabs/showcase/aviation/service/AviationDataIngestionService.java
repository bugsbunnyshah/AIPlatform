package io.appgallabs.showcase.aviation.service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.bugsbunny.endpoint.SecurityToken;
import io.bugsbunny.endpoint.SecurityTokenContainer;
import io.bugsbunny.restClient.OAuthClient;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class AviationDataIngestionService
{
    private static Logger logger = LoggerFactory.getLogger(AviationDataIngestionService.class);

    private OAuthClient oAuthClient;

    private SecurityTokenContainer securityTokenContainer;

    private IngestData ingestData;

    private List<Long> dataSetIds;

    private JsonObject appConfig;

    public AviationDataIngestionService()
    {
        try
        {
            this.dataSetIds = new ArrayList<>();

            String appConfigJson = IOUtils.toString(Thread.currentThread().getContextClassLoader().getResourceAsStream("appConfig.json"),
                    StandardCharsets.UTF_8);
            this.appConfig = JsonParser.parseString(appConfigJson).getAsJsonObject();
            String clientId = appConfig.get("client_id").getAsString();
            String clientSecret = appConfig.get("client_secret").getAsString();

            this.oAuthClient = new OAuthClient();
            JsonObject securityToken = oAuthClient.getAccessToken(clientId,clientSecret);
            String token = securityToken.get("access_token").getAsString();
            JsonObject securityTokenJson = new JsonObject();
            securityTokenJson.addProperty("access_token", securityToken.get("access_token").getAsString());
            securityTokenJson.addProperty("principal", clientId.hashCode());

            this.securityTokenContainer = new SecurityTokenContainer();
            this.securityTokenContainer.setSecurityToken(SecurityToken.fromJson(securityTokenJson.toString()));

            this.ingestData = new IngestData(this.oAuthClient,this.securityTokenContainer, this);
        }
        catch(Exception ioe)
        {
            throw new RuntimeException(ioe);
        }
    }

    public SecurityTokenContainer getSecurityTokenContainer()
    {
        return securityTokenContainer;
    }

    public JsonObject getAppConfig()
    {
        return appConfig;
    }

    public void startIngestion()
    {
        this.ingestData.start();
    }

    public void registerDataSetId(long dataSetId)
    {
        logger.info("***********************");
        logger.info("REGISTERING: "+dataSetId);
        logger.info("***********************");
        this.dataSetIds.add(dataSetId);
    }

    public List<Long> getDataSetIds()
    {
        return this.dataSetIds;
    }
}
