package com.uptrix.uptrix_backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class FileStorageConfig {

    @Value("${app.file.upload-dir:uploads}")
    private String uploadDir;

    /**
     * Returns an absolute, normalized path and ensures the root folder exists.
     */
    public Path getUploadRoot() {
        Path root = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(root);
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload root directory: " + root, e);
        }
        return root;
    }

    /**
     * For backward compatibility if you still call getUploadDir().
     */
    public String getUploadDir() {
        return getUploadRoot().toString();
    }
}
