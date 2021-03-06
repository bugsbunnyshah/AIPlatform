package showcase.endpoint;

import com.google.gson.JsonObject;
import showcase.service.AviationDataIngestionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("aviation")
public class AviationDataIngestion
{
    private static Logger logger = LoggerFactory.getLogger(AviationDataIngestion.class);

    @Inject
    private AviationDataIngestionService aviationDataIngestionService;

    @Path("start")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response eval(@RequestBody String input)
    {
        this.aviationDataIngestionService.startIngestion();
        JsonObject success = new JsonObject();
        success.addProperty("success", true);
        Response response = Response.ok(success.toString()).build();
        return response;
    }

}
