package io.bugsbunny.showcase.aviation;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.bugsbunny.dataIngestion.util.CSVDataUtil;
import io.bugsbunny.test.components.BaseTest;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.*;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class TrainAviationModelTests extends BaseTest
{
    private static Logger logger = LoggerFactory.getLogger(TrainAviationModelTests.class);

    //@Test
    public void testTrainAviationAIModel() throws Exception
    {
        Response response = given()
                .get("/aviation/train")
                .andReturn();
        response.body().prettyPrint();
        assertEquals(200, response.statusCode());
    }

    @Test
    public void preProcess() throws Exception
    {
        CSVDataUtil csvDataUtil = new CSVDataUtil();

        Map<String,String> features = new LinkedHashMap<>();
        features.put("date","flight_date");
        features.put("departureDelay","departure.delay");
        features.put("arrivalDelay","arrival.delay");

        String payload = IOUtils.toString(Thread.currentThread().getContextClassLoader()
                        .getResourceAsStream("aviation/flights0.json"),
                StandardCharsets.UTF_8);


        JsonArray data = JsonParser.parseString(payload).getAsJsonObject().get("data").getAsJsonArray();
        Iterator<JsonElement> dataObjects = data.iterator();
        JsonArray mlArray = new JsonArray();
        while(dataObjects.hasNext()){
            JsonObject dataObject = dataObjects.next().getAsJsonObject();
            JsonObject ml = new JsonObject();

            Set<Map.Entry<String,String>> entrySet = features.entrySet();
            for(Map.Entry<String,String> entry:entrySet){
                String feature = entry.getKey();
                String dataProperty = entry.getValue();

                JsonElement value = this.getProperty(dataObject,dataProperty);
                if(value != null && !value.isJsonNull() && value.isJsonPrimitive()){
                    ml.addProperty(feature,value.getAsString());
                }
                else{
                    ml.addProperty(feature,"0");
                }
            }

            mlArray.add(ml);
        }


        String result = csvDataUtil.convert(mlArray).get("data").getAsString();
        logger.info(result);
    }

    private JsonElement getProperty(JsonObject dataObject,String property)
    {
        JsonElement value = null;

        String[] propertyTokens = property.split("\\.");
        if(propertyTokens.length == 1) {
            value = dataObject.get(propertyTokens[0]);
        }
        else{
            value = dataObject.get(propertyTokens[0]);
            for(int i=1; i<propertyTokens.length;i++){
                value = value.getAsJsonObject().get(propertyTokens[i]);
            }
        }


        return value;
    }
}
