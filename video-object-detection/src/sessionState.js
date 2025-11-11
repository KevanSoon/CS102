// Shared state for active session
let activeSessionId = null;
let loadActiveSessionsCallback = null;

export function setActiveSessionId(sessionId) {
    activeSessionId = sessionId;
    console.log('[SessionState] Active session set to:', sessionId);
}

export function getActiveSessionId() {
    return activeSessionId;
}

export function clearActiveSessionId() {
    activeSessionId = null;
    console.log('[SessionState] Active session cleared');
}

// Register the loadActiveSessions function from professor.js
export function registerLoadActiveSessions(callback) {
    loadActiveSessionsCallback = callback;
    console.log('[SessionState] loadActiveSessions callback registered');
}

// Call loadActiveSessions from other modules
export async function reloadActiveSessions() {
    if (loadActiveSessionsCallback) {
        console.log('[SessionState] Calling loadActiveSessions...');
        await loadActiveSessionsCallback();
    } else {
        console.warn('[SessionState] loadActiveSessions callback not registered yet');
    }
}
