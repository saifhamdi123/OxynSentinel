package oxynsentinel.controller;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import oxynsentinel.model.Project;
import oxynsentinel.model.dto.ProjectCreateRequest;
import oxynsentinel.repository.UserOrganizationRepository;
import oxynsentinel.service.ProjectServiceInterface;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/main/projects")
public class ProjectController {
    @Autowired

    private final ProjectServiceInterface projectService;
    @Autowired

    private final UserOrganizationRepository userOrganizationRepository;



    @PostMapping("/create")
    public ResponseEntity<String> createProject(@RequestBody ProjectCreateRequest request,
                                                @RequestAttribute("roles") List<String> roles,
                                                @RequestAttribute("email") String email) {
        projectService.createProject(request, email, roles);
        return ResponseEntity.status(201).build();
    }

    @GetMapping
    public Object list(
            @RequestParam(required = false) Long organizationId,
            @RequestAttribute("userId") String userId) {
        List<Project> projects = projectService.listProjects(organizationId, userId);
        if (organizationId != null) {
            return java.util.Map.of("projects", projects);
        }
        return projects;
    }

    @GetMapping("/{project_id}")
    public Project getById(
            @RequestParam Long projectId,
            @RequestAttribute("userId") String userId
    ) {
        return projectService.getProjectById(projectId, userId);
    }

    @DeleteMapping("/{project_id}")
    public ResponseEntity<Void> delete(
            @RequestParam Long projectId,
            @RequestAttribute("userId") String userId,
            @RequestAttribute("roles") List<String> roles
    ) {
        projectService.deleteProjectWithAccess(projectId, userId, roles);
        return ResponseEntity.noContent().build(); // 204, empty body
    }
}
