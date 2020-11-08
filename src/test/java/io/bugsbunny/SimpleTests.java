package io.bugsbunny;

import io.bugsbunny.test.components.BaseTest;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@QuarkusTest
public class SimpleTests extends BaseTest
{
    private static Logger logger = LoggerFactory.getLogger(SimpleTests.class);

    @Test
    public void testSimpleOp() throws Exception
    {
    }
}
