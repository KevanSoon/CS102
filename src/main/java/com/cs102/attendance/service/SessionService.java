package com.cs102.attendance.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.cs102.attendance.dto.SessionUpdateDTO;
import com.cs102.attendance.model.AttendanceRecord;
import com.cs102.attendance.model.Session;


@Service
public class SessionService extends SupabaseService<Session> {
    private static final Logger logger = LoggerFactory.getLogger(SessionService.class);
    
    private final AttendanceRecordService attendanceRecordService;

    public SessionService(WebClient webClient, AttendanceRecordService attendanceRecordService) {
        super(webClient, "sessions", Session[].class, Session.class);
        this.attendanceRecordService = attendanceRecordService;
    }

    public Session update(String id, SessionUpdateDTO updatedDto) {
        // Calls the generic update method but with DTO object for patch
        return super.update(id, updatedDto);
    }

    public Session getActiveSession(String professorId) {
        // Get all sessions
        List<Session> allSessions = getAll();
        
        // Filter for active sessions by this professor
        List<Session> activeSessions = allSessions.stream()
                .filter(s -> s.getActive() != null && s.getActive())
                .filter(s -> s.getCreatedBy() != null && 
                            s.getCreatedBy().toString().equals(professorId))
                .collect(Collectors.toList());
        
        // Handle results
        if (activeSessions.isEmpty()) {
            return null;  // No active session
        }
        
        if (activeSessions.size() > 1) {
            logger.warn("Professor {} has {} active sessions!", professorId, activeSessions.size());
        }
        
        return activeSessions.get(0);  // Return first active session
    }

    public Session getById(String id) {
        return super.getById(id);
    }

    /**
     * Gets just the list of student IDs for a session (without full details)
     */
    public List<String> getSessionStudentIds(String sessionId) {
        try {
            Session session = getById(sessionId);
            
            if (session == null) {
                return List.of();
            }
            
            String classCode = session.getClassCode();
            String groupNumber = session.getGroupNumber();
            
            // Get the group to access student_list
            List<Map> groupsResponse = webClient.get()
                .uri(uriBuilder -> uriBuilder
                    .path("groups")
                    .queryParam("class_code", "eq." + classCode)
                    .queryParam("group_number", "eq." + groupNumber)
                    .queryParam("select", "student_list")
                    .build())
                .retrieve()
                .bodyToMono(List.class)
                .block();

            if (groupsResponse == null || groupsResponse.isEmpty()) {
                return List.of();
            }

            Map groupData = groupsResponse.get(0);
            List<String> studentIds = (List<String>) groupData.get("student_list");
            
            return studentIds != null ? studentIds : List.of();
            
        } catch (Exception e) {
            logger.error("Error fetching student IDs", e);
            return List.of();
        }
    }

