package io.bugsbunny.dataIngestion.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class StreamIngesterQueue {

    public static JsonArray data;

    public static JsonArray getData()
    {
        return StreamIngesterQueue.data;
    }
}
