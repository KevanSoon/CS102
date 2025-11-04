console.log("professor.js loaded");

// ===== AUTH CHECK =====
import { displayUserInfo, logout, authService } from './authCheck.js';

// Auth check and user info
const userInfo = displayUserInfo();
console.log('Logged in as:', userInfo.name);
const currentUser = authService.getUser();

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
    const professorId = currentUser.id;

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
// async function loadActiveSessions() {
//   try {
//     const response = await authService.apiRequest('/sessions');
//     if (!response.ok) {
//       throw new Error('Failed to load sessions');
//     }
    
//     const sessions = await response.json();
//     const activeSessions = document.getElementById('activeSessions');
    
//     if (sessions.length === 0) {
//       activeSessions.innerHTML = '<p class="welcome-subtitle">No active session at the moment</p>';
//     } else {
//       // Display the most recent session as "active"
//       const latestSession = sessions[0];
//       activeSessions.innerHTML = `
//         <div class="stat-card">
//           <div class="stat-content">
//             <h4>${latestSession.name}</h4>
//             <p>📅 ${latestSession.date} | ⏰ ${latestSession.startTime}</p>
//           </div>
//           <button class="btn btn-primary" onclick="openFaceScanning()">
//             Scan Face for Attendance
//           </button>
//           <button class="btn btn-danger" onclick="closeActiveSession()">Close Active Session</button>
//         </div>
        
//         <div class="student-list">
//           <div class="student-item">
//             <span>John Doe (john@example.com)</span>
//             <div class="attendance-controls">
//               <button class="btn btn-success btn-sm">Present</button>
//               <button class="btn btn-danger btn-sm">Absent</button>
//             </div>
//           </div>
//           <div class="student-item">
//             <span>Jane Smith (jane@example.com)</span>
//             <div class="attendance-controls">
//               <button class="btn btn-success btn-sm">Present</button>
//               <button class="btn btn-danger btn-sm">Absent</button>
//             </div>
//           </div>
//           <div class="student-item">
//             <span>Mike Johnson (mike@example.com)</span>
//             <div class="attendance-controls">
//               <button class="btn btn-success btn-sm">Present</button>
//               <button class="btn btn-danger btn-sm">Absent</button>
//             </div>
//           </div>
//         </div>
//       `;
//     }
//   } catch (error) {
//     console.error('Error loading sessions:', error);
//   }
// }

