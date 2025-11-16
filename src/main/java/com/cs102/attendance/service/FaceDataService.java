package com.cs102.attendance.service;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.cs102.attendance.model.FaceData;



@Service
public class FaceDataService extends SupabaseService<FaceData> {

    public FaceDataService(WebClient webClient) {
        super(webClient, "face_data", FaceData[].class, FaceData.class);
    }
    
}
