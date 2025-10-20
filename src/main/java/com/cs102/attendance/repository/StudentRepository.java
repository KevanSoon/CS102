package com.cs102.attendance.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.cs102.attendance.entity.Student;
import com.cs102.attendance.service.SupabaseRestService;

@Repository
public class StudentRepository {
    private static final String TABLE = "students";
    private final SupabaseRestService supabaseService;

    //auto initialize using the constructor
    @Autowired 
    public StudentRepository(SupabaseRestService supabaseService) {
        this.supabaseService = supabaseService;
    }

    //inserting a student record (POST Method)
    public Student create(Student student) {
        return supabaseService.create(TABLE, student, Student.class);
    }

    // retrieve all students (GET method)
    public List<Student> findAll() {
        return supabaseService.read(TABLE, null, Student[].class);
    }

    // maps to /search endpoint (GET method)
    public List<Student> findByName(String name) {
        Map<String,String> queryParams = new HashMap<>();
        queryParams.put("name","ilike." + name + "%");
        return supabaseService.read(TABLE, queryParams, Student[].class);

    }
    
    // maps to /{id} endpoint to update a student record (PUT Method) 
    public Student update(Long id, Student student) {
        return supabaseService.update(TABLE, id.toString(), student, Student.class);
    }

    // maps to /{id} endpoint to delete a student record (DELETE Method) 
    public void delete(Long id) {
            supabaseService.delete(TABLE, id.toString());
    }

    // find a single student by ID
    public Optional<Student> findById(Long id) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("id", "eq." + id);

        List<Student> result = supabaseService.read(TABLE, queryParams, Student[].class);
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }




}