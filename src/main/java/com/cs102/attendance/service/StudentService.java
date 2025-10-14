// package com.cs102.attendance.service;

// import com.cs102.attendance.entity.Student;
// import com.cs102.attendance.entity.FaceData;
// import com.cs102.attendance.repository.StudentRepository;
// import com.cs102.attendance.repository.FaceDataRepository;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.stereotype.Service;
// //import org.springframework.transaction.annotation.Transactional;

// import java.util.List;
// import java.util.UUID;
// import java.util.Optional;

// @Service
// //@Transactional
// public class StudentService {
    
//     @Autowired
//     private StudentRepository studentRepository;
    
//     @Autowired
//     private FaceDataRepository faceDataRepository;
    
//     public Student enrol(String code, String name, String className, String studentGroup, String email, String phone) {
//         Student student = new Student();
//         student.setCode(code);
//         student.setName(name);
//         student.setClassName(className);
//         student.setStudentGroup(studentGroup);
//         student.setEmail(email);
//         student.setPhone(phone);
        
//         return studentRepository.save(student);
//     }
    
//     public Student updateProfile(UUID studentId, String name, String className, String studentGroup, String email, String phone) {
//         Optional<Student> optionalStudent = studentRepository.findById(studentId);
//         if (optionalStudent.isPresent()) {
//             Student student = optionalStudent.get();
//             if (name != null) student.setName(name);
//             if (className != null) student.setClassName(className);
//             if (studentGroup != null) student.setStudentGroup(studentGroup);
//             if (email != null) student.setEmail(email);
//             if (phone != null) student.setPhone(phone);
            
//             return studentRepository.save(student);
//         }
//         throw new RuntimeException("Student not found with id: " + studentId);
//     }
    
//     public FaceData uploadFaceImage(UUID studentId, String imageUrl) {
//         Optional<Student> optionalStudent = studentRepository.findById(studentId);
//         if (optionalStudent.isPresent()) {
//             Student student = optionalStudent.get();
//             FaceData faceData = new FaceData(student, imageUrl);
//             return faceDataRepository.save(faceData);
//         }
//         throw new RuntimeException("Student not found with id: " + studentId);
//     }
    
//     public FaceData uploadFaceImage(UUID studentId, byte[] imageData) {
//         Optional<Student> optionalStudent = studentRepository.findById(studentId);
//         if (optionalStudent.isPresent()) {
//             Student student = optionalStudent.get();
//             FaceData faceData = new FaceData(student, imageData);
//             return faceDataRepository.save(faceData);
//         }
//         throw new RuntimeException("Student not found with id: " + studentId);
//     }
    
//     public List<Student> getAllStudents() {
//         return studentRepository.findAll();
//     }
    
//     public Optional<Student> getStudentById(UUID id) {
//         return studentRepository.findById(id);
//     }
    
//     public void deleteStudent(UUID id) {
//         studentRepository.deleteById(id);
//     }
// } 