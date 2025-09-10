package oxynsentinel.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import oxynsentinel.entity.CodeScan;
import oxynsentinel.event.ScanErrorEvent;
import oxynsentinel.event.ScanFinishedEvent;
import oxynsentinel.repository.CodeScanRepository;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
@RequiredArgsConstructor
public class CodeScanServiceImpl implements CodeScanIService {

    private final CodeScanRepository repository;
    private final ApplicationEventPublisher eventPublisher;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Scan d'un repo GitHub ou GitLab avec Semgrep
     */
    @Override
    public Map<String, Object> launchScan(CodeScan request) {
        Map<String, Object> result = new HashMap<>();
        Path tempDir = null;
        CodeScan scan = new CodeScan();

        try {
            // ✅ Création d'un enregistrement en base avant scan
            scan.setAccessToken(request.getAccessToken());
            scan.setRepositoryName(request.getRepositoryName());
            scan.setPlatform(request.getPlatform());
            scan.setScanDate(LocalDateTime.now());
            scan = repository.save(scan);

            Long scanId = scan.getId();
            tempDir = Files.createTempDirectory("repo-clone-");

            eventPublisher.publishEvent(new ScanFinishedEvent(this, scanId, "CODE", "Git repository scan started: " + request.getRepositoryName()));

            // ✅ Construction de l'URL Git
            String cloneUrl = switch (request.getPlatform().toLowerCase()) {
                case "github" -> "https://github.com/" + request.getRepositoryName() + ".git";
                case "gitlab" -> "https://gitlab.com/" + request.getRepositoryName() + ".git";
                default -> throw new IllegalArgumentException("Unsupported platform: " + request.getPlatform());
            };

            // ✅ Clone du repo
            ProcessBuilder cloneBuilder = new ProcessBuilder("git", "clone", cloneUrl, tempDir.toString());
            cloneBuilder.redirectErrorStream(true);
            Process cloneProcess = cloneBuilder.start();

            StringBuilder cloneLogs = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(cloneProcess.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) cloneLogs.append(line).append("\n");
            }

            if (cloneProcess.waitFor() != 0) {
                throw new RuntimeException("Git clone failed:\n" + cloneLogs);
            }
            eventPublisher.publishEvent(new ScanFinishedEvent(this, scanId, "CODE", "Repository cloned successfully"));

            // ✅ Lancement Semgrep
            File semgrepResultFile = tempDir.resolve("semgrep-result.json").toFile();
            ProcessBuilder semgrepBuilder = new ProcessBuilder(
                    "semgrep", "--config=p/security-audit", "--json", ".", "--output", semgrepResultFile.getAbsolutePath()
            );
            semgrepBuilder.directory(tempDir.toFile());
            semgrepBuilder.redirectErrorStream(true);
            Process semgrepProcess = semgrepBuilder.start();

            StringBuilder semgrepLogs = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(semgrepProcess.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) semgrepLogs.append(line).append("\n");
            }

            if (semgrepProcess.waitFor() != 0) {
                throw new RuntimeException("Semgrep scan failed:\n" + semgrepLogs);
            }

            String semgrepJson = Files.readString(semgrepResultFile.toPath(), StandardCharsets.UTF_8);

            // ✅ Mise à jour DB avec résultat
            scan.setLogs("Clone Logs:\n" + cloneLogs + "\n\nSemgrep Logs:\n" + semgrepLogs);
            scan.setScanResult(semgrepJson);
            repository.save(scan);

            eventPublisher.publishEvent(new ScanFinishedEvent(this, scanId, "CODE", "Git repository scan completed successfully"));

            // ✅ Résultat API
            Map<String, Object> scanResult = new HashMap<>();
            scanResult.put("clonedPath", tempDir.toString());
            scanResult.put("semgrepFindings", objectMapper.readTree(semgrepJson).get("results"));

