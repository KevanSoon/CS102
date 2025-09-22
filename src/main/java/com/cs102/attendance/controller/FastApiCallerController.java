package com.cs102.attendance.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.cs102.attendance.service.FastApiCallerService;

@RestController
@CrossOrigin(origins = "https://cs-102-vert.vercel.app/") 
public class FastApiCallerController {

    private final FastApiCallerService fastApiCallerService;

    public FastApiCallerController(FastApiCallerService fastApiCallerService) {
        this.fastApiCallerService = fastApiCallerService;
    }

    @PostMapping("/call-face-recognition")
    public String callFaceRecognition(@RequestParam("image") MultipartFile imageFile) {
        try {
            return fastApiCallerService.callFaceRecognitionWithImage(imageFile);
        } catch (Exception e) {
            e.printStackTrace();
            return "Error processing image: " + e.getMessage();
        }
    }
}
