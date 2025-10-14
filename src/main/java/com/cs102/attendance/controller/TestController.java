// package com.cs102.attendance.controller;

// import com.cs102.attendance.entity.Student;
// import com.cs102.attendance.repository.StudentRepository;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.*;

// import java.util.HashMap;
// import java.util.List;
// import java.util.Map;

// @RestController
// @RequestMapping("/api/test")
// public class TestController {

//     @Autowired
//     private StudentRepository studentRepository;

//     @GetMapping("/database-operations")
//     public ResponseEntity<Map<String, Object>> testDatabaseOperations() {
//         Map<String, Object> response = new HashMap<>();
        
//         try {
//             // Test 1: Insert operation
//             Student testStudent = new Student("TEST001", "Test Student");
//             testStudent.setEmail("test@example.com");
//             Student saved = studentRepository.save(testStudent);
//             response.put("insertOperation", saved.getId() != null ? "PASS" : "FAIL");
            
//             // Test 2: Read operation
//             List<Student> allRecords = studentRepository.findAll();
//             response.put("readOperation", !allRecords.isEmpty() ? "PASS" : "FAIL");
//             response.put("recordCount", allRecords.size());
            
//             // Test 3: Delete operation
//             studentRepository.deleteById(saved.getId());
//             response.put("deleteOperation", "PASS");
            
//             response.put("overallStatus", "SUCCESS");
//             response.put("message", "All database operations completed successfully");
            
//             return ResponseEntity.ok(response);
//         } catch (Exception e) {
//             response.put("overallStatus", "FAILED");
//             response.put("error", e.getMessage());
//             response.put("errorClass", e.getClass().getSimpleName());
//             return ResponseEntity.status(500).body(response);
//         }
//     }
    
//     @PostMapping("/insert-test-data")
//     public ResponseEntity<Student> insertTestData(
//             @RequestParam String code,
//             @RequestParam String name,
//             @RequestParam(required = false) String email) {
//         try {
//             Student testStudent = new Student(code, name);
//             if (email != null) {
//                 testStudent.setEmail(email);
//             }
//             Student saved = studentRepository.save(testStudent);
//             return ResponseEntity.ok(saved);
//         } catch (Exception e) {
//             return ResponseEntity.status(500).build();
//         }
//     }
    
//     @GetMapping("/all-test-data")
//     public ResponseEntity<List<Student>> getAllTestData() {
//         try {
//             List<Student> allRecords = studentRepository.findAll();
//             return ResponseEntity.ok(allRecords);
//         } catch (Exception e) {
//             return ResponseEntity.status(500).build();
//         }
//     }
// } 