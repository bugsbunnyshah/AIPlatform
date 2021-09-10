package io.bugsbunny.showcase.aviation;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.bugsbunny.dataIngestion.util.CSVDataUtil;
import io.bugsbunny.infrastructure.Tenant;
import io.bugsbunny.test.components.BaseTest;
import io.bugsbunny.util.BGNotificationReceiver;
import io.bugsbunny.util.BackgroundProcessListener;
import io.bugsbunny.util.JsonUtil;
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
public class AviationDataHistoryTests extends BaseTest
{
    private static Logger logger = LoggerFactory.getLogger(AviationDataHistoryTests.class);

    //@Test
    public void diffSamePayload() throws Exception{
        try {
            this.startIngester();
            String data = IOUtils.toString(Thread.currentThread().
                            getContextClassLoader().
                            getResourceAsStream("aviation/flights0.json"),
                    StandardCharsets.UTF_8);

            JsonArray json = JsonParser.parseString(data).getAsJsonObject().get("data").getAsJsonArray();
            //JsonUtil.print(json);

            JsonArray ingestion1 = new JsonArray();
            for (int i = 0; i < 2; i++) {
                ingestion1.add(json.get(i));
            }

            //JsonUtil.print(ingestion1);
            //JsonUtil.print(ingestion2);

            //Ingest1
            JsonObject r1 = this.sendData(ingestion1);
            //JsonUtil.print(r1);

            String dataLakeId = r1.get("dataLakeId").getAsString();
            String chainId = r1.get("chainId").getAsString();
            assertNotNull(dataLakeId);
            assertNotNull(chainId);

            /*JsonArray ingestion2 = new JsonArray();
            for (int i = 0; i < 2; i++) {
                JsonObject local = json.get(i).getAsJsonObject();
                local.addProperty("braineous_datalakeid",dataLakeId);
                local.addProperty("chainId",chainId);
                ingestion2.add(local);
            }

            //Ingest2
            //JsonObject r2 = this.sendData(ingestion2);
            //JsonUtil.print(r2);*/

            //Check the Data Chain
            this.handle(r1);

            //System.out.println("***********R2*************");
            //this.handle(r2);
        }
        finally {
            this.stopIngester();
        }
    }

    private void handle(JsonObject result){
        String dataLakeId = result.get("dataLakeId").getAsString();
        logger.info("********************************************************************************************************");
        logger.info("DATALAKE_ID: " + dataLakeId);
        logger.info("************************************************************************************************************");
        Response response = given().when().get("/dataMapper/readDataLakeObject/?dataLakeId=" + dataLakeId).andReturn();
        response.getBody().prettyPrint();
        JsonObject json = JsonParser.parseString(response.getBody().asString()).getAsJsonObject();
        JsonUtil.print(json);
        assertTrue(json.has("data"));
    }

    private JsonObject sendData(JsonArray ingestion) throws Exception{
        JsonUtil.print(ingestion);

        JsonObject input = new JsonObject();
        input.addProperty("sourceData", ingestion.toString());
        input.addProperty("entity", "flight");

        Response response;
        BGNotificationReceiver receiver = new BGNotificationReceiver();
        synchronized (receiver) {
            BackgroundProcessListener.getInstance().setThreshold(ingestion.size());
            BackgroundProcessListener.getInstance().setReceiver(receiver);

            response = given().body(input.toString()).when().post("/dataMapper/map/").andReturn();
            response.getBody().prettyPrint();
            assertEquals(200, response.getStatusCode());
            JsonObject returnValue = JsonParser.parseString(response.body().asString()).getAsJsonObject();
            String dataLakeId = returnValue.get("dataLakeId").getAsString();
            BackgroundProcessListener.getInstance().setDataLakeId(dataLakeId);

            receiver.wait();
            JsonUtil.print(receiver.getData());
        }

        return JsonParser.parseString(response.getBody().asString()).getAsJsonObject();
    }
}
