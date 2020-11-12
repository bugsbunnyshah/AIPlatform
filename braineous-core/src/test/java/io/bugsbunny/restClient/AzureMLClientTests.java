package io.bugsbunny.restClient;

import com.google.gson.JsonObject;
import io.quarkus.test.junit.QuarkusTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

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
