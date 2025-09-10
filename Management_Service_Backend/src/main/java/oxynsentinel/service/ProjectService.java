package oxynsentinel.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import oxynsentinel.model.Organization;
import oxynsentinel.model.Project;
import oxynsentinel.model.dto.ProjectCreateRequest;
import oxynsentinel.repository.OrganizationRepository;
import oxynsentinel.repository.ProjectRepository;
import oxynsentinel.repository.UserOrganizationRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProjectService implements ProjectServiceInterface {

    private final ProjectRepository projectRepository;
    private final OrganizationRepository organizationRepository;
    private final UserOrganizationRepository userOrganizationRepository;

    public ProjectService(ProjectRepository projectRepository,
                          OrganizationRepository organizationRepository,
                          UserOrganizationRepository userOrganizationRepository) {
        this.projectRepository = projectRepository;
        this.organizationRepository = organizationRepository;
        this.userOrganizationRepository = userOrganizationRepository;
    }

    @Override
    public void createProject(ProjectCreateRequest req, String creatorEmail, List<String> roles) {
        // Python: if admin or developer missing -> 403 "unauthorized user or invalid token"
        if (roles == null || (!roles.contains("admin") && !roles.contains("developer"))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "unauthorized user or invalid token");
        }

        // Duplicate name within same organization -> 400
        boolean exists = projectRepository.existsByNameAndOrganizationId(req.getName(), req.getOrganizationId());
        if (exists) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Project name already exists in this organization");
        }

        // Ensure organization exists (Python sample didn’t check, but safer to keep)
        Organization organization = organizationRepository.findById(req.getOrganizationId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "organization not found"));

        Project project = new Project();
        project.setName(req.getName());
        project.setDescription(req.getDescription());
        project.setType(req.getType());
        project.setUrl(req.getUrl());
        project.setCreator(creatorEmail);
        project.setCreationDate(LocalDateTime.now());
        project.setOrganization(organization);

        projectRepository.save(project);
    }

    /** Mirrors GET /api/v1/main/projects
     *  - if organizationId provided: return all projects of that org (no access check)
     *  - else: return only projects in user’s accessible organizations
     */
    public List<Project> listProjects(Long organizationId, String userId) {
        if (organizationId != null) {
            return projectRepository.findByOrganizationId(organizationId);
        }

        // Else: projects for all orgs the user can access
        List<Long> accessibleOrgIds = userOrganizationRepository.findAllByUserId(userId)
                .stream()
                .map(uo -> uo.getOrganizationId())
                .toList();

        return projectRepository.findAllByOrganizationIdIn(accessibleOrgIds);
    }

    @Override
    public List<Project> getProjectsForUser(String userId) {
        // kept for backward-compat; same as "no organizationId" branch
        List<Long> accessibleOrgIds = userOrganizationRepository.findAllByUserId(userId)
                .stream()
                .map(uo -> uo.getOrganizationId())
                .toList();
        return projectRepository.findAllByOrganizationIdIn(accessibleOrgIds);
    }

    @Override
    public List<Project> getProjectsByOrganization(Long orgId, String userId) {
        // Python version doesn’t check access here
        return projectRepository.findByOrganizationId(orgId);
    }

    @Override
    public Project getProjectById(Long id, String userId) {
        // 404 "Projet non trouvé" if absent
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Projet non trouvé"));

        // 403 if user doesn’t have access to the project’s organization
        boolean access = userOrganizationRepository.existsByUserIdAndOrganizationId(userId, project.getOrganization().getId());
        if (!access) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Accès refusé à ce projet");
        }

        return project;
    }

    public void deleteProjectWithAccess(Long id, String userId, List<String> roles) {
        if (roles == null || !roles.contains("admin")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Non autorisé");
        }

        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Projet non trouvé"));

        boolean access = userOrganizationRepository.existsByUserIdAndOrganizationId(userId, project.getOrganization().getId());
        if (!access) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Accès refusé à cette organisation");
        }

        projectRepository.deleteById(id);
    }

    // Keep the original signature if something else calls it, but prefer deleteProjectWithAccess in controllers

}
