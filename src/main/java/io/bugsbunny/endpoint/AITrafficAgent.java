package io.bugsbunny.endpoint;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.bugsbunny.data.history.service.PayloadReplayService;
import org.apache.commons.io.IOUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

@Priority(2)
@Provider
public class AITrafficAgent implements ContainerRequestFilter, ContainerResponseFilter
{
    private static Logger logger = LoggerFactory.getLogger(AITrafficAgent.class);

    @Inject
    private PayloadReplayService payloadReplayService;

    //TODO: cleanup
    private String chainId;
    private String responseChainId;

    @Override
    public void filter(ContainerRequestContext context) throws IOException
    {
        String payload = IOUtils.toString(context.getEntityStream(), StandardCharsets.UTF_8);
        JsonElement input = JsonParser.parseString(payload);
        logger.info("***********AITrafficAgent_Incoming**************");
        logger.info(payload);
        logger.info("************************************************");

        if(this.chainId == null)
        {
            if(input.isJsonObject()) {
                this.chainId = this.payloadReplayService.generateDiffChain(input.getAsJsonObject());
            }
            else
            {
                this.chainId = this.payloadReplayService.generateDiffChain(input.getAsJsonArray());
            }
        }
        else
        {
            if(input.isJsonObject()) {
                this.payloadReplayService.addToDiffChain(this.chainId, input.getAsJsonObject());
            }
            else
            {
                this.payloadReplayService.addToDiffChain(this.chainId, input.getAsJsonArray());
            }
        }

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

        JsonElement output = JsonParser.parseString(entity.toString());
        if(this.responseChainId == null)
        {
            if(output.isJsonObject()) {
                this.responseChainId = this.payloadReplayService.generateDiffChain(output.getAsJsonObject());
            }
            else
            {
                this.responseChainId = this.payloadReplayService.generateDiffChain(output.getAsJsonArray());
            }
        }
        else
        {
            if(output.isJsonObject()) {
                JsonObject outputJson = output.getAsJsonObject();
                outputJson.addProperty("isResponse", Boolean.TRUE);
                this.payloadReplayService.addToDiffChain(this.responseChainId, outputJson);
            }
            else
            {
                JsonArray outputArray = output.getAsJsonArray();
                Iterator<JsonElement> iterator = outputArray.iterator();
                while(iterator.hasNext())
                {
                    JsonElement element = iterator.next();
                    if(element.isJsonObject()) {
                        element.getAsJsonObject().addProperty("isResponse", Boolean.TRUE);
                    }
                    else
                    {
                        //TODO: Take care of nesting
                    }
                }
                this.payloadReplayService.addToDiffChain(this.responseChainId, outputArray);
            }
        }
    }
}
