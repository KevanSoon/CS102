package com.cs102.attendance;

import com.cs102.attendance.entity.*;
import com.cs102.attendance.enums.Method;
import com.cs102.attendance.enums.Status;
import com.cs102.attendance.repository.*;
import com.cs102.attendance.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class DatabaseIntegrationTest {

    @Autowired private StudentRepository studentRepository;
    @Autowired private SessionRepository sessionRepository;
    @Autowired private AttendanceRepository attendanceRepository;
    @Autowired private FaceDataRepository faceDataRepository;
    
    @Autowired private StudentService studentService;
    @Autowired private SessionService sessionService;
    
    @Test
    @Transactional
    public void testCompleteWorkflow() {
        System.out.println("Testing complete database workflow...");
        
        // 1. Test Student Creation
        System.out.println("1. Creating student...");
        Student student = studentService.enrol("CS001", "John Doe", "CS102", "Group A", "john@example.com", "123-456-7890");
        assertNotNull(student.getId());
        assertEquals("CS001", student.getCode());
        System.out.println("Student created: " + student.getName());
        
        // 2. Test Session Creation
        System.out.println("2. Creating session...");
        Session session = sessionService.createSession("Morning Lecture", LocalDate.now(), LocalTime.of(9, 0), LocalTime.of(11, 0));
        assertNotNull(session.getId());
        assertEquals("Morning Lecture", session.getName());
        System.out.println("Session created: " + session.getName());
        
        // 3. Test Face Data Upload
        System.out.println("3. Uploading face data...");
        FaceData faceData = studentService.uploadFaceImage(student.getId(), "http://example.com/face.jpg");
        assertNotNull(faceData.getId());
        assertEquals(student.getId(), faceData.getStudent().getId());
        System.out.println("Face data uploaded");
        
        // 4. Test Attendance Record
        System.out.println("4. Creating attendance record...");
        AttendanceRecord record = new AttendanceRecord(student, session, Status.PRESENT, Method.MANUAL);
        record = attendanceRepository.save(record);
        assertNotNull(record.getId());
        assertEquals(Status.PRESENT, record.getStatus());
        System.out.println("Attendance record created");
        
        // 5. Test Repository Queries
        System.out.println("5. Testing repository queries...");
        var foundRecord = attendanceRepository.findBySessionAndStudent(session, student);
        assertTrue(foundRecord.isPresent());
        
        long presentCount = attendanceRepository.countBySessionAndStatus(session, Status.PRESENT);
        assertEquals(1, presentCount);
        System.out.println("Repository queries working");
        
        // 6. Test Relationships
        System.out.println("6. Testing entity relationships...");
        Student savedStudent = studentRepository.findById(student.getId()).orElse(null);
        assertNotNull(savedStudent);
        assertEquals(1, savedStudent.getFaceData().size());
        assertEquals(1, savedStudent.getAttendanceRecords().size());
        System.out.println("Entity relationships working");
        
        System.out.println("All database tests passed.");
    }
    
    @Test
    public void testRepositoryBasicOperations() {
        System.out.println("Testing basic repository operations...");
        
        // Test counts
        long studentCount = studentRepository.count();
        long sessionCount = sessionRepository.count();
        long attendanceCount = attendanceRepository.count();
        long faceDataCount = faceDataRepository.count();
        
        System.out.println("Current counts:");
        System.out.println("   Students: " + studentCount);
        System.out.println("   Sessions: " + sessionCount);
        System.out.println("   Attendance Records: " + attendanceCount);
        System.out.println("   Face Data: " + faceDataCount);
        
        assertTrue(studentCount >= 0);
        assertTrue(sessionCount >= 0);
        assertTrue(attendanceCount >= 0);
        assertTrue(faceDataCount >= 0);
        
        System.out.println("Repository basic operations working");
    }
} 