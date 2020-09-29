package io.bugsbunny.dashboard.endpoint;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.bugsbunny.dashboard.service.ModelTrafficService;
import io.bugsbunny.data.history.service.PayloadReplayService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;

@Path("dashboard")
public class Dashboard {
    private static Logger logger = LoggerFactory.getLogger(Dashboard.class);

    @Inject
    private ModelTrafficService modelTrafficService;

    /*@Path("modelTraffic")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response modelTraffic()
    {
        Map<String, List<JsonObject>> modelTraffic = this.modelTrafficService.getModelTraffic();
        return Response.ok(modelTraffic.toString()).build();
    }*/
}