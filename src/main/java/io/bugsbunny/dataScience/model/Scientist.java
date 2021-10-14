package io.bugsbunny.dataScience.model;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Objects;

public class Scientist implements Serializable {
    private static Logger logger = LoggerFactory.getLogger(Team.class);

    private String email;

    public Scientist() {
    }

    public Scientist(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return this.toJson().toString();
    }

    public JsonObject toJson(){
        JsonObject json = new JsonObject();

        if(this.email != null){
            json.addProperty("email",this.email);
        }

        return json;
    }

    public static Scientist parse(String jsonString){
        Scientist scientist = new Scientist();

        JsonObject json = JsonParser.parseString(jsonString).getAsJsonObject();

        if(json.has("email")){
            scientist.email = json.get("email").getAsString();
        }

        return scientist;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Scientist scientist = (Scientist) o;
        return email.equals(scientist.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email);
    }
}
