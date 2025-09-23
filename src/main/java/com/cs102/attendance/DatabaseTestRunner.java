package com.cs102.attendance;

import com.cs102.attendance.entity.*;
import com.cs102.attendance.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;

@Component
public class DatabaseTestRunner implements CommandLineRunner {

    @Autowired private StudentService studentService;
    @Autowired private SessionService sessionService;
    
    @Override
    public void run(String... args) throws Exception {
        System.out.println("\nTESTING DATABASE OPERATIONS...\n");
        
        try {
            // 1. Test Student Creation
            System.out.println("1. Creating student...");
            Student student = studentService.enrol("TEST001", "John Doe", "CS102", "Group A", "john@test.com", "123-456-7890");
            System.out.println("Student created: " + student.getName() + " (ID: " + student.getId() + ")");
            
            // 2. Test Session Creation
            System.out.println("\n2. Creating session...");
            Session session = sessionService.createSession("Test Lecture", LocalDate.now(), LocalTime.of(9, 0), LocalTime.of(11, 0));
            System.out.println("Session created: " + session.getName() + " (ID: " + session.getId() + ")");
            
            // 3. Test Face Data Upload
            System.out.println("\n3. Uploading face data...");
            FaceData faceData = studentService.uploadFaceImage(student.getId(), "http://example.com/face.jpg");
            System.out.println("Face data uploaded (ID: " + faceData.getId() + ")");
            
            // 4. Get all students
            System.out.println("\n4. Retrieving all students...");
            var students = studentService.getAllStudents();
            System.out.println("Found " + students.size() + " student(s)");
            
            // 5. Get today's sessions
            System.out.println("\n5. Retrieving today's sessions...");
            var sessions = sessionService.getTodaySessions();
            System.out.println("Found " + sessions.size() + " session(s) for today");
            
            System.out.println("\nALL DATABASE TESTS PASSED!");
            System.out.println("Database is working correctly.\n");
            
        } catch (Exception e) {
            System.err.println("DATABASE TEST FAILED: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 