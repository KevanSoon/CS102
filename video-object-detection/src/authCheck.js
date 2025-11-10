// Auth Check Utility
// Import this at the top of protected pages (student.html, professor.html)
// to ensure user is authenticated

import authService from './auth.js';

// Check if user is authenticated
if (!authService.isAuthenticated()) {
    alert('Please log in to access this page');
    window.location.href = '/';
}

// Check if email is verified
if (!authService.isEmailVerified()) {
    alert('Please verify your email before accessing this page. Check your inbox for the verification email.');
    authService.clearAuth();
    window.location.href = '/';
}

// Get user info
const currentUser = authService.getUser();
const userRole = authService.getUserRole();

console.log('Current user:', currentUser);
console.log('User role:', userRole);

// Function to display user info in header
export function displayUserInfo() {
    const userName = currentUser?.userMetadata?.name || currentUser?.email || 'User';
    return {
        name: userName,
        email: currentUser?.email,
        role: userRole
    };
}

// Function to logout
export async function logout() {
    await authService.signOut();
    window.location.href = '/';
}

// Export auth service for making API calls
export { authService };
