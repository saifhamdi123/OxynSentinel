package oxynsentinel.entity;


import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity

public class CodeScan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;




    private String accessToken;
    private String repositoryName;
    private String platform; // "github" or "gitlab"

    @Lob

    @Column(columnDefinition = "TEXT")
    private String logs;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRepositoryName() {
        return repositoryName;
    }

    public void setRepositoryName(String repositoryName) {
        this.repositoryName = repositoryName;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getLogs() {
        return logs;
    }

    public void setLogs(String logs) {
        this.logs = logs;
    }

    public String getScanResult() {
        return scanResult;
    }

    public void setScanResult(String scanResult) {
        this.scanResult = scanResult;
    }

    public LocalDateTime getScanDate() {
        return scanDate;
    }

    public void setScanDate(LocalDateTime scanDate) {
        this.scanDate = scanDate;
    }
    @Lob

    @Column(columnDefinition = "TEXT")
    private String scanResult; // stocké en JSON string

    private LocalDateTime scanDate;

    // Constructors
    public CodeScan() {}
    public CodeScan(Long id, String accessToken, String repositoryName, String platform, String logs, String scanResult, LocalDateTime scanDate) {
        this.id = id;
        this.accessToken = accessToken;
        this.repositoryName = repositoryName;
        this.platform = platform;
        this.logs = logs;
        this.scanResult = scanResult;
        this.scanDate = scanDate;
    }




}
