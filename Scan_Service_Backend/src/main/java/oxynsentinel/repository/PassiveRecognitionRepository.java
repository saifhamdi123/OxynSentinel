package oxynsentinel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import oxynsentinel.entity.PassiveRecognition;


@Repository

public interface PassiveRecognitionRepository extends JpaRepository<PassiveRecognition, Long> {
    PassiveRecognition getResultById(Long id);

}
