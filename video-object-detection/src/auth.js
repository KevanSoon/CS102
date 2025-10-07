// Authentication utilities for other pages
class AuthManager {
    static getToken() {
        return localStorage.getItem('accessToken');
    }

    static getUserRole() {
        return localStorage.getItem('userRole');
    }

    static isAuthenticated() {
        return !!this.getToken();
    }

    static async makeAuthenticatedRequest(url, options = {}) {
        const token = this.getToken();
        if (!token) {
            throw new Error('No authentication token found');
        }

        const headers = {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`,
            ...options.headers
        };

        const response = await fetch(url, {
            ...options,
            headers
        });

        if (response.status === 401) {
            // Token expired or invalid
            this.logout();
            throw new Error('Authentication expired. Please login again.');
        }

        return response;
    }

    static logout() {
        localStorage.removeItem('accessToken');
        localStorage.removeItem('userRole');
        window.location.href = 'index.html';
    }

    static async checkAuth() {
        if (!this.isAuthenticated()) {
            window.location.href = 'index.html';
            return false;
        }

        try {
            const response = await this.makeAuthenticatedRequest('http://localhost:8080/api/auth/user');
            if (!response.ok) {
                this.logout();
                return false;
            }
            return true;
        } catch (error) {
            this.logout();
            return false;
        }
    }
}

// Auto-check authentication on page load
document.addEventListener('DOMContentLoaded', () => {
    if (window.location.pathname !== '/index.html' && window.location.pathname !== '/') {
        AuthManager.checkAuth();
    }
});
