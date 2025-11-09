console.log("professor.js loaded");

// ===== AUTH CHECK =====
import { displayUserInfo, logout, authService } from './authCheck.js';
import { setActiveSessionId, clearActiveSessionId } from './sessionState.js';

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

const startDateInput = document.querySelector("#startDate");
const endDateInput = document.querySelector("#endDate");


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


// ===== SESSION MANAGEMENT (UNCHANGED) =====

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

/**
 * Validates if a student is enrolled in the given session
 * @param {string} sessionId - The session ID to check
 * @param {string} studentId - The student ID to validate
 * @returns {Promise<boolean>} - True if student is enrolled, false otherwise
 */
async function validateStudentInSession(sessionId, studentId) {
  try {
    console.log(`Validating student ${studentId} in session ${sessionId}`);
    
    const students = await loadSessionStudents(sessionId);
    
    if (!students || students.length === 0) {
      console.warn('No students found in session');
      return false;
    }
    
    // Check if the studentId exists in the student list
    const isEnrolled = students.some(student => student.id === studentId);
    
    console.log(`Student ${studentId} enrollment status:`, isEnrolled);
    return isEnrolled;
    
  } catch (error) {
    console.error('Error validating student enrollment:', error);
    return false;
  }
}

// Create status badge element using CSS classes
function createStatusBadge(status) {
  // If no status (student not in attendance records), return null
  if (!status || status === '' || status === 'null' || status === 'undefined') {
    return null; // Don't show any badge
  }
  
  const badge = document.createElement('span');
  badge.className = 'status-badge';
  
  const icon = document.createElement('span');
  icon.className = 'status-badge-icon';
  
  const text = document.createElement('span');
  
  const statusUpper = status.toUpperCase();
  
  if (statusUpper === 'PRESENT') {
    badge.classList.add('status-present');
    icon.textContent = '✓';
    text.textContent = 'Present';
  } else if (statusUpper === 'ABSENT') {
    badge.classList.add('status-absent');
    icon.textContent = '✗';
    text.textContent = 'Absent';
  } else {
    // For any other status value, don't show badge
    return null;
  }
  
  badge.appendChild(icon);
  badge.appendChild(text);
  
  return badge;
}

// Calculate remaining time until auto-close (15 min after start)
function calculateRemainingTime(sessionDate, sessionStartTime) {
  // Combine date and time
  const sessionStartDateTime = `${sessionDate}T${sessionStartTime}`;
  const startTime = new Date(sessionStartDateTime);
  
  // Get current time
  const now = new Date();
  
  // Session closes 15 minutes after start time
  const closeTime = new Date(startTime.getTime() + 15 * 60 * 1000);
  
  // Calculate difference
  const diffMs = closeTime - now;
  
  // Check if expired
  if (diffMs <= 0 || isNaN(diffMs)) {
    return { 
      expired: true, 
      totalSeconds: 0, 
      minutes: 0, 
      seconds: 0 
    };
  }
  
  // Convert to minutes and seconds
  const totalSeconds = Math.floor(diffMs / 1000);
  const minutes = Math.floor(totalSeconds / 60);
  const seconds = totalSeconds % 60;
  
  return {
    expired: false,
    totalSeconds: totalSeconds,
    minutes: minutes,
    seconds: seconds
  };
}

// Update the timer display in the UI
function updateTimerDisplay(minutes, seconds, expired) {
  const timerElement = document.getElementById('sessionTimer');
  if (!timerElement) {
    return;
  }
  
  // Clear existing content
  while (timerElement.firstChild) {
    timerElement.removeChild(timerElement.firstChild);
  }
  
  const span = document.createElement('span');
  span.className = 'timer-text';
  
  if (expired) {
    // Session has expired
    span.classList.add('timer-urgent');
    span.textContent = '⏱️ Session Auto-Closed';
  } else {
    // Show countdown
    const display = `${minutes}:${seconds.toString().padStart(2, '0')}`;
    
    // Choose color and emoji based on time remaining
    let emoji = '⏱️';
    if (minutes < 3) {
      span.classList.add('timer-urgent');
      emoji = '🔴';
    } else if (minutes < 5) {
      span.classList.add('timer-warning');
      emoji = '🟡';
    } else {
      span.classList.add('timer-normal');
    }
    
    span.textContent = `${emoji} Auto-closes in: ${display}`;
  }
  
  timerElement.appendChild(span);
}

// Start the countdown timer
function startSessionCountdown(sessionDate, sessionStartTime, sessionId) {
  // Clear any existing timer
  if (window.sessionCountdownTimer) {
    clearInterval(window.sessionCountdownTimer);
  }
  
  // Update immediately
  updateCountdown();
  
  // Then update every second
  window.sessionCountdownTimer = setInterval(updateCountdown, 1000);
  
  function updateCountdown() {
    // Calculate time remaining
    const timeInfo = calculateRemainingTime(sessionDate, sessionStartTime);
    
    // Update the display
    updateTimerDisplay(timeInfo.minutes, timeInfo.seconds, timeInfo.expired);
    
    // If expired, stop the timer and reload
    if (timeInfo.expired) {
      console.log('Countdown finished!');
      clearInterval(window.sessionCountdownTimer);
      
      // Show expired message instead of reloading
      showSessionExpiredMessage();
    }
  }
}

