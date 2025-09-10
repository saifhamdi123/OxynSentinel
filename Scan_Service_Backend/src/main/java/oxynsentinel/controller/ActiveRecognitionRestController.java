package oxynsentinel.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import oxynsentinel.entity.ActiveRecognition;
import oxynsentinel.service.ActiveRecognitionIService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "Nmap Scan Management")
@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/scan")
public class ActiveRecognitionRestController {

    private final ActiveRecognitionIService service;

    @Operation(summary = "Start an Nmap scan asynchronously")
    @PostMapping("/scan_Nmap")
    public ResponseEntity<Map<String, Object>> startScan(@RequestParam String target) {
        ActiveRecognition scan = service.startScan(target);
        Map<String, Object> response = new HashMap<>();
        response.put("scanId", scan.getId());
        response.put("status", scan.getStatus());
        response.put("message", "Scan started. Use /status/{id} to check progress.");
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get scan status by ID")
    @GetMapping("/status/{id}")
    public ResponseEntity<ActiveRecognition> getScanStatus(@PathVariable Long id) {
        return service.getScan(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Retrieve all Nmap scans")
    @GetMapping("/retrive-all-scan-Nmap")
    public ResponseEntity<List<ActiveRecognition>> getAllReconnaissance() {
        return ResponseEntity.ok(service.getAllScans());
    }

    @Operation(summary = "Get scans by target")
    @GetMapping("/by-target-scan-Nmap")
    public ResponseEntity<List<ActiveRecognition>> getScansByTarget(@RequestParam String target) {
        return ResponseEntity.ok(service.getScansByTarget(target));
    }
}
