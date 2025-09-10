package oxynsentinel.controller;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import oxynsentinel.event.ScanErrorEvent;
import oxynsentinel.event.ScanFinishedEvent;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Controller
public class WebSocketScanController {

    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketScanController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @EventListener
    public void handleScanFinishedEvent(ScanFinishedEvent event) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("scanId", event.getScanId());
        payload.put("scanType", event.getScanType());
        payload.put("status", event.getStatus());
        payload.put("message", event.getMessage());
        payload.put("timestamp", event.getEventTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)); // ✅ FIXED
        messagingTemplate.convertAndSend("/topic/scan-events", payload);
    }

    @EventListener
    public void handleScanErrorEvent(ScanErrorEvent event) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("scanId", event.getScanId());
        payload.put("scanType", event.getScanType());
        payload.put("status", event.getStatus());
        payload.put("message", event.getMessage());
        payload.put("timestamp", event.getEventTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)); // ✅ FIXED
        messagingTemplate.convertAndSend("/topic/scan-events", payload);
    }
}
