package io.bugsbunny.datascience.service;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.bugsbunny.dataScience.service.AIModelService;

import javax.inject.Inject;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class AIModelServiceTests {
    private static Logger logger = LoggerFactory.getLogger(AIModelServiceTests.class);

    @Inject
    private AIModelService aiModelService;

    @Test
    public void testStartEval() throws Exception
    {
        String result = this.aiModelService.eval();
        assertNotNull(result);
        logger.info(result);
    }
}
