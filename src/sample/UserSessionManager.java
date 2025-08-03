package sample;

import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class UserSessionManager {
    private static UserSessionManager instance;
    private final Map<String, Integer> failedLoginAttempts;
    private final Map<String, LocalDateTime> lastFailedAttemptTime;
    private static final int MAX_ATTEMPTS = 3; // Example: Max allowed failed attempts
    private static final long LOCKOUT_DURATION_MINUTES = 5; // Example: Lockout duration

    private Stage primaryStage; // To store the primary stage if needed

    private UserSessionManager() {
        failedLoginAttempts = new HashMap<>();
        lastFailedAttemptTime = new HashMap<>();
    }

    public static synchronized UserSessionManager getInstance() {
        if (instance == null) {
            instance = new UserSessionManager();
        }
        return instance;
    }

    // Make sure this method exists and is public
    public boolean canAttemptLogin(String username) {
        if (failedLoginAttempts.containsKey(username)) {
            if (failedLoginAttempts.get(username) >= MAX_ATTEMPTS) {
                LocalDateTime lastAttempt = lastFailedAttemptTime.get(username);
                if (lastAttempt != null && LocalDateTime.now().isBefore(lastAttempt.plusMinutes(LOCKOUT_DURATION_MINUTES))) {
                    return false; // User is locked out
                } else {
                    // Lockout period expired, reset attempts
                    resetFailedAttempts(username);
                    return true;
                }
            }
        }
        return true; // No failed attempts or under max attempts
    }

    // Change this from private to public
    public void recordFailedLogin(String username) {
        failedLoginAttempts.put(username, failedLoginAttempts.getOrDefault(username, 0) + 1);
        lastFailedAttemptTime.put(username, LocalDateTime.now());
        System.out.println("Failed login attempt for: " + username + ". Attempts: " + failedLoginAttempts.get(username));
    }

    // Change this from private to public
    public void resetFailedAttempts(String username) {
        failedLoginAttempts.remove(username);
        lastFailedAttemptTime.remove(username);
        System.out.println("Failed attempts reset for: " + username);
    }

    // Make sure this method exists and is public
    public int getRemainingAttempts(String username) {
        int attempts = failedLoginAttempts.getOrDefault(username, 0);
        return MAX_ATTEMPTS - attempts;
    }

    // Assuming you have this method if you're setting a primary stage
    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }
}