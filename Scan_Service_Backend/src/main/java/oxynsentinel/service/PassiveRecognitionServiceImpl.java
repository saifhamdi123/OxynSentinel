package oxynsentinel.service;

import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import oxynsentinel.entity.PassiveRecognition;
import oxynsentinel.event.ScanErrorEvent;
import oxynsentinel.event.ScanFinishedEvent;
import oxynsentinel.repository.PassiveRecognitionRepository;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class PassiveRecognitionServiceImpl implements PassiveRecognitionIService {

    private final PassiveRecognitionRepository repo;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Async
    public void runCrtShScan(String domain) {
        try {
            // ✅ Fetch data from crt.sh
            URL url = new URL("https://crt.sh/?q=" + domain + "&output=json");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");

            StringBuilder rawResult = new StringBuilder();
            try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                String line;
                while ((line = in.readLine()) != null) rawResult.append(line);
            }

            // ✅ Save scan only once with data included
            PassiveRecognition scan = new PassiveRecognition();
            scan.setDomain(domain);
            scan.setSource("crt.sh");
            scan.setTimestamp(LocalDateTime.now());
            scan.setData(rawResult.toString());

            repo.save(scan);

            // ✅ Notify success via WebSocket
            eventPublisher.publishEvent(new ScanFinishedEvent(this, scan.getId(), "OSINT", "Passive OSINT scan completed successfully"));

        } catch (Exception e) {
            // ❌ Notify failure
            eventPublisher.publishEvent(new ScanErrorEvent(this, null, "OSINT", "Passive OSINT scan failed: " + e.getMessage()));
        }
    }


    public PassiveRecognition getResultById(Long id) {
        return repo.findById(id).orElse(null);
    }
}
