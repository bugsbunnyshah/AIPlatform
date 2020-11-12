package io.bugsbunny.data.history.endpoint;

import com.google.gson.JsonObject;
import io.bugsbunny.data.history.service.DataReplayService;

import io.bugsbunny.dataIngestion.service.ChainNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("replay")
public class DataReplay {
    private static Logger logger = LoggerFactory.getLogger(DataReplay.class);

    @Inject
    private DataReplayService dataReplayService;

    @Path("chain")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response chain(@QueryParam("oid") String oid)
    {
        try {
            List<JsonObject> diffChain = this.dataReplayService.replayDiffChain(oid);
            return Response.ok(diffChain.toString()).build();
        }
        catch (ChainNotFoundException cne)
        {
            JsonObject error = new JsonObject();
            error.addProperty("exception", cne.getMessage());
            return Response.status(404).entity(error.toString()).build();
        }
        catch(Exception e)
        {
            logger.error(e.getMessage(), e);
            JsonObject error = new JsonObject();
            error.addProperty("exception", e.getMessage());
            return Response.status(500).entity(error.toString()).build();
        }
    }
}