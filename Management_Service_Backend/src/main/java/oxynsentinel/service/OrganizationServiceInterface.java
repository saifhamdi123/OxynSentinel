package oxynsentinel.service;

import oxynsentinel.model.Organization;
import oxynsentinel.model.dto.OrganizationInput;

import java.util.List;
import java.util.Map;

public interface OrganizationServiceInterface {
    Organization createOrganization(OrganizationInput input, String creatorEmail, List<String> roles);
    Map<String, String> assignUserToOrganization(String userId, Long orgId, List<String> roles);
    Map<String, String> removeUserFromOrganization(String userId, Long orgId, List<String> roles);
    Map<String, String> deleteOrganization(Long orgId, List<String> roles);
    public List<Organization> getOrganizationsForUser(String userId) ;
    }
