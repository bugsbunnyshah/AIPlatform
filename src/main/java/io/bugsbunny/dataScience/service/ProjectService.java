package io.bugsbunny.dataScience.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.bugsbunny.dataScience.model.*;
import io.bugsbunny.infrastructure.MongoDBJsonStore;
import io.bugsbunny.preprocess.SecurityTokenContainer;
import io.bugsbunny.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class ProjectService {
    private static Logger logger = LoggerFactory.getLogger(ProjectService.class);

    @Inject
    private MongoDBJsonStore mongoDBJsonStore;

    @Inject
    private SecurityTokenContainer securityTokenContainer;

    @Inject
    private AIModelService aiModelService;

    @Inject
    private PackagingService packagingService;

    @Inject
    private ModelDataSetService modelDataSetService;

    @PostConstruct
    public void onStart(){

    }

    public Project readProject(String projectId)
    {
        return this.mongoDBJsonStore.readProject(this.securityTokenContainer.getTenant(),projectId);
    }

    public void addProject(Project project){
        this.mongoDBJsonStore.addProject(this.securityTokenContainer.getTenant(),project);
    }

    public void addScientist(String projectId,Scientist scientist)
    {
        Project project = this.mongoDBJsonStore.readProject(this.securityTokenContainer.getTenant(),projectId);
        project.getTeam().addScientist(scientist);
        this.mongoDBJsonStore.updateProject(this.securityTokenContainer.getTenant(),project);
    }

    public String addLakeArtifact(String projectId, JsonObject aiModelJson, JsonObject modelInput, Artifact artifact){
        //Store Model
        JsonObject deploymentJson = this.packagingService.performPackagingForDevelopment(aiModelJson.toString());
        String modelId = deploymentJson.get("modelId").getAsString();
        artifact.getAiModel().setModelId(modelId);

        //Store Model Input data..
        DataSet dataSet = new DataSet();
        dataSet.setDataSetId(UUID.randomUUID().toString());
        DataItem dataItem = DataItem.parse(modelInput.toString());
        dataSet.addDataItem(dataItem);
        artifact.setDataSet(dataSet);

        Project project = this.mongoDBJsonStore.readProject(this.securityTokenContainer.getTenant(),projectId);
        project.addArtifact(artifact);
        this.mongoDBJsonStore.updateProject(this.securityTokenContainer.getTenant(),project);

        return modelId;
    }

    public String addDataArtifact(String projectId, JsonObject aiModelJson, JsonArray modelInput, Artifact artifact){
        //Store Model
        JsonObject deploymentJson = this.packagingService.performPackagingForDevelopment(aiModelJson.toString());
        String modelId = deploymentJson.get("modelId").getAsString();
        artifact.getAiModel().setModelId(modelId);

        //Store Model Input data
        DataSet dataSet = new DataSet();
        dataSet.setDataSetId(UUID.randomUUID().toString());
        for(int i=0; i<modelInput.size();i++){
            JsonObject dataSetItemJson = modelInput.get(i).getAsJsonObject();
            JsonUtil.print(ProjectService.class,dataSetItemJson);

            String dataSetId = this.modelDataSetService.storeTrainingDataSet(dataSetItemJson);

            DataItem dataItem = new DataItem();
            dataItem.setDataSetId(dataSetId);
            dataItem.setDataLakeId("braineous_null");
            dataItem.setTenantId(this.securityTokenContainer.getTenant().getPrincipal());
            dataItem.setData(dataSetItemJson.toString());
            dataSet.addDataItem(dataItem);
        }
        artifact.setDataSet(dataSet);

        Project project = this.mongoDBJsonStore.readProject(this.securityTokenContainer.getTenant(),projectId);
        project.addArtifact(artifact);
        this.mongoDBJsonStore.updateProject(this.securityTokenContainer.getTenant(),project);

        return modelId;
    }

    public JsonObject evalModelFromData(String projectId,String artifactId){
        try {
            Project project = this.mongoDBJsonStore.readProject(this.securityTokenContainer.getTenant(), projectId);
            //JsonUtil.print(ProjectService.class, project.toJson());

            List<Artifact> artifacts = project.getArtifacts();
            Artifact artifact = null;
            //TODO: very minor maybe pull using a MongoQuery. This is perfectly fine also
            for (Artifact cour : artifacts) {
                if (cour.getArtifactId().equals(artifactId)) {
                    artifact = cour;
                    break;
                }
            }

            PortableAIModelInterface aiModel = artifact.getAiModel();
            String modelId = aiModel.getModelId();
            String[] dataSetIds = artifact.getDataSet().getDataSetIds();

            JsonObject evaluation = this.aiModelService.evalJavaDuringDevelopmentFromData(modelId, dataSetIds);
            return evaluation;
        }
        catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    public JsonObject evalModelDataFromLake(String projectId,String artifactId){
        try {
            Project project = this.mongoDBJsonStore.readProject(this.securityTokenContainer.getTenant(), projectId);
            //JsonUtil.print(ProjectService.class, project.toJson());

            List<Artifact> artifacts = project.getArtifacts();
            Artifact artifact = null;
            //TODO: very minor maybe pull using a MongoQuery. This is perfectly fine also
            for (Artifact cour : artifacts) {
                if (cour.getArtifactId().equals(artifactId)) {
                    artifact = cour;
                    break;
                }
            }

            PortableAIModelInterface aiModel = artifact.getAiModel();
            String modelId = aiModel.getModelId();
            String[] dataLakeIds = artifact.getDataSet().getDataLakeIds();

            JsonObject evaluation = this.aiModelService.evalJavaDuringDevelopmentFromLake(modelId, artifact, dataLakeIds);
            return evaluation;
        }
        catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    public void trainModel(){

    }

    public void deployModel(){

    }

    public void verifyDeployment(){

    }
}
