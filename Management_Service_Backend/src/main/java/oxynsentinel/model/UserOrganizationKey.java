package oxynsentinel.model;

import java.io.Serializable;
import java.util.Objects;

public class UserOrganizationKey implements Serializable {
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        UserOrganizationKey that = (UserOrganizationKey) o;
        return Objects.equals(userId, that.userId) && Objects.equals(organizationId, that.organizationId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, organizationId);
    }

    public UserOrganizationKey() {
    }

    private String userId;

    public UserOrganizationKey(String userId, Long organizationId) {
        this.userId = userId;
        this.organizationId = organizationId;
    }

    private Long organizationId;
}
