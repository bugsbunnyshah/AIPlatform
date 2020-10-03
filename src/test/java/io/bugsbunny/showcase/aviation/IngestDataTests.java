package io.bugsbunny.showcase.aviation;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

//@QuarkusTest
public class IngestDataTests
{
    private static Logger logger = LoggerFactory.getLogger(IngestDataTests.class);

    @Test
    public void testIngestion() throws Exception
    {
        IngestData ingestData = new IngestData();
        ingestData.start();
        Thread.sleep(20000);
    }
}
