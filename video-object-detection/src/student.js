import { AutoModel, AutoProcessor, RawImage } from "@huggingface/transformers";
import { Client } from "@gradio/client";
import { displayUserInfo, logout, authService } from './authCheck.js';

// Auth check and user info
const userInfo = displayUserInfo();
console.log('Logged in as:', userInfo.name);

// Update welcome text with actual user name
const welcomeText = document.querySelector('.welcome-text');
if (welcomeText) {
    welcomeText.textContent = `Welcome, ${userInfo.name}`;
}

// Make logout available globally for onclick handler
window.logout = logout;

// Reference the elements that we will need
const status = document.getElementById("status");
const container = document.getElementById("container");
const overlay = document.getElementById("overlay");
const canvas = document.getElementById("canvas");
const video = document.getElementById("video");
const thresholdSlider = document.getElementById("threshold");
const thresholdLabel = document.getElementById("threshold-value");
const sizeSlider = document.getElementById("size");
const sizeLabel = document.getElementById("size-value");
const scaleSlider = document.getElementById("scale");
const scaleLabel = document.getElementById("scale-value");

function setStreamSize(width, height) {
  video.width = canvas.width = Math.round(width);
  video.height = canvas.height = Math.round(height);
}

status.textContent = "Loading model...";

// Load model and processor
const model_id = "Xenova/gelan-c_all";
const model = await AutoModel.from_pretrained(model_id);
const processor = await AutoProcessor.from_pretrained(model_id);

// Set up controls
let scale = 0.5;
scaleSlider.addEventListener("input", () => {
  scale = Number(scaleSlider.value);
  setStreamSize(video.videoWidth * scale, video.videoHeight * scale);
  scaleLabel.textContent = scale;
});
scaleSlider.disabled = false;

let threshold = 0.80;
thresholdSlider.addEventListener("input", () => {
  threshold = Number(thresholdSlider.value);
  thresholdLabel.textContent = threshold.toFixed(2);
});
thresholdSlider.disabled = false;

let size = 96;
processor.feature_extractor.size = { shortest_edge: size };
sizeSlider.addEventListener("input", () => {
  size = Number(sizeSlider.value);
  processor.feature_extractor.size = { shortest_edge: size };
  sizeLabel.textContent = size;
});
sizeSlider.disabled = false;

status.textContent = "Ready";

const COLOURS = [
  "#EF4444", "#4299E1", "#059669", "#FBBF24", "#4B52B1", "#7B3AC2",
  "#ED507A", "#1DD1A1", "#F3873A", "#4B5563", "#DC2626", "#1852B4",
  "#18A35D", "#F59E0B", "#4059BE", "#6027A5", "#D63D60", "#00AC9B",
  "#E64A19", "#272A34",
];

async function sendCanvasImageToAPI(canvas) {
  return new Promise((resolve, reject) => {
    canvas.toBlob(async (blob) => {
      if (!blob) {
        reject("Failed to get Blob from canvas");
        return;
      }

      const file = new File([blob], "detected.png", { type: "image/png" });

      try {
        const formData = new FormData();
        formData.append("image", file);

        const response = await fetch("http://localhost:8080/api/face-recognition/call", {
          method: "POST",
          body: formData,
        });

        if (!response.ok) {
          reject("Failed to send image: " + response.statusText);
          return;
        }

        const result = await response.text();
        resolve(result);
      } catch (err) {
        reject(err);
      }
    }, "image/png");
  });
}

// Variables to store current bounding box and label elements
let currentBoxElement = null;
let currentLabelElement = null;

let hasSent = false;

var color = "yellow";
var text = "Verifying..."

