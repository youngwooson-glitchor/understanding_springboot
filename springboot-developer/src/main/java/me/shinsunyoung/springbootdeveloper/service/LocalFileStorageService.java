package me.shinsunyoung.springbootdeveloper.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import javax.management.RuntimeErrorException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import me.shinsunyoung.springbootdeveloper.dto.FileStorageService;
import me.shinsunyoung.springbootdeveloper.dto.UploadResponse;

@Service
public class LocalFileStorageService implements FileStorageService {
    private final Path uploadDir;

    public LocalFileStorageService(@Value("${app.upload-dir:uploads}") String uploadDir) {
        this.uploadDir = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    @Override
    public UploadResponse store(byte[] bytes, String filename) {
        try {
            Files.createDirectories(uploadDir);

            String ext =
                    filename.contains(".") ? filename.substring(filename.lastIndexOf('.')) : ".png";

            String saved = UUID.randomUUID() + ext;

            Files.write(uploadDir.resolve(saved), bytes);

            return new UploadResponse("/uploads/" + saved);
        } catch (Exception e) {
            // TODO: handle exception
            throw new RuntimeErrorException(null);
        }
    }

}
