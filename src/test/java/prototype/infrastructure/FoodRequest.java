package prototype.infrastructure;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

public class FoodRequest implements Serializable {
    private static Logger logger = LoggerFactory.getLogger(FoodRequest.class);

    private String id;
    private Enum<FoodTypes> foodType;
    private SourceOrg sourceOrg;

    public FoodRequest()
    {

    }

    public FoodRequest(Enum<FoodTypes> foodType, SourceOrg sourceOrg)
    {
        this.foodType = foodType;
        this.sourceOrg = sourceOrg;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Enum<FoodTypes> getFoodType() {
        return foodType;
    }

    public void setFoodType(Enum<FoodTypes> foodType) {
        this.foodType = foodType;
    }

    public SourceOrg getSourceOrg() {
        return sourceOrg;
    }

    public void setSourceOrg(SourceOrg sourceOrg) {
        this.sourceOrg = sourceOrg;
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
        if(this.foodType != null) {
            jsonObject.addProperty("foodType", this.foodType.name());
        }
        if(this.sourceOrg != null) {
            jsonObject.add("sourceOrg", this.sourceOrg.toJson());
        }

        return jsonObject;
    }

    public static FoodRequest parse(String json)
    {
        FoodRequest foodRequest = new FoodRequest();

        JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();

        if(jsonObject.has("id")) {
            foodRequest.id = jsonObject.get("id").getAsString();
        }
        if(jsonObject.has("foodType")) {
            String foodTypeValue = jsonObject.get("foodType").getAsString();
            foodRequest.foodType = FoodTypes.valueOf(foodTypeValue);
        }
        if(jsonObject.has("sourceOrg")) {
            foodRequest.sourceOrg = SourceOrg.parse(jsonObject.get("sourceOrg").getAsJsonObject().toString());
        }

        return foodRequest;
    }
}
