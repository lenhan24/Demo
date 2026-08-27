package dal;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import model.ReviewDTO;

/**
 * DAO cho tbReview — đánh giá sản phẩm kiểu "Verified Purchase": chỉ được
 * review nếu order_detail đó có thật, thuộc về chính account, VÀ đơn hàng
 * đã ở trạng thái DELIVERED. order_detail_id UNIQUE trong tbReview nên
 * mỗi dòng hàng đã mua chỉ review được đúng 1 lần.
 */
public class ReviewDAO extends DBContext {

    /** Danh sách review đang hiển thị (status VISIBLE) của 1 sản phẩm, mới nhất trước. */
    public List<ReviewDTO> getVisibleReviewsByProduct(int productId) {
        List<ReviewDTO> list = new ArrayList<>();
        String sql = "SELECT r.review_id, r.product_id, r.account_id, r.order_detail_id, "
                   + "r.rating, r.comment, r.status, r.created_at, a.full_name "
                   + "FROM tbReview r "
                   + "JOIN tbAccount a ON a.account_id = r.account_id "
                   + "WHERE r.product_id = ? AND r.status = 'VISIBLE' "
                   + "ORDER BY r.created_at DESC";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ReviewDTO r = new ReviewDTO();
                    r.setReviewId(rs.getInt("review_id"));
                    r.setProductId(rs.getInt("product_id"));
                    r.setAccountId(rs.getInt("account_id"));
                    r.setOrderDetailId(rs.getInt("order_detail_id"));
                    r.setRating(rs.getInt("rating"));
                    r.setComment(rs.getString("comment"));
                    r.setStatus(rs.getString("status"));
                    Timestamp ts = rs.getTimestamp("created_at");
                    if (ts != null) r.setCreatedAt(ts.toLocalDateTime());
                    r.setReviewerName(rs.getString("full_name"));
                    list.add(r);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /** Điểm trung bình + tổng số review — đọc trực tiếp từ cache ở tbProduct. */
    public double getAverageRating(int productId) {
        String sql = "SELECT ISNULL(AVG(CAST(rating AS DECIMAL(3,2))), 0) AS rating_avg FROM tbReview WHERE product_id = ? AND status = 'VISIBLE'";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble("rating_avg");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int getRatingCount(int productId) {
         String sql = "SELECT COUNT(*) AS rating_count FROM tbReview WHERE product_id = ? AND status = 'VISIBLE'";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("rating_count");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    /** Số lượng review theo từng mức sao (1..5) — dùng vẽ thanh % phân bố đánh giá. */
    public Map<Integer, Integer> getRatingBreakdown(int productId) {
        Map<Integer, Integer> breakdown = new LinkedHashMap<>();
        for (int i = 5; i >= 1; i--) breakdown.put(i, 0);
        String sql = "SELECT rating, COUNT(*) AS cnt FROM tbReview "
                   + "WHERE product_id = ? AND status = 'VISIBLE' GROUP BY rating";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    breakdown.put(rs.getInt("rating"), rs.getInt("cnt"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return breakdown;
    }

    /**
     * Tìm 1 order_detail của account này, đúng product này, đơn đã DELIVERED,
     * và CHƯA từng được review — dùng để hiện nút "Viết đánh giá" + biết chính
     * xác order_detail_id nào sẽ gắn vào review mới. Trả về null nếu không có
     * (chưa mua / chưa giao / đã review hết các lần mua rồi).
     */
    public Integer getReviewableOrderDetailId(int accountId, int productId) {
        String sql = "SELECT TOP 1 od.order_detail_id "
                   + "FROM tbOrderDetail od "
                   + "JOIN tbOrder o ON o.order_id = od.order_id "
                   + "WHERE o.account_id = ? AND od.product_id = ? AND o.status = 'DELIVERED' "
                   + "AND NOT EXISTS (SELECT 1 FROM tbReview r WHERE r.order_detail_id = od.order_detail_id) "
                   + "ORDER BY o.created_at DESC";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, accountId);
            ps.setInt(2, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("order_detail_id");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Thêm review mới — TỰ kiểm tra lại điều kiện "verified purchase" ở tầng
     * DB (không chỉ tin vào việc nút "Viết đánh giá" có hiện ra hay không),
     * để chặn trường hợp gọi thẳng request giả mạo order_detail_id của người khác.
     *
     * @return null nếu thành công, ngược lại là thông điệp lỗi cụ thể.
     */
    public String insertReview(int accountId, int orderDetailId, int rating, String comment) {
        if (rating < 1 || rating > 5) {
            return "Số sao đánh giá không hợp lệ.";
        }

        try {
            connection.setAutoCommit(false);

            // Xác thực: order_detail này thuộc đơn DELIVERED của ĐÚNG account này
            int productId;
            String sqlCheck = "SELECT od.product_id FROM tbOrderDetail od "
                            + "JOIN tbOrder o ON o.order_id = od.order_id "
                            + "WHERE od.order_detail_id = ? AND o.account_id = ? AND o.status = 'DELIVERED'";
            try (PreparedStatement ps = connection.prepareStatement(sqlCheck)) {
                ps.setInt(1, orderDetailId);
                ps.setInt(2, accountId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        connection.rollback();
                        return "Bạn chỉ có thể đánh giá sản phẩm đã mua và nhận hàng thành công.";
                    }
                    productId = rs.getInt("product_id");
                }
            }

            String sqlInsert = "INSERT INTO tbReview (product_id, account_id, order_detail_id, rating, comment) "
                              + "VALUES (?,?,?,?,?)";
            try (PreparedStatement ps = connection.prepareStatement(sqlInsert)) {
                ps.setInt(1, productId);
                ps.setInt(2, accountId);
                ps.setInt(3, orderDetailId);
                ps.setInt(4, rating);
                ps.setString(5, comment);
                ps.executeUpdate();
            } catch (Exception dupEx) {
                // order_detail_id UNIQUE -> đã review rồi
                connection.rollback();
                return "Bạn đã đánh giá sản phẩm này cho đơn hàng này rồi.";
            }

        
            connection.commit();
            return null;
        } catch (Exception e) {
            try { connection.rollback(); } catch (Exception ex) { /* ignore */ }
            e.printStackTrace();
            return "Có lỗi xảy ra, vui lòng thử lại.";
        } finally {
            try { connection.setAutoCommit(true); } catch (Exception ex) { /* ignore */ }
        }
    }

  
}
