package com.appgallabs.dataIngestion.service;

import com.appgallabs.test.components.BaseTest;
import io.quarkus.test.junit.QuarkusTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
public class MapperServiceTests extends BaseTest{
    private static Logger logger = LoggerFactory.getLogger(MapperServiceTests.class);

    @Inject
    private MapperService mapperService;

    /*@Test
    public void testMapAirlineData() throws Exception
    {
        String sourceData = IOUtils.toString(Thread.currentThread().getContextClassLoader().getResourceAsStream(
                "airlinesData.json"),
                StandardCharsets.UTF_8);
        JsonArray jsonArray = JsonParser.parseString(sourceData).getAsJsonArray();
        JsonArray array = this.mapperService.map(jsonArray);
        logger.info("*******");
        logger.info(array.toString());
        logger.info("*******");

        JsonObject jsonObject = array.get(0).getAsJsonObject();
        assertEquals("123456789", jsonObject.get("Id").getAsString());
        assertEquals("1234567", jsonObject.get("Rcvr").getAsString());
        assertEquals(Boolean.TRUE, jsonObject.get("HasSig").getAsBoolean());

        jsonObject = array.get(1).getAsJsonObject();
        assertEquals("7777777", jsonObject.get("Id").getAsString());
        assertEquals("77777", jsonObject.get("Rcvr").getAsString());
        assertEquals(Boolean.FALSE, jsonObject.get("HasSig").getAsBoolean());
    }

    @Test
    public void testMapAirlineDataBig() throws Exception
    {
        String sourceData = IOUtils.toString(Thread.currentThread().getContextClassLoader().getResourceAsStream(
                "aviation/flights0.json"),
                StandardCharsets.UTF_8);
        JsonArray jsonArray = JsonParser.parseString(sourceData).getAsJsonObject().get("data").getAsJsonArray();
        JsonArray array = this.mapperService.map(jsonArray);
        logger.info("*******");
        logger.info(array.toString());
        logger.info("*******");
    }*/
}