// Render a bounding box and label on the image
function renderBox([xmin, ymin, xmax, ymax, score, id], [w, h]) {
  if (score < threshold) return;

  let boxElement = document.createElement("div");
  boxElement.className = "bounding-box";
  Object.assign(boxElement.style, {
    borderColor: color,
    left: (100 * xmin) / w + "%",
    top: (100 * ymin) / h + "%",
    width: (100 * (xmax - xmin)) / w + "%",
    height: (100 * (ymax - ymin)) / h + "%",
  });

  let labelElement = document.createElement("span");
  labelElement.textContent = text;
  labelElement.className = "bounding-box-label";
  labelElement.style.backgroundColor = color;
  labelElement.style.color = "black";

  boxElement.appendChild(labelElement);
  overlay.appendChild(boxElement);

  currentBoxElement = boxElement;
  currentLabelElement = labelElement;

  if (!hasSent) {
    hasSent = true;
    sendCanvasImageToAPI(canvas)
      .then((response) => {
        const responseObj = JSON.parse(response); 
        const confidenceStr = responseObj.result;
        const decimalNumber = parseFloat(confidenceStr);
        console.log(decimalNumber);

        if (decimalNumber !== null && decimalNumber > 0.80) {
          color = "green";
          text = "Verified Successfully! " + decimalNumber;
          currentLabelElement.style.color = "black";
          console.log("Updated box to green");
        } else if (decimalNumber !== null) {
          color = "red";
          text = "Failed to verify"
          currentLabelElement.style.color = "black";
          console.log("Updated box to red");
        } else {
          currentLabelElement.textContent = "Verifying...";
          currentLabelElement.style.backgroundColor = "yellow";
          currentLabelElement.style.color = "black";
          currentBoxElement.style.borderColor = "yellow";
          console.log("Fallback to yellow");
        }
      })
      .catch((err) => {
        console.error("Error sending image to API:", err);
        if (currentLabelElement && currentBoxElement) {
          currentLabelElement.textContent = "Verifying...";
          currentLabelElement.style.backgroundColor = "yellow";
          currentLabelElement.style.color = "black";
          currentBoxElement.style.borderColor = "yellow";
        }
      });
  }
}

let isProcessing = false;
let previousTime;
const context = canvas.getContext("2d", { willReadFrequently: true });

function updateCanvas() {
  const { width, height } = canvas;
  context.drawImage(video, 0, 0, width, height);

  if (!isProcessing) {
    isProcessing = true;
    (async function () {
      const pixelData = context.getImageData(0, 0, width, height).data;
      const image = new RawImage(pixelData, width, height, 4);

      const inputs = await processor(image);
      const { outputs } = await model(inputs);

      overlay.innerHTML = "";

      const sizes = inputs.reshaped_input_sizes[0].reverse();
      outputs.tolist().forEach((x) => renderBox(x, sizes));

      if (previousTime !== undefined) {
        const fps = 1000 / (performance.now() - previousTime);
        status.textContent = `FPS: ${fps.toFixed(2)}`;
      }
      previousTime = performance.now();
      isProcessing = false;
    })();
  }

  window.requestAnimationFrame(updateCanvas);
}

// ===== ATTENDANCE MANAGEMENT =====
console.log("student.js loaded");

let selectedClass = ""
let attendanceStream = null
let faceScanned = false

// Global variable for attendance records
let globalAllAttendanceRecords = [];
let enrolledClasses = [];

const currentUser = authService.getUser();
console.log(currentUser.id);

// Fetch student's enrolled classes from the groups table
async function fetchStudentClasses() {
    try {
        const token = localStorage.getItem("access_token");
        if (!token) throw new Error("Please log in first");

        const userJson = localStorage.getItem("user");
        if (!userJson) throw new Error("User data not found in storage. Please re-login.");

        const userData = JSON.parse(userJson);
        const studentId = userData.id;
        if (!studentId) throw new Error("Student ID missing in storage");

        const res = await fetch(`http://localhost:8080/api/groups`, {
            headers: { Authorization: `Bearer ${token}` }
        });

        if (!res.ok) {
            throw new Error("Failed to load groups");
        }

        const allGroups = await res.json();
        
        const studentGroups = allGroups.filter(group => {
            if (!group.student_list) return false;
            
            let studentList = group.student_list;
            if (typeof studentList === 'string') {
                try {
                    studentList = JSON.parse(studentList);
                } catch (e) {
                    studentList = studentList.split(',').map(s => s.trim());
                }
            }
            
            return studentList.includes(studentId) || 
                studentList.includes(userData.email) ||
                studentList.includes(userData.code);
        });

        const uniqueClassCodes = [...new Set(studentGroups.map(g => g.class_code).filter(Boolean))];
        
        const classesRes = await fetch(`http://localhost:8080/api/classes`, {
            headers: { Authorization: `Bearer ${token}` }
        });
        
        let allClasses = [];
        if (classesRes.ok) {
            allClasses = await classesRes.json();
        }
        
        const classMap = new Map();
        
        for (const classCode of uniqueClassCodes) {
            const classData = allClasses.find(c => c.class_code === classCode);
            
            if (classData) {
                classMap.set(classCode, {
                    class_code: classData.class_code,
                    class_name: classData.class_name,
                    professor_list: classData.professor_list || ''
                });
            } else {
                console.warn(`Class ${classCode} not found in classes table`);
                classMap.set(classCode, {
                    class_code: classCode,
                    class_name: `Class ${classCode}`,
                    professor_list: ''
                });
            }
        }

        const classes = Array.from(classMap.values());
        
        if (classes.length === 0) {
            console.warn("No classes found for this student");
        }

        enrolledClasses = classes;
        populateClassDropdown(classes);
        return classes;

    } catch (err) {
        console.error("Error fetching classes:", err);
        alert("Error loading classes: " + err.message);
        
        enrolledClasses = [];
        populateClassDropdown([]);
        return [];
    }
}

