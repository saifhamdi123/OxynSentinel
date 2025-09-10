package oxynsentinel.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "organizations")
public class Organization {


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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    @Column(length = 500)
    private String description;

    private String creator;

    private LocalDateTime creationDate;

    public Organization() {
    }

    public Organization(Long id, String name, String description, String creator, LocalDateTime creationDate) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.creator = creator;
        this.creationDate = creationDate;
    }
}
