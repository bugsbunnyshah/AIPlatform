package io.bugsbunny.model;

import com.google.gson.JsonObject;
import io.bugsbunny.data.history.MessageWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

public class IngestionNotification implements Serializable {
    private static Logger logger = LoggerFactory.getLogger(IngestionNotification.class);

    public static String TOPIC = "ingestion_notification";

    private String notificationId;
    private MessageWindow messageWindow;

    public String getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
    }

    public MessageWindow getMessageWindow() {
        return messageWindow;
    }

    public void setMessageWindow(MessageWindow messageWindow) {
        this.messageWindow = messageWindow;
    }

    public JsonObject toJson()
    {
       JsonObject jsonObject = new JsonObject();

       jsonObject.addProperty("notificationId", this.notificationId);
       jsonObject.add("messageWindow", this.messageWindow.toJson());

       return jsonObject;
    }
}
