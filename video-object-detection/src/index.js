import authService from './auth.js';

let currentUser = 'student';
let faceStream = null;
let faceCaptured = false;
let capturedFaceImage = null; // Store captured face image blob
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
    capturedFaceImage = null;
    document.getElementById('captureResult').style.display = 'none';
    document.getElementById('captureBtn').style.display = 'block';
    document.getElementById('completeBtn').style.display = 'none';
}

window.showEmailVerificationModal = function(email) {
    const modal = document.getElementById('emailVerificationModal');
    modal.classList.add('active');

    // Store email for resend functionality
    window.verificationEmail = email;
}

window.closeEmailVerificationModal = function() {
    const modal = document.getElementById('emailVerificationModal');
    modal.classList.remove('active');

    // Clear auth since they can't proceed without verification
    authService.clearAuth();
}

window.resendVerificationEmail = async function() {
    const btn = document.getElementById('resendVerificationBtn');
    const email = window.verificationEmail;
    const messageDiv = document.getElementById('verificationMessage');

    if (!email) {
        alert('Email not found. Please try logging in again.');
        return;
    }

    btn.disabled = true;
    btn.textContent = 'Sending...';
    
    // Clear previous messages
    if (messageDiv) {
        messageDiv.textContent = '';
        messageDiv.style.color = '';
    }

    try {
        const result = await authService.resendVerificationEmail(email);

        if (result.success) {
            if (messageDiv) {
                messageDiv.textContent = 'Verification email sent! Please check your inbox.';
                messageDiv.style.color = '#22c55e';
            }
            alert('Verification email sent! Please check your inbox.');
        } else {
            throw new Error(result.error);
        }
    } catch (error) {
        const errorMessage = error.message || 'Failed to send verification email. Please try again later.';
        
        if (messageDiv) {
            messageDiv.textContent = errorMessage;
            messageDiv.style.color = '#ef4444';
        }
        
        alert(errorMessage);
    } finally {
        btn.disabled = false;
        btn.textContent = 'Resend Verification Email';
    }
}

window.captureFace = async function() {
    const video = document.getElementById('faceVideo');
    const canvas = document.getElementById('faceCanvas');
    const ctx = canvas.getContext('2d');

    canvas.width = video.videoWidth;
    canvas.height = video.videoHeight;
    ctx.drawImage(video, 0, 0);

    // Convert canvas to blob
    try {
        capturedFaceImage = await new Promise((resolve, reject) => {
            canvas.toBlob((blob) => {
                if (blob) {
                    resolve(blob);
                } else {
                    reject(new Error('Failed to capture image'));
                }
            }, 'image/jpeg', 0.9); // Use JPEG with 90% quality
        });

        // Simulate face detection processing
        setTimeout(() => {
            faceCaptured = true;
            document.getElementById('captureResult').style.display = 'block';
            document.getElementById('captureBtn').style.display = 'none';
            document.getElementById('completeBtn').style.display = 'block';
        }, 1000);
    } catch (error) {
        console.error('Error capturing face:', error);
        alert('Failed to capture face image. Please try again.');
    }
}

