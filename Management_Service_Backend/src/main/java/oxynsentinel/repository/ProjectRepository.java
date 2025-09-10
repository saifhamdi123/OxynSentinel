package oxynsentinel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import oxynsentinel.model.Project;

import java.util.List;

@Repository

public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByOrganizationId(Long orgId);
    boolean existsByNameAndOrganizationId(String name, Long organizationId);
    @Query("SELECT p FROM Project p WHERE p.organization.id IN (" +
            "SELECT uo.organizationId FROM UserOrganization uo WHERE uo.userId = :userId)")
    List<Project> findAllByUserId(@Param("userId") String userId);
    List<Project> findAllByOrganizationIdIn(List<Long> orgIds);


}
