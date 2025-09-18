package com.cs102.attendance.repository;

import com.cs102.attendance.entity.TestConnection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TestConnectionRepository extends JpaRepository<TestConnection, Long> {
    
    @Query(value = "SELECT 1", nativeQuery = true)
    Integer testQuery();
} 