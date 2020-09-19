package io.bugsbunny.dataIngestion.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

@ApplicationScoped
public class CSVDataUtil {
    private static Logger logger = LoggerFactory.getLogger(CSVDataUtil.class);

    public JsonArray convert(String csvData)
    {
        JsonArray array = new JsonArray();

        String[] lines = csvData.split("\n");
        int length = lines.length;
        for (int i = 0; i < length; i++)
        {
            String line = lines[i];
            String[] data = line.split(",");
            JsonObject row = new JsonObject();
            for (int j = 0; j < data.length; j++) {
                String token = data[j];
                String property = "" + j;
                try {
                    Number number = NumberFormat.getInstance().parse(token);
                    row.addProperty(property, number);
                } catch (ParseException e) {
                    row.addProperty(property, token);
                }
            }
            array.add(row);
        }
        return array;
    }

    public String convert(JsonArray data)
    {
        StringBuilder csvBuilder = new StringBuilder();
        Iterator<JsonElement> rows = data.iterator();
        while(rows.hasNext())
        {
            JsonObject row = rows.next().getAsJsonObject();
            Set<Map.Entry<String, JsonElement>> entrySet = row.entrySet();
            int size = entrySet.size();
            int count = 0;
            for(Map.Entry<String, JsonElement> entry:entrySet)
            {
                csvBuilder.append(entry.getValue());
                if(count != size-1)
                {
                    csvBuilder.append(",");
                }
                count++;
            }
            csvBuilder.append("\n");
        }
        return csvBuilder.toString();
    }
}
