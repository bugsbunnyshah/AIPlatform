package io.bugsbunny.data.history.endpoint;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.bugsbunny.data.history.service.PayloadReplayService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Path("replay")
public class DataReplay {
    private static Logger logger = LoggerFactory.getLogger(DataReplay.class);

    @Inject
    private PayloadReplayService payloadReplayService;

    @Path("map")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response map(@RequestBody String input)
    {
        try {
            JsonArray array = JsonParser.parseString(input).getAsJsonArray();

            String chainId = this.payloadReplayService.generateDiffChain(array);

            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("oid", chainId);

            Response response = Response.ok(jsonObject.toString()).build();
            return response;
        }
        catch(Exception e)
        {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("error", e.getMessage());
            return Response.status(500).entity(jsonObject.toString()).build();
        }
    }

    @Path("chain")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response chain(@QueryParam("oid") String oid)
    {
        List<JsonObject> diffChain = this.payloadReplayService.replayDiffChain(oid);
        return Response.ok(diffChain.toString()).build();
    }
}