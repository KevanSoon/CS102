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

import com.cs102.attendance.dto.FaceVerificationResult;
import com.cs102.attendance.service.FaceCompareService;

@RestController
@RequestMapping("/api")
public class FaceCompareController {

    @Autowired
    private FaceCompareService faceCompareService;

    /**
     * Accepts a Base64 image and a student image URL for comparison
     * @return FaceVerificationResult with verified status and confidence score
     */
    @PostMapping("/face-compare-url")
    public ResponseEntity<FaceVerificationResult> compareFaces(@RequestBody Map<String, String> payload) {
        System.out.println("=== Face Compare URL Request Received ===");
        
        String imageBase64 = payload.get("imageBase64"); // uploaded image
        String imageUrl2 = payload.get("imageUrl2");     // student image URL

        if (imageBase64 == null || imageUrl2 == null) {
            System.err.println("ERROR: Missing required parameters");
            return ResponseEntity.badRequest().build();
        }
        
        System.out.println("Image URL 2: " + imageUrl2);
        System.out.println("Base64 image length: " + imageBase64.length());

        try {
            // Convert Base64 image to temp file
            File file1 = File.createTempFile("img1_", ".jpg");
            byte[] decodedBytes = Base64.getDecoder().decode(imageBase64);
            try (FileOutputStream fos = new FileOutputStream(file1)) {
                fos.write(decodedBytes);
            }
            System.out.println("Created temp file 1: " + file1.getAbsolutePath());
            System.out.println("Temp file 1 size: " + file1.length() + " bytes");

            // Download student image to temp file
            File file2 = File.createTempFile("img2_", ".png"); // Changed to .png
            System.out.println("Downloading student image from: " + imageUrl2);
            try (InputStream in = new URL(imageUrl2).openStream()) {
                long bytesCopied = Files.copy(in, file2.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                System.out.println("Downloaded " + bytesCopied + " bytes");
            }
            System.out.println("Created temp file 2: " + file2.getAbsolutePath());
            System.out.println("Temp file 2 size: " + file2.length() + " bytes");
            
            // Verify both files are not empty
            if (file1.length() == 0) {
                System.err.println("ERROR: Temp file 1 is empty!");
                file1.delete();
                file2.delete();
                return ResponseEntity.badRequest().build();
            }
            if (file2.length() == 0) {
                System.err.println("ERROR: Temp file 2 is empty!");
                file1.delete();
                file2.delete();
                return ResponseEntity.badRequest().build();
            }

            // Compare faces
            System.out.println("Calling faceCompareService...");
            FaceVerificationResult result = faceCompareService.faceCompare(
                    file1.getAbsolutePath(),
                    file2.getAbsolutePath()
            );

            System.out.println("Comparison result: " + result);

            // Clean up temp files
            file1.delete();
            file2.delete();

            if (result == null) {
                System.err.println("ERROR: FaceCompareService returned null");
                return ResponseEntity.internalServerError().build();
            }

            System.out.println("=== Face Compare URL Request Complete ===");
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            System.err.println("ERROR in face comparison: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}
