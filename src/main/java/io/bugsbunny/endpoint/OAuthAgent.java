package io.bugsbunny.endpoint;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.bugsbunny.data.history.service.PayloadReplayService;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Priority;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Priority(1)
@Provider
public class OAuthAgent implements ContainerRequestFilter
{
    private static Logger logger = LoggerFactory.getLogger(OAuthAgent.class);

    @Inject
    private SecurityTokenContainer securityTokenContainer;

    @Override
    public void filter(ContainerRequestContext context) throws IOException
    {
        String payload = IOUtils.toString(context.getEntityStream(), StandardCharsets.UTF_8);
        logger.info("***********OAuthAgent_Incoming**************");
        logger.info(payload);
        logger.info("************************************************");

        String securityTokenJson = IOUtils.toString(Thread.currentThread().getContextClassLoader().
                getResourceAsStream("oauthAgent/token.json"),
                StandardCharsets.UTF_8);
        SecurityToken securityToken = SecurityToken.fromJson(securityTokenJson);
        this.securityTokenContainer.getTokenContainer().set(securityToken);

        context.setEntityStream(new ByteArrayInputStream(payload.getBytes(StandardCharsets.UTF_8)));
    }
}
