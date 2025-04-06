package src;

public class SessionManager {
    private static Session currentSession;

    public static void createSession(String username, String role) {
        currentSession = new Session(username, role);
    }

    // Modified to check first, then renew if valid
    public static boolean renewSession() {
        if (isValidSession()) {
            currentSession.renew();
            return true;
        }
        return false;
    }

    public static boolean isValidSession() {
        return currentSession != null && currentSession.isValid();
    }

    public static void logout() {
        currentSession = null;
    }

    public static String getCurrentUsername() {
        return isValidSession() ? currentSession.getUsername() : null;
    }

    public static String getCurrentRole() {
        return isValidSession() ? currentSession.getRole() : null;
    }
}