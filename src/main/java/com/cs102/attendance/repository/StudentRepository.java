package com.cs102.attendance.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.cs102.attendance.model.Student;
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

    // maps to /{id} endpoint to retrieve a single student record (GET Method) 
    public Student findById(String id) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("id", "eq." + id); // Supabase expects ?id=eq.<uuid>

        List<Student> students = supabaseService.read(TABLE, queryParams, Student[].class);
        return students.isEmpty() ? null : students.get(0);
    }


    // maps to /search endpoint (GET method)
    public List<Student> findByName(String name) {
        Map<String,String> queryParams = new HashMap<>();
        queryParams.put("name","ilike." + name + "%");
        return supabaseService.read(TABLE, queryParams, Student[].class);

    }
    
    // maps to /{id} endpoint to update a student record (PUT Method) 
    public Student update(String id, Student student) {
        return supabaseService.update(TABLE, id, student, Student.class);
    }

    // maps to /{id} endpoint to delete a student record (DELETE Method) 
    public void delete(Long id) {
            supabaseService.delete(TABLE, id.toString());
        }


}