package com.appgallabs.dataScience.model;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class AIModel implements PortableAIModelInterface{
    private static Logger logger = LoggerFactory.getLogger(Team.class);

    private String modelId;
    private String language;

    public AIModel() {
    }

    public String getModelId() {
        return modelId;
    }

    public void setModelId(String modelId) {
        this.modelId = modelId;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
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
        return modelId.equals(aiModel.modelId) && language.equals(aiModel.language);
    }

    @Override
    public int hashCode() {
        return Objects.hash(modelId, language);
    }

    public JsonObject toJson(){
        JsonObject json = new JsonObject();

        if(this.modelId != null){
            json.addProperty("modelId", this.modelId);
        }

        if(this.language != null){
            json.addProperty("language", this.language);
        }

        return json;
    }

    public static AIModel parse(String jsonString){
        AIModel aiModel = new AIModel();

        JsonObject json = JsonParser.parseString(jsonString).getAsJsonObject();

        if(json.has("modelId")){
            aiModel.modelId = json.get("modelId").getAsString();
        }

        if(json.has("language")){
            aiModel.language = json.get("language").getAsString();
        }

        return aiModel;
    }
}
