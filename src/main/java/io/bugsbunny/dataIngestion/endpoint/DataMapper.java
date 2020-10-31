package io.bugsbunny.dataIngestion.endpoint;

import com.google.gson.*;
import io.bugsbunny.dataIngestion.service.IngestionService;
import io.bugsbunny.dataIngestion.service.MapperService;
import io.bugsbunny.dataIngestion.util.CSVDataUtil;
import org.apache.commons.lang.StringUtils;
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
import java.text.NumberFormat;
import java.util.Iterator;

@Path("dataMapper")
public class DataMapper {
    private static Logger logger = LoggerFactory.getLogger(DataMapper.class);

    @Inject
    private MapperService mapperService;

    @Inject
    private IngestionService ingestionService;

    private CSVDataUtil csvDataUtil = new CSVDataUtil();

    @Path("map")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response map(@RequestBody String input)
    {
        try
        {
            JsonObject jsonObject = JsonParser.parseString(input).getAsJsonObject();

            String sourceData = jsonObject.get("sourceData").getAsString();
            JsonArray array = JsonParser.parseString(sourceData).getAsJsonArray();

            JsonArray result = this.mapperService.map(array);
            JsonObject responseJson  = this.ingestionService.ingestDevModelData(result.toString());

            Response response = Response.ok(responseJson.toString()).build();
            return response;
        }
        catch(Exception e)
        {
            logger.error(e.getMessage(), e);
            JsonObject error = new JsonObject();
            error.addProperty("exception", e.getMessage());
            return Response.status(500).entity(error.toString()).build();
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

            String xml = jsonObject.get("sourceData").getAsString();

            JSONObject sourceJson = XML.toJSONObject(xml);
            String json = sourceJson.toString(4);
            JsonObject sourceJsonObject = JsonParser.parseString(json).getAsJsonObject();

            JsonArray result = this.mapperService.mapXml(sourceJsonObject);
            JsonObject responseJson  = this.ingestionService.ingestDevModelData(result.toString());

            Response response = Response.ok(responseJson.toString()).build();
            return response;
        }
        catch(Exception e)
        {
            logger.error(e.getMessage(), e);
            JsonObject error = new JsonObject();
            error.addProperty("exception", e.getMessage());
            return Response.status(500).entity(error.toString()).build();
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
            String sourceData = jsonObject.get("sourceData").getAsString();
            boolean hasHeader = jsonObject.get("hasHeader").getAsBoolean();

            String[] lines = sourceData.split("\n");
            String[] columns = null;
            int head = 0;
            if(hasHeader) {
                head = 1;
                String header = lines[0];
                columns = header.split(",");
            }
            else
            {
                String top = lines[0];
                int columnCount = top.split(",").length;
                columns = new String[columnCount];
                for (int i = 0; i < columns.length; i++) {
                    columns[i] = "col" + (i+1);
                }
            }
            JsonArray array = new JsonArray();
            int length = lines.length;


            for(int i=head; i<length; i++)
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
            JsonArray result = this.mapperService.map(array);
            JsonObject responseJson  = this.ingestionService.ingestDevModelData(result.toString());

            Response response = Response.ok(responseJson.toString()).build();
            return response;
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