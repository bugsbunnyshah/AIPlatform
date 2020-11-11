package io.bugsbunny.showcase.aviation.service;

import io.bugsbunny.SimpleTests;
import io.bugsbunny.test.components.BaseTest;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import showcase.service.AviationDataIngestionService;

import javax.inject.Inject;

@QuarkusTest
public class AviationDataIngestionServiceTests extends BaseTest {
    private static Logger logger = LoggerFactory.getLogger(AviationDataIngestionServiceTests.class);

    @Inject
    private AviationDataIngestionService aviationDataIngestionService;

    @Test
    public void testStartIngestion() throws Exception
    {
        this.aviationDataIngestionService.startIngestion();

        while(this.aviationDataIngestionService.getDataSetIds().isEmpty());

        logger.info(this.aviationDataIngestionService.getDataSetIds().toString());
    }
}
