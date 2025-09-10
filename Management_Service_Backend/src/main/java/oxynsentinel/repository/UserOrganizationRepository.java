package oxynsentinel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import oxynsentinel.model.Organization;
import oxynsentinel.model.User;
import oxynsentinel.model.UserOrganization;
import oxynsentinel.model.UserOrganizationKey;

import java.util.List;

@Repository

public interface UserOrganizationRepository extends JpaRepository<UserOrganization, UserOrganizationKey> {
    List<UserOrganization> findAllByUserId(String userId);
    boolean existsByUserIdAndOrganizationId(String userId, Long organizationId);





    @Query("select uo.userId from UserOrganization uo where uo.organizationId = :organizationId")
    List<String> findUserIdsByOrganizationId(Long organizationId);

    @Query("select uo.organizationId from UserOrganization uo where uo.userId = :userId")
    List<Long> findOrganizationIdsByUserId(String userId);

    @Query("delete from UserOrganization uo where uo.userId = :userId")
    void deleteMappingsForUser(String userId);




}
