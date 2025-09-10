package oxynsentinel.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import oxynsentinel.entity.PassiveRecognition;
import oxynsentinel.service.PassiveRecognitionIService;

@Tag(name = "OSINT Scan Management")
@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/scan")
public class PassiveRecognitionRestController {

    private final PassiveRecognitionIService service;

    @PostMapping("/scan_OSINT")
    public ResponseEntity<String> scanCrtSh(@RequestParam String domain) {
        service.runCrtShScan(domain); // Now async
        return ResponseEntity.ok("OSINT scan started for domain: " + domain + ". Check WebSocket for updates.");
    }

    @GetMapping("/results_scan_OSINT")
    public ResponseEntity<?> getById(@RequestParam Long id) {
        PassiveRecognition result = service.getResultById(id);
        if (result != null) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
