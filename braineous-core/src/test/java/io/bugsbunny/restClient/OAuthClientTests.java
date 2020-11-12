package io.bugsbunny.restClient;

import com.google.gson.JsonObject;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;

@QuarkusTest
public class OAuthClientTests {
    private static Logger logger = LoggerFactory.getLogger(OAuthClientTests.class);

    @Inject
    private OAuthClient oAuthClient;


    @Test
    public void testGetAccessToken() throws Exception
    {
        try
        {
            String clientId = "PAlDekAoo0XWjAicU9SQDKgy7B0y2p2t";
            String clientSecret = "U2jMgxL8zJgYOMmHDYTe6-P9yO6Wq51VmixuZSRCaL-11EPE4WrQOWtGLVnQetdd";
            JsonObject jwtToken = this.oAuthClient.getAccessToken(clientId, clientSecret);
            logger.info(jwtToken.get("access_token").getAsString());
            logger.info(jwtToken.get("token_type").getAsString());
        }
        catch(NetworkException ne)
        {
        }
    }
}
