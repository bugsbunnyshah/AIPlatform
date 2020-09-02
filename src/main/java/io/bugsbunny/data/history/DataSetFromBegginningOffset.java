package io.bugsbunny.data.history;

import com.google.gson.JsonArray;

public class DataSetFromBegginningOffset {

    private JsonArray jsonArray;

    public DataSetFromBegginningOffset(JsonArray jsonArray)
    {
        this.jsonArray = jsonArray;
    }

    public JsonArray getJsonArray() {
        return jsonArray;
    }

    public void setJsonArray(JsonArray jsonArray) {
        this.jsonArray = jsonArray;
    }
}
