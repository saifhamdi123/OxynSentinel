package oxynsentinel.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import oxynsentinel.entity.zap;

import java.util.List;

@Repository

public interface zapRepository extends JpaRepository<zap, Long> {
    List<zap> findByTarget(String targetUrl);
}
