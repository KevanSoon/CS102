// package com.cs102.attendance.controller;

// import java.util.List;
// import java.util.Optional;
// import java.util.UUID;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.DeleteMapping;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.PathVariable;
// import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.RequestBody;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RestController;

// import com.cs102.attendance.dto.FaceDataDto;
// import com.cs102.attendance.entity.FaceData;
// import com.cs102.attendance.service.FaceDataService;

// @RestController
// @RequestMapping("/api/face-data")
// public class FaceDataController {
    
//     @Autowired
//     private FaceDataService faceDataService;

//     // Get all face data records
//     @GetMapping
//     public ResponseEntity<List<FaceData>> getAllFaceData() {
//         try {
//             List<FaceData> faceDataList = faceDataService.getAllFaceData();
//             if (faceDataList.isEmpty()) {
//                 System.out.println("No face data records found");
//             }
//             return ResponseEntity.ok(faceDataList);
//         } catch (Exception e) {
//             System.err.println("Error retrieving face data: " + e.getMessage());
//             return ResponseEntity.internalServerError().build();
//         }
//     }

//     // Get face data by ID
//     @GetMapping("/{id}")
//     public ResponseEntity<FaceData> getFaceDataById(@PathVariable UUID id) {
//         Optional<FaceData> faceData = faceDataService.getFaceDataById(id);
//         return faceData.map(ResponseEntity::ok)
//                 .orElse(ResponseEntity.notFound().build());
//     }

//     // Get face data in format for FastAPI
//     @GetMapping("/for-fastapi")
//     public ResponseEntity<List<FaceDataDto>> getFaceDataForFastApi() {
//         try {
//             List<FaceDataDto> faceDataDtos = faceDataService.getAllFaceDataForFastApi();
//             if (faceDataDtos.isEmpty()) {
//                 System.out.println("No face data records found for FastAPI");
//             }
//             return ResponseEntity.ok(faceDataDtos);
//         } catch (Exception e) {
//             System.err.println("Error retrieving face data for FastAPI: " + e.getMessage());
//             return ResponseEntity.internalServerError().build();
//         }
//     }

//     // Create new face data
//     @PostMapping
//     public ResponseEntity<FaceData> createFaceData(@RequestBody FaceData faceData) {
//         try {
//             FaceData savedFaceData = faceDataService.saveFaceData(faceData);
//             return ResponseEntity.ok(savedFaceData);
//         } catch (Exception e) {
//             return ResponseEntity.badRequest().build();
//         }
//     }

//     // Delete face data
//     @DeleteMapping("/{id}")
//     public ResponseEntity<Void> deleteFaceData(@PathVariable UUID id) {
//         try {
//             faceDataService.deleteFaceData(id);
//             return ResponseEntity.ok().build();
//         } catch (Exception e) {
//             return ResponseEntity.notFound().build();
//         }
//     }
// }