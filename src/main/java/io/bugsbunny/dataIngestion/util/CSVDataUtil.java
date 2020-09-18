package io.bugsbunny.dataIngestion.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CSVDataUtil {
    private static Logger logger = LoggerFactory.getLogger(CSVDataUtil.class);

    public JsonArray convert(String csvData)
    {
        JsonArray array = new JsonArray();

        String[] lines = csvData.split("\n");
        int length = lines.length;
        for(int i=0; i<length; i++)
        {
            String line = lines[i];
            String[] data = line.split(",");
            JsonObject row = new JsonObject();
            for(int j=0; j<data.length; j++)
            {
                row.addProperty(""+j,data[j]);
            }
            array.add(row);
        }

        return array;
    }
}
