package io.bugsbunny.data.history;

import com.github.wnameless.json.flattener.JsonFlattener;
import com.github.wnameless.json.unflattener.JsonUnflattener;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.quarkus.test.junit.QuarkusTest;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Map;

public class ObjectDiffAlgorithmTests {
    private static Logger logger = LoggerFactory.getLogger(ObjectDiffAlgorithmTests.class);

    @Test
    public void testFlattening() throws Exception
    {
        String json = IOUtils.toString(
                Thread.currentThread().getContextClassLoader().getResourceAsStream("dataLake/email.json"),
                StandardCharsets.UTF_8);
        Map<String, Object> flattenJson = JsonFlattener.flattenAsMap(json);

        logger.info("String: "+flattenJson.toString());
        logger.info("Map: "+flattenJson.toString());

        JsonObject jsonObject = JsonParser.parseString(JsonUnflattener.unflatten(flattenJson.toString())).getAsJsonObject();
        logger.info("JSON: "+jsonObject.toString());
    }
}
