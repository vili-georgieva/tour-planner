package com.tourplanner.controller;

import com.tourplanner.service.ImageStorageService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/images")
@CrossOrigin(origins = "http://localhost:4200")
public class ImageController {

    private final ImageStorageService imageStorageService;

    public ImageController(ImageStorageService imageStorageService) {
        this.imageStorageService = imageStorageService;
    }

    /**
     * Accepts a multipart image upload, stores it under a unique filename,
     * and returns the stored filename for later retrieval.
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> uploadImage(
        HttpServletRequest request,
        @RequestParam("file") MultipartFile file
    ) throws IOException {
        String contentType = file.getContentType();
        boolean validContentType = contentType != null && (
            contentType.equals("image/jpeg") || 
            contentType.equals("image/png") || 
            contentType.equals("image/webp")
        );

        String originalName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "image";
        String lowerName = originalName.toLowerCase();
        boolean validExtension = lowerName.endsWith(".jpg") || 
                                 lowerName.endsWith(".jpeg") || 
                                 lowerName.endsWith(".png") || 
                                 lowerName.endsWith(".webp");

        if (!validContentType || !validExtension) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid file format. Only JPEG, PNG, and WebP are allowed."));
        }

        Long userId = (Long) request.getAttribute("userId");
        // storedRef is "{userId}/{uniqueName}" so image URL embeds userId for auth-free retrieval
        String storedRef = imageStorageService.storeImage(userId, originalName, file.getBytes());
        return ResponseEntity.ok(Map.of("filename", storedRef));
    }

    /**
     * Returns image bytes. Ownership validated by comparing JWT userId to path userId.
     * Security: UUID-prefixed filename prevents guessing; path traversal blocked in service.
     */
    @GetMapping("/{userId}/{filename}")
    public ResponseEntity<byte[]> getImage(
        HttpServletRequest request,
        @PathVariable Long userId,
        @PathVariable String filename
    ) {
        Long requestUserId = (Long) request.getAttribute("userId");
        if (!userId.equals(requestUserId)) {
            return ResponseEntity.status(403).build();
        }
        byte[] imageBytes = imageStorageService.loadImage(userId, filename);
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(imageBytes);
    }
}
