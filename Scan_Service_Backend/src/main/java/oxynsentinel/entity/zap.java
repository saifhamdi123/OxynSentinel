package oxynsentinel.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity


public class zap {


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String target;
    @Lob
    private String result;
    private LocalDateTime timestamp;


    public zap() {
    }

     // JSON brut de ZAP

    public zap(Long id, String target, String result, LocalDateTime timestamp) {
        this.id = id;
        this.target = target;
        this.result = result;
        this.timestamp = timestamp;
    }


}
