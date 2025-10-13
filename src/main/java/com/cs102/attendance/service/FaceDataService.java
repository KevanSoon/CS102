package com.cs102.attendance.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cs102.attendance.entity.FaceData;
import com.cs102.attendance.repository.FaceDataRepository;

@Service
public class FaceDataService {
    
    @Autowired
    private FaceDataRepository faceDataRepository;

    public List<FaceData> getAllFaceData() {
        List<FaceData> faceDataList = faceDataRepository.findAll();
        System.out.println("Retrieved " + faceDataList.size() + " face data records");
        return faceDataList;
    }

    public Optional<FaceData> getFaceDataById(UUID id) {
        return faceDataRepository.findById(id);
    }

    public FaceData saveFaceData(FaceData faceData) {
        return faceDataRepository.save(faceData);
    }

    public void deleteFaceData(UUID id) {
        faceDataRepository.deleteById(id);
    }
}