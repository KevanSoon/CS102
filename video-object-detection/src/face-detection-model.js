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

// --- NEW: Cooldown State Variables ---
let cooldownActive = false;
let cooldownCounter = 0;
const COOLDOWN_DURATION_FRAMES = 1000; // Approx. 1.5 seconds on a 60fps system

function setStreamSize(width, height) {
  video.width = canvas.width = Math.round(width);
  video.height = canvas.height = Math.round(height);
}

status.textContent = "Loading model...";

const model_id = "Xenova/gelan-c_all";
const model = await AutoModel.from_pretrained(model_id);
const processor = await AutoProcessor.from_pretrained(model_id);

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
      if (!blob) return reject("Failed to get Blob from canvas");
      const file = new File([blob], "detected.png", { type: "image/png" });
      try {
        const formData = new FormData();
        formData.append("image", file);
        const response = await fetch("http://localhost:8080/api/face-batch/face-compare-all", { method: "POST", body: formData });
        if (!response.ok) return reject(`Failed to send image: ${response.statusText}`);
        const result = await response.json();
        resolve(result);
      } catch (err) {
        reject(err);
      }
    }, "image/png");
  });
}

let currentBoxElement = null;
let currentLabelElement = null;
let hasSent = false;
let color = "yellow";
let text = "Verifying...";

function renderBox([xmin, ymin, xmax, ymax, score, id], [w, h]) {
    if (!hasSent) {
        color = "yellow";
        text = "Verifying...";
    }
    let boxElement = document.createElement("div");
    boxElement.className = "bounding-box";
    Object.assign(boxElement.style, { borderColor: color, left: `${(100 * xmin) / w}%`, top: `${(100 * ymin) / h}%`, width: `${(100 * (xmax - xmin)) / w}%`, height: `${(100 * (ymax - ymin)) / h}%` });
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
        sendCanvasImageToAPI(canvas).then(response => {
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
        }).catch(err => {
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

// --- NEW: Helper function to display messages on the overlay ---
function displayMessage(message) {
    overlay.innerHTML = ""; // Clear any previous content
    const messageElement = document.createElement("div");
    messageElement.className = "cooldown-message";
    messageElement.textContent = message;
    overlay.appendChild(messageElement);
}

let isProcessing = false;
let previousTime;
const context = canvas.getContext("2d", { willReadFrequently: true });
let noFaceFramesCount = 0;
const NO_FACE_RESET_THRESHOLD = 5;

async function updateCanvas() {
    const { width, height } = canvas;
    context.drawImage(video, 0, 0, width, height);
    if (!isProcessing) {
        isProcessing = true;

        // --- MODIFIED LOGIC: Handle Cooldown State First ---
        if (cooldownActive) {
            displayMessage("Ready for next scan...");
            cooldownCounter++;
            if (cooldownCounter > COOLDOWN_DURATION_FRAMES) {
                cooldownActive = false;
                cooldownCounter = 0;
                overlay.innerHTML = ""; // Clear the message
            }
        } else {
            // --- Original detection logic runs only if not in cooldown ---
            const pixelData = context.getImageData(0, 0, width, height).data;
            const image = new RawImage(pixelData, width, height, 4);
            const inputs = await processor(image);
            const { outputs } = await model(inputs);
            const sizes = inputs.reshaped_input_sizes[0].reverse();
            const rawDetections = outputs.tolist();
            const visibleDetections = rawDetections.filter(detection => detection[4] >= threshold);

            if (visibleDetections.length === 0) {
                noFaceFramesCount++;
                if (noFaceFramesCount > NO_FACE_RESET_THRESHOLD) {
                    // --- Instead of just resetting, we now TRIGGER THE COOLDOWN ---
                    hasSent = false;
                    cooldownActive = true; // Activate the cooldown period
                    overlay.innerHTML = ""; // Clear the last "Verified" box
                }
            } else {
                noFaceFramesCount = 0;
                overlay.innerHTML = "";
                visibleDetections.forEach(detection => renderBox(detection, sizes));
            }
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


async function openFaceScanning() {
  document.getElementById("faceScanModal").classList.add("active");
  if (attendanceStream) {
    attendanceStream.getTracks().forEach(track => track.stop());
    attendanceStream = null;
  }
  try {
    attendanceStream = await navigator.mediaDevices.getUserMedia({ video: { width: 640, height: 480, facingMode: "user" } });
    video.srcObject = attendanceStream;
    video.play();
    const videoTrack = attendanceStream.getVideoTracks()[0];
    const { width, height } = videoTrack.getSettings();
    setStreamSize(width * scale, height * scale);
    const ar = width / height;
    const [cw, ch] = ar > 720 / 405 ? [720, 720 / ar] : [405 * ar, 405];
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
    attendanceStream.getTracks().forEach(track => track.stop());
    attendanceStream = null;
  }
  
  // --- Reset ALL state variables on close ---
  hasSent = false;
  noFaceFramesCount = 0;
  cooldownActive = false;
  cooldownCounter = 0;
  overlay.innerHTML = "";
  // ... reset other elements
}

window.openFaceScanning = openFaceScanning;
window.closeFaceScanning = closeFaceScanning;

// You may want to add some basic styling for the message
const style = document.createElement('style');
style.innerHTML = `
.cooldown-message {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    background-color: rgba(0, 0, 0, 0.6);
    color: white;
    padding: 10px 20px;
    border-radius: 8px;
    font-size: 1.2em;
    text-align: center;
}`;
document.head.appendChild(style);