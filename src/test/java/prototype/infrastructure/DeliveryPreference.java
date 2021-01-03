package prototype.infrastructure;

import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class DeliveryPreference {
    private List<FoodTypes> foodTypes;

    public DeliveryPreference() {
        this.foodTypes = new ArrayList<>();
    }

    public DeliveryPreference(List<FoodTypes> foodTypes) {
        this.foodTypes = foodTypes;
    }

    public List<FoodTypes> getFoodTypes() {
        return foodTypes;
    }

    public void setFoodTypes(List<FoodTypes> foodTypes) {
        this.foodTypes = foodTypes;
    }

    @Override
    public String toString()
    {
        return this.toJson().toString();
    }

    public JsonObject toJson()
    {
        JsonObject jsonObject = new JsonObject();
        return jsonObject;
    }

    public static DeliveryPreference parse(String json)
    {
        DeliveryPreference deliveryPreference = new DeliveryPreference();
        return deliveryPreference;
    }
}
