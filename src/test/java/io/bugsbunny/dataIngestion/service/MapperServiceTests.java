package io.bugsbunny.dataIngestion.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import io.quarkus.test.junit.QuarkusTest;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.nio.charset.StandardCharsets;

@QuarkusTest
public class MapperServiceTests {
    private static Logger logger = LoggerFactory.getLogger(MapperServiceTests.class);

    @Inject
    private MapperService mapperService;

    @Test
    public void testMapAirlineData() throws Exception
    {
        String sourceData = IOUtils.toString(Thread.currentThread().getContextClassLoader().getResourceAsStream(
                "airlinesData.json"),
                StandardCharsets.UTF_8);
        JsonArray jsonArray = JsonParser.parseString(sourceData).getAsJsonArray();
        JsonArray array = this.mapperService.map("", "", jsonArray);
        logger.info("*******");
        logger.info(array.toString());
        logger.info("*******");

        /*assertEquals("123456789", jsonObject.get("Id").getAsString());
        assertEquals("1234567", jsonObject.get("Rcvr").getAsString());
        assertEquals(Boolean.TRUE, jsonObject.get("HasSig").getAsBoolean());*/
    }

    /*@Test
    public void testMapPeopleData() throws Exception
    {
        String json = IOUtils.toString(Thread.currentThread().getContextClassLoader().getResourceAsStream(
                "people.json"),
                StandardCharsets.UTF_8);
        JsonObject jsonObject = this.mapperService.map(json, json, json);
        logger.info("*******");
        logger.info(jsonObject.toString());
        logger.info("*******");

        assertEquals("James", jsonObject.get("firstname").getAsString());
        //assertEquals("1234567", jsonObject.get("Rcvr").getAsString());
        //assertEquals(Boolean.TRUE, jsonObject.get("HasSig").getAsBoolean());
    }*/
}