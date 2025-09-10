package oxynsentinel.service;

import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import oxynsentinel.entity.ActiveRecognition;
import oxynsentinel.event.ScanErrorEvent;
import oxynsentinel.event.ScanFinishedEvent;
import oxynsentinel.repository.ActiveRecognitionRepository;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class ActiveRecognitionServiceImpl implements ActiveRecognitionIService {

    private final ActiveRecognitionRepository repo;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public ActiveRecognition startScan(String target) {
        ActiveRecognition scan = new ActiveRecognition();
        scan.setTarget(target);
        scan.setStatus("PENDING");
        scan.setTimestamp(LocalDateTime.now());
        repo.save(scan);

        runNmapAsync(scan.getId(), target);
        return scan;
    }

    @Async
    public void runNmapAsync(Long id, String target) {
        Optional<ActiveRecognition> optionalScan = repo.findById(id);
        if (optionalScan.isEmpty()) return;

        ActiveRecognition scan = optionalScan.get();
        scan.setStatus("RUNNING");
        repo.save(scan);

        StringBuilder output = new StringBuilder();
        try {
            ProcessBuilder builder = new ProcessBuilder("/usr/bin/nmap", "-sV", target);
            builder.redirectErrorStream(true);
            Process process = builder.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            process.waitFor();

            scan.setResult(output.toString());
            scan.setStatus("COMPLETED");
            scan.setTimestamp(LocalDateTime.now());
            repo.save(scan);

            // ✅ Publish success event with scanId
            eventPublisher.publishEvent(new ScanFinishedEvent(this, scan.getId(), "NMAP", "Nmap scan finished successfully"));

        } catch (Exception e) {
            scan.setResult("Error: " + e.getMessage());
            scan.setStatus("FAILED");
            repo.save(scan);

            // ✅ Publish error event with scanId
            eventPublisher.publishEvent(new ScanErrorEvent(this, scan.getId(), "NMAP", "Error during Nmap scan: " + e.getMessage()));
        }
    }

    @Override
    public Optional<ActiveRecognition> getScan(Long id) {
        return repo.findById(id);
    }

    @Override
    public List<ActiveRecognition> getAllScans() {
        return repo.findAll();
    }

    @Override
    public List<ActiveRecognition> getScansByTarget(String target) {
        return repo.findByTarget(target);
    }
}
