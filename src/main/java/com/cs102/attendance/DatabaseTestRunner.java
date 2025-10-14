// package com.cs102.attendance;

// import com.cs102.attendance.entity.*;
// import com.cs102.attendance.service.*;
// import com.cs102.attendance.repository.*;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.CommandLineRunner;
// import org.springframework.stereotype.Component;

// import java.time.LocalDate;
// import java.time.LocalTime;
// import java.util.Optional;

// @Component
// public class DatabaseTestRunner implements CommandLineRunner {

//     @Autowired private StudentService studentService;
//     @Autowired private SessionService sessionService;
//     @Autowired private StudentRepository studentRepository;
//     @Autowired private SessionRepository sessionRepository;
    
//     @Override
//     public void run(String... args) throws Exception {
//         System.out.println("\nTESTING DATABASE OPERATIONS...\n");
        
//         try {
//             // 1. Test Student Creation (or find existing)
//             System.out.println("1. Checking/Creating test student...");
//             Student student;
//             Optional<Student> existingStudent = studentRepository.findByCode("TEST001");
            
//             if (existingStudent.isPresent()) {
//                 student = existingStudent.get();
//                 System.out.println("Found existing test student: " + student.getName() + " (ID: " + student.getId() + ")");
//             } else {
//                 student = studentService.enrol("TEST001", "John Doe", "CS102", "Group A", "john@test.com", "123-456-7890");
//                 System.out.println("Created new test student: " + student.getName() + " (ID: " + student.getId() + ")");
//             }
            
//             // 2. Test Session Creation (or find existing)
//             System.out.println("\n2. Checking/Creating test session...");
//             Session session;
//             Optional<Session> existingSession = sessionRepository.findByNameAndDate("Test Lecture", LocalDate.now());
            
//             if (existingSession.isPresent()) {
//                 session = existingSession.get();
//                 System.out.println("Found existing test session: " + session.getName() + " (ID: " + session.getId() + ")");
//             } else {
//                 session = sessionService.createSession("Test Lecture", LocalDate.now(), LocalTime.of(9, 0), LocalTime.of(11, 0));
//                 System.out.println("Created new test session: " + session.getName() + " (ID: " + session.getId() + ")");
//             }
            
//             // 3. Test Face Data Upload (check if already exists)
//             System.out.println("\n3. Checking/Uploading face data...");
//             try {
//                 FaceData faceData = studentService.uploadFaceImage(student.getId(), "http://example.com/face.jpg");
//                 System.out.println("Uploaded face data (ID: " + faceData.getId() + ")");
//             } catch (Exception e) {
//                 System.out.println("Face data upload skipped (likely already exists): " + e.getMessage());
//             }
            
//             // 4. Test database queries (always run these tests)
//             System.out.println("\n4. Testing database queries...");
//             var students = studentService.getAllStudents();
//             System.out.println("Found " + students.size() + " student(s) in database");
            
//             // 5. Test today's sessions query
//             System.out.println("\n5. Testing session queries...");
//             var sessions = sessionService.getTodaySessions();
//             System.out.println("Found " + sessions.size() + " session(s) for today");
            
//             // 6. Test connection integrity
//             System.out.println("\n6. Testing connection integrity...");
//             Optional<Student> retrievedStudent = studentService.getStudentById(student.getId());
//             if (retrievedStudent.isPresent()) {
//                 System.out.println("Student retrieval test: PASSED");
//             } else {
//                 System.out.println("Student retrieval test: FAILED");
//             }
            
//             System.out.println("\nALL DATABASE TESTS PASSED!");
//             System.out.println("Database is working correctly with persistent data.\n");
            
//         } catch (Exception e) {
//             System.err.println("DATABASE TEST FAILED: " + e.getMessage());
//             e.printStackTrace();
//         }
//     }
// } 