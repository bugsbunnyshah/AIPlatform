package io.bugsbunny.configuration;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Singleton;
import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;

@Singleton
public class AIPlatformConfig {
    private static Logger logger = LoggerFactory.getLogger(AIPlatformConfig.class);

    private JsonObject configuration;

    @PostConstruct
    public void start()
    {
        try {
            logger.info(System.getenv("LANGUAGE"));
            logger.info(System.getenv("mongodbHost"));


            File aiPlatformConfig = FileUtils.getFile("/deployments/aiplatform.json");
            String configJson = IOUtils.toString(new FileInputStream(aiPlatformConfig), StandardCharsets.UTF_8);
            this.configuration = JsonParser.parseString(configJson).getAsJsonObject();
        }
        catch(Exception e)
        {
            this.configuration = null;
        }
    }

    public JsonObject getConfiguration()
    {
        return this.configuration;
    }
}
