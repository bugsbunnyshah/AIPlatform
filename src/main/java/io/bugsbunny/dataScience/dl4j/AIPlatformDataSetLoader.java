package io.bugsbunny.dataScience.dl4j;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.bugsbunny.persistence.MongoDBJsonStore;

import org.nd4j.common.loader.Loader;
import org.nd4j.common.loader.Source;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class AIPlatformDataSetLoader implements Loader
{
    private static Logger logger = LoggerFactory.getLogger(AIPlatformDataSetLoader.class);

    @Override
    public Object load(Source source) throws IOException
    {
        MongoDBJsonStore mongoDBJsonStore = new MongoDBJsonStore();
        JsonObject dataSet = JsonParser.parseString(mongoDBJsonStore.readDataSet().toString()).getAsJsonObject();
        String data = dataSet.get("data").getAsString();

        logger.info("*************AIPlatformDataSetLoader**********************");
        logger.info(data);
        logger.info("**********************************************************");

        return data;
    }
}
