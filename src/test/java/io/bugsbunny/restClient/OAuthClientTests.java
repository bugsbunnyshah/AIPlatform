package io.bugsbunny.restClient;

import com.google.gson.JsonObject;
import io.bugsbunny.endpoint.SecurityToken;
import io.bugsbunny.endpoint.SecurityTokenContainer;
import io.bugsbunny.persistence.MongoDBJsonStore;
import io.quarkus.test.junit.QuarkusTest;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.nio.charset.StandardCharsets;

@QuarkusTest
public class OAuthClientTests {
    private static Logger logger = LoggerFactory.getLogger(OAuthClientTests.class);

    @Inject
    private OAuthClient oAuthClient;

    /*@Inject
    private MongoDBJsonStore mongoDBJsonStore;

    @Inject
    private SecurityTokenContainer securityTokenContainer;

    @BeforeEach
    public void setUp() throws Exception
    {
        String securityTokenJson = IOUtils.toString(Thread.currentThread().getContextClassLoader().
                        getResourceAsStream("oauthAgent/token.json"),
                StandardCharsets.UTF_8);
        SecurityToken securityToken = SecurityToken.fromJson(securityTokenJson);
        this.securityTokenContainer.getTokenContainer().set(securityToken);
    }*/

    @Test
    public void testGetAccessToken() throws Exception
    {
        String clientId = "PAlDekAoo0XWjAicU9SQDKgy7B0y2p2t";
        String clientSecret = "U2jMgxL8zJgYOMmHDYTe6-P9yO6Wq51VmixuZSRCaL-11EPE4WrQOWtGLVnQetdd";
        JsonObject jwtToken = this.oAuthClient.getAccessToken(clientId, clientSecret);
        logger.info(jwtToken.get("access_token").getAsString());
        logger.info(jwtToken.get("token_type").getAsString());
    }
}
