package io.bugsbunny.persistence;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.quarkus.test.junit.QuarkusTest;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

@QuarkusTest
public class MongoDBJsonStoreTests {
    private static Logger logger = LoggerFactory.getLogger(MongoDBJsonStoreTests.class);

    @Inject
    private MongoDBJsonStore mongoDBJsonStore;

    private Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    @BeforeEach
    public void setUp()
    {

    }

    @AfterEach
    public void tearDown()
    {

    }

    @Test
    public void testIngestionPipeline() throws Exception
    {
        String json = IOUtils.toString(Thread.currentThread().getContextClassLoader().
                        getResourceAsStream("people.json"),
                StandardCharsets.UTF_8);

        JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
        List<JsonObject> objects = Arrays.asList(new JsonObject[]{jsonObject});

        this.mongoDBJsonStore.storeIngestion(objects);
        JsonObject storedJson = this.mongoDBJsonStore.getIngestion("1");

        logger.info("*******");
        logger.info(storedJson.toString());
        logger.info("*******");
    }

    @Test
    public void testImageData() throws Exception
    {
        String animalsCsvData = IOUtils.toString(Thread.currentThread().getContextClassLoader().
                getResourceAsStream("DataExamples/animals/animals.csv"), StandardCharsets.UTF_8);

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("data", animalsCsvData);

        logger.info("*******");
        logger.info(jsonObject.toString());
        logger.info("*******");

        this.mongoDBJsonStore.storeIngestionImage(jsonObject);
        List<JsonObject> result = this.mongoDBJsonStore.getIngestionImages();
        logger.info("*******");
        logger.info(result.toString());
        logger.info("*******");
    }
}
