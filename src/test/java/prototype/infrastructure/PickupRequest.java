package prototype.infrastructure;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

public class PickupRequest implements Serializable,Comparable {
    private static Logger logger = LoggerFactory.getLogger(PickupRequest.class);

    private String requestId;
    private SourceOrg sourceOrg;

    public PickupRequest() {
    }

    public PickupRequest(String requestId, SourceOrg sourceOrg) {
        this.requestId = requestId;
        this.sourceOrg = sourceOrg;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId)
    {
        this.requestId = requestId;
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

        if(this.sourceOrg != null) {
            jsonObject.add("sourceOrg", this.sourceOrg.toJson());
        }

        return jsonObject;
    }

    public static PickupRequest parse(String jsonBody)
    {
        PickupRequest pickupRequest = new PickupRequest();
        SourceOrg sourceOrg = new SourceOrg();
        pickupRequest.setSourceOrg(sourceOrg);
        JsonObject jsonObject = JsonParser.parseString(jsonBody).getAsJsonObject();

        if(jsonObject.has("orgId")) {
            sourceOrg.setOrgId(jsonObject.get("orgId").getAsString());
        }
        if(jsonObject.has("orgName")) {
            sourceOrg.setOrgName(jsonObject.get("orgName").getAsString());
        }
        if(jsonObject.has("orgContactEmail")) {
            sourceOrg.setOrgContactEmail(jsonObject.get("orgContactEmail").getAsString());
        }

        return pickupRequest;
    }

    @Override
    public int compareTo(Object o) {
        return 0;
    }
}
