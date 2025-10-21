console.log("professor.js loaded");

// ===== CONFIGURATION =====
const API_BASE_URL = 'http://localhost:8080/api'; // Add this at the top!

// ===== GLOBAL VARIABLES =====
let attendanceRecords = [
  { date: "2024-01-15", class: "CS101", status: "Present", time: "09:00 AM" },
  { date: "2024-01-14", class: "MATH201", status: "Present", time: "11:00 AM" },
  { date: "2024-01-13", class: "PHY301", status: "Absent", time: "-" },
];

let allSessions = [];

// ===== SESSION MANAGEMENT =====

// Fetch all sessions from backend
async function showSessions() {
  try {
    const response = await fetch(`${API_BASE_URL}/sessions`);
    return await response.json();
  } catch (error) {
    console.error('Error fetching sessions:', error);
    return [];
  }
}

// Load and display active sessions
async function loadActiveSessions() {
  try {
    const response = await fetch(`${API_BASE_URL}/sessions`);
    if (!response.ok) {
      throw new Error('Failed to load sessions');
    }
    
    const sessions = await response.json();
    const activeSessions = document.getElementById('activeSessions');
    
    if (sessions.length === 0) {
      activeSessions.innerHTML = '<p class="welcome-subtitle">No active session at the moment</p>';
    } else {
      // Display the most recent session as "active"
      const latestSession = sessions[0];
      activeSessions.innerHTML = `
        <div class="stat-card">
          <div class="stat-content">
            <h4>${latestSession.name}</h4>
            <p>📅 ${latestSession.date} | ⏰ ${latestSession.startTime}</p>
          </div>
          <button class="btn btn-primary" onclick="openFaceScanning()">
            Scan Face for Attendance
          </button>
          <button class="btn btn-danger" onclick="closeActiveSession()">Close Active Session</button>
        </div>
        
        <div class="student-list">
          <div class="student-item">
            <span>John Doe (john@example.com)</span>
            <div class="attendance-controls">
              <button class="btn btn-success btn-sm">Present</button>
              <button class="btn btn-danger btn-sm">Absent</button>
            </div>
          </div>
          <div class="student-item">
            <span>Jane Smith (jane@example.com)</span>
            <div class="attendance-controls">
              <button class="btn btn-success btn-sm">Present</button>
              <button class="btn btn-danger btn-sm">Absent</button>
            </div>
          </div>
          <div class="student-item">
            <span>Mike Johnson (mike@example.com)</span>
            <div class="attendance-controls">
              <button class="btn btn-success btn-sm">Present</button>
              <button class="btn btn-danger btn-sm">Absent</button>
            </div>
          </div>
        </div>
      `;
    }
  } catch (error) {
    console.error('Error loading sessions:', error);
  }
}

// Initialize sessions on page load
async function init() {
  allSessions = await showSessions();
  console.log('Sessions loaded:', allSessions);
  loadActiveSessions(); // Also load the active sessions display
}

// Helper function to calculate end time
function calculateEndTime(startTime, durationMinutes) {
  const [hours, minutes] = startTime.split(':').map(Number);
  const startDate = new Date();
  startDate.setHours(hours, minutes, 0);
  
  startDate.setMinutes(startDate.getMinutes() + durationMinutes);
  
  const endHours = String(startDate.getHours()).padStart(2, '0');
  const endMinutes = String(startDate.getMinutes()).padStart(2, '0');
  
  return `${endHours}:${endMinutes}`;
}

// ===== ATTENDANCE FUNCTIONS =====

function markAttendance() {
  if (!selectedClass || !faceScanned) {
    alert("Please scan your face first");
    return;
  }

  const now = new Date();
  const dateStr = now.toISOString().split("T")[0];
  const timeStr = now.toLocaleTimeString("en-US", {
    hour: "2-digit",
    minute: "2-digit",
    hour12: true,
  });

  const classSelect = document.getElementById("classSelect");
  const className = classSelect.options[classSelect.selectedIndex].text.split(" - ")[0];

  const newRecord = {
    date: dateStr,
    class: className,
    status: "Present",
    time: timeStr,
  };

  attendanceRecords = attendanceRecords.filter(
    (record) => !(record.date === dateStr && record.class === className)
  );

  attendanceRecords.unshift(newRecord);
  updateStudentStats();

  alert(`Attendance marked successfully for ${className}!`);
  closeFaceScanning();

  document.getElementById("classSelect").value = "";
  document.getElementById("scanFaceBtn").style.display = "none";
  selectedClass = "";
}

