package io.bugsbunny.dashboard.service;

import com.google.gson.JsonObject;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;

@QuarkusTest
public class ModelTrafficServiceTests
{
    private static Logger logger = LoggerFactory.getLogger(ModelTrafficServiceTests.class);

    @Inject
    private ModelTrafficService modelTrafficService;

    @Test
    public void testModelTraffic() throws Exception
    {
        Map<String, List<JsonObject>> modelTraffic = this.modelTrafficService.getModelTraffic("us", "bugsbunny");
        logger.info(modelTraffic.toString());
    }
}
