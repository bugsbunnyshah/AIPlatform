package prototype.infrastructure;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class SourceOrg implements Serializable {
    private static Logger logger = LoggerFactory.getLogger(SourceOrg.class);

    private String orgId;
    private String orgName;
    private String orgContactEmail;
    private DeliveryPreference deliveryPreference;
    private List<Profile> profiles;
    private Location location;

    public SourceOrg()
    {
        this.profiles = new ArrayList<>();
        this.location = new Location(0.0d, 0.0d);
    }

    public SourceOrg(String orgId, String orgName, String orgContactEmail, DeliveryPreference deliveryPreference, List<Profile> profiles, Location location) {
        this();
        this.orgId = orgId;
        this.orgName = orgName;
        this.orgContactEmail = orgContactEmail;
        this.deliveryPreference = deliveryPreference;
        this.profiles = profiles;
        this.location = location;
    }

    public SourceOrg(String orgId, String orgName, String orgContactEmail)
    {
        this();
        this.orgId = orgId;
        this.orgName = orgName;
        this.orgContactEmail = orgContactEmail;
        this.deliveryPreference = new DeliveryPreference();
    }

    public String getOrgId() {
        return orgId;
    }

    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public String getOrgContactEmail() {
        return orgContactEmail;
    }

    public void setOrgContactEmail(String orgContactEmail) {
        this.orgContactEmail = orgContactEmail;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public DeliveryPreference getDeliveryPreference() {
        return deliveryPreference;
    }

    public void setDeliveryPreference(DeliveryPreference deliveryPreference) {
        this.deliveryPreference = deliveryPreference;
    }

    public List<Profile> getProfiles() {
        return profiles;
    }

    public void setProfiles(List<Profile> profiles) {
        this.profiles = profiles;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SourceOrg sourceOrg = (SourceOrg) o;
        return orgId.equals(sourceOrg.orgId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orgId);
    }

    @Override
    public String toString()
    {
        return this.toJson().toString();
    }

    public JsonObject toJson()
    {
        JsonObject jsonObject = new JsonObject();

        if(this.orgId != null) {
            jsonObject.addProperty("orgId", this.orgId);
        }
        if(this.orgName != null) {
            jsonObject.addProperty("orgName", this.orgName);
        }
        if(this.orgContactEmail != null) {
            jsonObject.addProperty("orgContactEmail", this.orgContactEmail);
        }

        if(!this.profiles.isEmpty())
        {
            JsonArray jsonArray = JsonParser.parseString(this.profiles.toString()).getAsJsonArray();
            jsonObject.add("profiles", jsonArray);
        }

        if(this.location != null)
        {
            JsonObject json = this.location.toJson();
            jsonObject.add("location", json);
        }

        return jsonObject;
    }

    public static SourceOrg parse(String json)
    {
        SourceOrg sourceOrg = new SourceOrg();

        JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();

        if(jsonObject.has("orgId")) {
            sourceOrg.orgId = jsonObject.get("orgId").getAsString();
        }
        if(jsonObject.has("orgName")) {
            sourceOrg.orgName = jsonObject.get("orgName").getAsString();
        }
        if(jsonObject.has("orgContactEmail")) {
            sourceOrg.orgContactEmail = jsonObject.get("orgContactEmail").getAsString();
        }
        if(jsonObject.has("profiles"))
        {
            JsonArray profiles = jsonObject.get("profiles").getAsJsonArray();
            Iterator<JsonElement> itr = profiles.iterator();
            while(itr.hasNext())
            {
                JsonElement jsonElement = itr.next();
                sourceOrg.getProfiles().add(Profile.parse(jsonElement.toString()));
            }
        }
        if(jsonObject.has("deliveryPreference")) {
            JsonArray jsonArray = jsonObject.getAsJsonArray("deliveryPreference");
            sourceOrg.deliveryPreference = DeliveryPreference.parse(jsonArray.toString());
        }
        if(jsonObject.has("location"))
        {
            JsonObject locationJson = jsonObject.getAsJsonObject("location");
            sourceOrg.location = Location.parse(locationJson.toString());
        }

        return sourceOrg;
    }
}
