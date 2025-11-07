// Shared state for active session
let activeSessionId = null;

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
