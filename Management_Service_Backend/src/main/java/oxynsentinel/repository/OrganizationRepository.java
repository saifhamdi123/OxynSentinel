package oxynsentinel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import oxynsentinel.model.Organization;
import oxynsentinel.model.UserOrganization;

import java.util.List;
import java.util.Optional;

@Repository

public interface OrganizationRepository extends JpaRepository<Organization, Long> {
    boolean existsByNameIgnoreCase(String name);
    Optional<Organization> findById(Long organizationId);

}
