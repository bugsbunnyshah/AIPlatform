package io.bugsbunny.showcase.aviation.service;

import io.bugsbunny.test.components.BaseTest;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

@QuarkusTest
public class AviationDataIngestionServiceTests extends BaseTest
{
    private static Logger logger = LoggerFactory.getLogger(AviationDataIngestionServiceTests.class);

    @Inject
    private AviationDataIngestionService aviationDataIngestionService;

    @Test
    public void testIngestion() throws Exception
    {
        //IngestData ingestData = new IngestData();
        //ingestData.start();
        Thread.sleep(20000);
    }
}
