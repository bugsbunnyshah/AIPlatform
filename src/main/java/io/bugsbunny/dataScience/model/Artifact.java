package io.bugsbunny.dataScience.model;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

public class Artifact implements Serializable {
    private static Logger logger = LoggerFactory.getLogger(Team.class);

    private String artifactId;
    private PortableAIModelInterface aiModel;
    private DataSet dataSet;

    public Artifact() {
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

    @Override
    public String toString() {
        return this.toJson().toString();
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
            AIModel aiModel = new AIModel();
            aiModel.setModelId(json.get("aiModel").getAsJsonObject().get("modelId").getAsString());
            artifact.aiModel = aiModel;
        }

        return artifact;
    }
}
