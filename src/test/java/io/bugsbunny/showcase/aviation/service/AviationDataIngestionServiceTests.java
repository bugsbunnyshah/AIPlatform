package io.bugsbunny.showcase.aviation.service;

import io.bugsbunny.test.components.BaseTest;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
public class AviationDataIngestionServiceTests extends BaseTest
{
    private static Logger logger = LoggerFactory.getLogger(AviationDataIngestionServiceTests.class);

    @Inject
    private AviationDataIngestionService aviationDataIngestionService;

    //@Test
    public void testIngestion() throws Exception
    {
        this.aviationDataIngestionService.startIngestion();

        Thread.sleep(20000);

        List<Long> dataSetIds = this.aviationDataIngestionService.getDataSetIds();
        int counter = 100;
        while(dataSetIds.isEmpty() && counter > 0)
        {
            dataSetIds = this.aviationDataIngestionService.getDataSetIds();
            counter--;
        }
        assertFalse(dataSetIds.isEmpty());
    }
}
