package io.bugsbunny.dataScience.endpoint;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.bugsbunny.preprocess.SecurityTokenContainer;
import io.bugsbunny.test.components.BaseTest;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.nio.charset.StandardCharsets;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
public class ProjectTests extends BaseTest
{
    private static Logger logger = LoggerFactory.getLogger(ProjectTests.class);

    @Inject
    private SecurityTokenContainer securityTokenContainer;

    @Test
    public void readProjects() throws Exception
    {
        String url = "/projects/";
        String principal = this.securityTokenContainer.getSecurityToken().getPrincipal();
        String token = this.securityTokenContainer.getSecurityToken().getToken();
        Response response = given().header("Principal",principal).header("Bearer",token).get(url).andReturn();
        response.getBody().prettyPrint();
    }
}