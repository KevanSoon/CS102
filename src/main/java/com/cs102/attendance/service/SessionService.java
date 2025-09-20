package com.cs102.attendance.service;

import com.cs102.attendance.entity.Session;
import com.cs102.attendance.repository.SessionRepository;
import com.cs102.attendance.repository.AttendanceRepository;
import com.cs102.attendance.dto.SessionDto;
import com.cs102.attendance.enums.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class SessionService {
    
    @Autowired
    private SessionRepository sessionRepository;
    
    @Autowired
    private AttendanceRepository attendanceRepository;
    
    public Session createSession(String name, LocalDate date, LocalTime startTime, LocalTime endTime) {
        Session session = new Session(name, date, startTime, endTime);
        return sessionRepository.save(session);
    }
    
    public Session closeSession(UUID sessionId) {
        Optional<Session> optionalSession = sessionRepository.findById(sessionId);
        if (optionalSession.isPresent()) {
            Session session = optionalSession.get();
            // Session is considered closed when end time has passed
            // Additional logic can be added here if needed
            return session;
        }
        throw new RuntimeException("Session not found with id: " + sessionId);
    }
    
    public List<Session> getTodaySessions() {
        return getTodaySessions(LocalDate.now());
    }
    
    public List<Session> getTodaySessions(LocalDate date) {
        return sessionRepository.findAll().stream()
                .filter(session -> session.getDate().equals(date))
                .collect(Collectors.toList());
    }
    
    public SessionDto getSessionDto(Session session) {
        SessionDto dto = new SessionDto(
            session.getId(),
            session.getName(),
            session.getDate(),
            session.getStartTime(),
            session.getEndTime()
        );
        
        // Calculate attendance counts
        dto.setPresentCount(attendanceRepository.countBySessionAndStatus(session, Status.PRESENT));
        dto.setAbsentCount(attendanceRepository.countBySessionAndStatus(session, Status.ABSENT));
        dto.setLateCount(attendanceRepository.countBySessionAndStatus(session, Status.LATE));
        dto.setTotalStudents(dto.getPresentCount() + dto.getAbsentCount() + dto.getLateCount());
        
        return dto;
    }
    
    public List<SessionDto> getTodaySessionDtos() {
        return getTodaySessions().stream()
                .map(this::getSessionDto)
                .collect(Collectors.toList());
    }
    
    public List<Session> getAllSessions() {
        return sessionRepository.findAll();
    }
    
    public Optional<Session> getSessionById(UUID id) {
        return sessionRepository.findById(id);
    }
    
    public void deleteSession(UUID id) {
        sessionRepository.deleteById(id);
    }
} 