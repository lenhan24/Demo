/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 *
 * @author GiGaByte
 */
public class Account {
    private int accountId;
    private String fullName;
    private String email;
    private String passwordHash;
    private String phone;
    private int failedLoginCount;
   private LocalDateTime lockedUntil;
  private String role;
    // Các hàm Getter và Setter
    public int getAccountId() { return accountId; }
    public void setAccountId(int accountId) { this.accountId = accountId; }
    
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    
    public int getFailedLoginCount() { return failedLoginCount; }
    public void setFailedLoginCount(int failedLoginCount) { this.failedLoginCount = failedLoginCount; }

    public String getPhone() {
        return phone;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public LocalDateTime getLockedUntil() { return lockedUntil; }
    public void setLockedUntil(LocalDateTime lockedUntil) { this.lockedUntil = lockedUntil; }

    @Override
    public String toString() {
        return "Account{" + "accountId=" + accountId + ", fullName=" + fullName + ", email=" + email + ", passwordHash=" + passwordHash + ", phone=" + phone + ", failedLoginCount=" + failedLoginCount + ", lockedUntil=" + lockedUntil + '}';
    }
    
    
}
