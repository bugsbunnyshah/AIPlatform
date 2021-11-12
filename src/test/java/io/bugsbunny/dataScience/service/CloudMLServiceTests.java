package io.bugsbunny.dataScience.service;

import io.quarkus.test.junit.QuarkusTest;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.python.util.PythonInterpreter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.nio.charset.StandardCharsets;

@QuarkusTest
public class CloudMLServiceTests {
    private static Logger logger = LoggerFactory.getLogger(CloudMLServiceTests.class);

    @Inject
    private CloudMLService cloudMLService;

    @Test
    public void createModel() throws Exception {
        String script = IOUtils.toString(Thread.currentThread().getContextClassLoader().
                        getResourceAsStream("cloudml/createModel.py"),
                StandardCharsets.UTF_8);
        this.cloudMLService.executeScript(script);
    }
}
