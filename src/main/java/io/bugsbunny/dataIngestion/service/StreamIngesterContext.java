package io.bugsbunny.dataIngestion.service;

import com.google.gson.JsonArray;

import java.io.Serializable;

public class StreamIngesterContext implements Serializable {

    private StreamIngesterQueue streamIngesterQueue;
    private static StreamIngester streamIngester = new StreamIngester();
    private static StreamIngesterContext streamIngesterContext = new StreamIngesterContext();

    private StreamIngesterContext()
    {
        this.streamIngesterQueue = new StreamIngesterQueue();
    }

    public static StreamIngester getStreamIngester()
    {
        if(StreamIngesterContext.streamIngester == null){
            StreamIngesterContext.streamIngester = new StreamIngester();
        }
        return StreamIngesterContext.streamIngester;
    }

    public static StreamIngesterContext getStreamIngesterContext()
    {
        if(StreamIngesterContext.streamIngesterContext == null){
            StreamIngesterContext.streamIngesterContext = new StreamIngesterContext();
        }
        return StreamIngesterContext.streamIngesterContext;
    }

    public String getData()
    {
        return this.streamIngesterQueue.getData();
    }

    public void setData(String data)
    {
        this.streamIngesterQueue.setData(data);
    }
}
