package io.bugsbunny.data.history;

import com.github.wnameless.json.flattener.JsonFlattener;
import com.github.wnameless.json.unflattener.JsonUnflattener;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.quarkus.test.junit.QuarkusTest;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

//@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ObjectDiffAlgorithmTests {
    private static Logger logger = LoggerFactory.getLogger(ObjectDiffAlgorithmTests.class);

    private static Map<String, Object> diff = new HashMap<>();

    @Test
    public void testFlattening() throws Exception
    {
        String json = IOUtils.toString(
                Thread.currentThread().getContextClassLoader().getResourceAsStream("dataLake/email.json"),
                StandardCharsets.UTF_8);
        Map<String, Object> flattenJson = JsonFlattener.flattenAsMap(json);
        String flat = flattenJson.toString();

        logger.info("String: "+json);
        logger.info("Map: "+flat);

        JsonObject jsonObject = JsonParser.parseString(JsonUnflattener.unflatten(flat)).getAsJsonObject();
        logger.info("JSON: "+jsonObject.toString());
        //assertEquals(json, jsonObject.toString());
    }

    @Test
    public void testDiff() throws Exception
    {
        logger.info("****************");

        ObjectDiffAlgorithm objectDiffAlgorithm = new ObjectDiffAlgorithm();

        String email0 = IOUtils.toString(
                Thread.currentThread().getContextClassLoader().getResourceAsStream("historyEngine/email0.json"),
                StandardCharsets.UTF_8);

        String email1 = IOUtils.toString(
                Thread.currentThread().getContextClassLoader().getResourceAsStream("historyEngine/email1.json"),
                StandardCharsets.UTF_8);

        //JsonObject left = new JsonObject();
        JsonObject left = JsonParser.parseString(email0).getAsJsonObject();
        //left.add("object",JsonParser.parseString(email0).getAsJsonObject());
        logger.info("LEFT: "+left.toString());

        //JsonObject right = new JsonObject();
        JsonObject right = JsonParser.parseString(email1).getAsJsonObject();
        //right.add("object",JsonParser.parseString(email1).getAsJsonObject());
        logger.info("RIGHT: "+right.toString());

        JsonObject diff = objectDiffAlgorithm.diff(left, right);
        logger.info("DIFF: "+diff.toString());

        //assert
        logger.info("****************");
    }

    @Test
    public void testDiffChain() throws Exception
    {
        logger.info("****************");
        ObjectDiffAlgorithm objectDiffAlgorithm = new ObjectDiffAlgorithm();

        String email0 = IOUtils.toString(
                Thread.currentThread().getContextClassLoader().getResourceAsStream("historyEngine/diffChain/email0.json"),
                StandardCharsets.UTF_8);

        String email1 = IOUtils.toString(
                Thread.currentThread().getContextClassLoader().getResourceAsStream("historyEngine/diffChain/email1.json"),
                StandardCharsets.UTF_8);

        String email2 = IOUtils.toString(
                Thread.currentThread().getContextClassLoader().getResourceAsStream("historyEngine/diffChain/email2.json"),
                StandardCharsets.UTF_8);

        //First Payload
        JsonObject top = JsonParser.parseString(email0).getAsJsonObject();
        JsonObject next = JsonParser.parseString(email1).getAsJsonObject();
        logger.info("TOP: "+top.toString());
        logger.info("NEXT: "+next.toString());
        JsonObject diff = objectDiffAlgorithm.diff(top, next);
        logger.info("DIFF: "+diff.toString());

        logger.info("****************");

        //Next Payload
        top =  next;
        next = JsonParser.parseString(email2).getAsJsonObject();
        logger.info("TOP: "+top.toString());
        logger.info("NEXT: "+next.toString());
        diff = objectDiffAlgorithm.diff(top, next);
        logger.info("DIFF: "+diff.toString());
    }

    @Test
    public void testDiffReplay() throws Exception
    {
        ObjectDiffAlgorithm objectDiffAlgorithm = new ObjectDiffAlgorithm();
        LinkedList<JsonObject> chain = new LinkedList<>();
        LinkedList<JsonObject> incomingData = new LinkedList<>();

        String email0 = IOUtils.toString(
                Thread.currentThread().getContextClassLoader().getResourceAsStream("historyEngine/diffChain/email0.json"),
                StandardCharsets.UTF_8);

        String email1 = IOUtils.toString(
                Thread.currentThread().getContextClassLoader().getResourceAsStream("historyEngine/diffChain/email1.json"),
                StandardCharsets.UTF_8);

        String email2 = IOUtils.toString(
                Thread.currentThread().getContextClassLoader().getResourceAsStream("historyEngine/diffChain/email2.json"),
                StandardCharsets.UTF_8);

        //Payloads
        JsonObject top = JsonParser.parseString(email0).getAsJsonObject();
        JsonObject middle = JsonParser.parseString(email1).getAsJsonObject();
        JsonObject next = JsonParser.parseString(email2).getAsJsonObject();

        //Populate the chain
        incomingData.add(top);
        incomingData.add(middle);
        incomingData.add(next);
        logger.info("****************");
        logger.info(incomingData.toString());
        logger.info("****************");

        //Calculate Diffs
        JsonObject diff0 = objectDiffAlgorithm.diff(top, middle);
        logger.info("DIFF0: "+diff0.toString());

        logger.info("****************");

        //Next Payload
        JsonObject diff1 = objectDiffAlgorithm.diff(middle, next);
        logger.info("DIFF1: "+diff1.toString());

        //Populate the chain
        chain.add(diff0);
        chain.add(diff1);
        logger.info("****************");
        logger.info(chain.toString());
        logger.info("****************");
    }
}
