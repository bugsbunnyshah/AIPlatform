package prototype.infrastructure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

public class Profile implements Serializable {
    private static Logger logger = LoggerFactory.getLogger(Profile.class);

    private String id;
    private String email;
    private String mobile;
    private String photo;
    private String password;

    private String sourceOrgId;
    private ProfileType profileType;
    private Location location;

    public Profile()
    {

    }

    public Profile(String id, String email, String mobile, String photo, String password, ProfileType profileType)
    {
        this.id = id;
        this.email = email;
        this.mobile = mobile;
        this.photo = photo;
        this.password = password;
        this.profileType = profileType;
    }

    public Profile(String id, String email, String mobile, String photo, String password, ProfileType profileType, String sourceOrgId)
    {
        this(id,email,mobile,photo,password,profileType);
        this.sourceOrgId = sourceOrgId;
    }

    public Profile(String id, String email, String mobile, String photo, String password, ProfileType profileType, String sourceOrgId,
                   Location location)
    {
        this(id,email,mobile,photo,password,profileType, sourceOrgId);
        this.location = location;
    }

    public Profile(String id, String email, String mobile, String photo, String password, ProfileType profileType,
                   Location location)
    {
        this(id,email,mobile,photo,password,profileType);
        this.location = location;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public String getMobile()
    {
        return mobile;
    }

    public void setMobile(String mobile)
    {
        this.mobile = mobile;
    }

    public String getPhoto()
    {
        return photo;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setPhoto(String photo)
    {
        this.photo = photo;
    }

    public String getSourceOrgId() {
        return sourceOrgId;
    }

    public void setSourceOrgId(String sourceOrgId) {
        this.sourceOrgId = sourceOrgId;
    }

    public ProfileType getProfileType() {
        return profileType;
    }

    public void setProfileType(ProfileType profileType) {
        this.profileType = profileType;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    @Override
    public String toString()
    {
        return this.toJson().toString();
    }

    public JsonObject toJson()
    {
        JsonObject jsonObject = new JsonObject();

        if(this.id != null) {
            jsonObject.addProperty("id", this.id);
        }
        if(this.email != null) {
            jsonObject.addProperty("email", this.email);
        }
        if(this.mobile != null) {
            jsonObject.addProperty("mobile", this.mobile);
        }
        if(this.photo != null) {
            jsonObject.addProperty("photo", this.photo);
        }
        if(this.password != null)
        {
            jsonObject.addProperty("password", this.password);
        }
        if(this.sourceOrgId != null)
        {
            jsonObject.addProperty("sourceOrgId", this.sourceOrgId);
        }
        jsonObject.addProperty("profileType", this.profileType.name());

        if(this.location != null) {
            jsonObject.add("location", this.location.toJson());
        }

        return jsonObject;
    }

    public static Profile parse(String json)
    {
        Profile profile = new Profile();

        JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();

        if(jsonObject.has("id")) {
            profile.id = jsonObject.get("id").getAsString();
        }
        if(jsonObject.has("email")) {
            profile.email = jsonObject.get("email").getAsString();
        }
        if(jsonObject.has("mobile")) {
            profile.mobile = jsonObject.get("mobile").getAsString();
        }
        if(jsonObject.has("photo")) {
            profile.photo = jsonObject.get("photo").getAsString();
        }
        if(jsonObject.has("password")) {
            profile.password = jsonObject.get("password").getAsString();
        }
        if(jsonObject.has("sourceOrgId")) {
            profile.sourceOrgId = jsonObject.get("sourceOrgId").getAsString();
        }
        if(jsonObject.has("location")) {
            profile.location = Location.parse(jsonObject.get("location").getAsJsonObject().toString());
        }
        String profileTypeName = jsonObject.get("profileType").getAsString();
        profile.profileType = ProfileType.valueOf(profileTypeName);

        return profile;
    }
}