// Populate the class dropdown with enrolled classes
function populateClassDropdown(classes) {
    const classSelect = document.getElementById("classSelect");
    
    if (!classSelect) {
        console.error("classSelect element not found in DOM");
        return;
    }
    
    while (classSelect.firstChild) {
        classSelect.removeChild(classSelect.firstChild);
    }
    
    const defaultOption = document.createElement('option');
    defaultOption.value = "";
    defaultOption.textContent = "Select a class";
    classSelect.appendChild(defaultOption);
    
    if (classes && classes.length > 0) {
        classes.forEach(cls => {
            const option = document.createElement('option');
            option.value = cls.class_code.toLowerCase();
            option.textContent = `${cls.class_code} - ${cls.class_name}`;
            option.dataset.classCode = cls.class_code;
            option.dataset.className = cls.class_name;
            classSelect.appendChild(option);
        });
    } else {
        const option = document.createElement('option');
        option.value = "";
        option.textContent = "No classes enrolled";
        option.disabled = true;
        classSelect.appendChild(option);
    }
}

// Fetch attendance records for a specific class
async function fetchAttendanceRecordsForClass(classCode, studentId) {
    try {
        const token = localStorage.getItem("access_token");

        const res = await fetch(
            `http://localhost:8080/api/attendance_records`,
            {
                headers: { Authorization: `Bearer ${token}` }
            }
        );

        if (!res.ok) {
            console.log("No attendance records found");
            return [];
        }

        const allRecords = await res.json();
        
        const sessions = await fetchSessionsForClass(classCode);
        const sessionIds = sessions.map(s => s.id);
        
        console.log("Session IDs for class", classCode, ":", sessionIds);
        
        const studentRecords = allRecords.filter(record => {
            const matchesStudent = record.student_id === studentId;
            const matchesSession = sessionIds.includes(record.session_id);
            console.log(`Record ${record.id}: student match=${matchesStudent}, session match=${matchesSession}`);
            return matchesStudent && matchesSession;
        });
        
        console.log("Filtered attendance records:", studentRecords);
        
        return studentRecords;
    } catch (err) {
        console.error("Error fetching attendance records:", err);
        return [];
    }
}

// Fetch sessions for a class
async function fetchSessionsForClass(classCode) {
    try {
        const token = localStorage.getItem("access_token");
        
        const res = await fetch(`http://localhost:8080/api/sessions`, {
            headers: { Authorization: `Bearer ${token}` }
        });

        if (!res.ok) {
            return [];
        }

        const allSessions = await res.json();
        
        console.log("All sessions:", allSessions);
        console.log("Looking for class_code:", classCode);
        
        const filteredSessions = allSessions.filter(s => {
            console.log(`Session ${s.name}: class_code = ${s.class_code}`);
            return s.class_code === classCode;
        });
        
        console.log("Filtered sessions:", filteredSessions);
        return filteredSessions;
    } catch (err) {
        console.error("Error fetching sessions:", err);
        return [];
    }
}

// Helper functions
function clearContainer(container) {
    while (container.firstChild) {
        container.removeChild(container.firstChild);
    }
}

function createMessage(text, className = 'welcome-subtitle') {
    const p = document.createElement('p');
    p.className = className;
    p.textContent = text;
    return p;
}

