package oxynsentinel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import oxynsentinel.entity.ActiveRecognition;
import java.util.List;
@Repository

public interface ActiveRecognitionRepository extends JpaRepository<ActiveRecognition, Long> {
    List<ActiveRecognition> findByTarget(String target);
}
