package prototype.infrastructure;

import java.time.OffsetDateTime;

public class OutstandingFoodRunnerNotification {
    private String foodRunnerId;
    private OffsetDateTime start;
    private OffsetDateTime estimatedTimeOfArrival;

    public OutstandingFoodRunnerNotification()
    {

    }

    public String getFoodRunnerId() {
        return foodRunnerId;
    }

    public void setFoodRunnerId(String foodRunnerId) {
        this.foodRunnerId = foodRunnerId;
    }

    public OffsetDateTime getStart() {
        return start;
    }

    public void setStart(OffsetDateTime start) {
        this.start = start;
    }

    public OffsetDateTime getEstimatedTimeOfArrival() {
        return estimatedTimeOfArrival;
    }

    public void setEstimatedTimeOfArrival(OffsetDateTime estimatedTimeOfArrival) {
        this.estimatedTimeOfArrival = estimatedTimeOfArrival;
    }
}
