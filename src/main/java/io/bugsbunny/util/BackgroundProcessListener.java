package io.bugsbunny.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BackgroundProcessListener {
    private static Logger logger = LoggerFactory.getLogger(BackgroundProcessListener.class);

    private static BackgroundProcessListener singleton=null;

    public static BackgroundProcessListener getInstance(){
        if(singleton == null){
            singleton = new BackgroundProcessListener();
        }
        return BackgroundProcessListener.singleton;
    }

    private int threshold;
    private BGNotificationReceiver receiver;

    public BackgroundProcessListener(){
    }

    public int getThreshold() {
        return threshold;
    }

    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }

    public BGNotificationReceiver getReceiver() {
        return receiver;
    }

    public void setReceiver(BGNotificationReceiver receiver) {
        this.receiver = receiver;
    }

    public void decreaseThreshold(JsonObject jsonObject){
        this.threshold--;

        if(this.receiver != null && this.receiver.getData() != null) {
            this.receiver.getData().add(jsonObject);
            //JsonUtil.print(this.receiver.getData());
        }

        if(this.threshold == 0){
            this.notifyReceiver();
        }
    }

    public void clear(){
        BackgroundProcessListener.singleton = null;
    }
//-----------------------------------------------------
    private void notifyReceiver(){
        if(this.receiver != null){
            synchronized (this.receiver) {
                this.receiver.notifyAll();
            }
        }
    }
}
