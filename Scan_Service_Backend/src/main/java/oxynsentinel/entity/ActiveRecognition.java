package oxynsentinel.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class ActiveRecognition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String target;

    @Column(columnDefinition = "TEXT")
    private String result;

    private LocalDateTime timestamp;

    private String status; // PENDING, RUNNING, COMPLETED, FAILED

    public ActiveRecognition() {}

    public ActiveRecognition(Long id, String target, String result, LocalDateTime timestamp, String status) {
        this.id = id;
        this.target = target;
        this.result = result;
        this.timestamp = timestamp;
        this.status = status;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTarget() { return target; }
    public void setTarget(String target) { this.target = target; }

    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
