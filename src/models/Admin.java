package models;

public class Admin extends User {
    public Admin(int userId, String username, String email, String rawPassword, boolean isAlreadyHashed) {
        super(userId, username, email, rawPassword, "ADMIN", isAlreadyHashed);
    }
    public Admin(int userId, String username, String email, String rawPassword) {
        super(userId, username, email, rawPassword, "ADMIN");
    }
    // no extra methods here for now — permission logic is enforced in manager
}


