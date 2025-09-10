package oxynsentinel.service;

import oxynsentinel.model.Project;
import oxynsentinel.model.dto.ProjectCreateRequest;

import java.util.List;

public interface ProjectServiceInterface {
    void createProject(ProjectCreateRequest req, String creatorEmail, List<String> roles);
    List<Project> getProjectsForUser(String userId);
    List<Project> getProjectsByOrganization(Long orgId, String userId);
    Project getProjectById(Long id, String userId);
    public void deleteProjectWithAccess(Long id, String userId, List<String> roles) ;
    public List<Project> listProjects(Long organizationId, String userId) ;
    }
