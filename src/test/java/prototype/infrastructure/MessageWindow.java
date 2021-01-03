package prototype.infrastructure;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.time.OffsetDateTime;

public class MessageWindow {
    private OffsetDateTime start;
    private OffsetDateTime end;
    private JsonArray messages;
    private String topic;

    public MessageWindow()
    {

    }

    public MessageWindow(String topic, OffsetDateTime start, OffsetDateTime end)
    {
        this.start = start;
        this.end = end;
        this.messages = new JsonArray();
        this.topic = topic;
    }

    public OffsetDateTime getStart() {
        return start;
    }

    public OffsetDateTime getEnd() {
        return end;
    }

    public JsonArray getMessages() {
        return messages;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public void setStart(OffsetDateTime start) {
        this.start = start;
    }

    public void setEnd(OffsetDateTime end) {
        this.end = end;
    }

    public synchronized JsonArray getCopyOfMessages()
    {
        JsonArray copy = this.messages;
        this.messages = null;
        return copy;
    }

    public synchronized void addMessage(JsonObject jsonObject)
    {
        if(this.messages == null)
        {
            return;
        }
        this.messages.add(jsonObject);
    }

    public String getLookupTableIndex()
    {
        String lookupTableIndex = (this.start.toEpochSecond() + this.end.toEpochSecond())+"";
        lookupTableIndex = lookupTableIndex.substring(0, lookupTableIndex.length()-3);
        return lookupTableIndex;
    }

    @Override
    public String toString() {
        JsonObject json = new JsonObject();

        json.addProperty("start", this.start.toEpochSecond());
        json.addProperty("end", this.end.toEpochSecond());

        return json.toString();
    }
}
