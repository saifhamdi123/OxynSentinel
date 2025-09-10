package oxynsentinel.controller;


import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import oxynsentinel.model.dto.UserRequest;
import oxynsentinel.model.dto.UserResponse;
import oxynsentinel.service.UserServiceInterface;

import java.util.List;

@Tag(name= "User Management")

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/main/users")
public class UserController {

    @Autowired
    private final UserServiceInterface userService;



    @PostMapping("")
    public ResponseEntity<String> createUser(@RequestBody UserRequest request,
                                             @RequestAttribute("roles") List<String> roles) {
        userService.createUser(request, roles);
        return ResponseEntity.status(201).body("User created and role assigned");
    }

    @GetMapping("")
    public ResponseEntity<List<UserResponse>> listUsers(@RequestParam(required = false) Long organizationId,
                                                        @RequestAttribute("roles") List<String> roles,
                                                        @RequestAttribute("userId") String userId) {
        return ResponseEntity.ok(userService.listUsersFiltered(organizationId, userId, roles));
    }

    @DeleteMapping("/{user_id}")
    public ResponseEntity<Void> deleteUser(@RequestParam String userId,
                                           @RequestAttribute("roles") List<String> roles) {
        userService.deleteUser(userId, roles);
        return ResponseEntity.noContent().build();
    }
}
