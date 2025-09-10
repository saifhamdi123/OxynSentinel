package oxynsentinel.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import oxynsentinel.entity.zap;
import oxynsentinel.event.ScanErrorEvent;
import oxynsentinel.event.ScanFinishedEvent;
import oxynsentinel.repository.zapRepository;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class ZapServiceImpl implements ZapIService {

    @Autowired
    private zapRepository repo;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String zapBaseUrl = "http://127.0.0.1:8080"; // ZAP API endpoint

    @Override
    @Async
    public void runZapScan(String targetUrl) {
        zap scan = new zap();
        scan.setTarget(targetUrl);
        scan.setTimestamp(LocalDateTime.now());
        repo.save(scan);

        Long scanId = scan.getId();

        try {
            // 🔹 Spider the target
            String spiderResponse = sendGetRequest(zapBaseUrl + "/JSON/spider/action/scan/?url=" + targetUrl);
            int spiderScanId = objectMapper.readTree(spiderResponse).get("scan").asInt();

            while (true) {
                int status = objectMapper.readTree(sendGetRequest(zapBaseUrl + "/JSON/spider/view/status/?scanId=" + spiderScanId))
                        .get("status").asInt();
                if (status == 100) break;
                Thread.sleep(1000);
            }

            // 🔹 Active scan
            String activeScanResponse = sendGetRequest(zapBaseUrl + "/JSON/ascan/action/scan/?url=" + targetUrl);
            int activeScanId = objectMapper.readTree(activeScanResponse).get("scan").asInt();

            while (true) {
                int status = objectMapper.readTree(sendGetRequest(zapBaseUrl + "/JSON/ascan/view/status/?scanId=" + activeScanId))
                        .get("status").asInt();
                if (status == 100) break;
                Thread.sleep(1000);
            }

            // 🔹 Get alerts
            String alerts = sendGetRequest(zapBaseUrl + "/JSON/core/view/alerts/?baseurl=" + targetUrl);

            // 🔹 Save result
            scan.setResult(alerts);
            repo.save(scan);

            // ✅ Notify success
            eventPublisher.publishEvent(new ScanFinishedEvent(this, scanId, "ZAP", "ZAP scan finished successfully"));

        } catch (Exception e) {
            // ❌ Notify failure
            eventPublisher.publishEvent(new ScanErrorEvent(this, scanId, "ZAP", "ZAP scan failed: " + e.getMessage()));
        }
    }

    private String sendGetRequest(String urlStr) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");

        try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
            StringBuilder content = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            return content.toString();
        }
    }

    public String getZapScanResultById(Long scanId) {
        return repo.findById(scanId)
                .map(zap::getResult)
                .orElse("No result found for scan ID: " + scanId);
    }

}
