package models;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public abstract class User {
    private int userId;
    private String username;
    private String email;
    private String passwordHash;
    private String role;

    public static final String ROLE_STUDENT = "STUDENT";
    public static final String ROLE_INSTRUCTOR = "INSTRUCTOR";
    public static final String ROLE_ADMIN = "ADMIN";

    // existing constructor: raw password (will be hashed)
    public User(int userId, String username, String email, String rawPassword, String role) {
        this.userId = validateUserId(userId);
        this.username = requireNonEmpty("username", username);
        this.email = validateEmail(email);
        this.passwordHash = hashPassword(requireNonEmpty("password", rawPassword));
        this.role = validateRole(role);
    }

    // NEW: constructor when you already have hashed password (isAlreadyHashed = true)
    public User(int userId, String username, String email, String passwordOrHash, String role, boolean isAlreadyHashed) {
        this.userId = validateUserId(userId);
        this.username = requireNonEmpty("username", username);
        this.email = validateEmail(email);
        if (isAlreadyHashed) {
            this.passwordHash = requireNonEmpty("passwordHash", passwordOrHash);
        } else {
            this.passwordHash = hashPassword(requireNonEmpty("password", passwordOrHash));
        }
        this.role = validateRole(role);
    }

    // ... keep validation and hashing methods as you had
    private int validateUserId(int id) {
        if (id <= 0) throw new IllegalArgumentException("userId must be a positive integer.");
        return id;
    }

    private String validateEmail(String email) {
        email = requireNonEmpty("email", email);
        if (!email.matches("^[^@\\s]+@[^@\\s]+\\.com$")) {
            throw new IllegalArgumentException("Invalid email format. Email must be of form string@string.com");
        }
        return email;
    }

    private String validateRole(String role) {
        role = requireNonEmpty("role", role).toUpperCase();
        if (!role.equals(ROLE_STUDENT) && !role.equals(ROLE_INSTRUCTOR) && !role.equals(ROLE_ADMIN)) {
            throw new IllegalArgumentException("Role must be STUDENT, INSTRUCTOR or ADMIN.");
        }
        return role;
    }

    private String requireNonEmpty(String field, String value) {
        if (value == null || value.trim().isEmpty()) throw new IllegalArgumentException(field + " cannot be empty.");
        return value.trim();
    }

    private String hashPassword(String rawPassword) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(rawPassword.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found");
        }
    }

    // getters / setters...
    public int getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public String getRole() { return role; }

    public void setUsername(String username) { this.username = requireNonEmpty("username", username); }
    public void setEmail(String email) { this.email = validateEmail(email); }
    public void setRawPassword(String rawPassword) { this.passwordHash = hashPassword(requireNonEmpty("password", rawPassword)); }
    public void setRole(String role) { this.role = validateRole(role); }
}
