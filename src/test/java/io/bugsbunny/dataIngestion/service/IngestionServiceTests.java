package io.bugsbunny.dataIngestion.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.bugsbunny.test.components.BaseTest;
import io.quarkus.test.junit.QuarkusTest;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
public class IngestionServiceTests extends BaseTest {
    private static Logger logger = LoggerFactory.getLogger(IngestionServiceTests.class);

    @Inject
    private IngestionService ingestionService;

    @Test
    public void testGetIngestion() throws Exception
    {
        //TODO: temp code for debugging
        long dataLakeId = -2586030430120757939l;
        logger.info(this.ingestionService.readDataLakeData(dataLakeId).toString());
    }
}