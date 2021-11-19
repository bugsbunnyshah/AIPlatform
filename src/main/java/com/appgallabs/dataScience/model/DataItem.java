package com.appgallabs.dataScience.model;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Objects;

public class DataItem implements Serializable {
    private static Logger logger = LoggerFactory.getLogger(Team.class);

    private String tenantId;
    private String dataLakeId;
    private String chainId;
    private String dataSetId;
    private String data;

    public DataItem() {
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getDataLakeId() {
        return dataLakeId;
    }

    public void setDataLakeId(String dataLakeId) {
        this.dataLakeId = dataLakeId;
    }

    public String getChainId() {
        return chainId;
    }

    public void setChainId(String chainId) {
        this.chainId = chainId;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getDataSetId() {
        return dataSetId;
    }

    public void setDataSetId(String dataSetId) {
        this.dataSetId = dataSetId;
    }

    @Override
    public String toString() {
        return this.toJson().toString();
    }

    public JsonObject toJson(){
        JsonObject json = new JsonObject();


        if(this.tenantId != null){
            json.addProperty("tenantId", this.tenantId);
        }

        if(this.dataLakeId != null){
            json.addProperty("dataLakeId", this.dataLakeId);
        }

        if(this.chainId != null){
            json.addProperty("chainId", this.chainId);
        }

        if(this.dataSetId != null){
            json.addProperty("dataSetId", this.dataSetId);
        }

        if(this.data != null){
            json.addProperty("data", this.data);
        }

        return json;
    }

    public static DataItem parse(String jsonString){
        DataItem dataItem = new DataItem();

        JsonObject json = JsonParser.parseString(jsonString).getAsJsonObject();

        if(json.has("tenantId")){
            dataItem.tenantId = json.get("tenantId").getAsString();
        }

        if(json.has("dataLakeId")){
            dataItem.dataLakeId = json.get("dataLakeId").getAsString();
        }

        if(json.has("chainId")){
            dataItem.chainId = json.get("chainId").getAsString();
        }

        if(json.has("dataSetId")){
            dataItem.dataSetId = json.get("dataSetId").getAsString();
        }

        if(json.has("data")){
            dataItem.data = json.get("data").getAsString();
        }

        return dataItem;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DataItem dataItem = (DataItem) o;
        return tenantId.equals(dataItem.tenantId) && dataLakeId.equals(dataItem.dataLakeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tenantId, dataLakeId);
    }
}
