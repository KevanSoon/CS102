package com.cs102.attendance.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.Base64;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cs102.attendance.service.FaceCompareService;
import com.fasterxml.jackson.databind.JsonNode;

@RestController
@RequestMapping("/api")
public class FaceCompareController {

    @Autowired
    private FaceCompareService faceCompareService;

    /**
     * Accepts a Base64 image and a student image URL for comparison
     */
    @PostMapping("/face-compare-url")
    public ResponseEntity<JsonNode> compareFaces(@RequestBody Map<String, String> payload) {
        String imageBase64 = payload.get("imageBase64"); // uploaded image
        String imageUrl2 = payload.get("imageUrl2");     // student image URL

        if (imageBase64 == null || imageUrl2 == null) {
            return ResponseEntity.badRequest().build();
        }

        try {
            // Convert Base64 image to temp file
            File file1 = File.createTempFile("img1_", ".jpg");
            byte[] decodedBytes = Base64.getDecoder().decode(imageBase64);
            try (FileOutputStream fos = new FileOutputStream(file1)) {
                fos.write(decodedBytes);
            }

            // Download student image to temp file
            File file2 = File.createTempFile("img2_", ".jpg");
            try (InputStream in = new URL(imageUrl2).openStream()) {
                Files.copy(in, file2.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }

            // Compare faces
            JsonNode result = faceCompareService.faceCompare(
                    file1.getAbsolutePath(),
                    file2.getAbsolutePath()
            );

            // Clean up temp files
            file1.delete();
            file2.delete();

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}
