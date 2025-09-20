package com.cs102.attendance.repository;

import com.cs102.attendance.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface SessionRepository extends JpaRepository<Session, UUID> {
} 