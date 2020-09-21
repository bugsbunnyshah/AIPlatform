package io.bugsbunny.dataScience.endpoint;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.bugsbunny.dataScience.service.AIModelService;
import jep.Interpreter;
import jep.MainInterpreter;
import jep.SharedInterpreter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("liveModel")
public class LiveModel
{
    private static Logger logger = LoggerFactory.getLogger(LiveModel.class);

    @Inject
    private AIModelService aiModelService;

    @Path("eval")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response eval(@RequestBody String input)
    {
        JsonObject jsonInput = JsonParser.parseString(input).getAsJsonObject();
        long modelId =  jsonInput.get("modelId").getAsLong();
        String eval = this.aiModelService.eval(modelId);
        Response response = Response.ok(eval).build();
        return response;
    }

    @Path("python")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response python()
    {
        try
        {
            MainInterpreter.setJepLibraryPath("/Users/babyboy/opt/anaconda3/lib/python3.8/site-packages/jep/jep.cpython-38-darwin.so");
            StringBuilder script = new StringBuilder();
            script.append("a = 'Passed'\n");
            script.append("b = 'Failed'\n");
            script.append("result = max(a,b)\n");
            script.append("print('hello world')\n");
            try (Interpreter interp = new SharedInterpreter()) {
                interp.exec(script.toString());
                String result = interp.getValue("result", String.class);
                if (!"Passed".equals(result)) {
                    throw new IllegalStateException(
                            "multi-line exec returned " + result);
                }
            }

            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("LD_LIBRARY_PATH", System.getenv("LD_LIBRARY_PATH"));
            Response response = Response.ok(jsonObject.toString()).build();
            return response;
        }
        catch(Exception e)
        {
            throw new RuntimeException(e);
        }
    }
}
