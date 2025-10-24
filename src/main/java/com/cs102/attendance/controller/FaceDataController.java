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

//import com.cs102.attendance.dto.SessionUpdateDTO;

import com.cs102.attendance.dto.FaceDataUpdateDTO;
import com.cs102.attendance.model.FaceData;
import com.cs102.attendance.service.FaceDataService;


@RestController
@RequestMapping("/api/face_data")
public class FaceDataController {

    private final FaceDataService faceDataService;

    public FaceDataController(FaceDataService faceDataService) {
        this.faceDataService = faceDataService;
    }

    @PostMapping
    public FaceData createSession(@RequestBody FaceData faceData) {
        return faceDataService.create(faceData);
    }

    @GetMapping
    public List<FaceData> getAllSessions() {
        return faceDataService.getAll();
    }

    // Implement update and delete as needed, example:
  
    @PatchMapping("/{id}")
    public FaceData updateSession(@PathVariable String id, @RequestBody FaceDataUpdateDTO updateDTO) {
        return faceDataService.update(id, updateDTO);
    }

    @DeleteMapping("/{id}")
    public void deleteSession(@PathVariable String id) {
        faceDataService.delete(id);
    }
    
}
