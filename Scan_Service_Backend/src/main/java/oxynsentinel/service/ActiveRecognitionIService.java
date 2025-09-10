package oxynsentinel.service;

import oxynsentinel.entity.ActiveRecognition;
import java.util.List;
import java.util.Optional;

public interface ActiveRecognitionIService {
    ActiveRecognition startScan(String target);
    Optional<ActiveRecognition> getScan(Long id);
    List<ActiveRecognition> getAllScans();
    List<ActiveRecognition> getScansByTarget(String target);
}
