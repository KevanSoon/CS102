  let currentUser = 'student';
        let faceStream = null;
        let faceCaptured = false;

        function switchTab(userType) {
            currentUser = userType;
            const tabs = document.querySelectorAll('.tab');
            tabs.forEach(tab => tab.classList.remove('active'));
            event.target.classList.add('active');
        }

        function showRegisterForm() {
            document.getElementById('loginForm').style.display = 'none';
            document.getElementById('registerForm').style.display = 'block';
            document.querySelector('.login-tabs').style.display = 'none';
        }

        function showLoginForm() {
            document.getElementById('registerForm').style.display = 'none';
            document.getElementById('loginForm').style.display = 'block';
            document.querySelector('.login-tabs').style.display = 'flex';
        }

        async function showFaceRegister() {
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

        function closeFaceRegister() {
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

        function captureFace() {
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

        function completeRegistration() {
            alert('Registration completed successfully! You can now login.');
            closeFaceRegister();
            showLoginForm();
        }

        document.getElementById('loginForm').addEventListener('submit', function(e) {
            e.preventDefault();
            
            if (currentUser === 'student') {
                window.location.href = 'student.html';
            } else {
                window.location.href = 'professor.html';
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
            const selectedRole = document.getElementById('registerRole').value;
            sessionStorage.setItem('registeringRole', selectedRole);
            showFaceRegister();
        });