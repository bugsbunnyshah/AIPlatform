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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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

    @Test
    public void testObjectDiff() throws Exception
    {
        String email0 = IOUtils.toString(
                Thread.currentThread().getContextClassLoader().getResourceAsStream("historyEngine/email0.json"),
                StandardCharsets.UTF_8);

        String email1 = IOUtils.toString(
                Thread.currentThread().getContextClassLoader().getResourceAsStream("historyEngine/email1.json"),
                StandardCharsets.UTF_8);

        Map<String, Object> email0Map = JsonFlattener.flattenAsMap(email0);
        Map<String, Object> email1Map = JsonFlattener.flattenAsMap(email1);

        logger.info("Map: "+email0.toString());
        logger.info("Map: "+email1.toString());

        Map<String, Object> diff = new HashMap<>();
        Set<Map.Entry<String, Object>> entrySet = email0Map.entrySet();
        for(Map.Entry<String, Object> entry: entrySet)
        {
            String key = entry.getKey();
            int valueHash = entry.getValue().hashCode();
            int compareHash = email1Map.get(key).hashCode();
            if(valueHash != compareHash)
            {
                diff.put(key, email1Map.get(key));
            }
        }

        logger.info("Diff: "+diff.toString());
    }
}
