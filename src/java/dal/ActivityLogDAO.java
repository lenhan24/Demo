package dal;

import dal.DBContext;
import model.ActivityLog;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for dbo.tbActivityLog (see SQLBookyMart.sql).
 * tbActivityLog only stores (log_id, account_id, action, created_at); the actor's
 * name/role shown in the UI come from a JOIN against dbo.tbAccount.
 *
 * Extends DBContext (see dal.DBContext): the parent constructor opens a fresh
 * `connection` field that both methods below reuse directly.
 */
public class ActivityLogDAO extends DBContext {

    public static final int PAGE_SIZE = 10;

    /**
     * MỚI: ghi 1 dòng log hành động — TRƯỚC ĐÂY bảng/trang đã có sẵn nhưng
     * không có nơi nào insert vào cả, nên "Nhật ký hoạt động" luôn luôn trống.
     * actorAccountId = tài khoản admin/manager ĐANG THỰC HIỆN hành động (không
     * phải tài khoản bị tác động) — action nên mô tả rõ đối tượng bị tác động
     * trong chuỗi text, vì bảng chỉ có 1 cột account_id.
     */
    public void insertLog(int actorAccountId, String action) {
        String sql = "INSERT INTO dbo.tbActivityLog (account_id, action) VALUES (?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, actorAccountId);
            ps.setString(2, action);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int countAll() {
        String sql = "SELECT COUNT(*) FROM dbo.tbActivityLog";
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public List<ActivityLog> getPage(int page) {
        List<ActivityLog> list = new ArrayList<>();
        int offset = (page - 1) * PAGE_SIZE;
        String sql = "SELECT l.log_id, COALESCE(a.full_name, N'(Tài khoản đã xóa)') AS full_name, " +
                     "COALESCE(a.role, '-') AS role, l.action, l.created_at " +
                     "FROM dbo.tbActivityLog l LEFT JOIN dbo.tbAccount a ON l.account_id = a.account_id " +
                     "ORDER BY l.created_at DESC " +
                     "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, offset);
            ps.setInt(2, PAGE_SIZE);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new ActivityLog(
                        rs.getInt("log_id"),
                        rs.getString("full_name"),
                        rs.getString("role"),
                        rs.getString("action"),
                        rs.getTimestamp("created_at")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
