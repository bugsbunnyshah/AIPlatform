package io.bugsbunny.dataIngestion.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.bugsbunny.test.components.BaseTest;
import io.bugsbunny.util.JsonUtil;
import io.quarkus.test.junit.QuarkusTest;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.nio.charset.StandardCharsets;
import java.util.TimerTask;

import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
public class IngestionServiceTests extends BaseTest {
    private static Logger logger = LoggerFactory.getLogger(IngestionServiceTests.class);

    @Inject
    private IngestionService ingestionService;

    //@Test
    public void testGetIngestion() throws Exception
    {
        //TODO: temp code for debugging
        String dataLakeId = "-2586030430120757939";
        logger.info(this.ingestionService.readDataLakeData(dataLakeId).toString());
    }

    //TODO: Investigate why multiple fetch + map is not working
    @Test
    public void ingestData() throws Exception{
        String agentId = "ian";

        DataFetchAgent flightAgent = new FlightAgent();
        this.ingestionService.ingestData(agentId,"flight",flightAgent);

        Thread.sleep(20000);
    }

    private static class FlightAgent implements DataFetchAgent{

        @Override
        public JsonArray fetchData() throws FetchException{
            try {
                String responseJson = IOUtils.resourceToString("aviation/flights0.json", StandardCharsets.UTF_8,
                        Thread.currentThread().getContextClassLoader());
                JsonArray jsonArray = JsonParser.parseString(responseJson).getAsJsonObject().getAsJsonArray("data");

                System.out.println("************FETCHING_DATA************");

                return jsonArray;
            }
            catch(Exception e){
                throw new FetchException(e);
            }
        }
    }
}