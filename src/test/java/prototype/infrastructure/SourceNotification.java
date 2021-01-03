package prototype.infrastructure;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.OffsetDateTime;
import java.util.Iterator;

public class SourceNotification {
    private static Logger logger = LoggerFactory.getLogger(SourceNotification.class);

    public static final String TOPIC = "foodRunnerSyncProtocol_source_notification";

    private String sourceNotificationId;
    private String latitude = "0.0";
    private String longitude = "0.0";
    private MessageWindow messageWindow;
    private SourceOrg sourceOrg;

    public String getSourceNotificationId() {
        return sourceNotificationId;
    }

    public void setSourceNotificationId(String sourceNotificationId) {
        this.sourceNotificationId = sourceNotificationId;
    }

    public MessageWindow getMessageWindow() {
        return messageWindow;
    }

    public void setMessageWindow(MessageWindow messageWindow) {
        this.messageWindow = messageWindow;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public SourceOrg getSourceOrg() {
        return sourceOrg;
    }

    public void setSourceOrg(SourceOrg sourceOrg) {
        this.sourceOrg = sourceOrg;
    }

    @Override
    public String toString()
    {
        return this.toJson().toString();
    }

    public JsonObject toJson()
    {
        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("sourceNotificationId", this.sourceNotificationId);
        jsonObject.addProperty("startTimestamp", messageWindow.getStart().toEpochSecond());
        jsonObject.addProperty("endTimestamp", messageWindow.getEnd().toEpochSecond());
        jsonObject.addProperty("latitude", this.latitude);
        jsonObject.addProperty("longitude", this.longitude);
        jsonObject.add("sourceOrg", this.sourceOrg.toJson());

        return jsonObject;
    }

    public static SourceNotification parse(String json)
    {
        SourceNotification sourceNotification = new SourceNotification();

        JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
        if(jsonObject.has("sourceNotificationId")) {
            sourceNotification.sourceNotificationId = jsonObject.get("sourceNotificationId").getAsString();
        }
        if(jsonObject.has("latitude")) {
            sourceNotification.latitude = jsonObject.get("latitude").getAsString();
        }
        else
        {
            sourceNotification.latitude = "0.0";
        }
        if(jsonObject.has("longitude")) {
            sourceNotification.longitude = jsonObject.get("longitude").getAsString();
        }
        else
        {
            sourceNotification.longitude = "0.0";
        }
        if(jsonObject.has("messageWindow")) {
            Object messageWindowJson = jsonObject.get("messageWindow");
            if (messageWindowJson != null) {
                long start = jsonObject.get("start").getAsLong();
                long end = jsonObject.get("end").getAsLong();
                JsonArray messages = jsonObject.get("messages").getAsJsonArray();

                OffsetDateTime startTime = OffsetDateTime.parse("" + start);
                OffsetDateTime endTime = OffsetDateTime.parse("" + end);
                MessageWindow messageWindow = new MessageWindow();
                messageWindow.setStart(startTime);
                messageWindow.setEnd(endTime);
                Iterator<JsonElement> iterator = messages.iterator();
                while (iterator.hasNext()) {
                    messageWindow.addMessage((JsonObject) iterator.next());
                }
                sourceNotification.messageWindow = messageWindow;
            }
        }
        if(jsonObject.has("sourceOrg"))
        {
            JsonObject sourceOrgJson = jsonObject.get("sourceOrg").getAsJsonObject();
            sourceNotification.sourceOrg = SourceOrg.parse(sourceOrgJson.toString());
        }

        return sourceNotification;
    }
}
