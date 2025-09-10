package oxynsentinel.service;

import oxynsentinel.model.User;
import oxynsentinel.model.dto.UserRequest;
import oxynsentinel.model.dto.UserResponse;

import java.util.List;

public interface UserServiceInterface {
    public User createUser(UserRequest req, List<String> requesterRoles) ;
    void deleteUser(String userId, List<String> roles);
    List<UserResponse> listUsersFiltered(Long organizationId, String userId, List<String> roles);

}
