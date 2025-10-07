package com.cs102.attendance.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.cs102.attendance.service.FastApiCallerService;

@RestController
@RequestMapping("/api/face-recognition")
public class FastApiCallerController {

    private final FastApiCallerService fastApiCallerService;

    public FastApiCallerController(FastApiCallerService fastApiCallerService) {
        this.fastApiCallerService = fastApiCallerService;
    }

    @PostMapping("/call")
    public ResponseEntity<String> callFaceRecognition(@RequestParam("image") MultipartFile imageFile) {
        try {
            String result = fastApiCallerService.callFaceRecognitionWithImage(imageFile);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error processing image: " + e.getMessage());
        }
    }
}
