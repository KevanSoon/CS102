package com.cs102.attendance.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.cs102.attendance.entity.Student;
import com.cs102.attendance.service.SupabaseRestService;

@Repository
public class StudentRestRepository {
    private static final String TABLE = "students";
    private final SupabaseRestService supabaseService;

    @Autowired
    public StudentRestRepository(SupabaseRestService supabaseService) {
        this.supabaseService = supabaseService;
    }

    public Student create(Student student) {
        return supabaseService.create(TABLE, student, Student.class);
    }

    public List<Student> findAll() {
        return supabaseService.read(TABLE, null, Student[].class);
    }

    public List<Student> findByName(String name) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("name", "ilike." + name + "%");
        return supabaseService.read(TABLE, queryParams, Student[].class);
    }

    public Student update(Long id, Student student) {
        return supabaseService.update(TABLE, id.toString(), student, Student.class);
    }

    public void delete(Long id) {
        supabaseService.delete(TABLE, id.toString());
    }
}