// package com.cs102.attendance;

// import com.cs102.attendance.repository.StudentRepository;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.context.SpringBootTest;

// import javax.sql.DataSource;
// import java.sql.Connection;
// import java.sql.SQLException;

// import static org.junit.jupiter.api.Assertions.*;

// @SpringBootTest
// class Cs102AttendanceProjectApplicationTests {

// 	@Autowired
// 	private DataSource dataSource;

// 	@Autowired
// 	private StudentRepository studentRepository;

// 	@Test
// 	void contextLoads() {
// 	}

// 	@Test
// 	void testDatabaseConnection() throws SQLException {
// 		// Test that we can get a connection from the DataSource
// 		try (Connection connection = dataSource.getConnection()) {
// 			assertNotNull(connection, "Database connection should not be null");
// 			assertTrue(connection.isValid(5), "Database connection should be valid");
			
// 			// Print connection details for verification
// 			System.out.println("Database URL: " + connection.getMetaData().getURL());
// 			System.out.println("Database Driver: " + connection.getMetaData().getDriverName());
// 			System.out.println("Database Version: " + connection.getMetaData().getDatabaseProductVersion());
// 		}
// 	}

// 	@Test
// 	void testRepositoryConnection() {
// 		// Test that the repository can execute queries
// 		assertDoesNotThrow(() -> {
// 			long count = studentRepository.count();
// 			assertTrue(count >= 0, "Student count should be non-negative");
// 		}, "Repository test query should not throw an exception");
// 	}

// }