function showSessionExpiredMessage() {
  const activeSessions = document.getElementById('activeSessions');
  
  // Clear content
  while (activeSessions.firstChild) {
    activeSessions.removeChild(activeSessions.firstChild);
  }
  
  // Create message card
  const messageCard = document.createElement('div');
  messageCard.style.background = 'white';
  messageCard.style.padding = '3rem 2rem';
  messageCard.style.borderRadius = '12px';
  messageCard.style.textAlign = 'center';
  
  const icon = document.createElement('div');
  icon.style.fontSize = '4rem';
  icon.style.marginBottom = '1rem';
  icon.textContent = '⏱️';
  
  const title = document.createElement('h3');
  title.style.fontSize = '1.5rem';
  title.style.fontWeight = '700';
  title.style.color = '#1e293b';
  title.style.marginBottom = '0.75rem';
  title.textContent = 'Session Automatically Closed';
  
  const message = document.createElement('p');
  message.style.color = '#64748b';
  message.style.marginBottom = '1.5rem';
  message.textContent = 'This session closed 15 minutes after its start time. All unmarked students have been marked as absent.';
  
  const refreshBtn = document.createElement('button');
  refreshBtn.className = 'btn btn-primary';
  refreshBtn.textContent = '🔄 Refresh Dashboard';
  refreshBtn.onclick = () => location.reload();
  
  messageCard.appendChild(icon);
  messageCard.appendChild(title);
  messageCard.appendChild(message);
  messageCard.appendChild(refreshBtn);
  
  activeSessions.appendChild(messageCard);
}

