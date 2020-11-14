package io.bugsbunny.dataScience.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import io.bugsbunny.dataIngestion.util.CSVDataUtil;
import io.bugsbunny.test.components.BaseTest;
import io.quarkus.test.junit.QuarkusTest;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

@QuarkusTest
public class CSVDataUtilTests extends BaseTest
{
    private static Logger logger = LoggerFactory.getLogger(CSVDataUtilTests.class);

    //@Test
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
