package io.bugsbunny.data.history;

import com.google.gson.JsonArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.RecursiveTask;

public class NotificationFinderTask extends RecursiveTask<JsonArray> {
    private static Logger logger = LoggerFactory.getLogger(NotificationFinderTask.class);

    private NotificationContext notificationContext;
    private Map<String,Map<String, JsonArray>> lookupTable;

    public NotificationFinderTask(NotificationContext notificationContext,Map<String,Map<String, JsonArray>> lookupTable) {
        this.notificationContext = notificationContext;
        this.lookupTable = lookupTable;
    }

    @Override
    protected JsonArray compute() {
        String topic = this.notificationContext.getTopic();
        MessageWindow messageWindow = this.notificationContext.getMessageWindow();
        String lookupIndex = messageWindow.getLookupTableIndex();

        logger.info("**************");
        logger.info("LookupIndex: "+lookupIndex);
        logger.info("**************");

        Map<String, JsonArray> topicTable = this.lookupTable.get(topic);
        if(topicTable == null)
        {
            return new JsonArray();
        }
        else if(topicTable.get(lookupIndex) == null)
        {
            return new JsonArray();
        }
        return topicTable.get(lookupIndex);
    }
}
