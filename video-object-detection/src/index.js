import authService from './auth.js';

let currentUser = 'student';
let faceStream = null;
let faceCaptured = false;
let registerData = {}; // Store registration data

// Check if already logged in
if (authService.isAuthenticated()) {
    const role = authService.getUserRole();
    if (role === 'student') {
        window.location.href = '/student.html';
    } else if (role === 'professor') {
        window.location.href = '/professor.html';
    }
}

// Make functions globally available
window.switchTab = function(userType) {
    currentUser = userType;
    const tabs = document.querySelectorAll('.tab');
    tabs.forEach(tab => tab.classList.remove('active'));
    event.target.classList.add('active');
}

window.showRegisterForm = function() {
    document.getElementById('loginForm').style.display = 'none';
    document.getElementById('registerForm').style.display = 'block';
    const loginTabs = document.querySelector('.login-tabs');
    if (loginTabs) loginTabs.style.display = 'none';
}

window.showLoginForm = function() {
    document.getElementById('registerForm').style.display = 'none';
    document.getElementById('loginForm').style.display = 'block';
    const loginTabs = document.querySelector('.login-tabs');
    if (loginTabs) loginTabs.style.display = 'flex';
}

// Toggle student fields based on role selection
document.addEventListener('DOMContentLoaded', function() {
    const roleSelect = document.getElementById('registerRole');
    const studentFields = document.getElementById('studentFields');

    if (roleSelect && studentFields) {
        // Show student fields by default since student is the default option
        if (roleSelect.value === 'student') {
            studentFields.style.display = 'block';
        }

        roleSelect.addEventListener('change', function() {
            if (this.value === 'student') {
                studentFields.style.display = 'block';
            } else {
                studentFields.style.display = 'none';
            }
        });
    }
});

window.showFaceRegister = async function() {
    document.getElementById('faceRegisterModal').classList.add('active');

    try {
        faceStream = await navigator.mediaDevices.getUserMedia({
            video: {
                width: 640,
                height: 480,
                facingMode: 'user'
            }
        });
        document.getElementById('faceVideo').srcObject = faceStream;
    } catch (error) {
        console.error('Error accessing camera:', error);
        alert('Unable to access camera. Please ensure camera permissions are granted.');
    }
}

window.closeFaceRegister = function() {
    document.getElementById('faceRegisterModal').classList.remove('active');
    if (faceStream) {
        faceStream.getTracks().forEach(track => track.stop());
        faceStream = null;
    }
    // Reset capture state
    faceCaptured = false;
    document.getElementById('captureResult').style.display = 'none';
    document.getElementById('captureBtn').style.display = 'block';
    document.getElementById('completeBtn').style.display = 'none';
}

window.captureFace = function() {
    const video = document.getElementById('faceVideo');
    const canvas = document.getElementById('faceCanvas');
    const ctx = canvas.getContext('2d');

    canvas.width = video.videoWidth;
    canvas.height = video.videoHeight;
    ctx.drawImage(video, 0, 0);

    // Simulate face detection processing
    setTimeout(() => {
        faceCaptured = true;
        document.getElementById('captureResult').style.display = 'block';
        document.getElementById('captureBtn').style.display = 'none';
        document.getElementById('completeBtn').style.display = 'block';
    }, 1000);
}

window.completeRegistration = async function() {
    const submitBtn = document.getElementById('completeBtn');
    submitBtn.disabled = true;
    submitBtn.textContent = 'Registering...';

    try {
        // Perform actual registration
        const result = await authService.signUp(
            registerData.email,
            registerData.password,
            registerData.role,
            registerData.name,
            registerData.additionalData || {}
        );

        if (result.success) {
            alert('Registration completed successfully! Redirecting...');
            window.closeFaceRegister();

            // Redirect based on role
            if (registerData.role === 'student') {
                window.location.href = '/student.html';
            } else {
                window.location.href = '/professor.html';
            }
        } else {
            throw new Error(result.error);
        }
    } catch (error) {
        alert('Registration failed: ' + error.message);
        submitBtn.disabled = false;
        submitBtn.textContent = 'Complete Registration';
    }
}

window.completeProfessorRegistration = async function() {
    const submitBtn = document.querySelector('#registerForm button[type="submit"]');
    submitBtn.disabled = true;
    submitBtn.textContent = 'Registering...';

    try {
        // Perform actual registration without face scanning
        const result = await authService.signUp(
            registerData.email,
            registerData.password,
            registerData.role,
            registerData.name,
            registerData.additionalData || {}
        );

        if (result.success) {
            alert('Registration completed successfully! Redirecting...');
            window.location.href = '/professor.html';
        } else {
            throw new Error(result.error);
        }
    } catch (error) {
        alert('Registration failed: ' + error.message);
        submitBtn.disabled = false;
        submitBtn.textContent = 'Register';
    }
}

document.getElementById('loginForm').addEventListener('submit', async function(e) {
    e.preventDefault();

    const submitBtn = this.querySelector('button[type="submit"]');
    const email = document.getElementById('loginEmail').value;
    const password = document.getElementById('loginPassword').value;

    submitBtn.disabled = true;
    submitBtn.textContent = 'Signing in...';

    try {
        const result = await authService.signIn(email, password);

        if (result.success) {
            console.log('Sign in response:', result.data);
            console.log('User object:', result.data.user);
            console.log('User metadata:', result.data.user?.user_metadata);

            const role = authService.getUserRole();
            console.log('Extracted role:', role);

            if (role === 'student') {
                window.location.href = '/student.html';
            } else if (role === 'professor') {
                window.location.href = '/professor.html';
            } else {
                console.error('Role not found. User object:', authService.getUser());
                throw new Error('Unknown user role. Please check your registration.');
            }
        } else {
            throw new Error(result.error || 'Login failed');
        }
    } catch (error) {
        console.error('Login error:', error);
        alert('Login failed: ' + error.message);
        submitBtn.disabled = false;
        submitBtn.textContent = 'Sign In';
    }
});

document.getElementById('registerForm').addEventListener('submit', function(e) {
    e.preventDefault();

    const password = document.getElementById('registerPassword').value;
    const confirmPassword = document.getElementById('confirmPassword').value;
    const errorDiv = document.getElementById('passwordError');

    if (password !== confirmPassword) {
        errorDiv.style.display = 'block';
        return;
    }

    errorDiv.style.display = 'none';

    // Collect registration data
    const role = document.getElementById('registerRole').value;
    const name = document.getElementById('registerName').value;
    const email = document.getElementById('registerEmail').value;

    registerData = {
        email,
        password,
        role,
        name,
        additionalData: {}
    };

    // Add student-specific fields if role is student
    if (role === 'student') {
        registerData.additionalData = {
            code: document.getElementById('registerCode')?.value || `STU${Date.now()}`,
            phone: document.getElementById('registerPhone')?.value || '',
            class_name: document.getElementById('registerClass')?.value || '',
            student_group: document.getElementById('registerGroup')?.value || ''
        };
    }

    sessionStorage.setItem('registeringRole', role);

    // Only show face registration for students, skip for professors
    if (role === 'student') {
        showFaceRegister();
    } else if (role === 'professor') {
        // For professors, complete registration directly without face scanning
        completeProfessorRegistration();
    }
});