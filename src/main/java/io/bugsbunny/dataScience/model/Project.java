package io.bugsbunny.dataScience.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Project implements Serializable {
    private static Logger logger = LoggerFactory.getLogger(Team.class);

    private String projectId;
    private Team team;
    private List<Artifact> artifacts;

    public Project() {
        this.artifacts = new ArrayList<>();
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public List<Artifact> getArtifacts() {
        return artifacts;
    }

    public void addArtifact(Artifact artifact){
        this.artifacts.add(artifact);
    }

    public void setArtifacts(List<Artifact> artifacts) {
        this.artifacts = artifacts;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public JsonObject toJson(){
        JsonObject json = new JsonObject();

        if(this.projectId != null){
            json.addProperty("projectId", this.projectId);
        }

        if(this.team != null){
            json.add("team",this.team.toJson());
        }

        if(this.artifacts != null){
            json.add("artifacts",JsonParser.parseString(this.artifacts.toString()).getAsJsonArray());
        }

        return json;
    }

    public static Project parse(String jsonString){
        Project project = new Project();
        JsonObject json = JsonParser.parseString(jsonString).getAsJsonObject();

        if(json.has("projectId")){
            project.projectId = json.get("projectId").getAsString();
        }

        if(json.has("team")){
            project.team = Team.parse(json.get("team").getAsJsonObject().toString());
        }

        if(json.has("artifacts")){
            JsonArray array = json.get("artifacts").getAsJsonArray();
            for(int i=0; i<array.size(); i++){
                project.addArtifact(Artifact.parse(array.get(i).getAsJsonObject().toString()));
            }
        }

        return project;
    }

    @Override
    public String toString() {
        return this.toJson().toString();
    }
}