function updateStudentStats() {
  const totalClasses = attendanceRecords.length;
  const presentClasses = attendanceRecords.filter((record) => record.status === "Present").length;
  const attendanceRate = totalClasses > 0 ? Math.round((presentClasses / totalClasses) * 100) : 0;

  const statNumbers = document.querySelectorAll(".stat-number");
  if (statNumbers.length >= 3) {
    statNumbers[0].textContent = `${attendanceRate}%`;
    statNumbers[1].textContent = totalClasses;

    const today = new Date().toISOString().split("T")[0];
    const classesToday = attendanceRecords.filter((record) => record.date === today).length;
    statNumbers[2].textContent = classesToday;
  }
}

// ===== MODAL FUNCTIONS =====

function openCreateClass() {
  document.getElementById("createClassModal").classList.add("active");
}

function closeCreateClass() {
  document.getElementById("createClassModal").classList.remove("active");
  document.getElementById("createClassForm").reset();
}

function openManualAttendance() {
  document.getElementById("manualAttendanceModal").classList.add("active");
}

function closeManualAttendance() {
  document.getElementById("manualAttendanceModal").classList.remove("active");
}

function openGenerateReport() {
  document.getElementById("generateReportModal").classList.add("active");
}

function closeGenerateReport() {
  document.getElementById("generateReportModal").classList.remove("active");
}

function exportCSV() {
  alert("Exporting attendance report as CSV...");
}

function exportPDF() {
  alert("Exporting attendance report as PDF...");
}

function openStudentManagement() {
  document.getElementById("studentManagementModal").classList.add("active");
}

function closeStudentManagement() {
  document.getElementById("studentManagementModal").classList.remove("active");
}

function openAttendanceCheck() {
  document.getElementById("attendanceCheckModal").classList.add("active");
}

function closeAttendanceCheck() {
  document.getElementById("attendanceCheckModal").classList.remove("active");
}

function closeActiveSession() {
  if (confirm("Are you sure you want to close the active session?")) {
    alert("Active session closed successfully!");
    const activeSessions = document.getElementById("activeSessions");
    if (activeSessions) {
      activeSessions.innerHTML = '<p class="welcome-subtitle">No active sessions at the moment</p>';
    }
  }
}

function showRegister() {
  document.getElementById("faceRegisterModal").classList.add("active");
}

function closeFaceRegister() {
  document.getElementById("faceRegisterModal").classList.remove("active");
}

function captureFace() {
  alert("Face captured successfully! Registration complete.");
  closeFaceRegister();
}

function logout() {
  window.location.href = "index.html";
}

// ===== PAGE INITIALIZATION =====

document.addEventListener("DOMContentLoaded", () => {
  updateStudentStats();
  init();

  // Handle Create Session Form - SINGLE EVENT LISTENER
  const createClassForm = document.getElementById("createClassForm");
  if (createClassForm) {
    createClassForm.addEventListener("submit", async (e) => {
      e.preventDefault();
      
      // Get form values
      const className = document.getElementById('className').value.trim();
      const sessionDate = document.getElementById('sessionDate').value;
      const start_time = document.getElementById('startTime').value;
      // Check if duration field exists, if not default to 60 minutes
      const durationInput = document.getElementById('duration');
      const duration = durationInput ? parseInt(durationInput.value) : 90;
      
      // Calculate end time based on duration
      const end_time = calculateEndTime(start_time, duration);
      
      // Prepare data for API
      const sessionData = {
        name: className,
        date: sessionDate,
        start_time: start_time + ':00', // Add seconds
        end_time: end_time + ':00'
      };
      
      console.log('Sending session data:', sessionData);
      
      try {
        // Show loading state
        const submitButton = e.target.querySelector('button[type="submit"]');
        const originalText = submitButton.textContent;
        submitButton.textContent = 'Creating...';
        submitButton.disabled = true;
        
        const response = await fetch(`${API_BASE_URL}/sessions`, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify(sessionData)
        });
        
        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`);
        }
        
        const createdSession = await response.json();
        console.log('Session created successfully:', createdSession);
        
        alert('Session created successfully!');
        
        // Reset form and close modal
        closeCreateClass();
        
        // Refresh the sessions list
        await loadActiveSessions();
        
        // Reset button state
        submitButton.textContent = originalText;
        submitButton.disabled = false;
        
      } catch (error) {
        console.error('Error creating session:', error);
        alert('Error creating session: ' + error.message);
        
        // Reset button state
        const submitButton = e.target.querySelector('button[type="submit"]');
        submitButton.textContent = 'Create Session';
        submitButton.disabled = false;
      }
    });
  }
});