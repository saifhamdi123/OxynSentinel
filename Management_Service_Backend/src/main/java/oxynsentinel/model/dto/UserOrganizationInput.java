package oxynsentinel.model.dto;

public class UserOrganizationInput {
    public UserOrganizationInput() {
    }

    public UserOrganizationInput(String userId, Long organizationId) {
        this.userId = userId;
        this.organizationId = organizationId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }

    private String userId;
    private Long organizationId;
}
