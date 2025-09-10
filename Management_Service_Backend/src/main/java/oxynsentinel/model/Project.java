package oxynsentinel.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;


@Entity
@Table(name = "projects")
public class Project {


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    private String type;
    private String url;
    private String creator;

    private LocalDateTime creationDate;

    @ManyToOne
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    public Project() {
    }

    public Project(Long id, String name, String description, String type, String url, String creator, LocalDateTime creationDate, Organization organization) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type;
        this.url = url;
        this.creator = creator;
        this.creationDate = creationDate;
        this.organization = organization;
    }
}
