package prototype.infrastructure;

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
    public void testAddingDestinationNotifications() throws InterruptedException {
        OffsetDateTime start = OffsetDateTime.now(ZoneOffset.UTC);
        OffsetDateTime end = start.plusMinutes(Duration.ofMinutes(10).toMinutes());
        MessageWindow messageWindow = new MessageWindow();
        messageWindow.setStart(start);
        messageWindow.setEnd(end);
        Random random = new Random();
        for(int i=0; i<10; i++)
        {
            SourceOrg sourceOrg1 = new SourceOrg("microsoft", "Microsoft", "melinda_gates@microsoft.com");

            String sourceNotificationId = UUID.randomUUID().toString();
            SourceNotification sourceNotification = new SourceNotification();
            sourceNotification.setSourceNotificationId(sourceNotificationId);
            sourceNotification.setMessageWindow(messageWindow);
            sourceNotification.setSourceOrg(sourceOrg1);

            String destinationNotificationId = UUID.randomUUID().toString();
            DestinationNotification destinationNotification = new DestinationNotification();
            destinationNotification.setDestinationNotificationId(destinationNotificationId);
            destinationNotification.setSourceNotification(sourceNotification);
            SourceOrg destinationOrg = new SourceOrg("microsoft", "Microsoft", "melinda_gates@microsoft.com");
            Location location = new Location(30.25860595703125d,-97.74873352050781d);
            Profile profile = new Profile(UUID.randomUUID().toString(), "bugs.bunny.shah@gmail.com",
                    "8675309", "", "", ProfileType.FOOD_RUNNER, location);
            FoodRunner foodRunner = new FoodRunner(profile, location);
            DropOffNotification dropOffNotification = new DropOffNotification(destinationOrg, location, foodRunner);
            destinationNotification.setDropOffNotification(dropOffNotification);

            JsonObject jsonObject = JsonParser.parseString(destinationNotification.toString()).getAsJsonObject();

            JsonObject modelChain = new JsonObject();
            modelChain.addProperty("modelId", random.nextLong());
            modelChain.add("payload", jsonObject);
            String oid = this.dataReplayService.generateDiffChain(modelChain);
            logger.info("ChainId: "+oid);

            Response response = given().get("/replay/chain/?oid=" + oid).andReturn();
            logger.info("************************");
            logger.info(response.statusLine());
            response.body().prettyPrint();
            logger.info("************************");
            assertEquals(200, response.getStatusCode());
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
        Random random = new Random();
        for(int i=0; i<10; i++)
        {
            String sourceNotificationId = UUID.randomUUID().toString();
            SourceNotification sourceNotification = new SourceNotification();
            sourceNotification.setSourceNotificationId(sourceNotificationId);
            sourceNotification.setMessageWindow(messageWindow);

            notificationIds.add(sourceNotificationId);

            JsonObject jsonObject = JsonParser.parseString(sourceNotification.toString()).getAsJsonObject();

            JsonObject modelChain = new JsonObject();
            modelChain.addProperty("modelId", random.nextLong());
            modelChain.add("payload", jsonObject);
            String oid = this.dataReplayService.generateDiffChain(modelChain);
            logger.info("ChainId: "+oid);

            Response response = given().get("/replay/chain/?oid=" + oid).andReturn();
            logger.info("************************");
            logger.info(response.statusLine());
            response.body().prettyPrint();
            logger.info("************************");
            assertEquals(200, response.getStatusCode());
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

        Random random = new Random();
        JsonObject modelChain = new JsonObject();
        modelChain.addProperty("modelId", random.nextLong());
        modelChain.add("payload", sourceNotification.toJson());
        String oid = this.dataReplayService.generateDiffChain(modelChain);
        logger.info("ChainId: "+oid);

        Response response = given().get("/replay/chain/?oid=" + oid).andReturn();
        logger.info("************************");
        logger.info(response.statusLine());
        response.body().prettyPrint();
        logger.info("************************");
        assertEquals(200, response.getStatusCode());
    }
}
