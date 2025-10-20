package com.cs102.attendance.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cs102.attendance.entity.Student;
import com.cs102.attendance.repository.StudentRepository;


@RestController 
@RequestMapping("/api/students") // Sets the base URL for all endpoints 
public class StudentController {
    private final StudentRepository studentRepository;

    //auto initialize using the constructor
    @Autowired
    public StudentController(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    //maps to create() in StudentRepository 
    @PostMapping // maps HTTP POST requests to this method 
    public ResponseEntity<Student> createStudent(@RequestBody Student student) {
        return ResponseEntity.ok(studentRepository.create(student));
    }

    //maps to findAll() in StudentRepository 
    @GetMapping
    public ResponseEntity<List<Student>> getAllStudents() {
        return ResponseEntity.ok(studentRepository.findAll());
    }

    //maps to findbyName() in StudentRepository 
     @GetMapping("/search")
     public ResponseEntity<List<Student>> searchStudents(@RequestParam String name) {
        return ResponseEntity.ok(studentRepository.findByName(name));
     }

     //maps to update() in StudentRespository
     @PutMapping("/{id}")
     public ResponseEntity<Student> updateStudent(@PathVariable Long id, @RequestBody Student student) {
        return ResponseEntity.ok(studentRepository.update(id, student));
     } 

     //maps to delete() in StudentRespository
     @DeleteMapping("/{id}")
     public ResponseEntity<Void> deleteStudent(@PathVariable Long id) {
        studentRepository.delete(id);
        return ResponseEntity.ok().build();
     }


} 


