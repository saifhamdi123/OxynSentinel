// oxynsentinel/service/OrganizationService.java
package oxynsentinel.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import oxynsentinel.model.Organization;
import oxynsentinel.model.UserOrganization;
import oxynsentinel.model.UserOrganizationKey;
import oxynsentinel.model.dto.OrganizationInput;
import oxynsentinel.repository.OrganizationRepository;
import oxynsentinel.repository.UserOrganizationRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OrganizationService implements OrganizationServiceInterface {

    private final OrganizationRepository organizationRepository;
    private final UserOrganizationRepository userOrganizationRepository;

    @Override
    public Organization createOrganization(OrganizationInput input, String creatorEmail, List<String> roles) {
        // FastAPI: 403 "unauthorized user or invalid token"
        if (roles == null || !roles.contains("admin")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "unauthorized user or invalid token");
        }

        // FastAPI: 400 "invalid data"
        if (input == null || input.getName() == null || input.getName().trim().length() < 2) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid data");
        }
        if (organizationRepository.existsByNameIgnoreCase(input.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid data");
        }

        Organization org = new Organization();
        org.setName(input.getName().trim());
        org.setDescription(input.getDescription());
        org.setCreator(creatorEmail);
        org.setCreationDate(LocalDateTime.now());

        return organizationRepository.save(org); // FastAPI returns the created org as body (201)
    }

    @Override
    public Map<String, String> assignUserToOrganization(String userId, Long orgId, List<String> roles) {
        // FastAPI: 403 "unauthorized user"
        if (roles == null || !roles.contains("admin")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "unauthorized user");
        }

        // FastAPI: 404 "organization not found"
        if (!organizationRepository.existsById(orgId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "organization not found");
        }

        // FastAPI: 400 "user already assigned"
        UserOrganizationKey key = new UserOrganizationKey(userId, orgId);
        if (userOrganizationRepository.existsById(key)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "user already assigned");
        }

        UserOrganization uo = new UserOrganization(userId, orgId);
        userOrganizationRepository.save(uo);

        // FastAPI: 200 {"message":"user assigned successfully"}
        return Map.of("message", "user assigned successfully");
    }

    @Override
    public Map<String, String> removeUserFromOrganization(String userId, Long orgId, List<String> roles) {
        // FastAPI: 403 "unauthorized user"
        if (roles == null || !roles.contains("admin")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "unauthorized user");
        }

        UserOrganizationKey key = new UserOrganizationKey(userId, orgId);
        if (!userOrganizationRepository.existsById(key)) {
            // FastAPI: 404 "user not found in organization"
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found in organization");
        }

        userOrganizationRepository.deleteById(key);

        // FastAPI: 200 {"message":"user removed successfully"}
        return Map.of("message", "user removed successfully");
    }

    @Override
    public Map<String, String> deleteOrganization(Long orgId, List<String> roles) {
        // FastAPI: 403 "forbidden"
        if (roles == null || !roles.contains("admin")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "forbidden");
        }

        if (!organizationRepository.existsById(orgId)) {
            // FastAPI: 404 "organization not found"
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "organization not found");
        }

        organizationRepository.deleteById(orgId);

        // FastAPI: 200 {"message":"organization deleted successfully"}
        return Map.of("message", "organization deleted successfully");
    }

    public List<Organization> getOrganizationsForUser(String userId) {
        List<Long> orgIds = userOrganizationRepository.findOrganizationIdsByUserId(userId);
        if (orgIds.isEmpty()) return List.of();
        return organizationRepository.findAllById(orgIds);
    }
}