async function displayActiveSession(session) {
  console.log('Displaying session:', session);

  // NEW: Check if 15+ minutes have passed
  if (session.date && session.start_time) {
    const sessionStartDateTime = `${session.date}T${session.start_time}`;
    const startTime = new Date(sessionStartDateTime);
    const now = new Date();
    const minutesSinceStart = Math.floor((now - startTime) / (1000 * 60));
    
    // If expired, don't display it
    if (minutesSinceStart >= 15) {
      console.log('Session expired, not displaying');
      displayNoActiveSession();
      return; // STOP HERE
    }
  }
  
  // Store the active session ID globally
  setActiveSessionId(session.id);
  
  const activeSessions = document.getElementById('activeSessions');
  
  // Clear any existing content
  while (activeSessions.firstChild) {
    activeSessions.removeChild(activeSessions.firstChild);
  }
  
  // Create main card container
  const card = document.createElement('div');
  card.className = 'stat-card';
  
  // === Header Section ===
  const header = document.createElement('div');
  header.className = 'session-header';
  
  // Class and group info
  if (session.class_code || session.classCode) {
    const classInfo = document.createElement('p');
    classInfo.className = 'session-class-info';
    const classCode = session.class_code || session.classCode;
    const groupNum = session.group_number || session.groupNumber;
    classInfo.textContent = `Class: ${classCode}${groupNum ? ` | Group: ${groupNum}` : ''}`;
    header.appendChild(classInfo);
  }
  
  // Session name
  const h4 = document.createElement('h4');
  h4.className = 'session-name';
  h4.textContent = session.name;
  header.appendChild(h4);
  
  // Session details (date and time)
  const detailsDiv = document.createElement('div');
  detailsDiv.className = 'session-details';
  
  const dateSpan = document.createElement('span');
  dateSpan.textContent = `📅 ${session.date || 'No date'}`;
  detailsDiv.appendChild(dateSpan);
  
  const timeSpan = document.createElement('span');
  const startTime = session.startTime || session.start_time || 'No time';
  const endTime = session.endTime || session.end_time || '';
  timeSpan.textContent = `🕐 Start: ${startTime}${endTime ? ` | End: ${endTime}` : ''}`;
  detailsDiv.appendChild(timeSpan);
  
  header.appendChild(detailsDiv);
  card.appendChild(header);
  // Create the actions row container
  const actionsRow = document.createElement('div');
  actionsRow.className = 'session-actions-row';

  // === Countdown Timer ===
  const timerContainer = document.createElement('div');
  timerContainer.id = 'sessionTimer';
  timerContainer.className = 'session-timer';
  
  const timerText = document.createElement('span');
  timerText.className = 'timer-text timer-normal';
  timerText.textContent = '⏱️ Calculating remaining time...';
  timerContainer.appendChild(timerText);

  actionsRow.appendChild(timerContainer);
  
  
  // === Action Buttons ===
  const buttonContainer = document.createElement('div');
  buttonContainer.className = 'session-button-container';
  
  // Scan button
  const scanBtn = document.createElement('button');
  scanBtn.className = 'btn btn-primary';
  scanBtn.textContent = '📸 Scan Face for Attendance';
  scanBtn.onclick = openFaceScanning;
  buttonContainer.appendChild(scanBtn);
  
  // Close button
  const closeBtn = document.createElement('button');
  closeBtn.className = 'btn btn-danger';
  closeBtn.textContent = '✕ Close Session';
  closeBtn.onclick = () => closeActiveSession(session.id);
  buttonContainer.appendChild(closeBtn);
  
  actionsRow.appendChild(buttonContainer);
  card.appendChild(actionsRow);
  activeSessions.appendChild(card);
  
  // Start countdown timer
  const sessionDate = session.date || new Date().toISOString().split('T')[0];
  const sessionStartTime = session.start_time || session.startTime || '00:00:00';
  startSessionCountdown(sessionDate, sessionStartTime, session.id);
  
  // === Load and display students ===
  const students = await loadSessionStudents(session.id);
  
  if (students.length > 0) {
    // Student list header
    // Create header container
const studentHeaderContainer = document.createElement('div');
studentHeaderContainer.className = 'student-attendance-header';

// Title
const titleDiv = document.createElement('div');
titleDiv.className = 'attendance-title';
titleDiv.textContent = 'Student Attendance';
studentHeaderContainer.appendChild(titleDiv);

// Stats row
const statsRow = document.createElement('div');
statsRow.className = 'attendance-stats-row';

// Calculate stats
const totalStudents = students.length;
const presentCount = students.filter(s => s.status && s.status.toUpperCase() === 'PRESENT').length;
const absentCount = students.filter(s => s.status && s.status.toUpperCase() === 'ABSENT').length;

// Total Students stat
const totalStatCard = document.createElement('div');
totalStatCard.className = 'attendance-stat-card';

const totalLabel = document.createElement('div');
totalLabel.className = 'stat-label';
totalLabel.textContent = 'Total Students';
totalStatCard.appendChild(totalLabel);

const totalValue = document.createElement('div');
totalValue.className = 'stat-value';
totalValue.textContent = totalStudents;
totalStatCard.appendChild(totalValue);

statsRow.appendChild(totalStatCard);

// Present Students stat
const presentStatCard = document.createElement('div');
presentStatCard.className = 'attendance-stat-card stat-present-card';

const presentLabel = document.createElement('div');
presentLabel.className = 'stat-label';
presentLabel.textContent = 'Present';
presentStatCard.appendChild(presentLabel);

const presentValue = document.createElement('div');
presentValue.className = 'stat-value stat-present-value';
presentValue.textContent = presentCount;
presentStatCard.appendChild(presentValue);

statsRow.appendChild(presentStatCard);

// Absent Students stat
const absentStatCard = document.createElement('div');
absentStatCard.className = 'attendance-stat-card stat-absent-card';

const absentLabel = document.createElement('div');
absentLabel.className = 'stat-label';
absentLabel.textContent = 'Absent';
absentStatCard.appendChild(absentLabel);

const absentValue = document.createElement('div');
absentValue.className = 'stat-value stat-absent-value';
absentValue.textContent = absentCount;
absentStatCard.appendChild(absentValue);

statsRow.appendChild(absentStatCard);

// Add stats row to header
studentHeaderContainer.appendChild(statsRow);

// Add header to page
activeSessions.appendChild(studentHeaderContainer);
    
    // Student list container
    const studentListContainer = document.createElement('div');
    studentListContainer.className = 'student-list';
    
    students.forEach(student => {
      const studentItem = document.createElement('div');
      studentItem.className = 'student-item';
      
      // Left side: Student info
      const studentInfo = document.createElement('div');
      studentInfo.className = 'student-info';
      
      const nameDiv = document.createElement('div');
      nameDiv.className = 'student-name';
      nameDiv.textContent = student.name;
      studentInfo.appendChild(nameDiv);
      
      const emailDiv = document.createElement('div');
      emailDiv.className = 'student-email';
      emailDiv.textContent = student.email;
      studentInfo.appendChild(emailDiv);
      
      studentItem.appendChild(studentInfo);
      
      // Right side: Status badge and action buttons
      const actionsContainer = document.createElement('div');
      actionsContainer.className = 'student-actions';
      
      // Status badge (using CSS classes)
      const statusBadge = createStatusBadge(student.status);
      if (statusBadge) {
        actionsContainer.appendChild(statusBadge);
      }
      
      // Action buttons container
      const controls = document.createElement('div');
      controls.className = 'student-controls';
      
      const presentBtn = document.createElement('button');
      presentBtn.className = 'btn btn-success btn-sm';
      presentBtn.textContent = 'Mark Present';
      presentBtn.onclick = () => markStudentManually(session.id, student.id, 'present');
      controls.appendChild(presentBtn);
      
      const absentBtn = document.createElement('button');
      absentBtn.className = 'btn btn-danger btn-sm';
      absentBtn.textContent = 'Mark Absent';
      absentBtn.onclick = () => markStudentManually(session.id, student.id, 'absent');
      controls.appendChild(absentBtn);
      
      actionsContainer.appendChild(controls);
      studentItem.appendChild(actionsContainer);
      
      studentListContainer.appendChild(studentItem);
    });
    
    activeSessions.appendChild(studentListContainer);
  } else {
    const noStudents = document.createElement('p');
    noStudents.style.textAlign = 'center';
    noStudents.style.color = '#94a3b8';
    noStudents.style.padding = '1rem';
    noStudents.textContent = 'No students in this group';
    activeSessions.appendChild(noStudents);
  }
}

// ===== DISPLAY NO SESSION MESSAGE =====
function displayNoActiveSession() {
  // Clear the active session ID
  clearActiveSessionId();
  
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
      const errorText = await response.text();
      throw new Error(`HTTP error! status: ${response.status}, ${errorText}`);
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
  loadAttendanceSummary();
}

