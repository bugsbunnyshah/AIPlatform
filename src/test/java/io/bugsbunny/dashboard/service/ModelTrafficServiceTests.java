package io.bugsbunny.dashboard.service;

import com.google.gson.JsonObject;
import io.bugsbunny.dataScience.service.PackagingService;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
public class ModelTrafficServiceTests
{
    private static Logger logger = LoggerFactory.getLogger(ModelTrafficServiceTests.class);

    @Inject
    private ModelTrafficService modelTrafficService;

    @Inject
    private PackagingService packagingService;

    @Test
    public void testModelTraffic() throws Exception
    {
        String modelPackage = IOUtils.resourceToString("dataScience/aiplatform-model.json", StandardCharsets.UTF_8,
                Thread.currentThread().getContextClassLoader());

        JsonObject input = this.packagingService.performPackaging(modelPackage);
        Response response = given().body(input.toString()).when().post("/liveModel/eval").andReturn();
        assertEquals(200, response.getStatusCode());

        Map<String, List<JsonObject>> modelTraffic = this.modelTrafficService.getModelTraffic("us", "bugsbunny");
        assertNotNull(modelTraffic);
    }
}
