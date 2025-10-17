package com.cs102.attendance.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cs102.attendance.entity.FaceData;
import com.cs102.attendance.repository.FaceDataRepository;


@RestController 
@RequestMapping("/api/face-data")
public class FaceDataController {
    private final FaceDataRepository faceDataRepository;

    //auto initialize using the constructor
    @Autowired
    public FaceDataController(FaceDataRepository faceDataRepository) {
        this.faceDataRepository = faceDataRepository;
    }

    //maps to create() in FaceDataRepository 
    @PostMapping
    public ResponseEntity<FaceData> createFaceData(@RequestBody FaceData faceData) {
        return ResponseEntity.ok(faceDataRepository.create(faceData));
    }

    //maps to findAll() in FaceDataRepository 
    @GetMapping
    public ResponseEntity<List<FaceData>> getAllFaceData() {
        return ResponseEntity.ok(faceDataRepository.findAll());
    }

} 





//     // Get face data in format for FastAPI
    // @GetMapping("/for-fastapi")
    // public ResponseEntity<List<FaceDataDto>> getFaceDataForFastApi() {
    //     try {
    //         List<FaceDataDto> faceDataDtos = faceDataService.getAllFaceDataForFastApi();
    //         if (faceDataDtos.isEmpty()) {
    //             System.out.println("No face data records found for FastAPI");
    //         }
    //         return ResponseEntity.ok(faceDataDtos);
    //     } catch (Exception e) {
    //         System.err.println("Error retrieving face data for FastAPI: " + e.getMessage());
    //         return ResponseEntity.internalServerError().build();
    //     }
    // }