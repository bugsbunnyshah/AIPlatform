package io.bugsbunny.endpoint;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.restassured.RestAssured.given;

@QuarkusTest
public class OAuthAuthenticateTests
{
    private static Logger logger = LoggerFactory.getLogger(OAuthAuthenticateTests.class);

    @Test
    public void testOAuthGetStarted() throws Exception
    {
        String clientId = "PAlDekAoo0XWjAicU9SQDKgy7B0y2p2t";
        String clientSecret = "U2jMgxL8zJgYOMmHDYTe6-P9yO6Wq51VmixuZSRCaL-11EPE4WrQOWtGLVnQetdd";
        JsonObject payload = new JsonObject();
        payload.addProperty("client_id", clientId);
        payload.addProperty("client_secret", clientSecret);
        payload.addProperty("audience", "https://appgallabs.us.auth0.com/api/v2/");
        payload.addProperty("grant_type", "client_credentials");
        Response response = given().body(payload.toString()).when().post("/oauth/token/").andReturn();
        logger.info("************************");
        logger.info(response.statusLine());
        response.body().prettyPrint();
        logger.info("************************");
    }
}
