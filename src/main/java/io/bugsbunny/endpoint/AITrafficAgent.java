package io.bugsbunny.endpoint;

import org.apache.commons.io.IOUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Provider
public class AITrafficAgent implements ContainerRequestFilter, ContainerResponseFilter
{
    private static Logger logger = LoggerFactory.getLogger(AITrafficAgent.class);

    @Override
    public void filter(ContainerRequestContext context) throws IOException
    {
        String payload = IOUtils.toString(context.getEntityStream(), StandardCharsets.UTF_8);
        logger.info("***********AITrafficAgent_Incoming**************");
        logger.info(payload);
        logger.info("************************************************");

        context.setEntityStream(new ByteArrayInputStream(payload.getBytes(StandardCharsets.UTF_8)));
    }

    @Override
    public void filter(ContainerRequestContext containerRequestContext, ContainerResponseContext containerResponseContext) throws IOException
    {
        //Process the response
        Object entity = containerResponseContext.getEntity();
        logger.info("***********AITrafficAgent_Outgoing**************");
        logger.info(entity.toString());
        logger.info("************************************************");
    }
}
