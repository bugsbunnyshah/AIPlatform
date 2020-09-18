package io.bugsbunny.persistence;

import com.google.gson.*;
import com.google.common.hash.HashCode;

import io.bugsbunny.dataIngestion.util.CSVDataUtil;
import io.bugsbunny.endpoint.SecurityToken;
import io.bugsbunny.endpoint.SecurityTokenContainer;
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

import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
public class MongoDBJsonStoreTests {
    private static Logger logger = LoggerFactory.getLogger(MongoDBJsonStoreTests.class);

    private Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    @Inject
    private CSVDataUtil csvDataUtil;

    @Inject
    private MongoDBJsonStore mongoDBJsonStore;

    @Inject
    private SecurityTokenContainer securityTokenContainer;

    @BeforeEach
    public void setUp() throws Exception
    {
        String securityTokenJson = IOUtils.toString(Thread.currentThread().getContextClassLoader().
                        getResourceAsStream("oauthAgent/token.json"),
                StandardCharsets.UTF_8);
        SecurityToken securityToken = SecurityToken.fromJson(securityTokenJson);
        this.securityTokenContainer.getTokenContainer().set(securityToken);
    }

    @AfterEach
    public void tearDown()
    {

    }

    @Test
    public void testIngestionData() throws Exception
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
        assertEquals("James", storedJson.get("firstname").getAsString());
    }

    @Test
    public void testImageData() throws Exception
    {
        String animalsCsvData = IOUtils.toString(Thread.currentThread().getContextClassLoader().
                getResourceAsStream("DataExamples/animals/animals.csv"), StandardCharsets.UTF_8);

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("data", animalsCsvData);

        this.mongoDBJsonStore.storeIngestionImage(jsonObject);
        List<JsonObject> result = this.mongoDBJsonStore.getIngestionImages();
        JsonObject storedJson = result.get(0);
        int sourceHashCode = HashCode.fromBytes(jsonObject.toString().getBytes(StandardCharsets.UTF_8)).hashCode();
        int storedHashCode = HashCode.fromBytes(storedJson.toString().getBytes(StandardCharsets.UTF_8)).hashCode();

        logger.info("***************************");
        logger.info("SourceHashCode: "+sourceHashCode+", StoredHashCode: "+storedHashCode);
        logger.info("SourceJson: "+jsonObject.toString());
        logger.info("StoredJson: "+storedJson.toString());
        logger.info("***************************");
        assertEquals(sourceHashCode, storedHashCode);
    }

    @Test
    public void testDevModelData() throws Exception
    {

    }

    @Test
    public void testDataHistoryStorage() throws Exception
    {

    }

    @Test
    public void testStoreDataSet() throws Exception
    {
        String digitsCsv = IOUtils.toString(
                Thread.currentThread().getContextClassLoader().getResourceAsStream("dataLake/digits.csv"),
                StandardCharsets.UTF_8);
        logger.info(digitsCsv);

        JsonArray array = this.csvDataUtil.convert(digitsCsv);
        logger.info(array.toString());
        this.mongoDBJsonStore.storeDataSet(array);

        logger.info(this.mongoDBJsonStore.readDataSet().toString());
    }
}
