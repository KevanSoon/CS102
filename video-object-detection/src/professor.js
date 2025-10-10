console.log("professor.js loaded");
//original professor.js below onwards

// Global variables
let attendanceRecords = [
  { date: "2024-01-15", class: "CS101", status: "Present", time: "09:00 AM" },
  { date: "2024-01-14", class: "MATH201", status: "Present", time: "11:00 AM" },
  { date: "2024-01-13", class: "PHY301", status: "Absent", time: "-" },
]

let allSessions = [];

async function showSessions() {
  try {
    const response = await fetch('http://localhost:8080/api/sessions');
    return await response.json();
  } catch (error) {
    console.error('Error:', error);
    return [];
  }
}

async function init() {
  allSessions = await showSessions();
  console.log('Sessions loaded:', allSessions);
  
  // Call other functions that need allSessions
  // displaySessions();
}

// function displaySessions() {
//   // Use allSessions here
//   console.log(`Found ${allSessions.length} sessions`);
// }

// Call init when page loads
init();

function markAttendance() {
  if (!selectedClass || !faceScanned) {
    alert("Please scan your face first")
    return
  }

  const now = new Date()
  const dateStr = now.toISOString().split("T")[0]
  const timeStr = now.toLocaleTimeString("en-US", {
    hour: "2-digit",
    minute: "2-digit",
    hour12: true,
  })

  const classSelect = document.getElementById("classSelect")
  const className = classSelect.options[classSelect.selectedIndex].text.split(" - ")[0]

  const newRecord = {
    date: dateStr,
    class: className,
    status: "Present",
    time: timeStr,
  }

  // Remove existing record for same date and class
  attendanceRecords = attendanceRecords.filter((record) => !(record.date === dateStr && record.class === className))

  attendanceRecords.unshift(newRecord)

  // updateRecentAttendance()
  updateStudentStats()

  alert(`Attendance marked successfully for ${className}!`)
  closeFaceScanning()

  // Reset selection
  document.getElementById("classSelect").value = ""
  document.getElementById("scanFaceBtn").style.display = "none"
  selectedClass = ""
}

// function updateRecentAttendance() {
//   const recentSection = document.querySelector(".section-card:last-child .section-content")

//   if (attendanceRecords.length === 0) {
//     recentSection.innerHTML = '<p class="welcome-subtitle">No attendance records yet</p>'
//     return
//   }

//   const recentRecords = attendanceRecords.slice(0, 5)
//   let tableHTML = `
//         <table style="width: 100%; border-collapse: collapse;">
//             <thead>
//                 <tr style="border-bottom: 1px solid #e2e8f0;">
//                     <th style="text-align: left; padding: 0.5rem; color: #64748b; font-weight: 500;">Date</th>
//                     <th style="text-align: left; padding: 0.5rem; color: #64748b; font-weight: 500;">Class</th>
//                     <th style="text-align: left; padding: 0.5rem; color: #64748b; font-weight: 500;">Status</th>
//                     <th style="text-align: left; padding: 0.5rem; color: #64748b; font-weight: 500;">Time</th>
//                 </tr>
//             </thead>
//             <tbody>
//     `
//   // pull all

//   recentRecords.forEach((record) => {
//     const statusColor = record.status === "Present" ? "#10b981" : "#ef4444"
//     tableHTML += `
//             <tr style="border-bottom: 1px solid #f1f5f9;">
//                 <td style="padding: 0.75rem 0.5rem;">${record.date}</td>
//                 <td style="padding: 0.75rem 0.5rem;">${record.class}</td>
//                 <td style="padding: 0.75rem 0.5rem; color: ${statusColor}; font-weight: 500;">${record.status}</td>
//                 <td style="padding: 0.75rem 0.5rem;">${record.time}</td>
//             </tr>
//         `
//   })

//   tableHTML += "</tbody></table>"
//   recentSection.innerHTML = tableHTML
// }

function updateStudentStats() {
  const totalClasses = attendanceRecords.length
  const presentClasses = attendanceRecords.filter((record) => record.status === "Present").length
  const attendanceRate = totalClasses > 0 ? Math.round((presentClasses / totalClasses) * 100) : 0

  const statNumbers = document.querySelectorAll(".stat-number")
  if (statNumbers.length >= 3) {
    statNumbers[0].textContent = `${attendanceRate}%`
    statNumbers[1].textContent = totalClasses

    const today = new Date().toISOString().split("T")[0]
    const classesToday = attendanceRecords.filter((record) => record.date === today).length
    statNumbers[2].textContent = classesToday
  }
}

function openCreateClass() {
  document.getElementById("createClassModal").classList.add("active")
}

function closeCreateClass() {
  document.getElementById("createClassModal").classList.remove("active")
}

function openManualAttendance() {
  document.getElementById("manualAttendanceModal").classList.add("active")
}

function closeManualAttendance() {
  document.getElementById("manualAttendanceModal").classList.remove("active")
}

function openGenerateReport() {
  document.getElementById("generateReportModal").classList.add("active")
}

function closeGenerateReport() {
  document.getElementById("generateReportModal").classList.remove("active")
}

function exportCSV() {
  alert("Exporting attendance report as CSV...")
}

function exportPDF() {
  alert("Exporting attendance report as PDF...")
}

function openStudentManagement() {
  document.getElementById("studentManagementModal").classList.add("active")
}

function closeStudentManagement() {
  document.getElementById("studentManagementModal").classList.remove("active")
}

function openAttendanceCheck() {
  document.getElementById("attendanceCheckModal").classList.add("active")
}

function closeAttendanceCheck() {
  document.getElementById("attendanceCheckModal").classList.remove("active")
}

function closeActiveSession() {
  if (confirm("Are you sure you want to close the active session?")) {
    alert("Active session closed successfully!")
    const activeSessions = document.getElementById("activeSessions")
    if (activeSessions) {
      activeSessions.innerHTML = '<p class="welcome-subtitle">No active sessions at the moment</p>'
    }
  }
}

function showRegister() {
  document.getElementById("faceRegisterModal").classList.add("active")
}

function closeFaceRegister() {
  document.getElementById("faceRegisterModal").classList.remove("active")
}

function captureFace() {
  alert("Face captured successfully! Registration complete.")
  closeFaceRegister()
}

function logout() {
  window.location.href = "index.html"
}

document.addEventListener("DOMContentLoaded", () => {
  // updateRecentAttendance()
  updateStudentStats()
  init()

  // Handle form submissions
  const createClassForm = document.getElementById("createClassForm")
  if (createClassForm) {
    createClassForm.addEventListener("submit", (e) => {
      e.preventDefault()
      alert("Class session created successfully!")
      const activeSessions = document.getElementById("activeSessions")
      if (activeSessions) {
        activeSessions.innerHTML =
          `<div class="stat-card">
          <div class="stat-content">
          <h4>CS101 - Active Session</h4>
          <p>Started at 10:00 AM</p>
          </div>
            <button id="scanFaceBtn" class="btn btn-primary" onclick="openFaceScanning()">
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
          `

      }
      closeCreateClass()
    })
  }
})