    public List<Map<String, Object>> getSessionStudents(String sessionId) {
        try {
            // First, get the session to find class_code and group_number
            Session session = getById(sessionId);
            
            if (session == null) {
                throw new RuntimeException("Session not found");
            }
            
            String classCode = session.getClassCode();
            String groupNumber = session.getGroupNumber();
            
            logger.debug("Searching for group with class_code: {}, group_number: {}", classCode, groupNumber);
            
            // Get the group to access student_list
            List<Map> groupsResponse = webClient.get()
                .uri(uriBuilder -> uriBuilder
                    .path("groups")
                    .queryParam("class_code", "eq." + classCode)
                    .queryParam("group_number", "eq." + groupNumber)
                    .queryParam("select", "student_list")
                    .build())
                .retrieve()
                .bodyToMono(List.class)
                .block();

            if (groupsResponse == null || groupsResponse.isEmpty()) {
                logger.warn("No group found for class_code: {}, group_number: {}", classCode, groupNumber);
                return new ArrayList<>();
            }

            // Get the student_list from the group
            Map groupData = groupsResponse.get(0);
            List<String> studentIds = (List<String>) groupData.get("student_list");
            
            if (studentIds == null || studentIds.isEmpty()) {
                logger.warn("No students in group class_code: {}, group_number: {}", classCode, groupNumber);
                return new ArrayList<>();
            }
            
            logger.debug("Found {} students in group", studentIds.size());

            // Fetch student details for each student ID
            List<Map<String, Object>> result = new ArrayList<>();
            
            for (String studentId : studentIds) {
                List<Map> studentResponse = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                        .path("students")
                        .queryParam("id", "eq." + studentId)
                        .queryParam("select", "id,name,email")
                        .build())
                    .retrieve()
                    .bodyToMono(List.class)
                    .block();
                
                if (studentResponse != null && !studentResponse.isEmpty()) {
                    Map studentMap = studentResponse.get(0);
                    
                    Map<String, Object> studentInfo = new HashMap<>();
                    studentInfo.put("id", studentMap.get("id"));
                    studentInfo.put("name", studentMap.get("name"));
                    studentInfo.put("email", studentMap.get("email"));
                    studentInfo.put("status", "absent"); // Default status
                    
                    result.add(studentInfo);
                }
            }

            // Get attendance records for this session
            List<Map> attendanceResponse = webClient.get()
                .uri(uriBuilder -> uriBuilder
                    .path("attendance_records")
                    .queryParam("session_id", "eq." + sessionId)
                    .queryParam("select", "*")
                    .build())
                .retrieve()
                .bodyToMono(List.class)
                .block();

            // Update attendance status
            if (attendanceResponse != null) {
                Map<String, String> attendanceMap = new HashMap<>();
                for (Map record : attendanceResponse) {
                    String studentId = (String) record.get("student_id");
                    String status = (String) record.get("status");
                    attendanceMap.put(studentId, status);
                }
                
                for (Map<String, Object> student : result) {
                    String studentId = (String) student.get("id");
                    student.put("status", attendanceMap.getOrDefault(studentId, "absent"));
                }
            }

            return result;

        } catch (Exception e) {
            logger.error("Error fetching session students for session {}", sessionId, e);
            throw new RuntimeException("Failed to fetch students for session", e);
        }
    }
    
    /**
     * Marks all unmarked students as ABSENT for a given session
     * Used by both manual close and auto-close
     */
    public void markAbsentStudentsForSession(String sessionId) {
        try {
            logger.debug("Marking absent students for session {}", sessionId);
            
            // Get all attendance records for this session
            List<AttendanceRecord> existingRecords = attendanceRecordService.getBySession(sessionId);
            
            // Get the list of students who should be in this session
            List<String> expectedStudents = getSessionStudentIds(sessionId);
            
            if (expectedStudents == null || expectedStudents.isEmpty()) {
                logger.warn("No student list found for session {}", sessionId);
                return;
            }
            
            logger.debug("Expected students: {}, Existing attendance records: {}", 
                expectedStudents.size(), existingRecords.size());
            
            // Track which students already have attendance records
            List<String> markedStudents = existingRecords.stream()
                .map(AttendanceRecord::getStudent_id)
                .toList();
            
            // Mark remaining students as absent
            int absentCount = 0;
            for (String studentId : expectedStudents) {
                if (!markedStudents.contains(studentId)) {
                    try {
                        AttendanceRecord absentRecord = new AttendanceRecord();
                        absentRecord.setSession_id(sessionId);
                        absentRecord.setStudent_id(studentId);
                        absentRecord.setStatus("ABSENT");
                        absentRecord.setMethod("AUTO");
                        absentRecord.setMarked_at(LocalDateTime.now(ZoneId.of("Asia/Singapore")));
                        absentRecord.setConfidence(null);
                        
                        attendanceRecordService.create(absentRecord);
                        absentCount++;
                        
                        logger.debug("Marked student {} as ABSENT", studentId);
                    } catch (Exception e) {
                        logger.error("Error marking student {} as absent", studentId, e);
                    }
                }
            }
            
            logger.info("Marked {} student(s) as ABSENT for session {}", absentCount, sessionId);
            
        } catch (Exception e) {
            logger.error("Error marking absent students for session {}", sessionId, e);
        }
    }
    
}
