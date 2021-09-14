package io.bugsbunny.dataScience.model;

import com.google.gson.JsonObject;

public interface PortableAIModelInterface
{
    public String getModelId();
    public void load(String encodedModelString);
    public void unload();
    public double calculate();
    public JsonObject toJson();
}