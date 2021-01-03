package prototype.infrastructure;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Objects;

public class Location implements Serializable {
    private static Logger logger = LoggerFactory.getLogger(Location.class);

    private double latitude = 0.0d;
    private double longitude = 0.0d;

    public Location() {
    }

    public Location(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public JsonObject toJson()
    {
        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("latitude", this.latitude);
        jsonObject.addProperty("longitude", this.longitude);

        return jsonObject;
    }

    public static Location parse(String json)
    {
        Location location = new Location();

        JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
        if(jsonObject.has("latitude")) {
            location.latitude = jsonObject.get("latitude").getAsDouble();
        }
        else
        {
            location.latitude = 0.0d;
        }
        if(jsonObject.has("longitude")) {
            location.longitude = jsonObject.get("longitude").getAsDouble();
        }
        else
        {
            location.longitude = 0.0d;
        }

        return location;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Location location = (Location) o;
        return Double.compare(location.latitude, latitude) == 0 &&
                Double.compare(location.longitude, longitude) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(latitude, longitude);
    }
}
