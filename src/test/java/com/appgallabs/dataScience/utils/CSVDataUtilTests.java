package com.appgallabs.dataScience.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.appgallabs.dataIngestion.util.CSVDataUtil;
import com.appgallabs.test.components.BaseTest;
import io.quarkus.test.junit.QuarkusTest;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
public class CSVDataUtilTests extends BaseTest
{
    private static Logger logger = LoggerFactory.getLogger(CSVDataUtilTests.class);

    @Test
    public void testConvert() throws Exception
    {
        CSVDataUtil csvDataUtil = new CSVDataUtil();

        String payload = IOUtils.toString(Thread.currentThread().getContextClassLoader()
                        .getResourceAsStream("aviation/flights0.json"),
                StandardCharsets.UTF_8);

        JsonArray data = JsonParser.parseString(payload).getAsJsonObject().get("data").getAsJsonArray();
        String result = csvDataUtil.convert(data).get("data").getAsString();
        logger.info(result);
    }
}
