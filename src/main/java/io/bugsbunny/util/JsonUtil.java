package io.bugsbunny.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonUtil {
    private static Logger logger = LoggerFactory.getLogger(JsonUtil.class);

    private static Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static void print(JsonElement jsonElement)
    {
        if(jsonElement.isJsonArray())
        {
            logger.info("******ARRAY_SIZE: "+jsonElement.getAsJsonArray().size()+"**********");
        }
        logger.info(gson.toJson(jsonElement));
    }
}
