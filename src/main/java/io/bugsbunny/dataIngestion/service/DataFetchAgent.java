package io.bugsbunny.dataIngestion.service;

import com.google.gson.JsonArray;

public interface DataFetchAgent {
    public JsonArray fetchData() throws FetchException;
}