async function loadActiveSessions() {
  try {
    console.log('Loading active session for professor:', currentUser.id);
    
    const response = await fetch(`${API_BASE_URL}/sessions/active/${currentUser.id}`);
    
    if (!response.ok) {
      if (response.status === 404) {
        console.log('No active session found');
        displayNoActiveSession();
        return;
      }
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    
    // Check if response has content
    const text = await response.text();
    if (!text || text.trim() === '') {
      console.log('Empty response - no active session');
      displayNoActiveSession();
      return;
    }
    
    // Parse the JSON
    const activeSession = JSON.parse(text);
    console.log('Active session loaded:', activeSession);
    
    if (!activeSession || !activeSession.id) {
      displayNoActiveSession();
      return;
    }
    
    await displayActiveSession(activeSession);
    
  } catch (error) {
    console.error('Error loading active session:', error);
    displayNoActiveSession();
  }
}

async function loadSessionStudents(sessionId) {
  try {
    console.log('Loading students for session:', sessionId);
    
    const response = await fetch(`${API_BASE_URL}/sessions/${sessionId}/students`);
    
    if (!response.ok) {
      throw new Error('Failed to load students');
    }
    
    const students = await response.json();
    console.log('Students loaded:', students);
    
    return students;
    
  } catch (error) {
    console.error('Error loading students:', error);
    return [];
  }
}

async function displayActiveSession(session) {
  console.log('Displaying session:', session);
  
  const activeSessions = document.getElementById('activeSessions');
  
  // Clear any existing content
  while (activeSessions.firstChild) {
    activeSessions.removeChild(activeSessions.firstChild);
  }
  
  // Create card
  const card = document.createElement('div');
  card.className = 'stat-card';
  
  // Create content section
  const content = document.createElement('div');
  content.className = 'stat-content';
  
  // Session name (dynamic)
  const h4 = document.createElement('h4');
  h4.textContent = session.name;
  
  // Session details (dynamic)
  const p = document.createElement('p');
  const dateText = session.date || 'No date';
  const startTime = session.startTime || session.start_time || 'No time';
  p.textContent = `📅 ${dateText} | ⏰ Start: ${startTime}`;
  
  // Add end time if available
  const endTime = session.endTime || session.end_time;
  if (endTime) {
    const endSpan = document.createElement('span');
    endSpan.textContent = ` | End: ${endTime}`;
    p.appendChild(endSpan);
  }
  
  // Optional: Show class and group info
  if (session.class_code || session.classCode) {
    const classInfo = document.createElement('p');
    classInfo.style.fontSize = '0.875rem';
    classInfo.style.color = '#64748b';
    const classCode = session.class_code || session.classCode;
    const groupNum = session.group_number || session.groupNumber;
    classInfo.textContent = `Class: ${classCode}${groupNum ? ` | Group: ${groupNum}` : ''}`;
    content.appendChild(classInfo);
  }
  
  content.appendChild(h4);
  content.appendChild(p);
  
  // Scan button
  const scanBtn = document.createElement('button');
  scanBtn.className = 'btn btn-primary';
  scanBtn.textContent = 'Scan Face for Attendance';
  scanBtn.onclick = openFaceScanning;
  
  // Close button (dynamic session ID)
  const closeBtn = document.createElement('button');
  closeBtn.className = 'btn btn-danger';
  closeBtn.textContent = 'Close Session';
  closeBtn.onclick = () => closeActiveSession(session.id);
  
  // Assemble everything
  card.appendChild(content);
  card.appendChild(scanBtn);
  card.appendChild(closeBtn);
  
  activeSessions.appendChild(card);

  // Load and display students
  const students = await loadSessionStudents(session.id);
  
  if (students.length > 0) {
    const studentListContainer = document.createElement('div');
    studentListContainer.className = 'student-list';
    
    students.forEach(student => {
      const studentItem = document.createElement('div');
      studentItem.className = 'student-item';
      
      // Student name and email
      const studentInfo = document.createElement('span');
      studentInfo.textContent = `${student.name} (${student.email})`;
      
      // Attendance controls
      const controls = document.createElement('div');
      controls.className = 'attendance-controls';
      
      const presentBtn = document.createElement('button');
      presentBtn.className = 'btn btn-success btn-sm';
      presentBtn.textContent = 'Present';
      presentBtn.onclick = () => markStudentManually(session.id, student.id, 'present');
      
      // Highlight if already marked present
      if (student.status === 'present') {
        presentBtn.style.fontWeight = 'bold';
        presentBtn.style.backgroundColor = '#059669';
      }
      
      const absentBtn = document.createElement('button');
      absentBtn.className = 'btn btn-danger btn-sm';
      absentBtn.textContent = 'Absent';
      absentBtn.onclick = () => markStudentManually(session.id, student.id, 'absent');
      
      // Highlight if already marked absent
      if (student.status === 'absent') {
        absentBtn.style.fontWeight = 'bold';
        absentBtn.style.backgroundColor = '#dc2626';
      }
      
      controls.appendChild(presentBtn);
      controls.appendChild(absentBtn);
      
      studentItem.appendChild(studentInfo);
      studentItem.appendChild(controls);
      
      studentListContainer.appendChild(studentItem);
    });
    
    activeSessions.appendChild(studentListContainer);
  } else {
    const noStudents = document.createElement('p');
    noStudents.className = 'welcome-subtitle';
    noStudents.textContent = 'No students enrolled in this class/group';
    noStudents.style.marginTop = '1rem';
    activeSessions.appendChild(noStudents);
  }
}

// ===== DISPLAY NO SESSION MESSAGE =====
function displayNoActiveSession() {
  const activeSessions = document.getElementById('activeSessions');
  
  while (activeSessions.firstChild) {
    activeSessions.removeChild(activeSessions.firstChild);
  }
  
  const p = document.createElement('p');
  p.className = 'welcome-subtitle';
  p.textContent = 'No active session at the moment';
  activeSessions.appendChild(p);
}

// ===== CLOSE SESSION (DYNAMIC) =====
async function closeActiveSession(sessionId) {
  if (!confirm('Are you sure you want to close this session?')) {
    return;
  }
  
  try {
    console.log('Closing session:', sessionId);
    
    const response = await fetch(`${API_BASE_URL}/sessions/${sessionId}/close`, {
      method: 'PATCH',
      headers: {
        'Content-Type': 'application/json'
      }
    });
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    
    const closedSession = await response.json();
    console.log('Session closed:', closedSession);
    
    alert('Session closed successfully!');
    
    // Reload to show "no active session"
    await loadActiveSessions();
    
  } catch (error) {
    console.error('Error closing session:', error);
    alert('Failed to close session: ' + error.message);
  }
}

// Initialize all necessary data on page load
async function init() {
  // 1. Load professor classes first
  await fetchProfessorClasses(); 
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

// ===== SHARED ATTENDANCE LOGIC =====
async function saveAttendanceRecord(sessionId, studentId, status, method = 'manual') {
  try {
    // Check if record exists
    const checkResponse = await fetch(
      `${API_BASE_URL}/attendance-records?session_id=${sessionId}&student_id=${studentId}`
    );
    
    let response;
    
    if (checkResponse.ok) {
      const existingRecords = await checkResponse.json();
      
      if (existingRecords && existingRecords.length > 0) {
        // Update existing record
        const recordId = existingRecords[0].id;
        response = await fetch(`${API_BASE_URL}/attendance-records/${recordId}`, {
          method: 'PATCH',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            status: status,
            method: method,
            marked_at: new Date().toISOString()
          })
        });
      } else {
        // Create new record
        response = await fetch(`${API_BASE_URL}/attendance-records`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            session_id: sessionId,
            student_id: studentId,
            status: status,
            method: method,
            marked_at: new Date().toISOString()
          })
        });
      }
    }
    
    if (!response || !response.ok) {
      throw new Error('Failed to save attendance');
    }
    
    return await response.json();
    
  } catch (error) {
    console.error('Error saving attendance:', error);
    throw error;
  }
}

