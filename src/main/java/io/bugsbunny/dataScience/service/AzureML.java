package io.bugsbunny.dataScience.service;

import com.google.gson.JsonObject;
import io.bugsbunny.restClient.AzureMLClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class AzureML extends TrainingWorkflowBase
{
    private static Logger log = LoggerFactory.getLogger(AzureML.class);

    @Inject
    private AzureMLClient azureMLClient;

    @Override
    public String startTraining(JsonObject trainingMetaData)
    {
        return "blah";
    }

    @Override
    public String getData(String runId)
    {
        String query = "{microsoft}";
        JsonObject jsonObject = this.azureMLClient.search(query);
        return jsonObject.toString();
    }
}
