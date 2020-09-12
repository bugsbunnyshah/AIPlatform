package io.bugsbunny.endpoint;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.bugsbunny.data.history.service.PayloadReplayService;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Provider
public class OAuthAgent implements ContainerRequestFilter, ContainerResponseFilter
{
    private static Logger logger = LoggerFactory.getLogger(OAuthAgent.class);

    @Override
    public void filter(ContainerRequestContext context) throws IOException
    {
        String payload = IOUtils.toString(context.getEntityStream(), StandardCharsets.UTF_8);
        //JsonObject input = JsonParser.parseString(payload).getAsJsonObject();
        logger.info("***********OAuthAgent_Incoming**************");
        logger.info(payload);
        logger.info("************************************************");

        context.setEntityStream(new ByteArrayInputStream(payload.getBytes(StandardCharsets.UTF_8)));
    }

    @Override
    public void filter(ContainerRequestContext containerRequestContext,
                       ContainerResponseContext containerResponseContext) throws IOException
    {
    }
}
