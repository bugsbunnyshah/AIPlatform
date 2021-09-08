package io.bugsbunny.showcase.aviation;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.bugsbunny.dataIngestion.util.CSVDataUtil;
import io.bugsbunny.test.components.BaseTest;
import io.bugsbunny.util.JsonUtil;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
public class AviationDataHistoryTests extends BaseTest
{
    private static Logger logger = LoggerFactory.getLogger(AviationDataHistoryTests.class);

    @Test
    public void diffSamePayload() throws Exception{
        String data = IOUtils.toString(Thread.currentThread().
                        getContextClassLoader().
                        getResourceAsStream("aviation/flights0.json"),
                StandardCharsets.UTF_8);

        JsonArray json = JsonParser.parseString(data).getAsJsonObject().get("data").getAsJsonArray();
        //JsonUtil.print(json);

        JsonArray ingestion1 = new JsonArray();
        JsonArray ingestion2 = new JsonArray();
        for(int i=0; i<5; i++)
        {
            ingestion1.add(json.get(i));
            ingestion2.add(json.get(i));
        }

        JsonUtil.print(ingestion1);
        JsonUtil.print(ingestion2);

        //Ingest1

        //Ingest2

        //Check the Data Chain
    }
}
