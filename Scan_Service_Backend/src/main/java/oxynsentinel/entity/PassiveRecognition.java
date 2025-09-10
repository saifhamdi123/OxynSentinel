package oxynsentinel.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;


@Entity


public class PassiveRecognition {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String domain;
    private String source; // ex: crt.sh, Shodan
    @Lob
    private String data; // JSON brut ou résumé


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    private LocalDateTime timestamp;

public PassiveRecognition() {
}
    public PassiveRecognition(Long id, String domain, String source, String data, LocalDateTime timestamp) {
        this.id = id;
        this.domain = domain;
        this.source = source;
        this.data = data;
        this.timestamp = timestamp;
    }





}


