package io.bugsbunny.dataScience.service;

import com.google.gson.JsonObject;

public abstract class TrainingWorkflowBase
{
    public abstract String startTraining(JsonObject trainingMetaData);
    public abstract String getData(String runId);
}
