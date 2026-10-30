package com.tourplanner.service;

import com.tourplanner.exception.ServiceException;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Validated
@Service
public class ImageStorageService {

    @Value("${tourplanner.image.storage-path:#{systemProperties['java.io.tmpdir']}/tourplanner-images}")
    private String storagePath;

    /**
     * Stores image bytes under a unique filename inside a per-user directory.
     * Returns the stored filename (UUID prefix + original name).
     */
    public String storeImage(
        @NotNull Long userId,
        @NotBlank String originalFileName,
        @NotNull byte[] imageBytes
    ) {
        try {
            Path userDir = Paths.get(storagePath, userId.toString());
            Files.createDirectories(userDir);

            // Generate unique name to avoid collisions
            String uniqueName = UUID.randomUUID() + "_" + sanitizeFilename(originalFileName);
            Path target = userDir.resolve(uniqueName);
            Files.write(target, imageBytes);
            // Return "{userId}/{uniqueName}" so the URL embeds the user directory for auth-free GET
            return userId + "/" + uniqueName;
        } catch (IOException e) {
            throw new ServiceException("Failed to store image.", e);
        }
    }

    /**
     * Loads image bytes from the per-user directory.
     * Throws ServiceException if file is not found or path escapes user dir.
     */
    public byte[] loadImage(@NotNull Long userId, @NotBlank String storedFileName) {
        try {
            Path userDir = Paths.get(storagePath, userId.toString()).toRealPath();
            Path filePath = userDir.resolve(storedFileName).normalize();

            // Path traversal check: file must remain inside user directory
            if (!filePath.startsWith(userDir)) {
                throw new ServiceException("Access denied.");
            }

            if (!Files.exists(filePath)) {
                throw new ServiceException("Image not found.");
            }

            return Files.readAllBytes(filePath);
        } catch (ServiceException e) {
            throw e;
        } catch (IOException e) {
            throw new ServiceException("Failed to load image.", e);
        }
    }

    // Remove path separators and null bytes from the original filename
    private String sanitizeFilename(String name) {
        return name.replaceAll("[/\\\\\u0000]", "_");
    }
}
