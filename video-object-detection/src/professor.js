console.log("professor.js loaded");

// ===== AUTH CHECK =====
import { displayUserInfo, logout, authService } from './authCheck.js';

// Auth check and user info
const userInfo = displayUserInfo();
// NOTE: Using a hardcoded ID for demo purposes as requested, 
// but in a real app, userInfo.id should be used.
const DEMO_PROFESSOR_ID = "5ad0725f-18f5-446a-bb72-8b07730d8548"; 

console.log('Logged in as:', userInfo.name, 'ID:', userInfo.id); 

// Update welcome text with actual user name
const welcomeText = document.querySelector('.welcome-text');
if (welcomeText) {
    welcomeText.textContent = `${userInfo.name}`;
}

// ===== CONFIGURATION =====
const API_BASE_URL = 'http://localhost:8080/api';

// ===== GLOBAL VARIABLES =====
let attendanceRecords = [
  { date: "2024-01-15", class: "CS101", status: "Present", time: "09:00 AM" },
  { date: "2024-01-14", class: "MATH201", status: "Present", time: "11:00 AM" },
  { date: "2024-01-13", class: "PHY301", status: "Absent", time: "-" },
];

let allSessions = [];
// This will store flattened objects: { display_name: "CS102 Programming related - G3", class_code: "CS102", group_number: "G3" }
let professorClasses = []; 

// ===== CLASS DATA FETCHING (UPDATED FOR HARDCODED ID) =====

/**
 * Fetches the list of classes and flattens them into session-specific display names.
 * Format: "ClassCode ClassName - GroupNumber"
 * @returns {Array} An array of flattened class/group objects.
 */
async function fetchProfessorClasses() {
    // Use the hardcoded ID for the specific API endpoint requested
    const professorId = DEMO_PROFESSOR_ID; 

    try {
        // Construct the specific API endpoint
        const url = `/professors/${professorId}/classes`;
        const response = await authService.apiRequest(url);
        
        if (!response.ok) {
            const errorBody = await response.text();
            throw new Error(`Failed to fetch professor classes: ${response.status} - ${errorBody}`);
        }

        const classes = await response.json();
        
        // Flatten the data structure to get all groups taught by the professor
        const flattenedClasses = [];
        
        classes.forEach(classItem => {
            // Find groups where the current professor is the assigned professor (using the hardcoded ID)
            const professorGroups = classItem.groups.filter(group => group.professor_id === professorId);

            professorGroups.forEach(group => {
                // Creates the requested format: "CS102 Programming related - G3"
                const display_name = `${classItem.class_code} ${classItem.class_name} - ${group.group_number}`;
                
                flattenedClasses.push({
                    display_name: display_name,
                    class_code: classItem.class_code,
                    class_name: classItem.class_name,
                    group_number: group.group_number,
                    student_list: group.student_list 
                });
            });
        });

        professorClasses = flattenedClasses; // Store the flattened list globally
        console.log('Professor classes loaded:', professorClasses);
        return flattenedClasses;
    } catch (error) {
        console.error('Error fetching professor classes:', error);
        return [];
    }
}

/**
 * Dynamically creates a <datalist> for class names based on fetched data.
 */
function setupClassNameDatalist() {
    const classNameInput = document.getElementById('className');
    const datalistId = 'class-session-list';

    // 1. Ensure the input is correctly configured to use the datalist
    if (classNameInput) {
        classNameInput.setAttribute('list', datalistId);
    }
    
    // 2. Remove any existing datalist with the same ID
    let datalist = document.getElementById(datalistId);
    if (datalist) {
        datalist.remove();
    }

    // 3. Create the new datalist element
    datalist = document.createElement('datalist');
    datalist.id = datalistId;
    
    // 4. Populate the datalist with <option> elements using the display_name
    professorClasses.forEach(classItem => {
        const option = document.createElement('option');
        // The value attribute is what appears in the dropdown and in the input field.
        option.value = classItem.display_name; 
        datalist.appendChild(option);
    });

    // 5. Append the datalist to the form for proper scope
    const createClassForm = document.getElementById("createClassForm");
    if (createClassForm) {
        createClassForm.appendChild(datalist);
    } else {
        document.body.appendChild(datalist);
    }
}


// ===== SESSION MANAGEMENT (UNCHANGED) =====

