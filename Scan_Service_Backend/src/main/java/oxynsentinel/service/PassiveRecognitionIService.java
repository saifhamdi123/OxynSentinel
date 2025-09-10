package oxynsentinel.service;

import oxynsentinel.entity.PassiveRecognition;

public interface PassiveRecognitionIService {
    void runCrtShScan(String domain);
    public PassiveRecognition getResultById(Long id);}
