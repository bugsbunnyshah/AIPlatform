package io.bugsbunny.dataScience.model;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class AIModel implements PortableAIModelInterface{
    private static Logger logger = LoggerFactory.getLogger(Team.class);

    private String modelId;

    public AIModel() {
    }

    public String getModelId() {
        return modelId;
    }

    public void setModelId(String modelId) {
        this.modelId = modelId;
    }

    @Override
    public void load(String encodedModelString) {

    }

    @Override
    public void unload() {

    }

    @Override
    public double calculate() {
        return 0;
    }

    @Override
    public String toString() {
        return this.toJson().toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AIModel aiModel = (AIModel) o;
        return modelId.equals(aiModel.modelId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(modelId);
    }

    public JsonObject toJson(){
        JsonObject json = new JsonObject();

        if(this.modelId != null){
            json.addProperty("modelId", this.modelId);
        }

        return json;
    }

    public static AIModel parse(String jsonString){
        AIModel aiModel = new AIModel();

        JsonObject json = JsonParser.parseString(jsonString).getAsJsonObject();


        if(json.has("modelId")){
            aiModel.modelId = json.get("modelId").getAsString();
        }

        return aiModel;
    }
}
