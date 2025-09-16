package com.cs102.attendance.controller;

import com.cs102.attendance.entity.TestConnection;
import com.cs102.attendance.repository.TestConnectionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @Autowired
    private TestConnectionRepository testConnectionRepository;

    @GetMapping("/database-operations")
    public ResponseEntity<Map<String, Object>> testDatabaseOperations() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Test 1: Simple query
            Integer queryResult = testConnectionRepository.testQuery();
            response.put("simpleQuery", queryResult == 1 ? "PASS" : "FAIL");
            
            // Test 2: Insert operation
            TestConnection testEntity = new TestConnection("Connection test from Spring Boot");
            TestConnection saved = testConnectionRepository.save(testEntity);
            response.put("insertOperation", saved.getId() != null ? "PASS" : "FAIL");
            
            // Test 3: Read operation
            List<TestConnection> allRecords = testConnectionRepository.findAll();
            response.put("readOperation", !allRecords.isEmpty() ? "PASS" : "FAIL");
            response.put("recordCount", allRecords.size());
            
            // Test 4: Delete operation
            testConnectionRepository.deleteById(saved.getId());
            response.put("deleteOperation", "PASS");
            
            response.put("overallStatus", "SUCCESS");
            response.put("message", "All database operations completed successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("overallStatus", "FAILED");
            response.put("error", e.getMessage());
            response.put("errorClass", e.getClass().getSimpleName());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    @PostMapping("/insert-test-data")
    public ResponseEntity<TestConnection> insertTestData(@RequestParam(defaultValue = "Test message") String message) {
        try {
            TestConnection testEntity = new TestConnection(message);
            TestConnection saved = testConnectionRepository.save(testEntity);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
    
    @GetMapping("/all-test-data")
    public ResponseEntity<List<TestConnection>> getAllTestData() {
        try {
            List<TestConnection> allRecords = testConnectionRepository.findAll();
            return ResponseEntity.ok(allRecords);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
} 