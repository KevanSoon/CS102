// package com.cs102.attendance.controller;

// import com.cs102.attendance.entity.Student;
// import com.cs102.attendance.repository.StudentRepository;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
// import org.springframework.test.context.bean.override.mockito.MockitoBean;
// import org.springframework.http.MediaType;
// import org.springframework.test.web.servlet.MockMvc;

// import java.util.UUID;
// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.Mockito.when;
// import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// @WebMvcTest(TestController.class)
// public class TestControllerTest {

//     @Autowired
//     private MockMvc mockMvc;

//     @MockitoBean
//     private StudentRepository studentRepository;

//     @Test
//     public void testInsertTestDataWithValidData() throws Exception {
//         // Given
//         Student mockSavedStudent = new Student("TEST001", "Test Student");
//         mockSavedStudent.setId(UUID.randomUUID());
//         when(studentRepository.save(any(Student.class))).thenReturn(mockSavedStudent);

//         // When & Then
//         mockMvc.perform(post("/api/test/insert-test-data")
//                 .param("code", "TEST001")
//                 .param("name", "Test Student")
//                 .contentType(MediaType.APPLICATION_JSON))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.code").value("TEST001"))
//                 .andExpect(jsonPath("$.name").value("Test Student"));
//     }

//     @Test
//     public void testInsertTestDataWithEmail() throws Exception {
//         // Given
//         Student mockSavedStudent = new Student("TEST002", "Test Student 2");
//         mockSavedStudent.setEmail("test@example.com");
//         mockSavedStudent.setId(UUID.randomUUID());
//         when(studentRepository.save(any(Student.class))).thenReturn(mockSavedStudent);

//         // When & Then
//         mockMvc.perform(post("/api/test/insert-test-data")
//                 .param("code", "TEST002")
//                 .param("name", "Test Student 2")
//                 .param("email", "test@example.com")
//                 .contentType(MediaType.APPLICATION_JSON))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.code").value("TEST002"))
//                 .andExpect(jsonPath("$.name").value("Test Student 2"))
//                 .andExpect(jsonPath("$.email").value("test@example.com"));
//     }

//     @Test
//     public void testInsertTestDataWithException() throws Exception {
//         // Given
//         when(studentRepository.save(any(Student.class)))
//                 .thenThrow(new RuntimeException("Database error"));

//         // When & Then
//         mockMvc.perform(post("/api/test/insert-test-data")
//                 .param("code", "TEST003")
//                 .param("name", "Test Student 3")
//                 .contentType(MediaType.APPLICATION_JSON))
//                 .andExpect(status().isInternalServerError());
//     }
// } 