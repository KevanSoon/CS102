package com.cs102.attendance.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cs102.attendance.service.FastApiCallerService;

@RestController
public class FastApiCallerController {

    private final FastApiCallerService fastApiCallerService = new FastApiCallerService();

    @GetMapping("/call-face-recognition")
    public String callFaceRecognition() {
        try {
            return fastApiCallerService.callFaceRecognition();
        } catch (Exception e) {
            e.printStackTrace();
            return "Error calling face-recognition API";
        }
    }
}