// Helper function to calculate end time
function calculateEndTime(startTime, durationMinutes) {
  const [hours, minutes] = startTime.split(':').map(Number);
  // ✅ Create a Date object in Singapore timezone
  const now = new Date();
  const singaporeOffset = 8 * 60; // Singapore is UTC+8
  const utc = now.getTime() + now.getTimezoneOffset() * 60000;
  const startDate = new Date(utc + singaporeOffset * 60000);

  startDate.setHours(hours, minutes, 0);
  startDate.setMinutes(startDate.getMinutes() + durationMinutes);

  const endHours = String(startDate.getHours()).padStart(2, '0');
  const endMinutes = String(startDate.getMinutes()).padStart(2, '0');
  
  return `${endHours}:${endMinutes}`;
}

// ===== ATTENDANCE FUNCTIONS (UNCHANGED) =====


// ===== SHARED ATTENDANCE LOGIC =====
async function saveAttendanceRecord(sessionId, studentId, status, method = 'MANUAL') {
  try {
    const upperStatus = status.toUpperCase();
    const upperMethod = method.toUpperCase();
    
    // CRITICAL: Ensure we get the full ISO string with Z
    const now = new Date();
    const singaporeTime = now.toLocaleString('sv-SE', { timeZone: 'Asia/Singapore' }).replace(' ', 'T');
    const markedAt = singaporeTime; // Should be "2025-11-04T18:28:49.359Z"
    
    console.log('Timestamp being sent:', markedAt); // DEBUG: Check what's actually being sent
    
    // Check if record exists
    const checkResponse = await fetch(
      `${API_BASE_URL}/attendance_records?session_id=${sessionId}&student_id=${studentId}`
    );
    
    let response;
    
    if (checkResponse.ok) {
      const existingRecords = await checkResponse.json();
      
      if (existingRecords && existingRecords.length > 0) {
        // Update existing record
        const recordId = existingRecords[0].id;
        
        const updateBody = {
          status: upperStatus,
          method: upperMethod,
          marked_at: markedAt
        };
        
        console.log('Update body:', JSON.stringify(updateBody)); // Check the full payload
        
        response = await fetch(`${API_BASE_URL}/attendance_records/${recordId}`, {
          method: 'PATCH',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(updateBody)
        });
      } else {
        // Create new record
        const createBody = {
          session_id: sessionId,
          student_id: studentId,
          status: upperStatus,
          method: upperMethod,
          marked_at: markedAt
        };
        
        console.log('Create body:', JSON.stringify(createBody)); // Check the full payload
        
        response = await fetch(`${API_BASE_URL}/attendance_records`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(createBody)
        });
      }
    } else {
      // If check fails, try to create new record anyway
      console.warn('Failed to check existing records, creating new one');
      
      const createBody = {
        session_id: sessionId,
        student_id: studentId,
        status: upperStatus,
        method: upperMethod,
        marked_at: markedAt
      };
      
      console.log('Create body:', JSON.stringify(createBody));
      
      response = await fetch(`${API_BASE_URL}/attendance_records`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(createBody)
      });
    }
    
    if (!response || !response.ok) {
      const errorText = await response.text();
      console.error('Server error:', errorText);
      throw new Error('Failed to save attendance: ' + errorText);
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
    
    // Pass 'MANUAL' in uppercase
    await saveAttendanceRecord(sessionId, studentId, status, 'MANUAL');
    
    console.log('Attendance marked successfully');
    
    // Reload to update UI
    await loadActiveSessions();
    
  } catch (error) {
    alert('Failed to mark attendance: ' + error.message);
  }
}

function updateStudentStats() {
  const totalClasses = attendanceRecords.length;
  const presentClasses = attendanceRecords.filter((record) => record.status === "Present").length;
  const attendanceRate = totalClasses > 0 ? Math.round((presentClasses / totalClasses) * 100) : 0;

  const statNumbers = document.querySelectorAll(".stat-number");
  if (statNumbers.length >= 3) {
    statNumbers[0].textContent = `${attendanceRate}%`;
    statNumbers[1].textContent = totalClasses;

    // const today = new Date().toISOString().split("T")[0];
    const today = new Date().toLocaleDateString('en-CA', { 
      timeZone: 'Asia/Singapore' 
    });
    const classesToday = attendanceRecords.filter((record) => record.date === today).length;
    statNumbers[2].textContent = classesToday;
  }
}


// ===== MODAL FUNCTIONS (UPDATED openCreateClass) =====

/**
 * Opens the create class modal and sets up the class name datalist.
 */
function openCreateClass() {
  resetDynamicSection();
  // Populate the datalist with the fetched classes before showing the modal
  // setupClassNameDatalist();
  loadProfessorClasses();
  document.getElementById("createClassModal").classList.add("active");

  // Ensure group dropdown starts disabled
  const groupSelect = document.getElementById('groupNumberSelect');
  if (groupSelect) {
    groupSelect.disabled = true;
    
    // Clear and reset group dropdown to default state
    while (groupSelect.firstChild) {
      groupSelect.removeChild(groupSelect.firstChild);
    }
    const defaultOption = document.createElement('option');
    defaultOption.value = '';
    defaultOption.textContent = 'Select a class first';
    groupSelect.appendChild(defaultOption);
  }
  
  // Ensure roster section is hidden
  document.getElementById('studentRosterSection').style.display = 'none';
  rosterManagementEnabled = false;
  
  // Reset toggle button
  const toggleButton = document.getElementById('toggleRosterButton');
  if (toggleButton) {
    toggleButton.textContent = '👥 Manage Roster';
    toggleButton.classList.remove('btn-danger');
    toggleButton.classList.add('btn-secondary');
  }
}

