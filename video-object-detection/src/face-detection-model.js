console.log("face-detection-model loaded");

import { AutoModel, AutoProcessor, RawImage } from "@huggingface/transformers";
import { Client } from "@gradio/client";

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
let attendanceStream = null

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



async function openFaceScanning() {


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
//   faceScanned = false
  document.getElementById("scanResult").style.display = "none"
  document.getElementById("scanBtn").style.display = "block"
  document.getElementById("markPresentBtn").style.display = "none"
}


window.openFaceScanning = openFaceScanning;
window.closeFaceScanning = closeFaceScanning;




