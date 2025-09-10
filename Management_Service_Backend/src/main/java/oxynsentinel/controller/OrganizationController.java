package oxynsentinel.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import oxynsentinel.model.Organization;
import oxynsentinel.model.dto.OrganizationInput;
import oxynsentinel.model.dto.UserOrganizationInput;
import oxynsentinel.service.OrganizationServiceInterface;

import java.util.List;
import java.util.Map;

@Tag(name= "Organization Management")

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/main/organizations")
public class OrganizationController {
    @Autowired

    private final OrganizationServiceInterface organizationService;



    @PostMapping("/create")
    public ResponseEntity<String> createOrganization(@RequestBody OrganizationInput request,
                                                     @RequestAttribute("roles") List<String> roles,
                                                     @RequestAttribute("email") String email) {
        organizationService.createOrganization(request, email ,roles);
        return ResponseEntity.status(201).body("Organization created");
    }

    @PostMapping("/assign-user")
    public Map<String, String> assignUser(
            @RequestBody UserOrganizationInput data,
            @RequestAttribute("roles") List<String> roles
    ) {
        return organizationService.assignUserToOrganization(data.getUserId(), data.getOrganizationId(), roles);
    }

    @GetMapping("")
    public ResponseEntity<List<Organization>> getOrganizations(@RequestAttribute("userId") String userId) {
        return ResponseEntity.ok(organizationService.getOrganizationsForUser(userId));
    }



    @PostMapping("/remove-user")
    public ResponseEntity<String> removeUserFromOrganization(@RequestBody UserOrganizationInput input,
                                                             @RequestAttribute("roles") List<String> roles) {
        organizationService.removeUserFromOrganization(input.getUserId(), input.getOrganizationId(), roles);
        return ResponseEntity.ok("User removed from organization");
    }


    @DeleteMapping("/delete/{organization_id}")
    public Map<String, String> delete(
            @RequestParam Long organizationId,
            @RequestAttribute("roles") List<String> roles
    ) {
        return organizationService.deleteOrganization(organizationId, roles);
    }
}
