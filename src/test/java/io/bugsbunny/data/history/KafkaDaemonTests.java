package io.bugsbunny.data.history;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.bugsbunny.model.IngestionNotification;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
public class KafkaDaemonTests {
    private static Logger logger = LoggerFactory.getLogger(KafkaDaemonTests.class);

    @Inject
    private KafkaDaemon kafkaDaemon;

    @Test
    public void testStart() throws InterruptedException {
        this.kafkaDaemon.logStartUp();

        /*int counter=0;
        while(!this.kafkaDaemon.getActive()) {
            Thread.sleep(5000);
            if(counter++ == 3)
            {
                break;
            }
        }*/

        logger.info("****");
        logger.info("ABOUT_TO_PRODUCE_DATA");
        logger.info("****");

        List<String> notificationIds = new ArrayList<>();
        OffsetDateTime start = OffsetDateTime.now(ZoneOffset.UTC);
        OffsetDateTime end = start.plusMinutes(Duration.ofMinutes(10).toMinutes());
        MessageWindow messageWindow = new MessageWindow(start, end);
        for(int i=0; i<10; i++)
        {
            String notificationId = UUID.randomUUID().toString();
            IngestionNotification notification = new IngestionNotification();
            notification.setNotificationId(notificationId);
            notification.setMessageWindow(messageWindow);

            JsonObject jsonObject = notification.toJson();

            this.kafkaDaemon.produceData(IngestionNotification.TOPIC, jsonObject);
        }

        logger.info("****");
        logger.info("ABOUT_TO_ASSERT_DATA");
        logger.info("****");

        Thread.sleep(15000);
        this.kafkaDaemon.printNotificationsQueue();
        JsonArray jsonArray = this.kafkaDaemon.readNotifications(IngestionNotification.TOPIC, messageWindow);
        assertNotNull(jsonArray);
        logger.info(jsonArray.toString());
        //TODO: More asserts
    }

    /*@Test
    public void testAddingSourceNotifications() throws InterruptedException {
        this.kafkaDaemon.logStartUp();
        int counter=0;
        while(!this.kafkaDaemon.getActive()) {
            Thread.sleep(5000);
            if(counter++ == 15)
            {
                break;
            }
        }

        logger.info("****");
        logger.info("ABOUT_TO_PRODUCE_DATA");
        logger.info("****");

        List<String> notificationIds = new ArrayList<>();
        OffsetDateTime start = OffsetDateTime.now(ZoneOffset.UTC);
        OffsetDateTime end = start.plusMinutes(Duration.ofMinutes(10).toMinutes());
        MessageWindow messageWindow = new MessageWindow(start, end);
        JsonArray jsonArray = new JsonArray();
        for(int i=0; i<10; i++)
        {
            String sourceNotificationId = UUID.randomUUID().toString();
            SourceNotification sourceNotification = new SourceNotification();
            sourceNotification.setSourceNotificationId(sourceNotificationId);
            sourceNotification.setMessageWindow(messageWindow);

            notificationIds.add(sourceNotificationId);

            JsonObject jsonObject = JsonParser.parseString(sourceNotification.toString()).getAsJsonObject();

            this.kafkaDaemon.produceData(SourceNotification.TOPIC, jsonObject);
        }

        Thread.sleep(15000);

        logger.info("****");
        logger.info("ABOUT_TO_ASSERT_DATA");
        logger.info("****");

        jsonArray = this.kafkaDaemon.readNotifications(SourceNotification.TOPIC, messageWindow);
        logger.info("TIME_TO_ASSERT_SOURCE_NOTIFICATION");
        assertNotNull(jsonArray);
        logger.info(jsonArray.toString());
    }

    @Test
    public void testProduceActiveFoodRunnerData() throws InterruptedException {
        this.kafkaDaemon.logStartUp();
        int counter=0;
        while(!this.kafkaDaemon.getActive()) {
            Thread.sleep(5000);
            if(counter++ == 15)
            {
                break;
            }
        }

        logger.info("****");
        logger.info("ABOUT_TO_PRODUCE_DATA");
        logger.info("****");

        this.kafkaDaemon.addTopic(ActiveFoodRunnerData.TOPIC);

        ActiveFoodRunnerData activeFoodRunnerData = new ActiveFoodRunnerData(UUID.randomUUID().toString(),
                "44.9441", "-93.0852");
        List<ActiveFoodRunnerData> list = new ArrayList<>();
        list.add(activeFoodRunnerData);
        this.kafkaDaemon.produceActiveFoodRunnerData(ActiveFoodRunnerData.TOPIC, list);

        Thread.sleep(15000);

        logger.info("****");
        logger.info("ABOUT_TO_ASSERT_DATA");
        logger.info("****");

        OffsetDateTime start = OffsetDateTime.now(ZoneOffset.UTC);
        OffsetDateTime end = OffsetDateTime.now(ZoneOffset.UTC);
        MessageWindow messageWindow = new MessageWindow(start, end);
        String sourceNotificationId = UUID.randomUUID().toString();
        SourceNotification sourceNotification = new SourceNotification();
        sourceNotification.setSourceNotificationId(sourceNotificationId);
        sourceNotification.setMessageWindow(messageWindow);
        String latitude = "46.066667";
        String longitude = "11.116667";
        sourceNotification.setLatitude(latitude);
        sourceNotification.setLongitude(longitude);
        JsonArray foodRunners = this.kafkaDaemon.findTheClosestFoodRunner(sourceNotification);
        logger.info("....");
        logger.info(foodRunners.toString());
        logger.info("....");
    }*/
}
