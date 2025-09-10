package oxynsentinel.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

@Entity
@IdClass(UserOrganizationKey.class)
@Table(name = "user_organization")
public class UserOrganization {
    public UserOrganization() {
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

    @Id
    private String userId;



    @Id
    private Long organizationId;
    public UserOrganization(String userId, Long organizationId) {
        this.userId = userId;
        this.organizationId = organizationId;
    }
}
