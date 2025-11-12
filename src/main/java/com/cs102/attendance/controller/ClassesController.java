package com.cs102.attendance.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cs102.attendance.model.Classes;
import com.cs102.attendance.service.ClassesService;

@RestController
@RequestMapping("/api/classes")
public class ClassesController {

    private final ClassesService ClassesService;

    public ClassesController(ClassesService ClassesService) {
        this.ClassesService = ClassesService;
    }

    @GetMapping
    public List<Classes> getAllClasses() {
        return ClassesService.getAll();
    }
    
}
