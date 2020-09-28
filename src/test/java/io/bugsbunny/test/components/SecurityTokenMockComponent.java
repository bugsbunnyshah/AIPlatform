package io.bugsbunny.test.components;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.bugsbunny.endpoint.SecurityToken;
import io.bugsbunny.endpoint.SecurityTokenContainer;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.Priority;
import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Priority(0)
@Provider
public class SecurityTokenMockComponent implements ContainerRequestFilter
{
    private static Logger logger = LoggerFactory.getLogger(SecurityTokenMockComponent.class);

    @Inject
    private SecurityTokenContainer securityTokenContainer;

    public void start()
    {
        try
        {
            if(this.securityTokenContainer.getSecurityToken() != null)
            {
                return;
            }
            String credentials = IOUtils.resourceToString("oauth/credentials.json",
                    StandardCharsets.UTF_8,
                    Thread.currentThread().getContextClassLoader());
            JsonObject credentialsJson = JsonParser.parseString(credentials).getAsJsonObject();
            String token = IOUtils.resourceToString("oauth/jwtToken.json",
                    StandardCharsets.UTF_8,
                    Thread.currentThread().getContextClassLoader());
            JsonObject securityTokenJson = JsonParser.parseString(token).getAsJsonObject();
            securityTokenJson.addProperty("principal", "" + credentialsJson.get("client_id").getAsString().hashCode());
            SecurityToken securityToken = SecurityToken.fromJson(securityTokenJson.toString());
            this.securityTokenContainer.setSecurityToken(securityToken);

            logger.info("*****************************************************************");
            logger.info("(SecurityTokenContainer): " + this.securityTokenContainer);
            logger.info("(SecurityToken): " + this.securityTokenContainer.getSecurityToken());
            logger.info("*****************************************************************");
        }
        catch(Exception e)
        {
            throw new RuntimeException(e);
        }
    }


    @Override
    public void filter(ContainerRequestContext context) throws IOException
    {
        this.start();
    }
}