// Display attendance table
async function displayDummyAttendance() {
    const classSelect = document.getElementById("classSelect");
    const mainContainer = document.getElementById("attendanceTableContainer");
    const selectedClass = classSelect.value;

    clearContainer(mainContainer);

    if (!selectedClass) {
        mainContainer.appendChild(createMessage("Please select a class to view attendance records"));
        return;
    }

    const selectedOption = classSelect.options[classSelect.selectedIndex];
    const classCode = selectedOption.dataset.classCode;
    const className = selectedOption.dataset.className;

    const userJson = localStorage.getItem("user");
    const userData = JSON.parse(userJson);
    const studentId = userData.id;

    let sessions = await fetchSessionsForClass(classCode);
    const allAttendanceRecords = await fetchAttendanceRecordsForClass(classCode, studentId);

    if (sessions.length === 0) {
        const header = document.createElement('h3');
        header.className = 'attendance-header';
        header.textContent = `Attendance for ${classCode} - ${className}`;
        mainContainer.appendChild(header);
        mainContainer.appendChild(createMessage("No sessions found for this class yet"));
        return;
    }

    const attendanceMap = new Map();
    allAttendanceRecords.forEach(record => {
        attendanceMap.set(record.session_id, record);
    });

    sessions = sessions.filter(s => attendanceMap.has(s.id));
    
    if (sessions.length === 0) {
        const header = document.createElement('h3');
        header.className = 'attendance-header';
        header.textContent = `Attendance for ${classCode} - ${className}`;
        mainContainer.appendChild(header);
        mainContainer.appendChild(createMessage("No attendance records found for this class"));
        return;
    }

    const header = document.createElement('h3');
    header.className = 'attendance-header';
    header.textContent = `Attendance for ${classCode} - ${className}`;
    mainContainer.appendChild(header);

    const table = document.createElement('table');
    table.className = 'attendance-table';

    const thead = document.createElement('thead');
    const headerRow = document.createElement('tr');
    ['Session', 'Date', 'Status', 'Method', 'Marked At'].forEach(text => {
        const th = document.createElement('th');
        th.textContent = text;
        headerRow.appendChild(th);
    });
    thead.appendChild(headerRow);
    table.appendChild(thead);

    const tbody = document.createElement('tbody');
    sessions.sort((a, b) => new Date(a.date) - new Date(b.date));

    console.log(`Creating table with ${sessions.length} sessions`);
    console.log(`Attendance map has ${attendanceMap.size} records`);

    sessions.forEach(session => {
        const row = document.createElement('tr');
        
        const sessionDate = new Date(session.date);
        const formattedDate = sessionDate.toLocaleDateString('en-GB', {
            day: '2-digit',
            month: '2-digit',
            year: '2-digit'
        });
        
        const attendance = attendanceMap.get(session.id);
        const status = attendance ? attendance.status : 'Absent';
        const method = attendance ? (attendance.method || 'N/A') : 'N/A';
        
        let markedAt = 'N/A';
        if (attendance && attendance.marked_at) {
            const markedDate = new Date(attendance.marked_at);
            markedAt = markedDate.toLocaleDateString('en-GB', {
                day: '2-digit',
                month: '2-digit',
                year: '2-digit'
            }) + ' ' + markedDate.toLocaleTimeString('en-GB', {
                hour: '2-digit',
                minute: '2-digit'
            });
        }
        
        const sessionCell = document.createElement('td');
        const sessionStrong = document.createElement('strong');
        sessionStrong.textContent = session.name || 'Session';
        sessionCell.appendChild(sessionStrong);
        row.appendChild(sessionCell);
        
        const dateCell = document.createElement('td');
        dateCell.textContent = formattedDate;
        row.appendChild(dateCell);
        
        const statusCell = document.createElement('td');
        const statusBadge = document.createElement('span');
        statusBadge.className = `status-badge ${status.toLowerCase()}`;
        statusBadge.textContent = status;
        statusCell.appendChild(statusBadge);
        row.appendChild(statusCell);
        
        const methodCell = document.createElement('td');
        methodCell.textContent = method;
        row.appendChild(methodCell);
        
        const markedAtCell = document.createElement('td');
        markedAtCell.textContent = markedAt;
        row.appendChild(markedAtCell);
        
        tbody.appendChild(row);
    });

    table.appendChild(tbody);
    mainContainer.appendChild(table);
}