function closeCreateClass() {
  document.getElementById("createClassModal").classList.remove("active");
  document.getElementById("createClassForm").reset();

  //Roster reset
  document.getElementById('studentRosterSection').style.display = 'none';
  rosterManagementEnabled = false;
  currentGroupStudents = [];
  currentSelectedClass = null;
  currentSelectedGroup = null;

  // Reset the toggle button
  const toggleButton = document.getElementById('toggleRosterButton');
  if (toggleButton) {
    toggleButton.textContent = '👥 Manage Roster';
    toggleButton.classList.remove('btn-danger');
    toggleButton.classList.add('btn-secondary');
  }

  //roster display
  const container = document.getElementById('currentStudentsList');
  if (container) {
    container.innerHTML = '';
  }

  //clear search input
  const searchInput = document.getElementById('studentSearchInput');
  if (searchInput) {
    searchInput.value = '';
  }
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

document.querySelectorAll('.section-content button').forEach(button => {
  button.addEventListener('click', function() {
    document.querySelectorAll('.section-content button').forEach(btn => {
      btn.classList.remove('btn-primary');
      btn.classList.add('btn-secondary');
    });
    this.classList.remove('btn-secondary');
    this.classList.add('btn-primary');
  });
});


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

// ===== STUDENT ROSTER MANAGEMENT =====

// Store all students and current group's student list
let allStudents = [];
let currentGroupStudents = [];
let currentSelectedClass = null;
let currentSelectedGroup = null;
let rosterManagementEnabled = false;

// Fetch all students from the database
async function fetchAllStudents() {
  try {
    const response = await authService.apiRequest('/students');
    if (!response.ok) {
      throw new Error('Failed to fetch students');
    }
    allStudents = await response.json();
    console.log('All students loaded:', allStudents);
    return allStudents;
  } catch (error) {
    console.error('Error fetching students:', error);
    return [];
  }
}

// Load roster when a group is selected
async function loadGroupRoster(classCode, groupNumber) {
  if (!classCode || !groupNumber) {
    document.getElementById('studentRosterSection').style.display = 'none';
    return;
  }

  currentSelectedClass = classCode;
  currentSelectedGroup = groupNumber;

  // Show the roster section
  // document.getElementById('studentRosterSection').style.display = 'block';

  // Find the selected group in our data
  const selectedClass = professorClassesData.find(c => c.class_code === classCode);
  if (!selectedClass) return;

  const selectedGroup = selectedClass.groups.find(g => g.group_number === groupNumber);
  if (!selectedGroup) return;

  // Get student IDs in this group
  const groupStudentIds = selectedGroup.student_list || [];
  console.log('Group student IDs:', groupStudentIds);

  // Fetch all students if not already loaded
  if (allStudents.length === 0) {
    await fetchAllStudents();
  }

  // Filter students who are in this group
  currentGroupStudents = allStudents.filter(student => 
    groupStudentIds.includes(student.id) || 
    groupStudentIds.includes(student.email) ||
    groupStudentIds.includes(student.code)
  );

  console.log('Current group students:', currentGroupStudents);

  // Only display if roster management is enabled
  if (rosterManagementEnabled) {
    displayCurrentStudents();
    loadAvailableStudents();
  }
}

// Display current students in the group
function displayCurrentStudents() {
  const container = document.getElementById('currentStudentsList');
  container.innerHTML = '';

  if (currentGroupStudents.length === 0) {
    container.innerHTML = `
      <p style="text-align: center; color: #94a3b8; padding: 1rem;">
        No students in this group yet
      </p>
    `;
    return;
  }

  currentGroupStudents.forEach(student => {
    const studentDiv = document.createElement('div');

    studentDiv.setAttribute('data-student-id', student.id);
    studentDiv.setAttribute('data-student-name', student.name);
    studentDiv.setAttribute('data-student-email', student.email);
    studentDiv.setAttribute('data-student-code', student.code || student.id);

    studentDiv.style.cssText = `
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 0.75rem;
      border-bottom: 1px solid #e2e8f0;
      background: white;
    `;
    studentDiv.innerHTML = `
      <div style="flex: 1;">
        <div style="font-weight: 500; color: #1e293b;">${student.name}</div>
        <div style="font-size: 0.875rem; color: #64748b;">${student.email}</div>
        <div style="font-size: 0.75rem; color: #94a3b8;">ID: ${student.code || student.id}</div>
      </div>
      <button 
        type="button"
        class="btn btn-danger btn-sm" 
        onclick="removeStudentFromGroup('${student.id}')"
        style="padding: 0.25rem 0.75rem; font-size: 0.875rem;">
        Remove
      </button>
    `;
    container.appendChild(studentDiv);
  });
}

// Load students not in the current group
function loadAvailableStudents() {
  const select = document.getElementById('availableStudentsSelect');
  select.innerHTML = '<option value="">Select a student to add...</option>';

  // Filter out students already in the group
  const currentGroupStudentIds = currentGroupStudents.map(s => s.id);
  const availableStudents = allStudents.filter(student => 
    !currentGroupStudentIds.includes(student.id)
  );

  availableStudents.forEach(student => {
    const option = document.createElement('option');
    option.value = student.id;
    option.textContent = `${student.name} (${student.email})`;
    select.appendChild(option);
  });

  if (availableStudents.length === 0) {
    select.innerHTML = '<option value="">All students are in this group</option>';
    select.disabled = true;
  } else {
    select.disabled = false;
  }
}

// Add student to group
async function addStudentToGroup() {
  const select = document.getElementById('availableStudentsSelect');
  const studentId = select.value;

  if (!studentId) {
    alert('Please select a student to add');
    return;
  }

  if (!currentSelectedClass || !currentSelectedGroup) {
    alert('Please select a class and group first');
    return;
  }

  try {
    // Find the student
    const student = allStudents.find(s => s.id === studentId);
    if (!student) {
      throw new Error('Student not found');
    }

    // Get current group data
    const selectedClass = professorClassesData.find(c => c.class_code === currentSelectedClass);
    const selectedGroup = selectedClass.groups.find(g => g.group_number === currentSelectedGroup);

    // Add student ID to the group's student list
    const updatedStudentList = [...(selectedGroup.student_list || []), studentId];

    // Update group via API
    const response = await authService.apiRequest(`/groups/${currentSelectedClass}/${currentSelectedGroup}`, {
      method: 'PATCH',
      body: JSON.stringify({
        student_list: updatedStudentList
      })
    });

    if (!response.ok) {
      throw new Error('Failed to add student to group');
    }

    // Update local data
    selectedGroup.student_list = updatedStudentList;
    currentGroupStudents.push(student);

    // Refresh displays
    displayCurrentStudents();
    loadAvailableStudents();

    alert(`${student.name} added to the group successfully!`);
  } catch (error) {
    console.error('Error adding student:', error);
    alert('Failed to add student: ' + error.message);
  }
}

// Remove student from group
async function removeStudentFromGroup(studentId) {
  if (!confirm('Are you sure you want to remove this student from the group?')) {
    return;
  }

  try {
    // Get current group data
    const selectedClass = professorClassesData.find(c => c.class_code === currentSelectedClass);
    const selectedGroup = selectedClass.groups.find(g => g.group_number === currentSelectedGroup);

    // Remove student ID from the group's student list
    const updatedStudentList = selectedGroup.student_list.filter(id => id !== studentId);

    // Update group via API
    const response = await authService.apiRequest(`/groups/${currentSelectedClass}/${currentSelectedGroup}`, {
      method: 'PATCH',
      body: JSON.stringify({
        student_list: updatedStudentList
      })
    });

    if (!response.ok) {
      throw new Error('Failed to remove student from group');
    }

    // Update local data
    selectedGroup.student_list = updatedStudentList;
    currentGroupStudents = currentGroupStudents.filter(s => s.id !== studentId);

    // Refresh displays
    displayCurrentStudents();
    loadAvailableStudents();

    alert('Student removed from group successfully!');
  } catch (error) {
    console.error('Error removing student:', error);
    alert('Failed to remove student: ' + error.message);
  }
}

function toggleRosterManagement() {
  const selectedClass = document.getElementById('classCodeSelect').value;
  const selectedGroup = document.getElementById('groupNumberSelect').value;
  
  if (!selectedClass || !selectedGroup) {
    alert('Please select a class and group first');
    return;
  }
  
  rosterManagementEnabled = !rosterManagementEnabled;
  
  const rosterSection = document.getElementById('studentRosterSection');
  const toggleButton = document.getElementById('toggleRosterButton');
  
  if (rosterManagementEnabled) {
    // Show roster and load it
    rosterSection.style.display = 'block';
    toggleButton.textContent = '✕ Close Roster';
    toggleButton.classList.remove('btn-secondary');
    toggleButton.classList.add('btn-danger');
    loadGroupRoster(selectedClass, selectedGroup);
  } else {
    // Hide roster
    rosterSection.style.display = 'none';
    toggleButton.textContent = '👥 Manage Roster';
    toggleButton.classList.remove('btn-danger');
    toggleButton.classList.add('btn-secondary');
  }
}

function searchStudents() {
  const searchInput = document.getElementById('studentSearchInput');
  const searchTerm = searchInput.value.toLowerCase().trim();
  
  const container = document.getElementById('currentStudentsList');
  container.innerHTML = ''; // Clear the list
  
  // Filter through currentGroupStudents array
  const filteredStudents = currentGroupStudents.filter(student => {
    const name = student.name.toLowerCase();
    const email = student.email.toLowerCase();
    const id = (student.code || student.id).toLowerCase();
    
    return name.includes(searchTerm) || 
           email.includes(searchTerm) || 
           id.includes(searchTerm);
  });
  
  // Show message if no results
  if (filteredStudents.length === 0) {
    container.innerHTML = `
      <p style="text-align: center; color: #94a3b8; padding: 1rem;">
        ${searchTerm ? 'No students found matching "' + searchTerm + '"' : 'No students in this group'}
      </p>
    `;
    return;
  }
  
  // Display filtered students
  filteredStudents.forEach(student => {
    const studentDiv = document.createElement('div');
    studentDiv.style.cssText = `
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 0.75rem;
      border-bottom: 1px solid #e2e8f0;
      background: white;
    `;
    studentDiv.innerHTML = `
      <div style="flex: 1;">
        <div style="font-weight: 500; color: #1e293b;">${student.name}</div>
        <div style="font-size: 0.875rem; color: #64748b;">${student.email}</div>
        <div style="font-size: 0.75rem; color: #94a3b8;">ID: ${student.code || student.id}</div>
      </div>
      <button 
        type="button"
        class="btn btn-danger btn-sm" 
        onclick="removeStudentFromGroup('${student.id}')"
        style="padding: 0.25rem 0.75rem; font-size: 0.875rem;">
        Remove
      </button>
    `;
    container.appendChild(studentDiv);
  });
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
      
      // Hide roster when class changes
      document.getElementById('studentRosterSection').style.display = 'none';
      rosterManagementEnabled = false;
      
      // Reset toggle button
      const toggleButton = document.getElementById('toggleRosterButton');
      if (toggleButton) {
        toggleButton.textContent = '👥 Manage Roster';
        toggleButton.classList.remove('btn-danger');
        toggleButton.classList.add('btn-secondary');
      }
    });
  }

  // Listen for group selection changes
  const groupSelect = document.getElementById('groupNumberSelect');
  if (groupSelect) {
    groupSelect.addEventListener('change', (e) => {
      const selectedGroup = e.target.value;
      const selectedClass = document.getElementById('classCodeSelect').value;
      
      // Hide roster when group changes
      document.getElementById('studentRosterSection').style.display = 'none';
      rosterManagementEnabled = false;
      
      // Reset toggle button if it exists
      const toggleButton = document.getElementById('toggleRosterButton');
      if (toggleButton) {
        toggleButton.textContent = '👥 Manage Roster';
        toggleButton.classList.remove('btn-danger');
        toggleButton.classList.add('btn-secondary');
      }
      
      // Pre-load the data (but don't show it)
      if (selectedClass && selectedGroup) {
        loadGroupRoster(selectedClass, selectedGroup);
      }
    });
  }
});

