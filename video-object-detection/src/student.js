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

        const result = await response.text(); // or JSON if backend returns JSON
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

let hasSent = false; // Flag to send API request only once per detection

var color = "yellow";
var text = "Verifying..."
// Render a bounding box and label on the image
function renderBox([xmin, ymin, xmax, ymax, score, id], [w, h]) {
  if (score < threshold) return; // Skip boxes with low confidence



  // Create bounding box div
  let boxElement = document.createElement("div");
  boxElement.className = "bounding-box";
  Object.assign(boxElement.style, {
    borderColor: color,
    left: (100 * xmin) / w + "%",
    top: (100 * ymin) / h + "%",
    width: (100 * (xmax - xmin)) / w + "%",
    height: (100 * (ymax - ymin)) / h + "%",
  });

  // Create label span
  let labelElement = document.createElement("span");
  labelElement.textContent = text;
  labelElement.className = "bounding-box-label";
  labelElement.style.backgroundColor = color;
  labelElement.style.color = "black";

  boxElement.appendChild(labelElement);
  overlay.appendChild(boxElement);

  // Store references globally for updating after API response
  currentBoxElement = boxElement;
  currentLabelElement = labelElement;

  // Send image to the API on first detection
  if (!hasSent) {
    hasSent = true;
    sendCanvasImageToAPI(canvas)
      .then((response) => {
        
        const responseObj = JSON.parse(response); 
        const confidenceStr = responseObj.result; // "0.982708"

        // Extract decimal part only:
        // const decimalPart = confidenceStr.slice(confidenceStr.indexOf('.') + 1);
        // console.log(decimalPart);  // e.g., "982708"

        // Convert to float for comparison
        const decimalNumber = parseFloat(confidenceStr);
        console.log(decimalNumber);
        

        if (decimalNumber !== null && decimalNumber > 0.80) {
          // --- MODIFICATION START ---
          // Instead of removing and recreating the box, just update its style.
          color = "green";
          text = "Verified Successfully! " + decimalNumber;
          currentLabelElement.style.color = "black"; // Or "white" if it looks better

          console.log("Updated box to green");
          // --- MODIFICATION END ---
          
        } else if (decimalNumber !== null) {
          // Not identified case - update existing box and label to red
          color = "red";
          text = "Failed to verify"
          currentLabelElement.style.color = "black"; // Or "white" if it looks better

          console.log("Updated box to red");
        } else {
          // Fallback yellow verifying state
          currentLabelElement.textContent = "Verifying...";
          currentLabelElement.style.backgroundColor = "yellow";
          currentLabelElement.style.color = "black";
          currentBoxElement.style.borderColor = "yellow";

          console.log("Fallback to yellow");
        }
      })
      .catch((err) => {
        console.error("Error sending image to API:", err);
        // On error, fallback to yellow verifying
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


//ORIGINAL STUDENT
console.log("student.js loaded");

let selectedClass = ""
let attendanceStream = null
let faceScanned = false


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

// Function to open the edit profile modal and populate with current user data
async function openEditProfileModal() {
  try {
    // Hardcoded user data for testing
    const user = {
        name: "John Doe",
        email: "john.doe@example.com"
    };
    
    // Original API call (commented out)
    // const response = await fetch('http://localhost:8080/api/user/profile');
    // if (!response.ok) {
    //     throw new Error('Failed to load user data');
    // }
    // const user = await response.json();
    
    // Populate form with current data
    document.getElementById('editName').value = user.name || '';
    document.getElementById('editEmail').value = user.email || '';
    
    // Clear password fields
    document.getElementById('currentPassword').value = '';
    document.getElementById('editPassword').value = '';
    document.getElementById('editConfirmPassword').value = '';
    
    // Hide error/success messages
    document.getElementById('editPasswordError').style.display = 'none';
    document.getElementById('editSuccessMessage').style.display = 'none';
    document.getElementById('editErrorMessage').style.display = 'none';
    
    // Show the modal
    const modal = new bootstrap.Modal(document.getElementById('editProfileModal'));
    modal.show();
      
  } catch (error) {
    console.error('Error loading profile:', error);
    alert('Error loading profile data');
  }
}

// Function to save profile changes
async function saveProfileChanges() {
  // Get form values
  const name = document.getElementById('editName').value.trim();
  const email = document.getElementById('editEmail').value.trim();
  const currentPassword = document.getElementById('currentPassword').value;
  const newPassword = document.getElementById('editPassword').value;
  const confirmPassword = document.getElementById('editConfirmPassword').value;
  
  // Hide previous messages
  document.getElementById('editPasswordError').style.display = 'none';
  document.getElementById('editSuccessMessage').style.display = 'none';
  document.getElementById('editErrorMessage').style.display = 'none';
  
  // Validate passwords if user is trying to change password
  if (newPassword || confirmPassword) {
      if (newPassword !== confirmPassword) {
          document.getElementById('editPasswordError').style.display = 'block';
          return;
      }
      
      if (!currentPassword) {
          alert('Please enter your current password to change it');
          return;
      }
      
      if (newPassword.length < 6) {
          alert('New password must be at least 6 characters long');
          return;
      }
  }
  
  // Prepare update data
  const updateData = {
      name: name,
      email: email
  };
  
  // Add password fields only if user wants to change password
  if (newPassword) {
    updateData.currentPassword = currentPassword;
    updateData.newPassword = newPassword;
  }
  
  try {
    // Log the data that would be sent (for testing)
    console.log('Update data:', updateData);
    
    // Simulate successful update with hardcoded response
    const updatedUser = {
        id: "123e4567-e89b-12d3-a456-426614174000",
        name: name,
        email: email,
        updatedAt: new Date().toISOString()
    };
    
    // Original API call (commented out)
    // const response = await fetch('http://localhost:8080/api/user/profile', {
    //     method: 'PUT',
    //     headers: {
    //         'Content-Type': 'application/json'
    //     },
    //     body: JSON.stringify(updateData)
    // });
    // 
    // if (!response.ok) {
    //     const errorData = await response.json();
    //     throw new Error(errorData.message || 'Failed to update profile');
    // }
    // 
    // const updatedUser = await response.json();
    
    // Show success message
    document.getElementById('editSuccessMessage').style.display = 'block';
    
    // Update UI with new data if needed
    console.log('Profile updated:', updatedUser);
    
    // Close modal after 1.5 seconds
    setTimeout(() => {
        const modal = bootstrap.Modal.getInstance(document.getElementById('editProfileModal'));
        modal.hide();
        
        // Optionally refresh the page or update displayed user info
        // location.reload();
    }, 1500);
      
  } catch (error) {
    console.error('Error updating profile:', error);
    document.getElementById('editErrorMessage').textContent = error.message;
    document.getElementById('editErrorMessage').style.display = 'block';
  }
}

// Add real-time password match validation
// document.addEventListener('DOMContentLoaded', () => {
//   const newPassword = document.getElementById('editPassword');
//   const confirmPassword = document.getElementById('editConfirmPassword');
//   const errorDiv = document.getElementById('editPasswordError');
//   const editProfileBtn = document.getElementById('editProfileBtn');

//   console.log('DOM loaded');
//   console.log('Bootstrap available:', typeof bootstrap !== 'undefined');
//   console.log('Modal element exists:', document.getElementById('editProfileModal') !== null);

//   if (editProfileBtn) {
//     editProfileBtn.addEventListener('click', openEditProfileModal);
//   }

//   function checkPasswordMatch() {
//     if (confirmPassword.value && newPassword.value !== confirmPassword.value) {
//       errorDiv.style.display = 'block';
//     } else {
//       errorDiv.style.display = 'none';
//     }
//   }
    
//   if (newPassword && confirmPassword) {
//     newPassword.addEventListener('input', checkPasswordMatch);
//     confirmPassword.addEventListener('input', checkPasswordMatch);
//   }
  
//   function checkPasswordMatch() {
//     if (confirmPassword.value && newPassword.value !== confirmPassword.value) {
//       errorDiv.style.display = 'block';
//     } else {
//       errorDiv.style.display = 'none';
//     }
//   }
  
//   if (newPassword && confirmPassword) {
//     newPassword.addEventListener('input', checkPasswordMatch);
//     confirmPassword.addEventListener('input', checkPasswordMatch);
//   }
// });

const currentUser = authService.getUser();
console.log(currentUser.id);
const class_code_array = []


fetch('http://localhost:8080/api/groups')
  .then(response => {
    if (!response.ok) {
      throw new Error('Network response was not ok ' + response.statusText);
    }
    return response.json(); // if you expect JSON
  })
  .then(data => {
    console.log(data);
    console.log(currentUser.id);
    for (let group of data) {
      console.log(group.student_list);
      if (group.student_list.includes(currentUser.id)) {
          class_code_array.push(group.class_code)
      }
    }

    const classSelectDropDown = document.getElementById("classSelect");
    console.log(class_code_array);
    for (let class_code of class_code_array) {
      const option = document.createElement("option");
      option.value = class_code;
      option.innerText = class_code;
      classSelectDropDown.appendChild(option);
    }

    
  })
  .catch(error => {
    console.error('There was a problem with the fetch operation:', error);
  });


// fetch('http://localhost:8080/api/classes')
//   .then(response => {
//     if (!response.ok) {
//       throw new Error('Network response was not ok ' + response.statusText);
//     }
//     return response.json(); // if you expect JSON
//   })
//   .then(data => {
//     console.log(data); // will log your groups array/object to console
//   })
//   .catch(error => {
//     console.error('There was a problem with the fetch operation:', error);
//   });


// Close modal on escape key
document.addEventListener('keydown', (e) => {
  if (e.key === 'Escape') {
    const modal = bootstrap.Modal.getInstance(document.getElementById('editProfileModal'));
    if (modal) {
      modal.hide();
    }
  }
});

//so inline in html can be used
window.enableFaceScanning = enableFaceScanning;
window.openFaceScanning = openFaceScanning;
window.closeFaceScanning = closeFaceScanning;
window.scanFace = scanFace;
window.closeEditProfileModal = closeEditProfileModal;
window.markAttendance = markAttendance;
window.openEditProfileModal = openEditProfileModal;
window.saveProfileChanges = saveProfileChanges;