// ===== PROFESSOR MARKS STUDENT (MANUAL) =====
async function markStudentManually(sessionId, studentId, status) {
  try {
    console.log(`Marking student ${studentId} as ${status}`);
    
    await saveAttendanceRecord(sessionId, studentId, status, 'manual');
    
    console.log('Attendance marked successfully');
    
    // Reload to update UI
    await loadActiveSessions();
    
  } catch (error) {
    alert('Failed to mark attendance: ' + error.message);
  }
}

// ===== STUDENT MARKS THEMSELVES (FACE SCAN) =====
// function markAttendance() {
//   if (!selectedClass || !faceScanned) {
//     alert("Please scan your face first");
//     return;
//   }

//   const now = new Date();
//   const dateStr = now.toISOString().split("T")[0];
//   const timeStr = now.toLocaleTimeString("en-US", {
//     hour: "2-digit",
//     minute: "2-digit",
//     hour12: true,
//   });

//   const classSelect = document.getElementById("classSelect");
//   const className = classSelect.options[classSelect.selectedIndex].text.split(" - ")[0];

//   const newRecord = {
//     date: dateStr,
//     class: className,
//     status: "Present",
//     time: timeStr,
//   };

//   attendanceRecords = attendanceRecords.filter(
//     (record) => !(record.date === dateStr && record.class === className)
//   );

//   attendanceRecords.unshift(newRecord);
//   updateStudentStats();

//   alert(`Attendance marked successfully for ${className}!`);
//   closeFaceScanning();

//   document.getElementById("classSelect").value = "";
//   document.getElementById("scanFaceBtn").style.display = "none";
//   selectedClass = "";
// }

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
  // setupClassNameDatalist();
  loadProfessorClasses();
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

// function closeActiveSession() {
//   if (confirm("Are you sure you want to close the active session?")) {
//     alert("Active session closed successfully!");
//     const activeSessions = document.getElementById("activeSessions");
//     if (activeSessions) {
//       activeSessions.innerHTML = '<p class="welcome-subtitle">No active sessions at the moment</p>';
//     }
//   }
// }

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

// Store the classes data globally
let professorClassesData = [];

// Load professor's classes when modal opens
async function loadProfessorClasses() {
  try {
    console.log('Loading classes for professor:', currentUser.id);
    
    const response = await fetch(`${API_BASE_URL}/professors/${currentUser.id}/classes`);
    
    if (!response.ok) {
      throw new Error('Failed to load classes');
    }
    
    const classesData = await response.json();
    console.log('Classes loaded:', classesData);
    
    // Store data globally so we can access groups later
    professorClassesData = classesData;
    
    const classSelect = document.getElementById('classCodeSelect');
    
    // Clear existing options
    while (classSelect.firstChild) {
      classSelect.removeChild(classSelect.firstChild);
    }
    
    // Add default option
    const defaultOption = document.createElement('option');
    defaultOption.value = '';
    defaultOption.textContent = classesData.length > 0 ? 'Select a class' : 'No classes assigned';
    classSelect.appendChild(defaultOption);
    
    // Add class options
    classesData.forEach(classItem => {
      const option = document.createElement('option');
      option.value = classItem.class_code;
      option.textContent = `${classItem.class_code} - ${classItem.class_name}`;
      classSelect.appendChild(option);
    });
    
    // Enable the select if there are classes
    classSelect.disabled = classesData.length === 0;
    
  } catch (error) {
    console.error('Error loading classes:', error);
    alert('Failed to load classes. Please try again.');
    
    // Show error in dropdown
    const classSelect = document.getElementById('classCodeSelect');
    while (classSelect.firstChild) {
      classSelect.removeChild(classSelect.firstChild);
    }
    const errorOption = document.createElement('option');
    errorOption.value = '';
    errorOption.textContent = 'Error loading classes';
    classSelect.appendChild(errorOption);
    classSelect.disabled = true;
  }
}

