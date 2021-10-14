package io.bugsbunny.dataScience.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.*;

public class Team implements Serializable {
    private static Logger logger = LoggerFactory.getLogger(Team.class);

    private Set<Scientist> scientists;

    public Team() {
        this.scientists = new LinkedHashSet();
    }

    public void addScientist(Scientist scientist){
        this.scientists.add(scientist);
    }

    public Set<Scientist> getScientists() {
        return scientists;
    }

    public void setScientists(Set<Scientist> scientists) {
        this.scientists = scientists;
    }

    @Override
    public String toString() {
        return this.toJson().toString();
    }

    public JsonObject toJson(){
        JsonObject json = new JsonObject();

        if(this.scientists != null){
            json.add("scientists",JsonParser.parseString(this.scientists.toString()).getAsJsonArray());
        }

        return json;
    }

    public static Team parse(String jsonString){
        Team team = new Team();

        JsonObject json = JsonParser.parseString(jsonString).getAsJsonObject();

        if(json.has("scientists")){
            Set<Scientist> scientists = new HashSet<>();
            JsonArray array = json.get("scientists").getAsJsonArray();
            for(int i=0; i<array.size();i++){
                Scientist cour = Scientist.parse(array.get(i).toString());
                scientists.add(cour);
            }
            team.scientists = scientists;
        }

        return team;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Team team = (Team) o;
        return scientists.equals(team.scientists);
    }

    @Override
    public int hashCode() {
        return Objects.hash(scientists);
    }
}
