// Authentication Service
// Handles all API calls to the backend auth endpoints

const API_BASE_URL = 'http://localhost:8080/api';

class AuthService {
    constructor() {
        this.token = this.getToken();
        this.user = this.getUser();
    }

    // Get token from localStorage
    getToken() {
        return localStorage.getItem('access_token');
    }

    // Get user from localStorage
    getUser() {
        const userStr = localStorage.getItem('user');
        return userStr ? JSON.parse(userStr) : null;
    }

    // Save token and user to localStorage
    saveAuth(authResponse) {
        // Backend returns snake_case field names (access_token, refresh_token)
        const accessToken = authResponse.access_token || authResponse.accessToken;
        const refreshToken = authResponse.refresh_token || authResponse.refreshToken;

        console.log('Saving auth - access_token:', accessToken ? 'present' : 'MISSING');
        console.log('Saving auth - refresh_token:', refreshToken ? 'present' : 'MISSING');
        console.log('Saving auth - user:', authResponse.user ? 'present' : 'MISSING');

        localStorage.setItem('access_token', accessToken);
        localStorage.setItem('refresh_token', refreshToken);
        localStorage.setItem('user', JSON.stringify(authResponse.user));
        this.token = accessToken;
        this.user = authResponse.user;
    }

    // Clear auth data
    clearAuth() {
        localStorage.removeItem('access_token');
        localStorage.removeItem('refresh_token');
        localStorage.removeItem('user');
        this.token = null;
        this.user = null;
    }

    // Check if user is authenticated
    isAuthenticated() {
        return !!this.token;
    }

    // Get user role
    getUserRole() {
        // Try different possible field names for user metadata
        const metadata = this.user?.userMetadata || this.user?.user_metadata;
        const role = metadata?.role;

        console.log('Getting user role. User:', this.user);
        console.log('Metadata:', metadata);
        console.log('Role:', role);

        return role || null;
    }

    // Sign up a new user
    async signUp(email, password, role, name, additionalData = {}) {
        try {
            const requestBody = {
                email,
                password,
                userMetadata: {
                    role,
                    name
                },
                name
            };

            // Add student-specific fields
            if (role === 'student') {
                Object.assign(requestBody, {
                    code: additionalData.code,
                    phone: additionalData.phone || '',
                });
            }

            console.log('Signup request:', requestBody);

            const response = await fetch(`${API_BASE_URL}/auth/signup`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(requestBody)
            });

            const data = await response.json();

            if (!response.ok) {
                throw new Error(data.error || 'Sign up failed');
            }

            // Save auth data
            this.saveAuth(data);

            return { success: true, data };
        } catch (error) {
            console.error('Sign up error:', error);
            return { success: false, error: error.message };
        }
    }

    // Sign in
    async signIn(email, password) {
        try {
            const response = await fetch(`${API_BASE_URL}/auth/signin`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ email, password })
            });

            const data = await response.json();

            if (!response.ok) {
                throw new Error(data.error || 'Sign in failed');
            }

            // Save auth data
            this.saveAuth(data);

            return { success: true, data };
        } catch (error) {
            console.error('Sign in error:', error);
            return { success: false, error: error.message };
        }
    }

    // Sign out
    async signOut() {
        try {
            if (this.token) {
                await fetch(`${API_BASE_URL}/auth/signout`, {
                    method: 'POST',
                    headers: {
                        'Authorization': `Bearer ${this.token}`
                    }
                });
            }
        } catch (error) {
            console.error('Sign out error:', error);
        } finally {
            this.clearAuth();
        }
    }

    // Get current user info from server
    async getCurrentUser() {
        try {
            if (!this.token) {
                throw new Error('No auth token');
            }

            const response = await fetch(`${API_BASE_URL}/auth/user`, {
                headers: {
                    'Authorization': `Bearer ${this.token}`
                }
            });

            if (!response.ok) {
                throw new Error('Failed to get user');
            }

            const user = await response.json();
            localStorage.setItem('user', JSON.stringify(user));
            this.user = user;

            return { success: true, user };
        } catch (error) {
            console.error('Get user error:', error);
            return { success: false, error: error.message };
        }
    }

    // Resend verification email
    async resendVerificationEmail(email) {
        try {
            const response = await fetch(`${API_BASE_URL}/auth/resend-verification`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ email })
            });

            const data = await response.json();

            if (!response.ok) {
                throw new Error(data.error || 'Failed to resend verification email');
            }

            return { success: true, message: data.message };
        } catch (error) {
            console.error('Resend verification error:', error);
            return { success: false, error: error.message };
        }
    }

    // Check if user email is verified
    isEmailVerified() {
        return this.user?.email_confirmed_at != null;
    }

    // Make authenticated API request
    async apiRequest(endpoint, options = {}) {
        const headers = {
            'Content-Type': 'application/json',
            ...options.headers
        };

        if (this.token) {
            headers['Authorization'] = `Bearer ${this.token}`;
            console.log(`API request to ${endpoint} - token: present (${this.token.substring(0, 20)}...)`);
        } else {
            console.warn(`API request to ${endpoint} - token: MISSING`);
        }

        const response = await fetch(`${API_BASE_URL}${endpoint}`, {
            ...options,
            headers
        });

        if (response.status === 401) {
            // Token expired or invalid
            this.clearAuth();
            window.location.href = '/';
            throw new Error('Authentication expired');
        }

        return response;
    }

    // Update user email
    async updateEmail(newEmail) {
        try {
            if (!this.token) {
                throw new Error('No auth token');
            }

            const response = await fetch(`${API_BASE_URL}/auth/update-email`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${this.token}`
                },
                body: JSON.stringify({ email: newEmail })
            });

            const data = await response.json();

            if (!response.ok) {
                throw new Error(data.error || 'Failed to update email');
            }

            // Update stored user data
            this.user = data;
            localStorage.setItem('user', JSON.stringify(data));

            return { success: true, user: data };
        } catch (error) {
            console.error('Update email error:', error);
            return { success: false, error: error.message };
        }
    }

    // Update user password
    async updatePassword(newPassword) {
        try {
            if (!this.token) {
                throw new Error('No auth token');
            }

            const response = await fetch(`${API_BASE_URL}/auth/update-password`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${this.token}`
                },
                body: JSON.stringify({ password: newPassword })
            });

            const data = await response.json();

            if (!response.ok) {
                throw new Error(data.error || 'Failed to update password');
            }

            // Update stored user data (password update returns updated user object)
            this.user = data;
            localStorage.setItem('user', JSON.stringify(data));

            return { success: true, user: data };
        } catch (error) {
            console.error('Update password error:', error);
            return { success: false, error: error.message };
        }
    }

    // Update user profile (email and/or password together)
    async updateProfile(email, password) {
        try {
            if (!this.token) {
                throw new Error('No auth token');
            }

            const requestBody = {};
            if (email) requestBody.email = email;
            if (password) requestBody.password = password;

            const response = await fetch(`${API_BASE_URL}/auth/update-profile`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${this.token}`
                },
                body: JSON.stringify(requestBody)
            });

            const data = await response.json();

            if (!response.ok) {
                throw new Error(data.error || 'Failed to update profile');
            }

            // Update stored user data
            this.user = data;
            localStorage.setItem('user', JSON.stringify(data));

            return { success: true, user: data };
        } catch (error) {
            console.error('Update profile error:', error);
            return { success: false, error: error.message };
        }
    }
}

// Create singleton instance
const authService = new AuthService();

export default authService;
