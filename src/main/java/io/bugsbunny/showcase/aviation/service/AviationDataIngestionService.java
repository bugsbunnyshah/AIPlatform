package io.bugsbunny.showcase.aviation.service;

import io.bugsbunny.endpoint.SecurityTokenContainer;
import io.bugsbunny.restClient.OAuthClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class AviationDataIngestionService {
    private static Logger logger = LoggerFactory.getLogger(AviationDataIngestionService.class);

    @Inject
    private OAuthClient oAuthClient;

    @Inject
    private SecurityTokenContainer securityTokenContainer;

    private IngestData ingestData;

    public AviationDataIngestionService()
    {
    }

    public void startIngestion()
    {
        this.ingestData = new IngestData(this.oAuthClient,this.securityTokenContainer);
        ingestData.start();
    }
}
