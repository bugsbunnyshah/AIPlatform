package prototype.infrastructure;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DestinationNotification {
    private static Logger logger = LoggerFactory.getLogger(DestinationNotification.class);

    public static final String TOPIC = "foodRunnerSyncProtocol_destination_notification";

    private SourceNotification sourceNotification;
    private String destinationNotificationId;
    private DropOffNotification dropOffNotification;

    public SourceNotification getSourceNotification() {
        return sourceNotification;
    }

    public void setSourceNotification(SourceNotification sourceNotification) {
        this.sourceNotification = sourceNotification;
    }

    public String getDestinationNotificationId() {
        return destinationNotificationId;
    }

    public void setDestinationNotificationId(String destinationNotificationId) {
        this.destinationNotificationId = destinationNotificationId;
    }

    public DropOffNotification getDropOffNotification() {
        return dropOffNotification;
    }

    public void setDropOffNotification(DropOffNotification dropOffNotification) {
        this.dropOffNotification = dropOffNotification;
    }

    @Override
    public String toString()
    {
        JsonObject jsonObject = new JsonObject();

        jsonObject.add("sourceNotification", JsonParser.parseString(this.sourceNotification.toString()));
        jsonObject.addProperty("destinationNotificationId", this.destinationNotificationId);
        jsonObject.add("dropOffNotification", this.dropOffNotification.toJson());

        return jsonObject.toString();
    }
}
