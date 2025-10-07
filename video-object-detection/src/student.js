console.log("student.js loaded");

let selectedClass = ""
let attendanceStream = null
let faceScanned = false


function logout() {
  window.location.href = "index.html"
}

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
    alert("Please select a class first")
    return
  }

  document.getElementById("faceScanModal").classList.add("active")

  try {
    attendanceStream = await navigator.mediaDevices.getUserMedia({
      video: {
        width: 640,
        height: 480,
        facingMode: "user",
      },
    })
    document.getElementById("attendanceVideo").srcObject = attendanceStream
  } catch (error) {
    console.error("Error accessing camera:", error)
    alert("Unable to access camera. Please ensure camera permissions are granted.")
  }
}

function closeFaceScanning() {
  document.getElementById("faceScanModal").classList.remove("active")

  if (attendanceStream) {
    attendanceStream.getTracks().forEach((track) => track.stop())
    attendanceStream = null
  }

  // Reset scan state
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

  // Simulate face verification processing
  setTimeout(() => {
    faceScanned = true
    document.getElementById("scanResult").style.display = "block"
    document.getElementById("scanBtn").style.display = "none"
    document.getElementById("markPresentBtn").style.display = "block"
  }, 1500)
}
