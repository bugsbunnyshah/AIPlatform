package io.bugsbunny.dataScience.model;

import com.google.gson.JsonObject;
import io.bugsbunny.util.JsonUtil;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;

public class AllModelTests {
    private static Logger logger = LoggerFactory.getLogger(AllModelTests.class);

    @Test
    public void testScientistSer() throws Exception{
        String email = "test@test.io";
        Scientist scientist = new Scientist();
        scientist.setEmail(email);

        JsonObject json = scientist.toJson();
        JsonUtil.print(json);
        String emailInJson = json.get("email").getAsString();
        assertEquals(email,emailInJson);

        Scientist deser = Scientist.parse(json.toString());
        logger.info(deser.toString());
        assertEquals(email,deser.getEmail());
    }
}
