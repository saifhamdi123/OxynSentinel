package oxynsentinel.listener;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import oxynsentinel.event.ScanErrorEvent;
import oxynsentinel.event.ScanFinishedEvent;

@Component
@RequiredArgsConstructor
public class ScanEventWebSocketPublisher {

    private final SimpMessagingTemplate messagingTemplate;

    @EventListener
    public void onScanFinished(ScanFinishedEvent event) {
        messagingTemplate.convertAndSend("/topic/scans", "✅ " + event.getMessage());
    }

    @EventListener
    public void onScanError(ScanErrorEvent event) {
        messagingTemplate.convertAndSend("/topic/scans", "❌ " + event.getMessage());
    }
}