            result.put("exitCode", 0);
            result.put("logs", "Clone + Semgrep OK");
            result.put("scanResult", scanResult);
            result.put("reportFormat", "application/json");

        } catch (Exception e) {
            result.put("exitCode", 1);
            result.put("logs", "Error: " + e.getMessage());
            eventPublisher.publishEvent(new ScanErrorEvent(this, scan.getId(), "CODE", "Git repository scan failed: " + e.getMessage()));
        } finally {
            // ✅ Nettoyage
            if (tempDir != null) deleteDirectory(tempDir);
        }

        return result;
    }

    /**
     * Scan d'un fichier ZIP uploadé localement
     */
    @Override
    public Map<String, Object> scanUploadedZip(MultipartFile file) {
        Map<String, Object> result = new HashMap<>();
        Path tempDir = null;
        CodeScan scan = new CodeScan();

        try {
            // ✅ Création en base avant démarrage
            scan.setRepositoryName(file.getOriginalFilename());
            scan.setPlatform("local");
            scan.setScanDate(LocalDateTime.now());
            scan = repository.save(scan);

            Long scanId = scan.getId();
            eventPublisher.publishEvent(new ScanFinishedEvent(this, scanId, "CODE", "Local ZIP scan started: " + file.getOriginalFilename()));

            tempDir = Files.createTempDirectory("upload-zip-");
            Path zipPath = tempDir.resolve("upload.zip");

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, zipPath, StandardCopyOption.REPLACE_EXISTING);
            }

            Path extractedDir = tempDir.resolve("extracted");
            Files.createDirectories(extractedDir);

            try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zipPath))) {
                ZipEntry entry;
                while ((entry = zis.getNextEntry()) != null) {
                    Path newFile = extractedDir.resolve(entry.getName()).normalize();
                    if (!newFile.startsWith(extractedDir)) {
                        throw new IOException("Invalid zip entry: " + entry.getName());
                    }
                    if (entry.isDirectory()) {
                        Files.createDirectories(newFile);
                    } else {
                        Files.createDirectories(newFile.getParent());
                        Files.copy(zis, newFile, StandardCopyOption.REPLACE_EXISTING);
                    }
                }
            }

            eventPublisher.publishEvent(new ScanFinishedEvent(this, scanId, "CODE", "ZIP extracted successfully"));

            // ✅ Lancement Semgrep
            File semgrepResultFile = extractedDir.resolve("semgrep-result.json").toFile();
            ProcessBuilder semgrepBuilder = new ProcessBuilder(
                    "semgrep", "--config=p/security-audit", "--json", ".", "--output", semgrepResultFile.getAbsolutePath()
            );
            semgrepBuilder.directory(extractedDir.toFile());
            semgrepBuilder.redirectErrorStream(true);
            Process semgrepProcess = semgrepBuilder.start();

            StringBuilder semgrepLogs = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(semgrepProcess.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) semgrepLogs.append(line).append("\n");
            }

            if (semgrepProcess.waitFor() != 0) {
                throw new RuntimeException("Semgrep scan failed:\n" + semgrepLogs);
            }

            String semgrepJson = Files.readString(semgrepResultFile.toPath(), StandardCharsets.UTF_8);

            // ✅ Mise à jour DB
            scan.setLogs("Semgrep Logs:\n" + semgrepLogs);
            scan.setScanResult(semgrepJson);
            repository.save(scan);

            eventPublisher.publishEvent(new ScanFinishedEvent(this, scanId, "CODE", "Local ZIP scan completed successfully"));

            Map<String, Object> scanResult = new HashMap<>();
            scanResult.put("uploadedZipPath", extractedDir.toString());
            scanResult.put("semgrepFindings", objectMapper.readTree(semgrepJson).get("results"));

            result.put("exitCode", 0);
            result.put("logs", semgrepLogs.toString());
            result.put("scanResult", scanResult);
            result.put("reportFormat", "application/json");

        } catch (Exception e) {
            result.put("exitCode", 1);
            result.put("logs", "Error: " + e.getMessage());
            eventPublisher.publishEvent(new ScanErrorEvent(this, scan.getId(), "CODE", "Local code scan failed: " + e.getMessage()));
        } finally {
            if (tempDir != null) deleteDirectory(tempDir);
        }

        return result;
    }

    private void deleteDirectory(Path path) {
        try {
            Files.walk(path)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        } catch (IOException ignored) {
        }
    }
}
