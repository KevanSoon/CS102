// src/main/java/com/example/schoolapp/service/StudentService.java
package com.cs102.attendance.service;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.cs102.attendance.dto.StudentUpdateDTO;
import com.cs102.attendance.model.Student;

@Service
public class StudentService extends SupabaseService<Student> {

    public StudentService(WebClient webClient) {
        super(webClient, "students", Student[].class, Student.class);
    }
    public Student update(String id, StudentUpdateDTO updatedDto) {
        // Calls the generic update method but with DTO object for patch
        return super.update(id, updatedDto);
    }
}
