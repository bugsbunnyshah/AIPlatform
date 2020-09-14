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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Priority(2)
@Provider
public class AITrafficAgent implements ContainerRequestFilter, ContainerResponseFilter
{
    private static Logger logger = LoggerFactory.getLogger(AITrafficAgent.class);

    @Inject
    private PayloadReplayService payloadReplayService;

    @Inject
    private SecurityTokenContainer securityTokenContainer;

    private Map<String, String> tokenToRequestChainId = new HashMap<>();

    private Map<String, String> tokenToResponseChainId = new HashMap<>();

    @Override
    public void filter(ContainerRequestContext context) throws IOException
    {
        if(!context.getUriInfo().getRequestUri().getPath().contains("liveModel/eval"))
        {
            return;
        }

        String payload = IOUtils.toString(context.getEntityStream(), StandardCharsets.UTF_8);
        JsonElement input = JsonParser.parseString(payload);
        //logger.info("***********AITrafficAgent_Incoming**************");
        //logger.info(payload);
        //logger.info("************************************************");

        String requestChainId = this.getRequestChainId();
        if(requestChainId == null)
        {
            if(input.isJsonObject()) {
                requestChainId = this.payloadReplayService.generateDiffChain(input.getAsJsonObject());
            }
            else
            {
                requestChainId = this.payloadReplayService.generateDiffChain(input.getAsJsonArray());
            }
            this.setRequestChainId(requestChainId);
        }
        else
        {
            if(input.isJsonObject()) {
                this.payloadReplayService.addToDiffChain(requestChainId, input.getAsJsonObject());
            }
            else
            {
                this.payloadReplayService.addToDiffChain(requestChainId, input.getAsJsonArray());
            }
        }

        context.setEntityStream(new ByteArrayInputStream(payload.getBytes(StandardCharsets.UTF_8)));
    }

    @Override
    public void filter(ContainerRequestContext containerRequestContext, ContainerResponseContext containerResponseContext) throws IOException
    {
        if(!containerRequestContext.getUriInfo().getRequestUri().getPath().contains("liveModel/eval"))
        {
            return;
        }

        //Process the response
        Object entity = containerResponseContext.getEntity();
        //logger.info("***********AITrafficAgent_Outgoing**************");
        //logger.info(entity.toString());
        //logger.info("************************************************");

        JsonElement output = JsonParser.parseString(entity.toString());
        String responseChainId = this.getResponseChainId();
        if(responseChainId == null)
        {
            if(output.isJsonObject()) {
                responseChainId = this.payloadReplayService.generateDiffChain(output.getAsJsonObject());
            }
            else
            {
                responseChainId = this.payloadReplayService.generateDiffChain(output.getAsJsonArray());
            }
            this.setResponseChainId(responseChainId);
        }
        else
        {
            if(output.isJsonObject()) {
                JsonObject outputJson = output.getAsJsonObject();
                outputJson.addProperty("isResponse", Boolean.TRUE);
                this.payloadReplayService.addToDiffChain(responseChainId, outputJson);
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
                this.payloadReplayService.addToDiffChain(responseChainId, outputArray);
            }
        }
    }

    private String getRequestChainId()
    {
        String token = this.securityTokenContainer.getTokenContainer().get().getToken();
        String requestChainId = this.tokenToRequestChainId.get(token);
        return requestChainId;
    }

    private void setRequestChainId(String requestChainId)
    {
        String token = this.securityTokenContainer.getTokenContainer().get().getToken();
        this.tokenToRequestChainId.put(token, requestChainId);
    }

    private String getResponseChainId()
    {
        String token = this.securityTokenContainer.getTokenContainer().get().getToken();
        String responseChainId = this.tokenToResponseChainId.get(token);
        return responseChainId;
    }

    private void setResponseChainId(String responseChainId)
    {
        String token = this.securityTokenContainer.getTokenContainer().get().getToken();
        this.tokenToResponseChainId.put(token, responseChainId);
    }

    String findRequestChainId(String token)
    {
        String requestChainId = this.tokenToRequestChainId.get(token);
        return requestChainId;
    }

    String findResponseChainId(String token)
    {
        String responseChainId = this.tokenToResponseChainId.get(token);
        return responseChainId;
    }
}
