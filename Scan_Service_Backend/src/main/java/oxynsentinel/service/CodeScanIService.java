package oxynsentinel.service;

import org.springframework.web.multipart.MultipartFile;
import oxynsentinel.entity.CodeScan;

import java.util.Map;

public interface CodeScanIService {

        Map<String, Object> launchScan(CodeScan request) throws Exception; // Async
        Map<String, Object> scanUploadedZip(MultipartFile file) throws Exception; // Async
        //CodeScan getScanById(Long id);



}
