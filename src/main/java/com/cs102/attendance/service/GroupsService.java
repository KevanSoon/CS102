package com.cs102.attendance.service;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

// import com.cs102.attendance.dto.GroupsUpdateDTO;
import com.cs102.attendance.model.Groups;


@Service
public class GroupsService extends SupabaseService<Groups> {

    public GroupsService(WebClient webClient) {
        super(webClient, "groups", Groups[].class, Groups.class);
    }

    // public Groups update(String id, GroupsUpdateDTO updatedDto) {
    //     // Calls the generic update method but with DTO object for patch
    //     return super.update(id, updatedDto);
    // }

    
}
