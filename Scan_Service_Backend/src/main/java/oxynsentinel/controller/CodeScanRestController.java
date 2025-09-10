package oxynsentinel.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import oxynsentinel.entity.CodeScan;
import oxynsentinel.service.CodeScanIService;

import java.util.Map;

@Tag(name = "Code Scan Management")

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/scan")
public class CodeScanRestController {

    private final CodeScanIService service;

    @PostMapping("/git")
    public ResponseEntity<?> launchScan(@RequestBody CodeScan request) {
        try {
            Map<String, Object> result = service.launchScan(request);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // ✅ Lancer scan ZIP local
    @PostMapping(value = "/uploadZip", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> scanUploadedZip(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty() || !file.getOriginalFilename().endsWith(".zip")) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid or missing zip file"));
        }
        try {
            Map<String, Object> result = service.scanUploadedZip(file);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }



}
