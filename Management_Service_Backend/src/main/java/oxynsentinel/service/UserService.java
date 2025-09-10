package oxynsentinel.service;


import java.util.Collections;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import oxynsentinel.model.User; // used only as a return DTO (not persisted)
import oxynsentinel.model.dto.UserRequest;
import oxynsentinel.model.dto.UserResponse;
import oxynsentinel.repository.UserOrganizationRepository;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService implements UserServiceInterface {

    @Value("${keycloak.base-url}")
    private String keycloakBaseUrl;

    @Value("${keycloak.client-id}")            // oxyn01
    private String clientId;

    @Value("${keycloak.admin-client-id}")      // oxyn02
    private String adminClientId;

    @Value("${keycloak.admin-client-secret}")
    private String adminClientSecret;

    private final UserOrganizationRepository userOrganizationRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    /* --------------------- helpers --------------------- */
    private String realm() { return "oxynas"; }

    private String tokenUrl() {
        return keycloakBaseUrl + "/realms/" + realm() + "/protocol/openid-connect/token";
    }

    private String adminBase() {
        return keycloakBaseUrl + "/admin/realms/" + realm();
    }

    private HttpHeaders jsonAuth(String token) {
        HttpHeaders h = new HttpHeaders();
        h.setBearerAuth(token);
        h.setContentType(MediaType.APPLICATION_JSON);
        return h;
    }

    public String getAdminToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "client_credentials");
        form.add("client_id", adminClientId);
        form.add("client_secret", adminClientSecret);

        HttpEntity<?> entity = new HttpEntity<>(form, headers);
        ResponseEntity<Map> res = restTemplate.postForEntity(tokenUrl(), entity, Map.class);
        if (!res.getStatusCode().is2xxSuccessful() || res.getBody() == null || res.getBody().get("access_token") == null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error creating user: failed to obtain admin token");
        }
        return (String) res.getBody().get("access_token");
    }

    private String getClientUUID(String token, String clientId) {
        String url = adminBase() + "/clients?clientId=" + URLEncoder.encode(clientId, StandardCharsets.UTF_8);
        ResponseEntity<List> res = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(jsonAuth(token)), List.class);
        if (res.getStatusCodeValue() != 200) {
            // FastAPI: raise HTTPException(500, "Failed to retrieve client ID")
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to retrieve client ID");
        }
        List list = res.getBody();
        if (list == null || list.isEmpty()) {
            // FastAPI create_user message
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Client not found in Keycloak");
        }
        return (String) ((Map<?, ?>) list.get(0)).get("id");
    }

    private Map<String, Object> getClientRole(String token, String clientUUID, String roleName) {
        String url = adminBase() + "/clients/" + clientUUID + "/roles/" + URLEncoder.encode(roleName, StandardCharsets.UTF_8);
        ResponseEntity<Map> res = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(jsonAuth(token)), Map.class);
        if (res.getStatusCodeValue() == 404) {
            // FastAPI: raise HTTPException(404, f"Role '{role}' not found in client '{client}'")
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Role '" + roleName + "' not found in client '" + clientId + "'");
        }
        if (res.getStatusCodeValue() != 200 || res.getBody() == null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to retrieve role");
        }
        return res.getBody();
    }

    private String userIdFromLocation(ResponseEntity<?> res) {
        URI loc = res.getHeaders().getLocation();
        if (loc == null) return null;
        String p = loc.getPath();
        return p.substring(p.lastIndexOf('/') + 1);
    }

    private String findUserIdByUsername(String token, String username) {
        String url = adminBase() + "/users?username=" + URLEncoder.encode(username, StandardCharsets.UTF_8);
        ResponseEntity<List> res = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(jsonAuth(token)), List.class);
        if (res.getStatusCodeValue() != 200 || res.getBody() == null || res.getBody().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error creating user: cannot resolve created user ID");
        }
        Map first = (Map) res.getBody().get(0);
        return (String) first.get("id");
    }

    private List<Map<String, Object>> ListUsers(String token , int first, int max) {
        String url = adminBase() + "/users?first=" + first + "&max=" + max;
        ResponseEntity<List> res = restTemplate.exchange(
                url, HttpMethod.GET, new HttpEntity<>(jsonAuth(token)), List.class);

        if (res.getStatusCodeValue() != 200) {
            // mirrors: raise HTTPException(500, "Failed to retrieve users")
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to retrieve users");
        }

        List<?> body = res.getBody();
        if (body == null) {
            return Collections.emptyList(); // typed as List<Map<String,Object>> by method signature
        }
        return (List<Map<String, Object>>) body;
    }

    private List<String> GetUserClientRoles(String token, String userId, String clientUUID) {
        String url = adminBase() + "/users/" + userId + "/role-mappings/clients/" + clientUUID;
        ResponseEntity<List> res = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(jsonAuth(token)), List.class);
        if (res.getStatusCodeValue() != 200) {
            // FastAPI: raise HTTPException(500, "Failed to retrieve roles")
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to retrieve roles");
        }
        List<Map<String, Object>> roles = res.getBody();
        if (roles == null) return List.of();
        return roles.stream().map(r -> (String) r.get("name")).collect(Collectors.toList());
    }

    /* --------------------- API --------------------- */

    // === CREATE (mirror FastAPI statuses/messages) ===
    @Override
    public User createUser(UserRequest req, List<String> requesterRoles) {
        if (requesterRoles == null || !requesterRoles.contains("admin")) {
            // FastAPI: raise HTTPException(403, "Access forbidden")
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access forbidden");
        }

        String token = getAdminToken();
        HttpHeaders headers = jsonAuth(token);

        Map<String, Object> payload = new HashMap<>();
        payload.put("enabled", true);
        payload.put("username", req.getUsername());
        payload.put("email", req.getEmail());
        payload.put("firstName", req.getFirstName());
        payload.put("lastName", req.getLastName());
        payload.put("emailVerified", true);
        payload.put("credentials", List.of(Map.of(
                "type", "password",
                "value", "ChangeMe123!",
                "temporary", true
        )));

        String createUrl = adminBase() + "/users";

        // Use String body so we can echo error text like FastAPI's res.text
        ResponseEntity<String> res = restTemplate.exchange(
                createUrl, HttpMethod.POST, new HttpEntity<>(payload, headers), String.class);

        if (res.getStatusCodeValue() == 201) {
            // resolve user id
            String userId = userIdFromLocation(res);
            if (userId == null) userId = findUserIdByUsername(token, req.getUsername());

            // client uuid
            String clientUUID = getClientUUID(token, clientId);

            // role fetch & assign (with FastAPI-style errors)
            Map<String, Object> role = getClientRole(token, clientUUID, req.getRole());
            String assignUrl = adminBase() + "/users/" + userId + "/role-mappings/clients/" + clientUUID;
            ResponseEntity<String> assignRes = restTemplate.exchange(
                    assignUrl, HttpMethod.POST, new HttpEntity<>(List.of(role), headers), String.class);
            if (assignRes.getStatusCodeValue() != 200 && assignRes.getStatusCodeValue() != 204) {
                // FastAPI: raise HTTPException(500, "User created but role assignment failed")
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "User created but role assignment failed");
            }

            User out = new User();
            out.setId(userId);
            out.setUsername(req.getUsername());
            out.setEmail(req.getEmail());
            out.setFirstName(req.getFirstName());
            out.setLastName(req.getLastName());
            out.setRoles(req.getRole() == null ? List.of() : List.of(req.getRole()));
            return out;

        } else if (res.getStatusCodeValue() == 409) {
            // FastAPI: raise HTTPException(400, "Username or email already exists")
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username or email already exists");
        } else {
            // FastAPI: raise HTTPException(500, f"Error creating user: {res.text}")
            String body = res.getBody() == null ? "" : res.getBody();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error creating user: " + body);
        }
    }
    private List<Map<String, Object>> ListUsers(String token) {
        return ListUsers(token, 0, 100); // default page
    }

    // === LIST (mirror FastAPI statuses/messages) ===
    @Override
    public List<UserResponse> listUsersFiltered(Long organizationId, String requesterUserId, List<String> roles) {
        // If org is specified → we’ll filter; If not specified → require admin (FastAPI behavior)
        if (organizationId == null && (roles == null || !roles.contains("admin"))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access forbidden");
        }

        String token = getAdminToken();
        List<Map<String, Object>> users = ListUsers(token);

        String clientUUID = getClientUUID(token, clientId);

        List<UserResponse> result = new ArrayList<>();
        for (Map<String, Object> u : users) {
            String uid = (String) u.get("id");

            // If organization filter present, restrict to those IDs
            if (organizationId != null) {
                List<String> orgUserIds = userOrganizationRepository.findUserIdsByOrganizationId(organizationId);
                if (!orgUserIds.contains(uid)) continue;
            }

            List<String> roleNames = GetUserClientRoles(token, uid, clientUUID);

            UserResponse r = new UserResponse();
            r.setId(uid);
            r.setUsername((String) u.getOrDefault("username", ""));
            r.setEmail((String) u.getOrDefault("email", ""));
            r.setFirstName((String) u.getOrDefault("firstName", ""));
            r.setLastName((String) u.getOrDefault("lastName", ""));
            r.setRoles(roleNames);

            result.add(r);
        }

        return result;
    }

    // === DELETE (mirror FastAPI statuses/messages) ===
    @Override
    public void deleteUser(String userId, List<String> roles) {
        if (roles == null || !roles.contains("admin")) {
            // FastAPI: raise HTTPException(403, "Access forbidden")
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access forbidden");
        }

        String token = getAdminToken();
        String url = adminBase() + "/users/" + userId;

        ResponseEntity<String> res = restTemplate.exchange(url, HttpMethod.DELETE, new HttpEntity<>(jsonAuth(token)), String.class);

        if (res.getStatusCodeValue() == 204) {
            // (optional) clean local org mappings
            try { userOrganizationRepository.deleteMappingsForUser(userId); } catch (Exception ignore) {}
            return;
        } else if (res.getStatusCodeValue() == 404) {
            // FastAPI: raise HTTPException(404, "User not found")
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        } else {
            // FastAPI: raise HTTPException(500, "Failed to delete user")
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to delete user");
        }
    }
}
