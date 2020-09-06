import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
public class LiveModelTests {
    private Logger logger = LoggerFactory.getLogger(LiveModelTests.class);

    @Test
    public void testLiveModel() throws Exception
    {
        JsonObject jsonObject = new JsonObject();

        Response response = given().body(jsonObject.toString()).when().post("/liveModel/calculate")
                .andReturn();
        String jsonResponse = response.getBody().prettyPrint();
        logger.info("****");
        logger.info(response.getStatusLine());
        logger.info(jsonResponse);
        logger.info("****");

        JsonObject result = JsonParser.parseString(jsonResponse).getAsJsonObject();
        assertNotNull(result.get("calculation"));
    }
}
