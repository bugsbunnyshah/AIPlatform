package io.bugsbunny.dataScience.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.bugsbunny.dataScience.model.*;
import io.bugsbunny.infrastructure.MongoDBJsonStore;
import io.bugsbunny.preprocess.SecurityTokenContainer;
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
    private ModelDataSetService modelDataSetService;

    @PostConstruct
    public void onStart(){

    }

    public Project createArtifactForTraining(String scientist, JsonObject artifactData){
        try
        {
            Project project = new Project();
            project.setProjectId(UUID.randomUUID().toString());
            Artifact artifact = new Artifact();
            artifact.setArtifactId(UUID.randomUUID().toString());
            artifact.setScientist(scientist);

            //Store the AI Model
            artifactData.addProperty("live", false);
            String modelId = this.mongoDBJsonStore.storeModel(this.securityTokenContainer.getTenant(),artifactData);

            //Link to a Project
            JsonArray labels = artifactData.get("labels").getAsJsonArray();
            JsonArray features = artifactData.get("features").getAsJsonArray();
            JsonObject parameters = artifactData.get("parameters").getAsJsonObject();
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
            project.getTeam().addScientist(new Scientist(scientist));

            this.addProject(project);

            return this.readProject(project.getProjectId());
        }
        catch(Exception e){
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


    public String getAiModel(String projectId, String artifactId){
        try{
            Project project = this.readProject(projectId);
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
