package com.cs102.attendance.repository;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.cs102.attendance.model.FaceData;
import com.cs102.attendance.service.SupabaseRestService;


@Repository
public class FaceDataRepository {
    private static final String TABLE = "face_data";
    private final SupabaseRestService supabaseService;

    //auto intialize using the constructor
    @Autowired
    public FaceDataRepository(SupabaseRestService supabaseService) {
        this.supabaseService = supabaseService;
    }

    //inserting a student record (POST Method)
    public FaceData create(FaceData faceData) {
        return supabaseService.create(TABLE, faceData, FaceData.class);
    }

    // retrieve all students (GET method)
    public List<FaceData> findAll() {
        return supabaseService.read(TABLE, null, FaceData[].class);
    }



}