package oxynsentinel.model.dto;

public class ProjectCreateRequest {
    public ProjectCreateRequest() {
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

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
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

    private String name;
    private String description;



    private Long organizationId;
    private String type;
    private String url;


    public ProjectCreateRequest(String name, String description, Long organizationId, String type, String url) {
        this.name = name;
        this.description = description;
        this.organizationId = organizationId;
        this.type = type;
        this.url = url;
    }
}
