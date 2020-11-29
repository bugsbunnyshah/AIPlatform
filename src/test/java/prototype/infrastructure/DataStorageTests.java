package prototype.infrastructure;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.bugsbunny.data.history.service.DataReplayService;
import io.bugsbunny.test.components.BaseTest;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
public class DataStorageTests extends BaseTest {
    private static Logger logger = LoggerFactory.getLogger(DataStorageTests.class);

    @Inject
    private DataReplayService dataReplayService;

    @Test
    public void testProduceData() throws Exception
    {
        try {
            logger.info("***************");
            System.out.println(this.dataReplayService);
            String json = " {\"foodRunner\":{\"profile\":{\"id\":\"0e9f47c3-a447-496f-8162-0d62014d6975\",\"email\":\"bugs.bunny.shah@gmail.com\",\"mobile\":\"8675309\",\"photo\":\"\",\"password\":\"\",\"profileType\":\"FOOD_RUNNER\"},\"location\":{\"latitude\":30.25860595703125,\"longitude\":-97.74873352050781}},\"sourceOrg\":{\"orgId\":\"microsoft\",\"orgName\":\"Microsoft\",\"orgContactEmail\":\"melinda_gates@microsoft.com\",\"location\":{\"latitude\":0.0,\"longitude\":0.0}},\"location\":{\"latitude\":30.25860595703125,\"longitude\":-97.74873352050781}}";
            JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
            JsonObject modelChain = new JsonObject();
            Random random = new Random();
            modelChain.addProperty("modelId", random.nextLong());
            modelChain.add("payload", jsonObject);
            String oid = this.dataReplayService.generateDiffChain(modelChain);

            Response response = given().get("/replay/chain/?oid=" + oid).andReturn();
            logger.info("************************");
            logger.info(response.statusLine());
            response.body().prettyPrint();
            logger.info("************************");
            assertEquals(200, response.getStatusCode());
        }
        catch(Exception e)
        {
            logger.error(e.getMessage(), e);
            throw e;
        }
    }

    @Test
    public void testAddingDestinationNotifications() throws InterruptedException {
        List<String> notificationIds = new ArrayList<>();
        OffsetDateTime start = OffsetDateTime.now(ZoneOffset.UTC);
        OffsetDateTime end = start.plusMinutes(Duration.ofMinutes(10).toMinutes());
        MessageWindow messageWindow = new MessageWindow();
        messageWindow.setStart(start);
        messageWindow.setEnd(end);
        for(int i=0; i<10; i++)
        {
            String sourceNotificationId = UUID.randomUUID().toString();
            SourceNotification sourceNotification = new SourceNotification();
            sourceNotification.setSourceNotificationId(sourceNotificationId);
            sourceNotification.setMessageWindow(messageWindow);

            String destinationNotificationId = UUID.randomUUID().toString();
            DestinationNotification destinationNotification = new DestinationNotification();
            destinationNotification.setDestinationNotificationId(destinationNotificationId);
            destinationNotification.setSourceNotification(sourceNotification);

            notificationIds.add(destinationNotificationId);

            JsonObject jsonObject = JsonParser.parseString(destinationNotification.toString()).getAsJsonObject();

            logger.info(jsonObject.toString());
        }
    }

    @Test
    public void testAddingSourceNotifications() throws InterruptedException {
        List<String> notificationIds = new ArrayList<>();
        OffsetDateTime start = OffsetDateTime.now(ZoneOffset.UTC);
        OffsetDateTime end = start.plusMinutes(Duration.ofMinutes(10).toMinutes());
        MessageWindow messageWindow = new MessageWindow();
        messageWindow.setStart(start);
        messageWindow.setEnd(end);
        JsonArray jsonArray = new JsonArray();
        for(int i=0; i<10; i++)
        {
            String sourceNotificationId = UUID.randomUUID().toString();
            SourceNotification sourceNotification = new SourceNotification();
            sourceNotification.setSourceNotificationId(sourceNotificationId);
            sourceNotification.setMessageWindow(messageWindow);

            notificationIds.add(sourceNotificationId);

            JsonObject jsonObject = JsonParser.parseString(sourceNotification.toString()).getAsJsonObject();

            logger.info(jsonObject.toString());
        }
    }

    @Test
    public void testProduceActiveFoodRunnerData() throws InterruptedException {
        ActiveFoodRunnerData activeFoodRunnerData = new ActiveFoodRunnerData(UUID.randomUUID().toString(),
                "44.9441", "-93.0852");
        List<ActiveFoodRunnerData> list = new ArrayList<>();
        list.add(activeFoodRunnerData);

        Thread.sleep(15000);

        logger.info("****");
        logger.info("ABOUT_TO_ASSERT_DATA");
        logger.info("****");

        OffsetDateTime start = OffsetDateTime.now(ZoneOffset.UTC);
        OffsetDateTime end = OffsetDateTime.now(ZoneOffset.UTC);
        MessageWindow messageWindow = new MessageWindow();
        messageWindow.setStart(start);
        messageWindow.setEnd(end);
        String sourceNotificationId = UUID.randomUUID().toString();
        SourceNotification sourceNotification = new SourceNotification();
        sourceNotification.setSourceNotificationId(sourceNotificationId);
        sourceNotification.setMessageWindow(messageWindow);
        String latitude = "46.066667";
        String longitude = "11.116667";
        sourceNotification.setLatitude(latitude);
        sourceNotification.setLongitude(longitude);

        logger.info(sourceNotification.toJson().toString());
    }
}
