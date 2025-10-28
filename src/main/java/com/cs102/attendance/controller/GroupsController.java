package com.cs102.attendance.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
