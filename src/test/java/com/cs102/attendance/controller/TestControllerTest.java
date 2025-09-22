// package com.cs102.attendance.controller;

// import com.cs102.attendance.entity.TestConnection;
// import com.cs102.attendance.repository.TestConnectionRepository;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
// import org.springframework.test.context.bean.override.mockito.MockitoBean;
// import org.springframework.http.MediaType;
// import org.springframework.test.web.servlet.MockMvc;

// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.Mockito.when;
// import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// @WebMvcTest(TestController.class)
// public class TestControllerTest {

//     @Autowired
//     private MockMvc mockMvc;

//     @MockitoBean
//     private TestConnectionRepository testConnectionRepository;

//     @Test
//     public void testInsertTestDataWithDefaultMessage() throws Exception {
//         // Given
//         TestConnection mockSavedEntity = new TestConnection("Test message");
//         mockSavedEntity.setId(1L);
//         when(testConnectionRepository.save(any(TestConnection.class))).thenReturn(mockSavedEntity);

//         // When & Then
//         mockMvc.perform(post("/api/test/insert-test-data")
//                 .contentType(MediaType.APPLICATION_JSON))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.id").value(1L))
//                 .andExpect(jsonPath("$.message").value("Test message"));
//     }

//     @Test
//     public void testInsertTestDataWithCustomMessage() throws Exception {
//         // Given
//         String customMessage = "Custom test message";
//         TestConnection mockSavedEntity = new TestConnection(customMessage);
//         mockSavedEntity.setId(2L);
//         when(testConnectionRepository.save(any(TestConnection.class))).thenReturn(mockSavedEntity);

//         // When & Then
//         mockMvc.perform(post("/api/test/insert-test-data")
//                 .param("message", customMessage)
//                 .contentType(MediaType.APPLICATION_JSON))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.id").value(2L))
//                 .andExpect(jsonPath("$.message").value(customMessage));
//     }

//     @Test
//     public void testInsertTestDataWithException() throws Exception {
//         // Given
//         when(testConnectionRepository.save(any(TestConnection.class)))
//                 .thenThrow(new RuntimeException("Database error"));

//         // When & Then
//         mockMvc.perform(post("/api/test/insert-test-data")
//                 .contentType(MediaType.APPLICATION_JSON))
//                 .andExpect(status().isInternalServerError());
//     }
// } 