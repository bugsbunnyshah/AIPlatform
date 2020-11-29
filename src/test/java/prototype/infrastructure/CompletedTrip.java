package prototype.infrastructure;

import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CompletedTrip {
    private static Logger logger = LoggerFactory.getLogger(CompletedTrip.class);

    private FoodRunner foodRunner;
    private DropOffNotification dropOffNotification;
    private PickupRequest pickupRequest;

    public CompletedTrip()
    {

    }

    public CompletedTrip(FoodRunner foodRunner, DropOffNotification dropOffNotification, PickupRequest pickupRequest) {
        this.foodRunner = foodRunner;
        this.dropOffNotification = dropOffNotification;
        this.pickupRequest = pickupRequest;
    }

    public FoodRunner getFoodRunner() {
        return foodRunner;
    }

    public void setFoodRunner(FoodRunner foodRunner) {
        this.foodRunner = foodRunner;
    }

    public DropOffNotification getDropOffNotification() {
        return dropOffNotification;
    }

    public void setDropOffNotification(DropOffNotification dropOffNotification) {
        this.dropOffNotification = dropOffNotification;
    }

    public PickupRequest getPickupRequest() {
        return pickupRequest;
    }

    public void setPickupRequest(PickupRequest pickupRequest) {
        this.pickupRequest = pickupRequest;
    }

    public JsonObject toJson()
    {
        JsonObject jsonObject = new JsonObject();
        if(this.foodRunner != null) {
            jsonObject.add("foodRunner", this.foodRunner.toJson());
        }
        //jsonObject.add("pickUpRequest", this.foodRunner.toJson());
        if(this.dropOffNotification != null) {
            jsonObject.add("dropOffNotification", this.dropOffNotification.toJson());
        }
        return jsonObject;
    }

    public static CompletedTrip parse(String json)
    {
        return null;
    }

    @Override
    public String toString() {
        return this.toJson().toString();
    }
}
