package io.bugsbunny.dataIngestion.endpoint;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.bugsbunny.dataIngestion.service.IngestionService;
import io.bugsbunny.dataIngestion.service.MapperService;
import org.json.JSONObject;
import org.json.XML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;

import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("dataMapper")
public class DataMapper {
    private static Logger logger = LoggerFactory.getLogger(DataMapper.class);

    @Inject
    private MapperService mapperService;

    @Inject
    private IngestionService ingestionService;

    @Path("map")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response map(@RequestBody String input)
    {
        try
        {
            JsonObject jsonObject = JsonParser.parseString(input).getAsJsonObject();

            String sourceSchema = jsonObject.get("sourceSchema").getAsString();
            String destinationSchema = jsonObject.get("destinationSchema").getAsString();
            String sourceData = jsonObject.get("sourceData").getAsString();
            JsonArray array = JsonParser.parseString(sourceData).getAsJsonArray();

            JsonArray result = this.mapperService.map(sourceSchema, destinationSchema, array);
            this.ingestionService.ingestDevModelData(result.toString());

            Response response = Response.ok(result.toString()).build();
            return response;
        }
        catch(Exception e)
        {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("error", e.getMessage());
            return Response.status(500).entity(jsonObject.toString()).build();
        }
    }

    @Path("mapXml")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response mapXmlSourceData(@RequestBody String input)
    {
        try
        {
            JsonObject jsonObject = JsonParser.parseString(input).getAsJsonObject();

            String sourceSchema = jsonObject.get("sourceSchema").getAsString();
            String destinationSchema = jsonObject.get("destinationSchema").getAsString();
            String sourceData = jsonObject.get("sourceData").getAsString();
            JSONObject sourceJson = XML.toJSONObject(sourceData);
            String json = sourceJson.toString(4);
            JsonElement jsonElement = JsonParser.parseString(json);
            JsonArray array;
            if(jsonElement.isJsonObject())
            {
                array = new JsonArray();
                array.add(jsonElement);
            }
            else
            {
                array = jsonElement.getAsJsonArray();
            }

            JsonArray result = this.mapperService.map(sourceSchema, destinationSchema, array);
            this.ingestionService.ingestDevModelData(result.toString());

            Response response = Response.ok(result.toString()).build();
            return response;
        }
        catch(Exception e)
        {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("error", e.getMessage());
            return Response.status(500).entity(jsonObject.toString()).build();
        }
    }

    @Path("mapCsv")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response mapCsvSourceData(@RequestBody String input)
    {
        try
        {
            JsonObject jsonObject = JsonParser.parseString(input).getAsJsonObject();
            String sourceSchema = jsonObject.get("sourceSchema").getAsString();
            String destinationSchema = jsonObject.get("destinationSchema").getAsString();
            String sourceData = jsonObject.get("sourceData").getAsString();

            String[] lines = sourceData.split("\n");
            String header = lines[0];
            String[] columns = header.split(",");
            JsonArray array = new JsonArray();
            int length = lines.length;
            for(int i=1; i<length; i++)
            {
                String line = lines[i];
                String[] data = line.split(",");
                JsonObject row = new JsonObject();
                for(int j=0; j<data.length; j++)
                {
                    row.addProperty(columns[j],data[j]);
                }
                array.add(row);
            }
            JsonArray result = this.mapperService.map(sourceSchema,destinationSchema,array);
            this.ingestionService.ingestDevModelData(result.toString());

            Response response = Response.ok(result.toString()).build();
            return response;
        }
        catch(Exception e)
        {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("error", e.getMessage());
            return Response.status(500).entity(jsonObject.toString()).build();
        }
    }
}