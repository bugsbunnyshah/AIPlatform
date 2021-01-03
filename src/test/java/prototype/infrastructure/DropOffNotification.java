package prototype.infrastructure;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

public class DropOffNotification implements Serializable {
    private static Logger logger = LoggerFactory.getLogger(DropOffNotification.class);

    private SourceOrg sourceOrg;
    private Location location;
    private FoodRunner foodRunner;

    public DropOffNotification() {
    }

    public DropOffNotification(SourceOrg sourceOrg, Location location, FoodRunner foodRunner) {
        this.sourceOrg = sourceOrg;
        this.location = location;
        this.foodRunner = foodRunner;
    }

    public SourceOrg getSourceOrg() {
        return sourceOrg;
    }

    public void setSourceOrg(SourceOrg sourceOrg) {
        this.sourceOrg = sourceOrg;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public FoodRunner getFoodRunner() {
        return foodRunner;
    }

    public void setFoodRunner(FoodRunner foodRunner) {
        this.foodRunner = foodRunner;
    }

    public static DropOffNotification parse(String json)
    {
        DropOffNotification dropOffNotification = new DropOffNotification();

        JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
        if(jsonObject.has("sourceOrg")) {
            dropOffNotification.sourceOrg = SourceOrg.parse(jsonObject.get("sourceOrg").toString());
        }
        if(jsonObject.has("location")) {
            dropOffNotification.location = Location.parse(jsonObject.get("location").toString());
        }
        if(jsonObject.has("foodRunner")) {
            dropOffNotification.foodRunner = FoodRunner.parse(jsonObject.get("foodRunner").toString());
        }

        return dropOffNotification;
    }

    public JsonObject toJson()
    {
        JsonObject jsonObject = new JsonObject();
        if(this.foodRunner != null) {
            jsonObject.add("foodRunner", this.foodRunner.toJson());
        }
        if(this.sourceOrg != null) {
            jsonObject.add("sourceOrg", this.sourceOrg.toJson());
        }
        if(this.location != null) {
            jsonObject.add("location", this.location.toJson());
        }
        return jsonObject;
    }

    @Override
    public String toString()
    {
        return this.toJson().toString();
    }
}
