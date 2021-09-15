package io.bugsbunny.dataScience.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class DataSet implements Serializable {
    private static Logger logger = LoggerFactory.getLogger(Team.class);

    private String dataSetId;
    List<DataItem> data;

    public DataSet() {
        this.data = new ArrayList<>();
    }

    public List<DataItem> getData() {
        return data;
    }

    public void setData(List<DataItem> data) {
        this.data = data;
    }

    public void addDataItem(DataItem dataItem){
        this.data.add(dataItem);
    }

    public String getDataSetId() {
        return dataSetId;
    }

    public void setDataSetId(String dataSetId) {
        this.dataSetId = dataSetId;
    }

    public List<String> getDataList(){
        List<String> dataList = new ArrayList<>();

        for(DataItem dataItem:this.data){
            dataList.add(dataItem.getData());
        }

        return dataList;
    }

    public String[] getDataSetIds(){
        String[] dataSetIds = new String[this.data.size()];

        int size = this.data.size();
        for(int i=0;i<size;i++){
            DataItem cour = this.data.get(i);
            dataSetIds[i] = cour.getDataSetId();
        }

        return dataSetIds;
    }

    public JsonObject toJson(){
        JsonObject json = new JsonObject();

        if(this.dataSetId != null){
            json.addProperty("dataSetId", this.dataSetId);
        }

        if(this.data != null){
            json.add("data",JsonParser.parseString(this.data.toString()).getAsJsonArray());
        }

        return json;
    }

    public static DataSet parse(String jsonString){
        DataSet dataSet = new DataSet();

        JsonObject json = JsonParser.parseString(jsonString).getAsJsonObject();


        if(json.has("dataSetId")){
            dataSet.dataSetId = json.get("dataSetId").getAsString();
        }

        if(json.has("data")){
            List<DataItem> dataItems = new ArrayList<>();
            JsonArray array = json.get("data").getAsJsonArray();
            for(int i=0; i<array.size();i++){
                DataItem cour = DataItem.parse(array.get(i).toString());
                dataItems.add(cour);
            }
            dataSet.data = dataItems;
        }


        return dataSet;
    }

    @Override
    public String toString() {
        return this.toJson().toString();
    }
}
