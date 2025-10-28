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

    // @PostMapping
    // public Classes createClasses(@RequestBody Classes Classes) {
    //     return ClassesService.create(Classes);
    // }

    @GetMapping
    public List<Classes> getAllClasses() {
        return ClassesService.getAll();
    }

    // Implement update and delete as needed, example:
  
    // @PatchMapping("/{id}")
    // public Classes updateClasses(@PathVariable String id, @RequestBody ClassesUpdateDTO updateDTO) {
    //     return ClassesService.update(id, updateDTO);
    // }

    // @DeleteMapping("/{id}")
    // public void deleteClasses(@PathVariable String id) {
    //     ClassesService.delete(id);
    // }
    
}
