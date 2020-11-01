package io.bugsbunny.dashboard.endpoint;

import io.bugsbunny.dashboard.service.ModelTrafficService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.*;

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