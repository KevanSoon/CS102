package com.cs102.attendance.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cs102.attendance.dto.ProfessorUpdateDTO;
import com.cs102.attendance.model.Professor;
import com.cs102.attendance.service.ProfessorService;

@RestController
@RequestMapping("/api/professors")
public class ProfessorController {

    private final ProfessorService professorService;

    public ProfessorController(ProfessorService professorService) {
        this.professorService = professorService;
    }

    @PostMapping
    public Professor create(@RequestBody Professor professor) {
        return professorService.create(professor);
    }

    @GetMapping
    public List<Professor> getAll() {
        return professorService.getAll();
    }

    @GetMapping("/{id}")
    public Professor getById(@PathVariable String id) {
        return professorService.getById(id);
    }

    @GetMapping("/{id}/classes")
    public Object getProfessorClasses(@PathVariable String id) {
        return professorService.getProfessorClasses(id);
    }

    @PatchMapping("/{id}")
    public Professor update(@PathVariable String id, @RequestBody ProfessorUpdateDTO updateDto) {
        return professorService.update(id, updateDto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        professorService.delete(id);
    }
}