async function exportCSV() {
    await exportReport("csv");
}

function exportPDF() {
    const classSelect = document.querySelector("#classSelect");
    const selectedClass = classSelect ? classSelect.value : "";
    const startDate = startDateInput  ? startDateInput.value : "";
    const endDate = endDateInput  ? endDateInput.value : "";

    let url = `${API_BASE_URL}/reports/generate?format=pdf`;
    if (selectedClass) url += `&className=${encodeURIComponent(selectedClass)}`;
    if (startDate) url += `&startDate=${encodeURIComponent(startDate)}`;
    if (endDate) url += `&endDate=${encodeURIComponent(endDate)}`;

    window.open(url, "_blank");
}

async function exportReport(format) {
    const classSelect = document.querySelector("#classSelect");
    const selectedClass = classSelect ? classSelect.value : "";

    const startDate = startDateInput  ? startDateInput .value : "";
    const endDate = endDateInput  ? endDateInput .value : "";

    let url = `/reports/generate?format=${format}`;
    if (selectedClass) url += `&className=${encodeURIComponent(selectedClass)}`;
    if (startDate) url += `&startDate=${encodeURIComponent(startDate)}`;
    if (endDate) url += `&endDate=${encodeURIComponent(endDate)}`;

    try {
        const response = await authService.apiRequest(url, { method: "POST" });

        if (!response.ok) {
            alert("Failed to generate report.");
            return;
        }

        const blob = await response.blob();
        const downloadUrl = window.URL.createObjectURL(blob);
        const a = document.createElement("a");
        a.href = downloadUrl;
        a.download = `attendance_report.${format}`;
        document.body.appendChild(a);
        a.click();
        a.remove();
        window.URL.revokeObjectURL(downloadUrl);
    } catch (error) {
        console.error("Error generating report:", error);
        alert("Error generating report");
    }
}

