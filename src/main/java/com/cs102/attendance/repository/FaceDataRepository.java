package com.cs102.attendance.repository;

import com.cs102.attendance.entity.FaceData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface FaceDataRepository extends JpaRepository<FaceData, UUID> {
} 