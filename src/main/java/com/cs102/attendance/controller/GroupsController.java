package com.cs102.attendance.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cs102.attendance.dto.GroupUpdateDTO;
import com.cs102.attendance.model.Groups;
import com.cs102.attendance.service.GroupsService;

@RestController
@RequestMapping("/api/groups")
public class GroupsController {

    private final GroupsService GroupsService;

    public GroupsController(GroupsService GroupsService) {
        this.GroupsService = GroupsService;
    }

    // @PostMapping
    // public Groups createGroups(@RequestBody Groups Groups) {
    //     return GroupsService.create(Groups);
    // }

    @GetMapping
    public List<Groups> getAllGroups() {
        return GroupsService.getAll();
    }

    /**
     * Update group student list
     * PATCH /api/groups/{classCode}/{groupNumber}
     * Body: { "student_list": ["student_id1", "student_id2", ...] }
     */
    @PatchMapping("/{classCode}/{groupNumber}")
    public ResponseEntity<Groups> updateGroupStudentList(
            @PathVariable String classCode,
            @PathVariable String groupNumber,
            @RequestBody GroupUpdateDTO updateDTO) {
        try {
            // Use the composite key format: "classCode.groupNumber"
            String compositeKey = classCode + "." + groupNumber;
            Groups updatedGroup = GroupsService.update(compositeKey, updateDTO);
            
            if (updatedGroup == null) {
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok(updatedGroup);
        } catch (Exception e) {
            System.err.println("Error updating group: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    // Implement update and delete as needed, example:
  
    // @PatchMapping("/{id}")
    // public Groups updateGroups(@PathVariable String id, @RequestBody GroupsUpdateDTO updateDTO) {
    //     return GroupsService.update(id, updateDTO);
    // }

    // @DeleteMapping("/{id}")
    // public void deleteGroups(@PathVariable String id) {
    //     GroupsService.delete(id);
    // }
    
}
