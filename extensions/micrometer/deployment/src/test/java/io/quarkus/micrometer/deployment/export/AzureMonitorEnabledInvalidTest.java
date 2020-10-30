package io.quarkus.micrometer.deployment.export;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.micrometer.core.instrument.config.validate.ValidationException;
import io.quarkus.test.QuarkusUnitTest;

public class AzureMonitorEnabledInvalidTest {
    final static String testedAttribute = "quarkus.micrometer.export.azuremonitor.instrumentation-key";

    @RegisterExtension
    static final QuarkusUnitTest config = new QuarkusUnitTest()
            .withConfigurationResource("test-logging.properties")
            .overrideConfigKey("quarkus.micrometer.binder-enabled-default", "false")
            .overrideConfigKey("quarkus.micrometer.export.azuremonitor.enabled", "true")
            .overrideConfigKey("quarkus.micrometer.registry-enabled-default", "false")
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                    .addClass(AzureMonitorRegistryProcessor.REGISTRY_CLASS))
            .setLogRecordPredicate(r -> "io.quarkus.micrometer.runtime.export.ConfigAdapter".equals(r.getLoggerName()))
            .assertLogRecords(r -> Util.assertMessage(testedAttribute, r))
            .assertException(t -> Assertions.assertEquals(ValidationException.class.getName(), t.getClass().getName(),
                    "Unexpected exception in test: " + Util.stackToString(t)));

    @Test
    public void testMeterRegistryPresent() {
        Assertions.fail("Runtime should not have initialized with missing " + testedAttribute);
    }
}