window.completeRegistration = async function() {
    const submitBtn = document.getElementById('completeBtn');
    submitBtn.disabled = true;
    submitBtn.textContent = 'Registering...';

    try {
        // Check if face was captured
        if (!faceCaptured || !capturedFaceImage) {
            alert('Please capture your face first before completing registration.');
            submitBtn.disabled = false;
            submitBtn.textContent = 'Complete Registration';
            return;
        }

        // Perform actual registration
        const result = await authService.signUp(
            registerData.email,
            registerData.password,
            registerData.role,
            registerData.name,
            registerData.additionalData || {}
        );

        if (result.success) {
            console.log('=== REGISTRATION SUCCESS ===');
            console.log('Full result object:', result);
            console.log('Result.data:', result.data);
            
            // Get student ID from the response - try multiple sources
            // Note: With email verification, user object might be null
            let userId = result.data?.user?.id;
            console.log('userId from result.data.user.id:', userId);
            
            // If not in response, try to get from localStorage (saved by authService)
            if (!userId) {
                const savedUser = authService.getUser();
                console.log('Checking localStorage for user:', savedUser);
                userId = savedUser?.id;
                console.log('userId from localStorage:', userId);
            }
            
            // Log the actual response for debugging
            console.log('Registration response:', result.data);
            console.log('User from localStorage:', authService.getUser());
            
            if (!userId) {
                // If still no userId, check if user object exists but id is in a different format
                const userObj = result.data?.user || authService.getUser();
                console.log('Checking userObj for alternative ID fields:', userObj);
                if (userObj) {
                    userId = userObj.id || userObj.user_id || userObj.sub;
                    console.log('userId after checking alternative fields:', userId);
                }
            }
            
            console.log('Final userId before upload check:', userId);
            console.log('faceCaptured:', faceCaptured);
            console.log('capturedFaceImage exists:', !!capturedFaceImage);
            
            // Upload face image if we have userId, otherwise store it for later
            if (userId) {
                console.log('=== FACE IMAGE UPLOAD ===');
                console.log('Using user ID:', userId);
                console.log('Face image blob:', capturedFaceImage);
                console.log('Face image size:', capturedFaceImage?.size, 'bytes');
                console.log('Face image type:', capturedFaceImage?.type);
                
                try {
                    submitBtn.textContent = 'Uploading face data...';
                    
                    const formData = new FormData();
                    formData.append('image', capturedFaceImage, 'face.jpg');
                    formData.append('studentId', userId);

                    console.log('Sending upload request to: http://localhost:8080/api/face_data/upload');
                    
                    const uploadResponse = await fetch('http://localhost:8080/api/face_data/upload', {
                        method: 'POST',
                        body: formData
                    });

                    console.log('Upload response status:', uploadResponse.status);
                    console.log('Upload response ok:', uploadResponse.ok);

                    if (!uploadResponse.ok) {
                        const errorText = await uploadResponse.text();
                        console.error('Upload error response:', errorText);
                        let errorData;
                        try {
                            errorData = JSON.parse(errorText);
                        } catch (e) {
                            errorData = { error: errorText };
                        }
                        throw new Error(errorData.error || 'Failed to upload face image');
                    }

                    const uploadResult = await uploadResponse.json();
                    console.log('Face data uploaded successfully:', uploadResult);
                    console.log('Face data record:', uploadResult.faceData);
                    console.log('Image URL:', uploadResult.imageUrl);
                    
                    // Clear stored face image if upload succeeds
                    capturedFaceImage = null;
                    
                } catch (uploadError) {
                    console.error('Error uploading face data:', uploadError);
                    console.error('Error details:', uploadError.message);
                    console.error('Error stack:', uploadError.stack);
                    
                    // Store face image for upload on first login
                    try {
                        const faceImageDataUrl = await new Promise(resolve => {
                            const reader = new FileReader();
                            reader.onload = () => resolve(reader.result);
                            reader.readAsDataURL(capturedFaceImage);
                        });
                        sessionStorage.setItem('pendingFaceImage', faceImageDataUrl);
                        console.log('Stored face image for upload on first login');
                        alert('Registration successful! Your face data will be saved when you verify your email and log in for the first time.');
                    } catch (storageError) {
                        console.error('Error storing face image in sessionStorage:', storageError);
                    }
                }
            } else {
                // User ID not available (email verification required)
                // Store face image as data URL in sessionStorage for upload on first login
                console.warn('User ID not available in signup response. Storing face image for upload on first login.');
                
                try {
                    const faceImageDataUrl = await new Promise(resolve => {
                        const reader = new FileReader();
                        reader.onload = () => resolve(reader.result);
                        reader.readAsDataURL(capturedFaceImage);
                    });
                    sessionStorage.setItem('pendingFaceImage', faceImageDataUrl);
                    console.log('Face image stored for upload on first login');
                } catch (storageError) {
                    console.error('Error storing face image:', storageError);
                }
            }

            window.closeFaceRegister();

            // Check if email is verified
            if (!authService.isEmailVerified()) {
                showEmailVerificationModal(registerData.email);
            } else {
                // If email is already verified (shouldn't happen normally), redirect
                alert('Registration completed successfully! Redirecting...');
                if (registerData.role === 'student') {
                    window.location.href = '/student.html';
                } else {
                    window.location.href = '/professor.html';
                }
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
            // Check if email is verified
            if (!authService.isEmailVerified()) {
                showEmailVerificationModal(registerData.email);
                submitBtn.disabled = false;
                submitBtn.textContent = 'Register';
            } else {
                // If email is already verified (shouldn't happen normally), redirect
                alert('Registration completed successfully! Redirecting...');
                window.location.href = '/professor.html';
            }
        } else {
            throw new Error(result.error);
        }
    } catch (error) {
        alert('Registration failed: ' + error.message);
        submitBtn.disabled = false;
        submitBtn.textContent = 'Register';
    }
}

// Helper function to upload pending face image if exists
async function uploadPendingFaceImage(userId) {
    console.log('=== CHECKING PENDING FACE IMAGE ===');
    console.log('userId:', userId);
    
    const pendingFaceImageDataUrl = sessionStorage.getItem('pendingFaceImage');
    console.log('pendingFaceImageDataUrl exists:', !!pendingFaceImageDataUrl);
    
    if (!pendingFaceImageDataUrl || !userId) {
        console.log('Skipping pending upload - missing dataUrl or userId');
        return;
    }

    try {
        console.log('=== UPLOADING PENDING FACE IMAGE ===');
        console.log('Uploading pending face image for user:', userId);
        
        // Convert data URL to blob
        const response = await fetch(pendingFaceImageDataUrl);
        const blob = await response.blob();
        console.log('Converted blob size:', blob.size, 'bytes');
        console.log('Blob type:', blob.type);
        
        const formData = new FormData();
        formData.append('image', blob, 'face.jpg');
        formData.append('studentId', userId);

        console.log('Sending upload request to: http://localhost:8080/api/face_data/upload');
        
        const uploadResponse = await fetch('http://localhost:8080/api/face_data/upload', {
            method: 'POST',
            body: formData
        });

        console.log('Upload response status:', uploadResponse.status);
        console.log('Upload response ok:', uploadResponse.ok);

        if (uploadResponse.ok) {
            const uploadResult = await uploadResponse.json();
            console.log('Pending face data uploaded successfully:', uploadResult);
            sessionStorage.removeItem('pendingFaceImage');
            return true;
        } else {
            const errorText = await uploadResponse.text();
            console.error('Failed to upload pending face image. Response:', errorText);
            let errorData;
            try {
                errorData = JSON.parse(errorText);
            } catch (e) {
                errorData = { error: errorText };
            }
            console.error('Error details:', errorData);
            return false;
        }
    } catch (error) {
        console.error('Error uploading pending face image:', error);
        console.error('Error stack:', error.stack);
        return false;
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

            // Check if email is verified
            if (!authService.isEmailVerified()) {
                console.log('Email not verified');
                showEmailVerificationModal(email);
                submitBtn.disabled = false;
                submitBtn.textContent = 'Login';
                return;
            }

            const user = authService.getUser();
            const userId = user?.id;
            const role = authService.getUserRole();
            console.log('Extracted role:', role);

            // Upload pending face image if exists (for users who registered with email verification)
            if (userId && role === 'student') {
                await uploadPendingFaceImage(userId);
            }

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
        submitBtn.textContent = 'Login';
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
    const role = "student";
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