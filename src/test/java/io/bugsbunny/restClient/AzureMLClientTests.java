package io.bugsbunny.restClient;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.bugsbunny.endpoint.SecurityToken;
import io.bugsbunny.endpoint.SecurityTokenContainer;
import io.bugsbunny.persistence.MongoDBJsonStore;
import io.quarkus.test.junit.QuarkusTest;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.nio.charset.StandardCharsets;

@QuarkusTest
public class AzureMLClientTests {
    private static Logger logger = LoggerFactory.getLogger(AzureMLClientTests.class);

    @Inject
    private AzureMLClient azureMLClient;

    //@Test
    public void testInvokeAzureModel() throws Exception
    {
        JsonObject json = this.azureMLClient.invokeAzureModel();
        logger.info(json.toString());
    }
}