async function loadAttendanceSummary() {
    const classSelect = document.querySelector("#classSelect");
    const selectedClass = classSelect ? classSelect.value : "";
    const tbody = document.querySelector("#attendanceSummaryBody");

    tbody.innerHTML = `<tr><td colspan="4" style="text-align:center; color:RoyalBlue">Loading data...</td></tr>`;

    try {
        const response = await authService.apiRequest(
            `/reports/summary${selectedClass ? `?className=${encodeURIComponent(selectedClass)}` : ""}`,
            { method: "GET" }
        );

        if (!response.ok) {
            alert("Failed to fetch attendance summary.");
            return;
        }

        const summaryList = await response.json();
        const tbody = document.querySelector("#attendanceSummaryBody");
        tbody.innerHTML = "";

        if (summaryList.length === 0) {
            tbody.innerHTML = `<tr><td colspan="4" style="text-align:center; color:red">No data found for this class</td></tr>`;
            return;
        }

        summaryList.forEach(entry => {
            const tr = document.createElement("tr");
            const attendanceRate = entry.attendanceRate || 0;
            const statusClass = attendanceRate >= 75 ? "status-present" : "status-absent";

            tr.innerHTML = `
                <td>${entry.name}</td>
                <td>${entry.totalClasses}</td>
                <td>${entry.present}</td>
                <td><span class="status-badge ${statusClass}">${attendanceRate}%</span></td>
            `;
            tbody.appendChild(tr);
        });

    } catch (err) {
        console.error("Error loading attendance summary:", err);
    }
}

