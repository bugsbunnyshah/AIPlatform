package io.bugsbunny.endpoint;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.bugsbunny.restClient.OAuthClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;

import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/oauth")
public class OAuthAuthenticate
{
    private static Logger logger = LoggerFactory.getLogger(OAuthAuthenticate.class);

    @Inject
    private OAuthClient oAuthClient;

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("token")
    public Response authenticate(@RequestBody String json)
    {
        try
        {
            JsonObject request = JsonParser.parseString(json).getAsJsonObject();

            String clientId = request.get("client_id").getAsString();
            String clientSecret = request.get("client_secret").getAsString();

            JsonObject jsonObject = this.oAuthClient.getAccessToken(clientId, clientSecret);
            return Response.ok(jsonObject.toString()).build();
        }
        catch(Exception e)
        {
            logger.error(e.getMessage(), e);
            return Response.status(500).build();
        }
    }
}