// Face scanning functions
function enableFaceScanning() {
  const classSelect = document.getElementById("classSelect")
  const scanBtn = document.getElementById("scanFaceBtn")

  selectedClass = classSelect.value

  if (selectedClass) {
    scanBtn.style.display = "block"
    const className = classSelect.options[classSelect.selectedIndex].text
    scanBtn.textContent = `Scan Face for ${className}`
  } else {
    scanBtn.style.display = "none"
    selectedClass = ""
  }
}

async function openFaceScanning() {
  if (!selectedClass) {
    alert("Please select a class first");
    return;
  }

  document.getElementById("faceScanModal").classList.add("active");

  if (attendanceStream) {
    attendanceStream.getTracks().forEach(track => track.stop());
    attendanceStream = null;
  }

  try {
    attendanceStream = await navigator.mediaDevices.getUserMedia({
      video: {
        width: 640,
        height: 480,
        facingMode: "user",
      },
    });

    const video = document.getElementById("video");
    video.srcObject = attendanceStream;
    video.play();

    const videoTrack = attendanceStream.getVideoTracks()[0];
    const { width, height } = videoTrack.getSettings();

    setStreamSize(width * scale, height * scale);

    const ar = width / height;
    const [cw, ch] = ar > 720 / 405 ? [720, 720 / ar] : [405 * ar, 405];
    const container = document.getElementById("container");
    container.style.width = `${cw}px`;
    container.style.height = `${ch}px`;

    window.requestAnimationFrame(updateCanvas);
  } catch (error) {
    console.error("Error accessing camera:", error);
    alert("Unable to access camera. Please ensure camera permissions are granted.");
  }
}

function closeFaceScanning() {
  document.getElementById("faceScanModal").classList.remove("active")

  if (attendanceStream) {
    attendanceStream.getTracks().forEach((track) => track.stop())
    attendanceStream = null
  }

  faceScanned = false
  document.getElementById("scanResult").style.display = "none"
  document.getElementById("scanBtn").style.display = "block"
  document.getElementById("markPresentBtn").style.display = "none"
}

function scanFace() {
  const video = document.getElementById("attendanceVideo")
  const canvas = document.getElementById("attendanceCanvas")
  const ctx = canvas.getContext("2d")

  canvas.width = video.videoWidth
  canvas.height = video.videoHeight
  ctx.drawImage(video, 0, 0)

  setTimeout(() => {
    faceScanned = true
    document.getElementById("scanResult").style.display = "block"
    document.getElementById("scanBtn").style.display = "none"
    document.getElementById("markPresentBtn").style.display = "block"
  }, 1500)
}

function markAttendance() {
  // Implement attendance marking logic
  alert("Attendance marked!");
  closeFaceScanning();
}

// Profile editing functions
let currentUserProfile = null;

async function getCurrentStudent() {
    try {
        const token = localStorage.getItem("access_token");
        if (!token) throw new Error("Please log in first");

        const userJson = localStorage.getItem("user");
        if (!userJson) throw new Error("User data not found in storage. Please re-login.");

        const userData = JSON.parse(userJson);
        const studentId = userData.id;
        if (!studentId) throw new Error("Student ID missing in storage");

        const res = await fetch(`http://localhost:8080/api/students/${studentId}`, {
            headers: { Authorization: `Bearer ${token}` }
        });

        if (res.status === 404) throw new Error("Student profile not found.");
        if (!res.ok) {
            const errorData = await res.json();
            throw new Error(errorData.message || "Failed to load student profile.");
        }

        const student = await res.json();
        currentUserProfile = student;
        localStorage.setItem("student_id", student.id); 
        return student;

    } catch (err) {
        console.error(err);
        alert(err.message);
        return null; 
    }
}

async function openEditProfileModal() {
    try {
        const student = await getCurrentStudent();
        if (!student) return;

        document.getElementById("editName").value = student.name || "";
        document.getElementById("editEmail").value = student.email || "";
        document.getElementById("editClass").value = student.class_name || "";

        document.getElementById("currentPassword").value = "";
        document.getElementById("editPassword").value = "";
        document.getElementById("editConfirmPassword").value = "";

        document.getElementById("editPasswordError").style.display = "none";
        document.getElementById("editSuccessMessage").style.display = "none";
        document.getElementById("editErrorMessage").style.display = "none";

        document.getElementById("editProfileModal").classList.add("active");
    } catch (err) {
        console.error("Error opening modal:", err);
        alert("Error loading profile");
    }
}

