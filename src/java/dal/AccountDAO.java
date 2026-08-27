/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dal;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import model.Account;
import java.sql.Timestamp;


public class AccountDAO extends DBContext {
    
    // Hàm kiểm tra email đã tồn tại chưa
    public boolean checkEmailExist(String email) {
        String query = "SELECT COUNT(*) FROM tbAccount WHERE email = ?";
        
        // Sử dụng trực tiếp biến 'connection' được thừa kế từ DBContext
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.out.println("Lỗi checkEmailExist: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    // Hàm tạo tài khoản mới
    public boolean registerAccount(String fullName, String email, String passwordHash) {
        // Role mặc định là 'CUSTOMER', Status là 'ACTIVE', email_verified là 0 (chưa xác minh)
        String query = "INSERT INTO tbAccount (full_name, email, password_hash, role, status, email_verified) " +
                       "VALUES (?, ?, ?, 'CUSTOMER', 'ACTIVE', 0)";
        
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            
            ps.setString(1, fullName);
            ps.setString(2, email);
            ps.setString(3, passwordHash);
            
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.out.println("Lỗi registerAccount: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    public Account getAccountByEmail(String email) {
        String query = "SELECT account_id, full_name, email, password_hash,phone ,failed_login_count, locked_until FROM tbAccount WHERE email = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Account acc = new Account();
                    acc.setAccountId(rs.getInt("account_id"));
                    acc.setFullName(rs.getString("full_name"));
                    acc.setEmail(rs.getString("email"));
                    acc.setPasswordHash(rs.getString("password_hash"));
                    acc.setPhone(rs.getString("phone"));
                    acc.setFailedLoginCount(rs.getInt("failed_login_count"));
                    Timestamp ts = rs.getTimestamp("locked_until");
                   acc.setLockedUntil(ts != null ? ts.toLocalDateTime() : null);
                    return acc;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    // Hàm đổi mật khẩu
    public boolean changePassword(String email, String newPasswordHash) {
        String query = "UPDATE tbAccount SET password_hash = ? WHERE email = ?";
        
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            
            // Set tham số cho câu lệnh SQL
            ps.setString(1, newPasswordHash);
            ps.setString(2, email);
            
            // Thực thi và kiểm tra số dòng bị ảnh hưởng
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0; // Trả về true nếu update thành công ít nhất 1 dòng
            
        } catch (SQLException e) {
            System.out.println("Lỗi changePassword: " + e.getMessage());
            e.printStackTrace();
        }
        return false; // Trả về false nếu có lỗi xảy ra
    }
     /**
      * Kiểm tra đăng nhập — TRƯỚC ĐÂY so sánh password_hash = ? ngay trong SQL
      * bằng chuỗi thô (plain text), nghĩa là mật khẩu chưa từng được băm dù
      * tên cột là password_hash. Giờ chỉ tìm account theo email (+ status),
      * rồi verify mật khẩu bằng PasswordUtil ở tầng Java — an toàn hơn nhiều
      * vì DB không còn lưu/so sánh mật khẩu dạng đọc được.
      */
     public Account getAccountByEmail(String email,String password) {
        String query = "SELECT account_id, full_name, email, password_hash,phone, failed_login_count, locked_until FROM tbAccount WHERE email = ? and status = 'ACTIVE'";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("password_hash");
                    if (!utilities.PasswordUtil.verifyPassword(password, storedHash)) {
                        return null; // sai mật khẩu
                    }
                    Account acc = new Account();
                    acc.setAccountId(rs.getInt("account_id"));
                    acc.setFullName(rs.getString("full_name"));
                    acc.setEmail(rs.getString("email"));
                    acc.setPasswordHash(rs.getString("password_hash"));
                    acc.setPhone(rs.getString("phone"));
                    acc.setFailedLoginCount(rs.getInt("failed_login_count"));
                    Timestamp ts = rs.getTimestamp("locked_until");
                   acc.setLockedUntil(ts != null ? ts.toLocalDateTime() : null);
                    return acc;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    public String getRoleByAccount(Account account) {
        // Kiểm tra đầu vào hợp lệ
        if (account == null || account.getEmail() == null) {
            return null;
        }

        // Truy vấn cột role dựa trên email (hoặc bạn có thể dùng account_id)
        String query = "SELECT role FROM tbAccount WHERE email = ?";
        
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, account.getEmail());
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("role");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return null; // Trả về null nếu không tìm thấy hoặc có lỗi
    }
     public boolean updateAccountInfo(int accountId, String fullName, String phone) {
        String sql = "UPDATE tbAccount SET full_name = ?, phone = ? WHERE account_id = ?";
        
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            
            ps.setString(1, fullName);
            ps.setString(2, phone);
            ps.setInt(3, accountId);
            
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0; // Trả về true nếu cập nhật thành công ít nhất 1 dòng
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
}
