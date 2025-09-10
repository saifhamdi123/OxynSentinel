package oxynsentinel.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;

@Getter
public class ScanFinishedEvent extends ApplicationEvent {

    private final Long scanId;
    private final String scanType;
    private final String status;
    private final String message;
    private final LocalDateTime eventTime;

    public ScanFinishedEvent(Object source, Long scanId, String scanType, String message) {
        super(source);
        this.scanId = scanId;
        this.scanType = scanType;
        this.status = "COMPLETED";
        this.message = message;
        this.eventTime = LocalDateTime.now();
    }
}
