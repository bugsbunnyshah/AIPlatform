package io.bugsbunny.data.history;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.UUID;

@ApplicationScoped
public class SourceNotificationEmitter {
    private static Logger logger = LoggerFactory.getLogger(SourceNotificationEmitter.class);

    @Inject
    private KafkaDaemon kafkaDaemon;

    public void emit(Object sourceNotification)
    {
        //TODO: cleanup
        MessageWindow messageWindow = null;

        //find the SourceNotifications with this time window
        //TODO: cleanup
        //JsonArray jsonArray = this.kafkaDaemon.readNotifications("TOPIC", messageWindow);

        //Broadcast each SourceNotification to the FoodRunner Network
        /*String destinationNotificationId = UUID.randomUUID().toString();
        DestinationNotification destinationNotification = new DestinationNotification();
        destinationNotification.setSourceNotification(sourceNotification);
        destinationNotification.setDestinationNotificationId(destinationNotificationId);
        JsonObject destinationNotificationJson = JsonParser.parseString(destinationNotification.toString()).getAsJsonObject();
        this.kafkaDaemon.produceData(DestinationNotification.TOPIC, destinationNotificationJson);*/
    }
}
