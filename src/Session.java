package src;

import java.time.LocalDateTime;

public class Session {
    private final String username;
    private final String role;
    private LocalDateTime lastActive;

    public Session(String username, String role) {
        this.username = username;
        this.role = role;
        renew();
    }

    public void renew() {
        this.lastActive = LocalDateTime.now();
    }

    public boolean isValid() {
        return lastActive.plusMinutes(1).isAfter(LocalDateTime.now());
    }

    public String getUsername() { return username; }
    public String getRole() { return role; }
}