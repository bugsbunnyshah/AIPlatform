package io.bugsbunny.dataScience.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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
import java.util.Map;
import java.util.Set;
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

    public Project storeModelForTraining(String packageString){
        try
        {
            Project project = new Project();
            project.setProjectId(UUID.randomUUID().toString());
            Artifact artifact = new Artifact();
            artifact.setArtifactId(UUID.randomUUID().toString());

            JsonObject modelPackage = JsonParser.parseString(packageString).getAsJsonObject();
            modelPackage.addProperty("live", false);
            String modelId = this.mongoDBJsonStore.storeModel(this.securityTokenContainer.getTenant(),modelPackage);

            JsonArray labels = modelPackage.get("labels").getAsJsonArray();
            JsonArray features = modelPackage.get("features").getAsJsonArray();
            JsonObject parameters = modelPackage.get("parameters").getAsJsonObject();
            AIModel aiModel = new AIModel();
            aiModel.setModelId(modelId);
            artifact.setAiModel(aiModel);

            for(int i=0; i<labels.size(); i++){
                JsonObject local = labels.get(i).getAsJsonObject();
                artifact.addLabel(new Label(local.get("value").getAsString(),local.get("field").getAsString()));
            }

            for(int i=0; i<features.size(); i++){
                JsonObject local = features.get(i).getAsJsonObject();
                artifact.addFeature(new Feature(local.get("value").getAsString()));
            }

            Set<Map.Entry<String,JsonElement>> entrySet = parameters.entrySet();
            for(Map.Entry<String,JsonElement> entry:entrySet){
                artifact.addParameter(entry.getKey(),entry.getValue().getAsString());
            }

            project.addArtifact(artifact);

            this.addProject(project);

            return this.readProject(project.getProjectId());
        }
        catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    public String getAiModel(String projectId, String artifactId){
        try{
            Project project = this.readProject(projectId);
            Artifact artifact = null;
            List<Artifact> artifacts = project.getArtifacts();
            for(Artifact local:artifacts){
                if(local.getArtifactId().equals(artifactId)){
                    String modelId = local.getAiModel().getModelId();
                    String model = this.mongoDBJsonStore.getModel(this.securityTokenContainer.getTenant(),modelId);
                    return model;
                }
            }
            return null;
        }
        catch (Exception e){
            throw new RuntimeException(e);
        }
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

    public List<Project> readProjects(){
        return this.mongoDBJsonStore.readProjects(this.securityTokenContainer.getTenant());
    }

    public Project readProject(String projectId)
    {
        return this.mongoDBJsonStore.readProject(this.securityTokenContainer.getTenant(),projectId);
    }

    public JsonObject trainModelFromData(String projectId, String artifactId){
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
            aiModel.setModelId(modelId);
            String[] dataSetIds = artifact.getDataSet().getDataSetIds();

            JsonObject evaluation = this.aiModelService.trainJavaFromDataSetInProject(artifact, dataSetIds);
            return evaluation;
        }
        catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    public JsonObject trainModelFromDataLake(String projectId, String artifactId){
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
            aiModel.setModelId(modelId);
            String[] dataLakeIds = artifact.getDataSet().getDataLakeIds();

            JsonObject evaluation = this.aiModelService.trainJavaFromDataLakeInProject(artifact, dataLakeIds);
            return evaluation;
        }
        catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    public void deployModel(Artifact artifact){
        try
        {
            this.aiModelService.deployModel(artifact.getAiModel().getModelId());
        }
        catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    public void verifyDeployment(JsonObject payload){
        try{

        }
        catch(Exception e){
            throw new RuntimeException(e);
        }
    }
}