function closeEditProfileModal() {
    document.getElementById("editProfileModal").classList.remove("active");
}

async function saveProfileChanges() {
    try {
        // Get student ID from user data instead of separate storage
        const userJson = localStorage.getItem("user");
        if (!userJson) throw new Error("User data not found. Please log in again.");
        
        const userData = JSON.parse(userJson);
        const studentId = userData.id;
        
        console.log("Updating profile for student ID:", studentId);
        
        if (!studentId) throw new Error("Student ID missing");

        const token = localStorage.getItem("access_token");
        if (!token) throw new Error("Please log in first");

        const name = document.getElementById("editName").value.trim();
        const email = document.getElementById("editEmail").value.trim();
        const class_name = document.getElementById("editClass").value.trim();
        const currentPassword = document.getElementById("currentPassword").value;
        const newPassword = document.getElementById("editPassword").value;
        const confirmPassword = document.getElementById("editConfirmPassword").value;

        document.getElementById("editPasswordError").style.display = "none";
        document.getElementById("editSuccessMessage").style.display = "none";
        document.getElementById("editErrorMessage").style.display = "none";

        const updateData = { name, email, class_name };

        if (newPassword) {
            if (!currentPassword) {
                alert("Please enter your current password to change it");
                return;
            }
            if (newPassword !== confirmPassword) {
                document.getElementById("editPasswordError").textContent = "Passwords do not match";
                document.getElementById("editPasswordError").style.display = "block";
                return;
            }
            if (newPassword.length < 6) {
                alert("New password must be at least 6 characters");
                return;
            }
            updateData.currentPassword = currentPassword;
            updateData.newPassword = newPassword;
        }

        console.log("Sending update request:", updateData);
        console.log("URL:", `http://localhost:8080/api/students/${studentId}`);

        const res = await fetch(`http://localhost:8080/api/students/${studentId}`, {
            method: "PATCH",
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${token}`
            },
            body: JSON.stringify(updateData)
        });

        console.log("Response status:", res.status);

        if (!res.ok) {
            let errorMessage = "Failed to update profile";
            try {
                const errorData = await res.json();
                errorMessage = errorData.message || errorMessage;
                console.error("Error response:", errorData);
            } catch (e) {
                const errorText = await res.text();
                console.error("Error response (text):", errorText);
            }
            throw new Error(errorMessage);
        }

        const updatedStudent = await res.json();
        console.log("Profile updated:", updatedStudent);

        localStorage.setItem("user", JSON.stringify(updatedStudent));
        currentUserProfile = updatedStudent;

        document.getElementById("editSuccessMessage").style.display = "block";

        setTimeout(() => closeEditProfileModal(), 1500);

    } catch (err) {
        console.error("Error updating profile:", err);
        document.getElementById("editErrorMessage").textContent = err.message;
        document.getElementById("editErrorMessage").style.display = "block";
    }
}

// Initialize immediately when module loads
(async function initialize() {
    console.log('Initializing student dashboard...');
    
    // Wait a bit for DOM to be ready if needed
    if (document.readyState === 'loading') {
        await new Promise(resolve => document.addEventListener('DOMContentLoaded', resolve));
    }
    
    console.log('DOM ready, fetching classes...');
    
    // Fetch and populate classes
    await fetchStudentClasses();
    
    // Display initial state
    displayDummyAttendance();
    
    // Add event listener for class selection
    const classSelect = document.getElementById("classSelect");
    if (classSelect) {
        classSelect.addEventListener("change", displayDummyAttendance);
        console.log('Event listener added to classSelect');
    } else {
        console.error('classSelect element not found!');
    }
})();

// Make functions available globally for onclick handlers
window.enableFaceScanning = enableFaceScanning;
window.openFaceScanning = openFaceScanning;
window.closeFaceScanning = closeFaceScanning;
window.scanFace = scanFace;
window.closeEditProfileModal = closeEditProfileModal;
window.markAttendance = markAttendance;
window.openEditProfileModal = openEditProfileModal;
window.saveProfileChanges = saveProfileChanges;




