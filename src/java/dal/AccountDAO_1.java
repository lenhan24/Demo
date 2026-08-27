package dal;

import dal.DBContext;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import model.Account_1;

/**
 * DAO for dbo.tbAccount (see SQLBookyMart.sql).
 * Columns are snake_case; role is one of CUSTOMER/STAFF/MANAGER/ADMIN and
 * status is one of ACTIVE/LOCKED/INACTIVE (both enforced by CHECK constraints
 * in the DB — always pass/compare these in uppercase).
 *
 * Extends DBContext (see dal.DBContext) instead of managing its own JDBC
 * connections: the parent constructor opens a fresh `connection` field, which
 * every method below reuses directly (only the PreparedStatement/ResultSet
 * per call are opened/closed locally — the connection itself is NOT closed
 * here, matching the DBContext template's design).
 */
public class AccountDAO_1 extends DBContext {

    public static final int PAGE_SIZE = 10;

    /** Total number of accounts, used to compute total pages for pagination buttons. */
    public int countAll() {
        return countAll(null, null);
    }

    /** Total number of accounts matching the optional search/role filters. */
    public int countAll(String search, String role) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM dbo.tbAccount WHERE 1=1");
        appendFilters(sql, search, role);
        try (PreparedStatement ps = connection.prepareStatement(sql.toString())) {
            bindFilters(ps, 1, search, role);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Fetch one page of accounts using SQL Server's OFFSET/FETCH.
     * page is 1-based.
     */
    public List<Account_1> getPage(int page) {
        return getPage(page, null, null);
    }

    /**
     * Fetch one page of accounts, optionally filtered by a search term (matched
     * against full_name/email) and/or role. search/role may be null or blank
     * to skip that filter. page is 1-based.
     */
    public List<Account_1> getPage(int page, String search, String role) {
        List<Account_1> list = new ArrayList<>();
        int offset = (page - 1) * PAGE_SIZE;
        StringBuilder sql = new StringBuilder(
            "SELECT account_id, full_name, email, phone, role, status, created_at, locked_until " +
            "FROM dbo.tbAccount WHERE 1=1");
        appendFilters(sql, search, role);
        sql.append(" ORDER BY account_id OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");
        try (PreparedStatement ps = connection.prepareStatement(sql.toString())) {
            int idx = bindFilters(ps, 1, search, role);
            ps.setInt(idx++, offset);
            ps.setInt(idx, PAGE_SIZE);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Account_1(
                        rs.getInt("account_id"),
                        rs.getString("full_name"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        rs.getString("role"),
                        rs.getString("status"),
                        rs.getTimestamp("created_at"),
                        rs.getTimestamp("locked_until")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private void appendFilters(StringBuilder sql, String search, String role) {
        if (search != null && !search.isBlank()) {
            sql.append(" AND (full_name LIKE ? OR email LIKE ?)");
        }
        if (role != null && !role.isBlank()) {
            sql.append(" AND role = ?");
        }
    }

    private int bindFilters(PreparedStatement ps, int startIdx, String search, String role) throws SQLException {
        int idx = startIdx;
        if (search != null && !search.isBlank()) {
            String like = "%" + search.trim() + "%";
            ps.setString(idx++, like);
            ps.setString(idx++, like);
        }
        if (role != null && !role.isBlank()) {
            ps.setString(idx++, role);
        }
        return idx;
    }

    /** True if an account with this email already exists (used to reject duplicates on create). */
    public boolean emailExists(String email) {
        String sql = "SELECT 1 FROM dbo.tbAccount WHERE email = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Create a new Staff/Manager account (used by the "Thêm tài khoản" modal on admin-accounts.jsp).
     *
     * tbAccount.password_hash is NOT NULL, so a temporary password is generated here,
     * hashed, and stored. The PLAINTEXT temp password is returned (not stored anywhere)
     * so the caller can show it to the admin once, to hand off to the new staff member,
     * who should be required to change it on first login.
     *
     * NOTE: this hashes with SHA-256 as a placeholder. If your customer-facing registration
     * flow (in the main BookyMart project) hashes passwords differently (e.g. BCrypt), swap
     * the hashing in {@link #hashPassword(String)} to match, so login works consistently
     * across both flows.
     *
     * @return the generated temporary plaintext password on success, or null on failure.
     */
    public String createAccount(String fullName, String email, String phone, String role) {
        String tempPassword = generateTempPassword();
        String passwordHash = hashPassword(tempPassword);

        String sql = "INSERT INTO dbo.tbAccount (full_name, email, password_hash, role, status, phone) " +
                     "VALUES (?, ?, ?, ?, 'ACTIVE', ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, fullName);
            ps.setString(2, email);
            ps.setString(3, passwordHash);
            ps.setString(4, role);
            if (phone == null || phone.isEmpty()) {
                ps.setNull(5, Types.VARCHAR);
            } else {
                ps.setString(5, phone);
            }
            int rows = ps.executeUpdate();

            if (rows > 0) {
                int newId = 0;
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) newId = keys.getInt(1);
                }
                String log = "INSERT INTO dbo.tbActivityLog (account_id, action) VALUES (?, ?)";
                try (PreparedStatement logPs = connection.prepareStatement(log)) {
                    logPs.setInt(1, newId);
                    logPs.setString(2, "Tạo tài khoản " + role + ": " + fullName);
                    logPs.executeUpdate();
                }
                return tempPassword;
            }
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /** True if a DIFFERENT account already uses this email (used when editing, to allow keeping one's own email). */
    public boolean emailExistsExcluding(String email, int excludeAccountId) {
        String sql = "SELECT 1 FROM dbo.tbAccount WHERE email = ? AND account_id <> ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setInt(2, excludeAccountId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Fetch a single account by id (used to prefill the edit modal on error/reload). */
    public Account_1 getById(int accountId) {
        String sql = "SELECT account_id, full_name, email, phone, role, status, created_at, locked_until " +
                     "FROM dbo.tbAccount WHERE account_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Account_1(
                        rs.getInt("account_id"),
                        rs.getString("full_name"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        rs.getString("role"),
                        rs.getString("status"),
                        rs.getTimestamp("created_at"),
                        rs.getTimestamp("locked_until")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Update an existing account's editable profile fields (used by the "Sửa" /
     * pencil-icon modal on admin-accounts.jsp). Does not touch password_hash or status.
     */
    /**
     * Update an existing account's editable profile fields, including status
     * (used by the "Sửa" / pencil-icon modal on admin-accounts.jsp). Does not
     * touch password_hash.
     *
     * status must be one of ACTIVE / INACTIVE / LOCKED (matches the DB CHECK
     * constraint). lockedUntil is only meaningful when status is LOCKED — pass
     * null for ACTIVE/INACTIVE, or when the lock has no fixed expiry.
     */
    public boolean updateAccount(int accountId, String fullName, String email, String phone,
                                  String role, String status, Timestamp lockedUntil) {
        String sql = "UPDATE dbo.tbAccount SET full_name = ?, email = ?, phone = ?, role = ?, " +
                     "status = ?, locked_until = ? WHERE account_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, fullName);
            ps.setString(2, email);
            if (phone == null || phone.isEmpty()) {
                ps.setNull(3, Types.VARCHAR);
            } else {
                ps.setString(3, phone);
            }
            ps.setString(4, role);
            ps.setString(5, status);
            if ("LOCKED".equals(status) && lockedUntil != null) {
                ps.setTimestamp(6, lockedUntil);
            } else {
                ps.setNull(6, Types.TIMESTAMP);
            }
            ps.setInt(7, accountId);
            int rows = ps.executeUpdate();

            if (rows > 0) {
                String log = "INSERT INTO dbo.tbActivityLog (account_id, action) VALUES (?, ?)";
                try (PreparedStatement logPs = connection.prepareStatement(log)) {
                    logPs.setInt(1, accountId);
                    String desc = "Cập nhật thông tin tài khoản: " + fullName + " — trạng thái: " + status;
                    if ("LOCKED".equals(status) && lockedUntil != null) {
                        desc += " (đến " + lockedUntil + ")";
                    }
                    logPs.setString(2, desc);
                    logPs.executeUpdate();
                }
            }
            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Permanently delete an account from dbo.tbAccount (used by the trash-icon button).
     *
     * tbActivityLog rows referencing this account are removed first (in the same
     * transaction) since they have no ON DELETE CASCADE and would otherwise block
     * the delete. Other tables that reference tbAccount (tbOrder, tbReview,
     * tbImportReceipt, tbReturnRequest, etc.) also have NO cascade — if this account
     * placed any orders, wrote any reviews, etc., SQL Server will reject the delete
     * with a REFERENCE constraint (FK) error, which is caught below and turned into
     * a clear message rather than a raw stack trace.
     *
     * NOTE: this method toggles connection.setAutoCommit()/commit()/rollback() directly
     * on the shared `connection` field (inherited from DBContext) instead of opening and
     * closing its own Connection, since the connection is owned by this DAO instance for
     * its whole lifetime, not per-call.
     *
     * @return null on success, or a user-facing error message on failure.
     */
    /**
     * Permanently delete an account from dbo.tbAccount (used by the trash-icon button).
     *
     * A "deleted" entry is written to tbActivityLog first, while the account
     * still exists (so the FK insert succeeds). tbActivityLog.account_id must be
     * nullable with ON DELETE SET NULL (one-time ALTER TABLE — see chat) so that
     * once the account is deleted, all of its log rows — including this new one —
     * survive with account_id becoming NULL, instead of being deleted or blocking
     * the delete.
     *
     * Other tables that reference tbAccount (tbOrder, tbReview, tbImportReceipt,
     * tbReturnRequest, etc.) still have NO cascade — if this account placed any
     * orders, wrote any reviews, etc., SQL Server will reject the delete with a
     * REFERENCE constraint (FK) error, caught below and turned into a clear message.
     *
     * NOTE: this method toggles connection.setAutoCommit()/commit()/rollback() directly
     * on the shared `connection` field (inherited from DBContext) instead of opening and
     * closing its own Connection, since the connection is owned by this DAO instance for
     * its whole lifetime, not per-call.
     *
     * @return null on success, or a user-facing error message on failure.
     */
    public String deleteAccount(int accountId) {
        String logSql = "INSERT INTO dbo.tbActivityLog (account_id, action) VALUES (?, ?)";
        String deleteAccountSql = "DELETE FROM dbo.tbAccount WHERE account_id = ?";

        Account_1 target = getById(accountId);
        String label = target != null ? target.getFullName() + " (" + target.getEmail() + ")" : "#" + accountId;

        try {
            connection.setAutoCommit(false);
            try {
                try (PreparedStatement ps = connection.prepareStatement(logSql)) {
                    ps.setInt(1, accountId);
                    ps.setString(2, "Tài khoản đã bị xóa: " + label);
                    ps.executeUpdate();
                }
                int rows;
                try (PreparedStatement ps = connection.prepareStatement(deleteAccountSql)) {
                    ps.setInt(1, accountId);
                    rows = ps.executeUpdate();
                }
                connection.commit();
                return rows > 0 ? null : "Không tìm thấy tài khoản để xóa.";
            } catch (SQLException e) {
                connection.rollback();
                // SQL Server error 547 = REFERENCE constraint conflict (FK from
                // tbOrder/tbReview/tbImportReceipt/etc. still pointing at this account).
                if (e.getErrorCode() == 547) {
                    return "Không thể xóa: tài khoản này còn dữ liệu liên quan (đơn hàng, đánh giá, phiếu nhập...). " +
                           "Hãy vô hiệu hóa tài khoản thay vì xóa hẳn.";
                }
                e.printStackTrace();
                return "Không thể xóa tài khoản. Vui lòng thử lại.";
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "Lỗi kết nối cơ sở dữ liệu.";
        }
    }

    /** Update the role of a single account (used by admin-roles.jsp). newRole must be uppercase. */
    public boolean updateRole(int accountId, String newRole) {
        String sql = "UPDATE dbo.tbAccount SET role = ? WHERE account_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, newRole);
            ps.setInt(2, accountId);
            int rows = ps.executeUpdate();

            if (rows > 0) {
                String log = "INSERT INTO dbo.tbActivityLog (account_id, action) VALUES (?, ?)";
                try (PreparedStatement logPs = connection.prepareStatement(log)) {
                    logPs.setInt(1, accountId);
                    logPs.setString(2, "Đổi vai trò thành " + newRole);
                    logPs.executeUpdate();
                }
            }
            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private String generateTempPassword() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(10);
        for (int i = 0; i < 10; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    /**
     * TRƯỚC ĐÂY hàm này tự băm bằng SHA-256 không muối (không salt) — khác
     * hoàn toàn định dạng với utilities.PasswordUtil (dùng cho đăng ký/đăng
     * nhập thông thường). Hậu quả: tài khoản Admin tạo ra sẽ KHÔNG đăng nhập
     * được, vì AccountDAO.getAccountByEmail() verify theo định dạng của
     * PasswordUtil, không parse được chuỗi hex SHA-256 thô. Giờ dùng chung
     * đúng 1 chuẩn hash trong toàn bộ hệ thống.
     */
    private String hashPassword(String plaintext) {
        return utilities.PasswordUtil.hashPassword(plaintext);
    }
}
