package com.cs102.attendance.controller;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
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

    @PostMapping("/face-compare-url")
    public ResponseEntity<JsonNode> compareFacesByUrl(@RequestBody Map<String, String> payload) {
        String imageUrl1 = payload.get("imageUrl1");
        String imageUrl2 = payload.get("imageUrl2");

        if (imageUrl1 == null || imageUrl2 == null) {
            return ResponseEntity.badRequest().build();
        }

        try {
            // Download images temporarily
            File file1 = File.createTempFile("img1_", ".jpg");
            File file2 = File.createTempFile("img2_", ".jpg");

            try (InputStream in1 = new URL(imageUrl1).openStream();
                 InputStream in2 = new URL(imageUrl2).openStream()) {
                Files.copy(in1, file1.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                Files.copy(in2, file2.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }

            // Call FaceCompareService
            JsonNode result = faceCompareService.faceCompare(
                    file1.getAbsolutePath(),
                    file2.getAbsolutePath()
            );

            // Clean up temporary files
            file1.delete();
            file2.delete();

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}
