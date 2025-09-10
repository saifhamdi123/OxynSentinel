package oxynsentinel.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import oxynsentinel.service.ZapIService;

@Tag(name = "Zap Scan Management")
@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/scan")
public class ZapRestController {

    private final ZapIService zapScanService;

    @PostMapping("/scan_Zap")
    public ResponseEntity<String> scanTarget(@RequestParam String targetUrl) {
        zapScanService.runZapScan(targetUrl);
        return ResponseEntity.ok("ZAP scan started for target: " + targetUrl + ". You will receive updates via WebSocket.");
    }

    @GetMapping("/scan_Zap/result")
    public ResponseEntity<String> getScanResult(@RequestParam Long scanId) {
        try {
            String result = zapScanService.getZapScanResultById(scanId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving result: " + e.getMessage());
        }
    }

}
