console.log("face-detection-model loaded");

import { AutoModel, AutoProcessor, RawImage } from "@huggingface/transformers";

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
let attendanceStream = null;

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

async function sendCanvasImageToAPI(canvas) {
  return new Promise((resolve, reject) => {
    canvas.toBlob(async (blob) => {
      if (!blob) {
        return reject("Failed to get Blob from canvas");
      }
      const file = new File([blob], "detected.png", { type: "image/png" });
      try {
        const formData = new FormData();
        formData.append("image", file);
        const response = await fetch("http://localhost:8080/api/face-batch/face-compare-all", {
          method: "POST",
          body: formData,
        });
        if (!response.ok) {
          return reject("Failed to send image: " + response.statusText);
        }
        const result = await response.json();
        if (result.bestMatchStudentId) {
          console.log("✅ Best Match Found! Student:", result.bestMatchName, "Similarity:", result.highestSimilarity);
        } else {
          console.log("❌ No match above threshold:", result.message || "No match found");
        }
        resolve(result);
      } catch (err) {
        reject(err);
      }
    }, "image/png");
  });
}

// Variables to manage detection state
let currentBoxElement = null;
let currentLabelElement = null;
let hasSent = false;
let color = "yellow"; // Default color
let text = "Verifying..."; // Default text

function renderBox([xmin, ymin, xmax, ymax, score, id], [w, h]) {
    // The threshold check here is still useful as a fallback, but the main logic is now in updateCanvas.
    if (score < threshold) return;

    if (!hasSent) {
        
        color = "yellow";
        text = "Verifying...";
    }

    let boxElement = document.createElement("div");
    boxElement.className = "bounding-box";
    Object.assign(boxElement.style, {
        borderColor: color,
        left: `${(100 * xmin) / w}%`,
        top: `${(100 * ymin) / h}%`,
        width: `${(100 * (xmax - xmin)) / w}%`,
        height: `${(100 * (ymax - ymin)) / h}%`,
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
                const { highestSimilarity, bestMatchName = "Unknown" } = response;
                if (highestSimilarity && highestSimilarity > 0.80) {
                    color = "green";
                    text = `Verified! ${bestMatchName} (${(highestSimilarity * 100).toFixed(2)}%)`;
                } else {
                    color = "red";
                    text = "Failed to verify";
                }
                if (currentLabelElement && currentBoxElement) {
                    currentLabelElement.textContent = text;
                    currentLabelElement.style.backgroundColor = color;
                    currentBoxElement.style.borderColor = color;
                }
            })
            .catch((err) => {
                console.error("Error sending image to API:", err);
                color = "red";
                text = "Error";
                if (currentLabelElement && currentBoxElement) {
                    currentLabelElement.textContent = text;
                    currentLabelElement.style.backgroundColor = color;
                    currentBoxElement.style.borderColor = color;
                }
            });
    }
}

let isProcessing = false;
let previousTime;
const context = canvas.getContext("2d", { willReadFrequently: true });
let noFaceFramesCount = 0;
const NO_FACE_RESET_THRESHOLD = 5;

// =====================================================================================
// === THE CORRECTED LOGIC IS IN THIS FUNCTION =========================================
// =====================================================================================
async function updateCanvas() {
  const { width, height } = canvas;
  context.drawImage(video, 0, 0, width, height);

  if (!isProcessing) {
    isProcessing = true;
    
    const pixelData = context.getImageData(0, 0, width, height).data;
    const image = new RawImage(pixelData, width, height, 4);
    const inputs = await processor(image);
    const { outputs } = await model(inputs);

    const sizes = inputs.reshaped_input_sizes[0].reverse();
    const rawDetections = outputs.tolist();

    // ✅ --- THE FIX: Filter detections by the confidence threshold first --- ✅
    // The score is the 5th element in each detection array (index 4).
    const visibleDetections = rawDetections.filter(detection => detection[4] >= threshold);

    // Now, base ALL logic on the filtered list of visible detections.
    if (visibleDetections.length === 0) {
      // No VISIBLE face was detected in this frame.
      noFaceFramesCount++;
      
      // If the counter exceeds our threshold, reset the state and clear the screen.
      if (noFaceFramesCount > NO_FACE_RESET_THRESHOLD) {
        console.log("Resetted")
        hasSent = false;
        overlay.innerHTML = ""; // Clear the lingering box now.
      }
      // If we are within the grace period, we do nothing, leaving the old box on screen.
    } else {
      // A visible face (or faces) was detected.
      noFaceFramesCount = 0; // Reset the counter.

      // Clear the overlay ONLY when we are about to draw new boxes.
      overlay.innerHTML = ""; 
      
      // Loop over the VISIBLE detections to draw them.
      visibleDetections.forEach((detection) => renderBox(detection, sizes));
    }

    if (previousTime !== undefined) {
      const fps = 1000 / (performance.now() - previousTime);
      status.textContent = `FPS: ${fps.toFixed(2)}`;
    }
    previousTime = performance.now();
    isProcessing = false;
  }

  window.requestAnimationFrame(updateCanvas);
}

// ... (The rest of your code, openFaceScanning and closeFaceScanning, remains the same)

async function openFaceScanning() {
  document.getElementById("faceScanModal").classList.add("active");
  if (attendanceStream) {
    attendanceStream.getTracks().forEach(track => track.stop());
    attendanceStream = null;
  }
  try {
    attendanceStream = await navigator.mediaDevices.getUserMedia({
      video: { width: 640, height: 480, facingMode: "user" },
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
  document.getElementById("faceScanModal").classList.remove("active");
  if (attendanceStream) {
    attendanceStream.getTracks().forEach((track) => track.stop());
    attendanceStream = null;
  }
  
  hasSent = false;
  noFaceFramesCount = 0;
  overlay.innerHTML = "";
  document.getElementById("scanResult").style.display = "none";
  document.getElementById("scanBtn").style.display = "block";
  document.getElementById("markPresentBtn").style.display = "none";
}

window.openFaceScanning = openFaceScanning;
window.closeFaceScanning = closeFaceScanning;