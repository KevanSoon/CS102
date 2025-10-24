package com.cs102.attendance.service;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.cs102.attendance.dto.FaceDataUpdateDTO;
import com.cs102.attendance.model.FaceData;



@Service
public class FaceDataService extends SupabaseService<FaceData> {

    public FaceDataService(WebClient webClient) {
        super(webClient, "face_data", FaceData[].class, FaceData.class);
    }
    public FaceData update(String id, FaceDataUpdateDTO updatedDto) {
        // Calls the generic update method but with DTO object for patch
        return super.update(id, updatedDto);
    }

    
}
