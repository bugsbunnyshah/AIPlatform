package io.bugsbunny.dataScience.model;

import com.github.wnameless.json.flattener.JsonFlattener;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.bugsbunny.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Artifact implements Serializable {
    private static Logger logger = LoggerFactory.getLogger(Team.class);

    private String artifactId;
    private PortableAIModelInterface aiModel;
    private DataSet dataSet;

    private List<Label> labels;
    private List<Feature> features;

    public Artifact() {
        this.labels = new ArrayList<>();
        this.features = new ArrayList<>();
    }

    public PortableAIModelInterface getAiModel() {
        return aiModel;
    }

    public void setAiModel(PortableAIModelInterface aiModel) {
        this.aiModel = aiModel;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public DataSet getDataSet() {
        return dataSet;
    }

    public void setDataSet(DataSet dataSet) {
        this.dataSet = dataSet;
    }

    public List<Label> getLabels() {
        return labels;
    }

    public void setLabels(List<Label> labels) {
        this.labels = labels;
    }

    public void addLabel(Label label){
        this.labels.add(label);
    }

    public List<Feature> getFeatures() {
        return features;
    }

    public void setFeatures(List<Feature> features) {
        this.features = features;
    }

    public void addFeature(Feature feature){
        this.features.add(feature);
    }

    public String convertJsonToCsv(JsonArray jsonArray){
        StringBuilder csvBuilder = new StringBuilder();
        csvBuilder.append(this.getHeader()+"\n");
        int size = jsonArray.size();
        int labelSize = this.labels.size();
        for(int i=0; i<size; i++) {
            JsonObject json = jsonArray.get(i).getAsJsonObject();
            Map<String, Object> fieldMap = JsonFlattener.flattenAsMap(json.toString());
            for(int j=0; j<labelSize;j++) {
                Label label = this.labels.get(j);
                String value = fieldMap.get(label.getField()).toString();
                if(j != labelSize-1) {
                    csvBuilder.append(value + ",");
                }
                else{
                    csvBuilder.append(value);
                }
            }
            csvBuilder.append("\n");
        }

        return csvBuilder.toString();
    }

    private String getHeader(){
        StringBuilder headerBuilder = new StringBuilder();
        for(Label cour: this.labels){
            headerBuilder.append(cour.getValue()+",");
        }

        String header = headerBuilder.toString().substring(0, headerBuilder.toString().length()-1);

        return header;
    }

    @Override
    public String toString() {
        return this.toJson().toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Artifact artifact = (Artifact) o;
        return artifactId.equals(artifact.artifactId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(artifactId);
    }

    public JsonObject toJson(){
        JsonObject json = new JsonObject();

        if(this.artifactId != null){
            json.addProperty("artifactId",this.artifactId);
        }

        if(this.dataSet != null){
            json.add("dataSet", this.dataSet.toJson());
        }

        if(this.aiModel != null){
            json.add("aiModel",this.aiModel.toJson());
        }

        if(this.labels != null){
            json.add("labels", JsonParser.parseString(this.labels.toString()).getAsJsonArray());
        }

        if(this.features != null){
            json.add("features", JsonParser.parseString(this.features.toString()).getAsJsonArray());
        }

        return json;
    }

    public static Artifact parse(String jsonString){
        Artifact artifact = new Artifact();

        JsonObject json = JsonParser.parseString(jsonString).getAsJsonObject();

        if(json.has("artifactId")){
            artifact.artifactId = json.get("artifactId").getAsString();
        }

        if(json.has("dataSet")){
            artifact.dataSet = DataSet.parse(json.get("dataSet").getAsJsonObject().toString());
        }

        if(json.has("aiModel") && json.get("aiModel").getAsJsonObject().has("modelId")){
            AIModel aiModel = AIModel.parse(json.get("aiModel").getAsJsonObject().toString());
            artifact.aiModel = aiModel;
        }

        if(json.has("labels")){
            JsonArray array = json.get("labels").getAsJsonArray();
            for(int i=0; i<array.size(); i++){
                JsonObject cour = array.get(i).getAsJsonObject();
                artifact.addLabel(new Label(cour.get("value").getAsString(),cour.get("field").getAsString()));
            }
        }

        if(json.has("features")){
            JsonArray array = json.get("features").getAsJsonArray();
            for(int i=0; i<array.size(); i++){
                JsonObject cour = array.get(i).getAsJsonObject();
                artifact.addFeature(new Feature(cour.get("value").getAsString()));
            }
        }

        return artifact;
    }
}
