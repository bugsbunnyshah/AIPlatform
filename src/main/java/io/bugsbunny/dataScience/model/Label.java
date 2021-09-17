package io.bugsbunny.dataScience.model;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Objects;

public class Label implements Serializable {
    private static Logger logger = LoggerFactory.getLogger(Label.class);

    private String value;
    private String field;

    public Label() {
    }

    public Label(String value, String field) {
        this.value = value;
        this.field = field;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public JsonObject toJson(){
        JsonObject json = new JsonObject();

        if(this.value != null){
            json.addProperty("value",this.value);
        }

        if(this.field != null){
            json.addProperty("field",this.field);
        }

        return json;
    }

    public static Label parse(String jsonString){
        Label label = new Label();
        JsonObject json = JsonParser.parseString(jsonString).getAsJsonObject();

        if(json.has("value")){
            label.value = json.get("value").getAsString();
        }

        if(json.has("field")){
            label.field = json.get("field").getAsString();
        }

        return label;
    }

    @Override
    public String toString() {
        return this.toJson().toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Label label = (Label) o;
        return value.equals(label.value) && field.equals(label.field);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, field);
    }
}
