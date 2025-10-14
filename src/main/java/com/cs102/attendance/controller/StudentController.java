// package com.cs102.attendance.controller;

// import com.cs102.attendance.entity.Student;
// import com.cs102.attendance.entity.FaceData;
// import com.cs102.attendance.service.StudentService;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.*;

// import java.util.List;
// import java.util.UUID;
// import java.util.Optional;

// @RestController
// @RequestMapping("/api/students")
// public class StudentController {

//     @Autowired
//     private StudentService studentService;

//     // Create a new student
//     @PostMapping
//     public ResponseEntity<Student> createStudent(@RequestBody CreateStudentRequest request) {
//         try {
//             Student student = studentService.enrol(
//                 request.getCode(),
//                 request.getName(),
//                 request.getClassName(),
//                 request.getStudentGroup(),
//                 request.getEmail(),
//                 request.getPhone()
//             );
//             return ResponseEntity.ok(student);
//         } catch (Exception e) {
//             return ResponseEntity.badRequest().build();
//         }
//     }

//     // Get all students
//     @GetMapping
//     public ResponseEntity<List<Student>> getAllStudents() {
//         try {
//             List<Student> students = studentService.getAllStudents();
//             return ResponseEntity.ok(students);
//         } catch (Exception e) {
//             return ResponseEntity.status(500).body(null);
//         }
//     }

//     // Simple test endpoint
//     @GetMapping("/health-check")
//     public ResponseEntity<String> testEndpoint() {
//         return ResponseEntity.ok("Students endpoint is working!");
//     }

//     // Count endpoint
//     @GetMapping("/count")
//     public ResponseEntity<Long> getStudentCount() {
//         try {
//             List<Student> students = studentService.getAllStudents();
//             return ResponseEntity.ok((long) students.size());
//         } catch (Exception e) {
//             return ResponseEntity.status(500).body(-1L);
//         }
//     }

//     // Get student by ID
//     @GetMapping("/{id}")
//     public ResponseEntity<Student> getStudentById(@PathVariable UUID id) {
//         Optional<Student> student = studentService.getStudentById(id);
//         return student.map(ResponseEntity::ok)
//                      .orElse(ResponseEntity.notFound().build());
//     }

//     // Update student
//     @PutMapping("/{id}")
//     public ResponseEntity<Student> updateStudent(@PathVariable UUID id, @RequestBody UpdateStudentRequest request) {
//         try {
//             Student updated = studentService.updateProfile(
//                 id,
//                 request.getName(),
//                 request.getClassName(),
//                 request.getStudentGroup(),
//                 request.getEmail(),
//                 request.getPhone()
//             );
//             return ResponseEntity.ok(updated);
//         } catch (RuntimeException e) {
//             return ResponseEntity.notFound().build();
//         }
//     }

//     // Delete student
//     @DeleteMapping("/{id}")
//     public ResponseEntity<Void> deleteStudent(@PathVariable UUID id) {
//         try {
//             studentService.deleteStudent(id);
//             return ResponseEntity.ok().build();
//         } catch (Exception e) {
//             return ResponseEntity.notFound().build();
//         }
//     }

//     // Upload face image (URL)
//     @PostMapping("/{id}/face-image")
//     public ResponseEntity<FaceData> uploadFaceImage(@PathVariable UUID id, @RequestBody FaceImageRequest request) {
//         try {
//             FaceData faceData = studentService.uploadFaceImage(id, request.getImageUrl());
//             return ResponseEntity.ok(faceData);
//         } catch (RuntimeException e) {
//             return ResponseEntity.notFound().build();
//         }
//     }

//     // DTOs for request bodies
//     public static class CreateStudentRequest {
//         private String code;
//         private String name;
//         private String className;
//         private String studentGroup;
//         private String email;
//         private String phone;

//         // Getters and setters
//         public String getCode() { return code; }
//         public void setCode(String code) { this.code = code; }
//         public String getName() { return name; }
//         public void setName(String name) { this.name = name; }
//         public String getClassName() { return className; }
//         public void setClassName(String className) { this.className = className; }
//         public String getStudentGroup() { return studentGroup; }
//         public void setStudentGroup(String studentGroup) { this.studentGroup = studentGroup; }
//         public String getEmail() { return email; }
//         public void setEmail(String email) { this.email = email; }
//         public String getPhone() { return phone; }
//         public void setPhone(String phone) { this.phone = phone; }
//     }

//     public static class UpdateStudentRequest {
//         private String name;
//         private String className;
//         private String studentGroup;
//         private String email;
//         private String phone;

//         // Getters and setters
//         public String getName() { return name; }
//         public void setName(String name) { this.name = name; }
//         public String getClassName() { return className; }
//         public void setClassName(String className) { this.className = className; }
//         public String getStudentGroup() { return studentGroup; }
//         public void setStudentGroup(String studentGroup) { this.studentGroup = studentGroup; }
//         public String getEmail() { return email; }
//         public void setEmail(String email) { this.email = email; }
//         public String getPhone() { return phone; }
//         public void setPhone(String phone) { this.phone = phone; }
//     }

//     public static class FaceImageRequest {
//         private String imageUrl;

//         public String getImageUrl() { return imageUrl; }
//         public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
//     }
// } 