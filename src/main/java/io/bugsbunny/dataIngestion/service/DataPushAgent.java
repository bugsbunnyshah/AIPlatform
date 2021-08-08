package io.bugsbunny.dataIngestion.service;

import com.google.gson.JsonArray;

public interface DataPushAgent {
    public void receiveData(JsonArray json) throws FetchException;
}
