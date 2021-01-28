package io.bugsbunny.dataIngestion.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.bugsbunny.test.components.BaseTest;
import io.bugsbunny.util.JsonUtil;
import io.quarkus.test.junit.QuarkusTest;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
public class MapperServiceTests extends BaseTest {
    private static Logger logger = LoggerFactory.getLogger(MapperServiceTests.class);

    @Inject
    private MapperService mapperService;

    @Test
    public void testMapAirlineData() throws Exception
    {
        //String sourceData = IOUtils.toString(Thread.currentThread().getContextClassLoader().getResourceAsStream(
        //        "airlinesData.json"),
        //        StandardCharsets.UTF_8);
        String sourceData = IOUtils.toString(Thread.currentThread().getContextClassLoader().getResourceAsStream(
                "aviation/flights.json"),
                StandardCharsets.UTF_8);
        JsonArray jsonArray = JsonParser.parseString(sourceData).getAsJsonObject().getAsJsonArray("data");

        JsonArray input = new JsonArray();
        for(int i=0; i<100; i++)
        {
            input.add(jsonArray.get(0));
        }

        long start = System.currentTimeMillis();
        JsonArray array = this.mapperService.map(input);
        long end = System.currentTimeMillis();
        logger.info("*******");
        JsonUtil.print(array);
        logger.info("Processing Time: "+(end - start));
        logger.info("*******");

        /*JsonObject jsonObject = array.get(0).getAsJsonObject();
        assertEquals("123456789", jsonObject.get("Id").getAsString());
        assertEquals("1234567", jsonObject.get("Rcvr").getAsString());
        assertEquals(Boolean.TRUE, jsonObject.get("HasSig").getAsBoolean());

        jsonObject = array.get(1).getAsJsonObject();
        assertEquals("7777777", jsonObject.get("Id").getAsString());
        assertEquals("77777", jsonObject.get("Rcvr").getAsString());
        assertEquals(Boolean.FALSE, jsonObject.get("HasSig").getAsBoolean());*/
    }

    @Test
    public void testMapFlights() throws Exception
    {
        String sourceData = IOUtils.toString(Thread.currentThread().getContextClassLoader().getResourceAsStream(
                "aviation/flights.json"),
                StandardCharsets.UTF_8);
        JsonArray jsonArray = JsonParser.parseString(sourceData).getAsJsonObject().getAsJsonArray("data");

        JsonArray input = new JsonArray();
        for(int i=0; i<jsonArray.size(); i++)
        {
            input.add(jsonArray.get(i));
        }

        long start = System.currentTimeMillis();
        JsonArray array = this.mapperService.map(input);
        long end = System.currentTimeMillis();
        logger.info("*******");
        logger.info(array.toString());
        logger.info("Processing Time: "+(end - start));
        logger.info("*******");
    }
}