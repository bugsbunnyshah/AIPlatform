package io.bugsbunny.dataScience.service;

import io.bugsbunny.dataScience.model.AllModelTests;
import io.bugsbunny.dataScience.model.Artifact;
import io.bugsbunny.dataScience.model.Project;
import io.bugsbunny.test.components.BaseTest;
import io.bugsbunny.util.JsonUtil;
import io.quarkus.test.junit.QuarkusTest;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.json.Json;

@QuarkusTest
public class ProjectServiceTests extends BaseTest {
    private static Logger logger = LoggerFactory.getLogger(ProjectServiceTests.class);

    @Inject
    private ProjectService projectService;

    @Test
    public void addArtifact() throws Exception{
        Artifact artifact = AllModelTests.mockArtifact();
        Project project = AllModelTests.mockProject();
        project.getArtifacts().clear();

        String projectId = project.getProjectId();
        this.projectService.addProject(project);
        this.projectService.addArtifact(projectId,artifact);

        Project stored = this.projectService.readProject(projectId);
        JsonUtil.print(stored.toJson());
        assertEquals(stored.getProjectId(),project.getProjectId());
    }
}
