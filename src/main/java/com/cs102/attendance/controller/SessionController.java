// package com.cs102.attendance.controller;

// import java.util.HashMap;
// import java.util.List;
// import java.util.Map;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.http.HttpStatus;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.DeleteMapping;
// import org.springframework.web.bind.annotation.ExceptionHandler;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.PathVariable;
// import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.PutMapping;
// import org.springframework.web.bind.annotation.RequestBody;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RequestParam;
// import org.springframework.web.bind.annotation.RestController;

// import com.cs102.attendance.model.Session;
// import com.cs102.attendance.repository.SessionRepository;


// @RestController 
// @RequestMapping("/api/sessions")
// public class SessionController {
//     private final SessionRepository sessionRepository;

//     //auto initialize using the constructor
//     @Autowired
//     public SessionController(SessionRepository sessionRepository) {
//         this.sessionRepository = sessionRepository;
//     }

//     //maps to create() in SessionRepository 
//     @PostMapping
//     public ResponseEntity<Session> createSession(@RequestBody Session session) {
//         return ResponseEntity.ok(sessionRepository.create(session));
//     }

//     //maps to findAll() in SessionRepository 
//     @GetMapping
//     public ResponseEntity<List<Session>> getAllSession() {
//         return ResponseEntity.ok(sessionRepository.findAll());
//     }


//     // maps to findById() in StudentRepository
//     // @GetMapping("/{id}")
//     // public ResponseEntity<Student> getStudentById(@PathVariable String id) {
//     //     Student student = studentRepository.findById(id);
//     //     if (student != null) {
//     //         return ResponseEntity.ok(student);
//     //     } else {
//     //         return ResponseEntity.notFound().build();
//     //     }
//     // }


//     //maps to findbyName() in StudentRepository 
//     //  @GetMapping("/search")
//     //  public ResponseEntity<List<Student>> searchStudents(@RequestParam String name) {
//     //     return ResponseEntity.ok(studentRepository.findByName(name));
//     //  }

//      //maps to update() in StudentRespository
//     //  @PutMapping("/{id}")
//     //  public ResponseEntity<Student> updateStudent(@PathVariable Long id, @RequestBody Student student) {
//     //     return ResponseEntity.ok(studentRepository.update(id, student));
//     //  } 

//      //maps to delete() in StudentRespository
//     //  @DeleteMapping("/{id}")
//     //  public ResponseEntity<Void> deleteStudent(@PathVariable Long id) {
//     //     studentRepository.delete(id);
//     //     return ResponseEntity.ok().build();
//     //  }


// } 


