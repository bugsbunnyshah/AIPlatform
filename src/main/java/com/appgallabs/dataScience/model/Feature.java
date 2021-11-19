package com.appgallabs.dataScience.model;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Objects;

public class Feature implements Serializable {
    private static Logger logger = LoggerFactory.getLogger(Feature.class);

    private String value;

    public Feature() {
    }

    public Feature(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public JsonObject toJson(){
        JsonObject json = new JsonObject();

        if(this.value != null){
            json.addProperty("value",this.value);
        }

        return json;
    }

    public static Feature parse(String jsonString){
        Feature feature = new Feature();
        JsonObject json = JsonParser.parseString(jsonString).getAsJsonObject();

        if(json.has("value")){
            feature.value = json.get("value").getAsString();
        }


        return feature;
    }

    @Override
    public String toString() {
        return this.toJson().toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Feature feature = (Feature) o;
        return value.equals(feature.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
