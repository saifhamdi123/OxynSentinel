package oxynsentinel.controller;


import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@Tag(name = "Test Management")

@RestController
@AllArgsConstructor
@RequestMapping("/api")

public class TestRestController {
    @GetMapping("/me")
    public ResponseEntity<?> getMe(Authentication auth) {
        return ResponseEntity.ok(auth);
    }

}