// Load groups when a class is selected
function loadClassGroups(classCode) {
  const groupSelect = document.getElementById('groupNumberSelect');
  
  // Clear existing options
  while (groupSelect.firstChild) {
    groupSelect.removeChild(groupSelect.firstChild);
  }
  
  if (!classCode) {
    // No class selected - show placeholder
    const defaultOption = document.createElement('option');
    defaultOption.value = '';
    defaultOption.textContent = 'Select a class first';
    groupSelect.appendChild(defaultOption);
    groupSelect.disabled = true;
    return;
  }
  
  // Find the selected class in our data
  const selectedClass = professorClassesData.find(c => c.class_code === classCode);
  
  if (!selectedClass || !selectedClass.groups || selectedClass.groups.length === 0) {
    // No groups found
    const noGroupsOption = document.createElement('option');
    noGroupsOption.value = '';
    noGroupsOption.textContent = 'No groups available';
    groupSelect.appendChild(noGroupsOption);
    groupSelect.disabled = true;
    return;
  }
  
  // Add default option
  const defaultOption = document.createElement('option');
  defaultOption.value = '';
  defaultOption.textContent = 'Select a group';
  groupSelect.appendChild(defaultOption);
  
  // Add group options
  selectedClass.groups.forEach(group => {
    const option = document.createElement('option');
    option.value = group.group_number;
    option.textContent = `Group ${group.group_number}`;
    groupSelect.appendChild(option);
  });
  
  // Enable the select
  groupSelect.disabled = false;
}

// Update openCreateClass to load classes
// function openCreateClass() {
//   document.getElementById("createClassModal").classList.add("active");
//   loadProfessorClasses();  // Load classes when modal opens
// }
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
      const className = document.getElementById('className').value.trim();
      const sessionDate = document.getElementById('sessionDate').value;
      const start_time = document.getElementById('startTime').value;
      const classCode = document.getElementById('classCodeSelect').value;
      const groupNumber = document.getElementById('groupNumberSelect').value;
      
      // Validate selections
      if (!classCode) {
        alert('Please select a class');
        return;
      }
      
      if (!groupNumber) {
        alert('Please select a group');
        return;
      }
      
      const durationInput = document.getElementById('duration');
      const duration = durationInput ? parseInt(durationInput.value) : 90;
      const end_time = calculateEndTime(start_time, duration);
      
      // Prepare data for API
      const sessionData = {
        name: className,
        date: sessionDate,
        start_time: start_time + ':00',
        end_time: end_time + ':00',
        active: true,
        created_by: currentUser.id,
        class_code: classCode,
        group_number: groupNumber
      };
      
      console.log('Sending session data:', sessionData);
      
      try {
        const submitButton = e.target.querySelector('button[type="submit"]');
        const originalText = submitButton.textContent;
        submitButton.textContent = 'Creating...';
        submitButton.disabled = true;

        const response = await authService.apiRequest('/sessions', {
          method: 'POST',
          body: JSON.stringify(sessionData)
        });
        
        if (!response.ok) {
          const errorText = await response.text();
          throw new Error(`HTTP error! status: ${response.status}, ${errorText}`);
        }
        
        const createdSession = await response.json();
        console.log('Session created successfully:', createdSession);
        
        alert('Session created successfully! It will auto-close 15 minutes after start time.');
        
        // Reset form
        document.getElementById('createClassForm').reset();
        
        // Reset group dropdown
        const groupSelect = document.getElementById('groupNumberSelect');
        while (groupSelect.firstChild) {
          groupSelect.removeChild(groupSelect.firstChild);
        }
        const defaultOption = document.createElement('option');
        defaultOption.value = '';
        defaultOption.textContent = 'Select a class first';
        groupSelect.appendChild(defaultOption);
        groupSelect.disabled = true;
        
        closeCreateClass();
        await loadActiveSessions();
        
        submitButton.textContent = originalText;
        submitButton.disabled = false;
        
      } catch (error) {
        console.error('Error creating session:', error);
        alert('Error creating session: ' + error.message);
        
        const submitButton = e.target.querySelector('button[type="submit"]');
        submitButton.textContent = 'Create Session';
        submitButton.disabled = false;
      }
    });
  }
  // Listen for class selection changes
  const classSelect = document.getElementById('classCodeSelect');
  if (classSelect) {
    classSelect.addEventListener('change', (e) => {
      const selectedClass = e.target.value;
      loadClassGroups(selectedClass);
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