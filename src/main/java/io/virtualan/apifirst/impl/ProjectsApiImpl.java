package io.virtualan.apifirst.impl;

import io.virtualan.apifirst.ProjectsApi;
import io.virtualan.apifirst.model.Project;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProjectsApiImpl implements ProjectsApi {

    @Override
    public ResponseEntity<Project> getProject(Integer projectId) {

        Project project = new Project();
        project.setProjectId(projectId);
        project.setDescription("The description of the project");
        project.setTitle("My awesome project");

        return new ResponseEntity<>(project, HttpStatus.OK);
    }
}
