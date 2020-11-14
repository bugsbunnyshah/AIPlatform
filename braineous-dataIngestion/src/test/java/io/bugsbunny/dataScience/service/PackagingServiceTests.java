package io.bugsbunny.dataScience.service;

import com.google.gson.JsonObject;
import io.bugsbunny.infrastructure.MongoDBJsonStore;
import io.bugsbunny.test.components.BaseTest;
import io.quarkus.test.junit.QuarkusTest;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.nio.charset.StandardCharsets;

@QuarkusTest
public class PackagingServiceTests extends BaseTest
{
    private static Logger logger = LoggerFactory.getLogger(PackagingServiceTests.class);

    @Inject
    private PackagingService packagingService;

    @Inject
    private MongoDBJsonStore mongoDBJsonStore;


    //@Test
    public void testPerformPackaging() throws Exception
    {
        String modelPackage = IOUtils.resourceToString("dataScience/aiplatform-model.json", StandardCharsets.UTF_8,
                Thread.currentThread().getContextClassLoader());

        JsonObject response = this.packagingService.performPackaging(modelPackage);
        logger.info(response.toString());
    }

    //@Test
    public void testPerformPackagingRemoteModel() throws Exception
    {
        String modelPackage = IOUtils.resourceToString("dataScience/aiplatform-remote-model.json", StandardCharsets.UTF_8,
                Thread.currentThread().getContextClassLoader());

        JsonObject response = this.packagingService.performPackaging(modelPackage);
        logger.info(response.toString());
    }
}
