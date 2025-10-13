package com.cs102.attendance.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cs102.attendance.dto.FaceDataDto;
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

    @Transactional(readOnly = true)
    public List<FaceDataDto> getAllFaceDataForFastApi() {
        try {
            List<FaceData> faceDataList = faceDataRepository.findAll();
            System.out.println("Retrieved " + faceDataList.size() + " face data records for FastAPI");
            
            return faceDataList.stream()
                .map(faceData -> {
                    try {
                        UUID studentId = faceData.getStudent().getId();
                        String imageUrl = faceData.getImageUrl();
                        System.out.println("Processing face data - Student ID: " + studentId + ", Image URL: " + imageUrl);
                        return new FaceDataDto(studentId.toString(), imageUrl);
                    } catch (Exception e) {
                        System.err.println("Error processing face data: " + e.getMessage());
                        throw e;
                    }
                })
                .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Error in getAllFaceDataForFastApi: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
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