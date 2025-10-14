package com.cs102.attendance.repository;

import org.springframework.stereotype.Repository;

@Repository
public interface SessionRepository /*extends JpaRepository<Session, UUID>*/ {
    //Optional<Session> findByNameAndDate(String name, LocalDate date);
} 