function resetDynamicSection() {
  const section = document.getElementById("dynamicSection");
  section.innerHTML = `
    <div class="section-header">
      <h3 class="section-title">Active Session</h3>
    </div>
    <div class="section-content" id="activeSessions">
      <p class="welcome-subtitle">No active session at the moment</p>
    </div>
  `;
}

function openAnalytics() {
  const section = document.getElementById("dynamicSection");

  section.innerHTML = `
    <div class="analytics-wrapper">
      <div class="analytics-header">
        <h3 class="analytics-title">Attendance Analytics</h3>
        <p class="analytics-subtitle">Overview of attendance ratio for the selected class</p>
      </div>

      <div class="analytics-controls">
        <label for="analyticsClassSelect" class="form-label">Select Class</label>
        <select id="analyticsClassSelect" class="form-select">
          <option value="">All Classes</option>
          <option value="CS102" selected>CS102 - Programming Fundamentals II</option>
          <option value="IS116">IS116 - Web Application Development</option>
          <option value="COR3001">COR3001 - Big Questions</option>
        </select>
      </div>

      <div id="analyticsStatus" class="analytics-status">
        Select a class to load analytics...
      </div>

      <div class="analytics-chart-wrapper">
        <h4 class="chart-title">Overall Attendance Ratio</h4>
        <canvas id="attendancePieChart" height="450"></canvas>
      </div>
    </div>
  `;

  initAnalyticsCharts();
}


function initAnalyticsCharts() {
  let pieChartInstance = null;
  const select = document.getElementById("analyticsClassSelect");
  const status = document.getElementById("analyticsStatus");

  async function loadAnalytics(className) {
    status.textContent = "Loading analytics...";
    
    if (pieChartInstance) pieChartInstance.destroy();

    try {
      const url = `${API_BASE_URL}/reports/summary${className ? `?className=${encodeURIComponent(className)}` : ""}`;
      const res = await fetch(url);
      if (!res.ok) throw new Error("Failed to fetch data");
      const summary = await res.json();

      let totalPresent = 0;
      let totalClasses = 0;
      summary.forEach(student => {
        totalPresent += student.present;
        totalClasses += student.totalClasses;
      });
      const totalAbsent = totalClasses - totalPresent;

      const chartWrapper = document.querySelector(".analytics-chart-wrapper");
      if (totalPresent + totalAbsent === 0) {
        chartWrapper.style.display = "none";
        status.textContent = "No attendance data available for this class.";
        return;
      } else {
        chartWrapper.style.display = "block";
        status.textContent = "";
    }

      // Render pie chart
      const ctx = document.getElementById("attendancePieChart").getContext("2d");
      const canvas = document.getElementById("attendancePieChart");
      canvas.width = 500;
      canvas.height = 450;
      pieChartInstance = new Chart(ctx, {
        type: "pie",
        data: {
          labels: ["Present", "Absent"],
          datasets: [{
            data: [totalPresent, totalAbsent],
            backgroundColor: ["#11d7d8", "#fd9ab6"],
            borderColor: "#fff",
            borderWidth: 2,
            hoverOffset: 20,
          }],
        },
        options: {
          responsive: false,
          plugins: {
            legend: {
              position: "bottom",
              labels: {
                padding: 20,
                font: { size: 14, weight: "600" },
                boxWidth: 20,
              },
            },
            tooltip: {
              callbacks: {
                label: function(context) {
                  const label = context.label || '';
                  const value = context.raw || 0;
                  const total = context.chart._metasets[context.datasetIndex].total;
                  const percentage = ((value / total) * 100).toFixed(1);
                  return `${label}: ${value} (${percentage}%)`;
                }
              }
            },
            title: { display: false },
          }
        }
      });
    } catch (error) {
      console.error(error);
      status.textContent = "Failed to load analytics.";
    }
  }

  select.addEventListener("change", () => loadAnalytics(select.value));
  loadAnalytics(select.value);
}

document.querySelector("#classSelect").addEventListener("change", loadAttendanceSummary);

startDateInput.addEventListener("change", () => {
    if (startDateInput.value) {
        endDateInput.min = startDateInput.value;
    } else {
        endDateInput.min = "";
    }
});

endDateInput.addEventListener("change", () => {
    if (endDateInput.value) {
        startDateInput.max = endDateInput.value;
    } else {
        startDateInput.max = "";
    }
});

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
window.openAnalytics = openAnalytics;
window.openManualAttendance = openManualAttendance;
window.openStudentManagement = openStudentManagement;
window.logout = logout;
window.addStudentToGroup = addStudentToGroup;
window.removeStudentFromGroup = removeStudentFromGroup;
window.toggleRosterManagement = toggleRosterManagement;
window.searchStudents = searchStudents;
window.addStudentToGroup = addStudentToGroup;
window.removeStudentFromGroup = removeStudentFromGroup;
window.createStatusBadge = createStatusBadge;
window.calculateRemainingTime = calculateRemainingTime;
window.updateTimerDisplay = updateTimerDisplay;
window.startSessionCountdown = startSessionCountdown;

// Export for use in other modules
export { saveAttendanceRecord, validateStudentInSession, API_BASE_URL };