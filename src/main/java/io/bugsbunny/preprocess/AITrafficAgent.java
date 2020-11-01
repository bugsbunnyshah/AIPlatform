package io.bugsbunny.preprocess;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.bugsbunny.data.history.service.DataReplayService;
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
import java.util.Map;

@Priority(2)
@Provider
public class AITrafficAgent implements ContainerRequestFilter, ContainerResponseFilter
{
    private static Logger logger = LoggerFactory.getLogger(AITrafficAgent.class);

    @Inject
    private DataReplayService dataReplayService;

    @Inject
    private SecurityTokenContainer securityTokenContainer;

    @Inject
    private AITrafficContainer aiTrafficContainer;

    private Map<String, String> tokenToRequestChainId = new HashMap<>();

    private Map<String, String> tokenToResponseChainId = new HashMap<>();

    @Override
    public void filter(ContainerRequestContext context) throws IOException
    {
        if(!context.getUriInfo().getRequestUri().getPath().contains("liveModel/") &&
                !context.getUriInfo().getRequestUri().getPath().contains("trainModel/") &&
                !context.getUriInfo().getRequestUri().getPath().contains("remoteModel/")
        )
        {
            return;
        }

        String payload = IOUtils.toString(context.getEntityStream(), StandardCharsets.UTF_8);
        if(payload == null || payload.length() == 0)
        {
            return;
        }

        JsonElement input;
        try
        {
           input  = JsonParser.parseString(payload);
           if(!input.isJsonObject() && !input.isJsonArray())
           {
               return;
           }
        }
        catch (Exception e)
        {
            return;
        }

        String requestChainId = this.getRequestChainId();
        if(requestChainId == null)
        {
            if(input.isJsonObject())
            {
                requestChainId = this.dataReplayService.generateDiffChain(input.getAsJsonObject());
            }
            else
            {
                requestChainId = this.dataReplayService.generateDiffChain(input.getAsJsonArray());
            }
            this.setRequestChainId(requestChainId);
        }
        else
        {
            if(input.isJsonObject())
            {
                this.dataReplayService.addToDiffChain(requestChainId, input.getAsJsonObject());
            }
            else
            {
                this.dataReplayService.addToDiffChain(requestChainId, input.getAsJsonArray());
            }
        }

        this.aiTrafficContainer.setChainId(requestChainId);

        context.setEntityStream(new ByteArrayInputStream(payload.getBytes(StandardCharsets.UTF_8)));
    }

    @Override
    public void filter(ContainerRequestContext context, ContainerResponseContext containerResponseContext) throws IOException
    {
        if(!context.getUriInfo().getRequestUri().getPath().contains("liveModel/") &&
                !context.getUriInfo().getRequestUri().getPath().contains("trainModel/") &&
                !context.getUriInfo().getRequestUri().getPath().contains("remoteModel/")
        )
        {
            return;
        }

        //Process the response
        Object entity = containerResponseContext.getEntity();
        if(entity == null)
        {
            return;
        }
        String entityString = entity.toString();
        if(entityString == null || entityString.length() == 0)
        {
            return;
        }

        JsonElement output;
        try
        {
            output  = JsonParser.parseString(entityString);
            if(!output.isJsonObject() && !output.isJsonArray())
            {
                return;
            }
        }
        catch (Exception e)
        {
            return;
        }

        String responseChainId = this.getResponseChainId();
        if(responseChainId == null)
        {
            if(output.isJsonObject()) {
                responseChainId = this.dataReplayService.generateDiffChain(output.getAsJsonObject());
            }
            else
            {
                responseChainId = this.dataReplayService.generateDiffChain(output.getAsJsonArray());
            }
            this.setResponseChainId(responseChainId);
        }
        else
        {
            String requestChainId = this.getRequestChainId();
            if(output.isJsonObject()) {
                JsonObject outputJson = output.getAsJsonObject();
                this.dataReplayService.addToDiffChain(requestChainId, responseChainId, outputJson);
            }
            else
            {
                JsonArray outputArray = output.getAsJsonArray();
                this.dataReplayService.addToDiffChain(requestChainId, responseChainId, outputArray);
            }
        }
    }

    private String getRequestChainId()
    {
        String token = this.securityTokenContainer.getSecurityToken().getToken();
        String requestChainId = this.tokenToRequestChainId.get(token);
        return requestChainId;
    }

    private void setRequestChainId(String requestChainId)
    {
        String token = this.securityTokenContainer.getSecurityToken().getToken();
        this.tokenToRequestChainId.put(token, requestChainId);
    }

    private String getResponseChainId()
    {
        String token = this.securityTokenContainer.getSecurityToken().getToken();
        String responseChainId = this.tokenToResponseChainId.get(token);
        return responseChainId;
    }

    private void setResponseChainId(String responseChainId)
    {
        String token = this.securityTokenContainer.getSecurityToken().getToken();
        this.tokenToResponseChainId.put(token, responseChainId);
    }

    public String findRequestChainId(String token)
    {
        String requestChainId = this.tokenToRequestChainId.get(token);
        return requestChainId;
    }

    public String findResponseChainId(String token)
    {
        String responseChainId = this.tokenToResponseChainId.get(token);
        return responseChainId;
    }
}
