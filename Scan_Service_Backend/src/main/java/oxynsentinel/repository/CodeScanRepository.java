package oxynsentinel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import oxynsentinel.entity.CodeScan;
@Repository

public interface CodeScanRepository extends JpaRepository<CodeScan, Long> {
}
