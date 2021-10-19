package io.bugsbunny;

import com.github.wnameless.json.flattener.JsonFlattener;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.bugsbunny.util.JsonUtil;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class JsonUtilTest {
    private static Logger logger = LoggerFactory.getLogger(JsonUtilTest.class);

    @Test
    public void getJsonHash() throws Exception{
        JsonObject top = new JsonObject();
        top.addProperty("1", "1");
        top.addProperty("2","2");
        top.addProperty("3", "3");
        String topHash = JsonUtil.getJsonHash(top);

        Map<String, Object> topMap = JsonFlattener.flattenAsMap(top.toString());
        JsonObject next = new JsonObject();
        next.addProperty("2", topMap.get("2").toString());
        next.addProperty("3",topMap.get("3").toString());
        next.addProperty("1", topMap.get("1").toString());
        String nextHash = JsonUtil.getJsonHash(next);

        logger.info(topHash);
        logger.info(nextHash);
        String hash = "A6A5B337044A7C14AB492FA1BA44DC05";
        assertEquals(hash,topHash);
        assertEquals(hash,nextHash);
    }

    @Test
    public void getJsonHashReal() throws Exception{
        String topJson = IOUtils.toString(Thread.currentThread().getContextClassLoader().getResourceAsStream("prototype/top.json"),
                StandardCharsets.UTF_8);

        String nextJson = IOUtils.toString(Thread.currentThread().getContextClassLoader().getResourceAsStream("prototype/next.json"),
                StandardCharsets.UTF_8);

        JsonObject top = JsonParser.parseString(topJson).getAsJsonObject();
        String topHash = JsonUtil.getJsonHash(top);

        Map<String, Object> topMap = JsonFlattener.flattenAsMap(top.toString());
        JsonObject next = JsonParser.parseString(nextJson).getAsJsonObject();
        String nextHash = JsonUtil.getJsonHash(next);

        logger.info(topHash);
        logger.info(nextHash);
        String hash = "C8039313242D428E65759381CA63A693";
        assertEquals(hash,topHash);
        assertEquals(hash,nextHash);
    }
}
