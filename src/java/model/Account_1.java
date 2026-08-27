package model;

import java.sql.Timestamp;

public class Account_1 {
    private int accountId;
    private String fullName;
    private String email;
    private String phone;
    private String role;
    private String status;
    private Timestamp createdAt;
    private Timestamp lockedUntil;

    public Account_1() {}

    public Account_1(int accountId, String fullName, String email, String phone,
                    String role, String status, Timestamp createdAt, Timestamp lockedUntil) {
        this.accountId = accountId;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.status = status;
        this.createdAt = createdAt;
        this.lockedUntil = lockedUntil;
    }

    public int getAccountId() { return accountId; }
    public void setAccountId(int accountId) { this.accountId = accountId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getLockedUntil() { return lockedUntil; }
    public void setLockedUntil(Timestamp lockedUntil) { this.lockedUntil = lockedUntil; }
}
