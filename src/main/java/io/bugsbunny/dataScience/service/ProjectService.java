package io.bugsbunny.dataScience.service;

import io.bugsbunny.dataScience.model.Artifact;
import io.bugsbunny.dataScience.model.Project;
import io.bugsbunny.infrastructure.MongoDBJsonStore;
import io.bugsbunny.preprocess.SecurityTokenContainer;
import io.bugsbunny.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class ProjectService {
    private static Logger logger = LoggerFactory.getLogger(ProjectService.class);

    @Inject
    private MongoDBJsonStore mongoDBJsonStore;

    @Inject
    private SecurityTokenContainer securityTokenContainer;

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

    public void addArtifact(String projectId,Artifact artifact){
        this.mongoDBJsonStore.addArtifact(this.securityTokenContainer.getTenant(),projectId,artifact);
    }
}