// Fetch all sessions from backend
async function showSessions() {
  try {
    const response = await authService.apiRequest('/sessions');
    return await response.json();
  } catch (error) {
    console.error('Error fetching sessions:', error);
    return [];
  }
}

// Load and display active sessions
async function loadActiveSessions() {
  try {
    const response = await authService.apiRequest('/sessions');
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

// Initialize all necessary data on page load
async function init() {
  // 1. Load professor classes first
  await fetchProfessorClasses(); 
  
  // 2. Load sessions and display active sessions
  allSessions = await showSessions();
  console.log('Sessions loaded:', allSessions);
  loadActiveSessions(); 
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

// ===== ATTENDANCE FUNCTIONS (UNCHANGED) =====

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


// ===== MODAL FUNCTIONS (UPDATED openCreateClass) =====

/**
 * Opens the create class modal and sets up the class name datalist.
 */
function openCreateClass() {
  // Populate the datalist with the fetched classes before showing the modal
  setupClassNameDatalist();
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

// ===== PAGE INITIALIZATION (UPDATED) =====

document.addEventListener("DOMContentLoaded", () => {
  updateStudentStats();
  init(); 

  // Handle Create Session Form - SINGLE EVENT LISTENER
  const createClassForm = document.getElementById("createClassForm");
  if (createClassForm) {
    createClassForm.addEventListener("submit", async (e) => {
      e.preventDefault();
      
      // Get form values
      const classNameValue = document.getElementById('className').value.trim();
      const sessionDate = document.getElementById('sessionDate').value;
      const start_time = document.getElementById('startTime').value;
      const durationInput = document.getElementById('duration');
      const duration = durationInput ? parseInt(durationInput.value) : 90;
      
      // Find the corresponding class object in professorClasses using the display name
      const selectedClass = professorClasses.find(c => c.display_name === classNameValue);
      
      if (!selectedClass) {
          alert("Please select a valid class and group from the list.");
          return;
      }
      
      // Calculate end time based on duration
      const end_time = calculateEndTime(start_time, duration);
      
      // Prepare data for API
      const sessionData = {
        // Use the full display name for the session name
        name: selectedClass.display_name, 
        class_code: selectedClass.class_code,
        group_number: selectedClass.group_number, // Include group number in submission
        date: sessionDate,
        start_time: start_time + ':00', 
        end_time: end_time + ':00'
      };
      
      console.log('Sending session data:', sessionData);
      
      try {
        // Show loading state
        const submitButton = e.target.querySelector('button[type="submit"]');
        const originalText = submitButton.textContent;
        submitButton.textContent = 'Creating...';
        submitButton.disabled = true;

        const response = await authService.apiRequest('/sessions', {
          method: 'POST',
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

async function exportCSV() {
  await exportReport("csv");
}

function exportPDF() {
    const classSelect = document.querySelector("#generateReportModal select");
    const selectedClass = classSelect ? classSelect.value : "";

    const url = `${API_BASE_URL}/reports/generate?format=pdf&className=${encodeURIComponent(selectedClass)}`;

    window.open(url, "_blank");
}


async function exportReport(format) {
  try {
    const response = await authService.apiRequest(`/reports/generate?format=${format}`, {
      method: "POST"
    });

    if (!response.ok) {
      alert("Failed to generate report.");
      return;
    }

    const blob = await response.blob();
    const url = window.URL.createObjectURL(blob);

    const a = document.createElement("a");
    a.href = url;
    a.download = `students.${format}`;
    document.body.appendChild(a);
    a.click();

    a.remove();
    window.URL.revokeObjectURL(url);
  } catch (error) {
    console.error("Error generating report:", error);
    alert("Error generating report");
  }
}

// ===== MAKE FUNCTIONS GLOBALLY ACCESSIBLE FOR ONCLICK HANDLERS (UNCHANGED) =====
window.closeActiveSession = closeActiveSession;
window.closeAttendanceCheck = closeAttendanceCheck;
window.closeCreateClass = closeCreateClass;
window.closeGenerateReport = closeGenerateReport;
window.closeManualAttendance = closeManualAttendance;
window.closeStudentManagement = closeStudentManagement;
window.exportCSV = exportCSV;
window.exportPDF = exportPDF;
window.openAttendanceCheck = openAttendanceCheck;
window.openCreateClass = openCreateClass; 
window.openGenerateReport = openGenerateReport;
window.openManualAttendance = openManualAttendance;
window.openStudentManagement = openStudentManagement;
window.logout = logout;