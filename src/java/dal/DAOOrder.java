/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dal;

import com.sun.jdi.connect.spi.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import model.OrderDTO;
import model.OrderDetailDTO;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author GiGaByte
 */
public class DAOOrder extends DBContext {

    public String generateOrderCode() {
        // VD: BM250702153045123 (đủ ngắn để nằm trong VARCHAR(20))
        return "BM" + new SimpleDateFormat("yyMMddHHmmss").format(new Date())
                + String.format("%03d", new Random().nextInt(1000));
    }

   public int createOrder(OrderDTO o) {
        String sql = "INSERT INTO tbOrder (order_code, account_id, status, receiver_name, receiver_phone, "
                + "shipping_address, discount_amount, shipping_fee, "
                + "payment_method, payment_status) VALUES (?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement stm = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stm.setString(1, o.getOrderCode());
            stm.setInt(2, o.getAccountId());
            stm.setString(3, o.getStatus());
            stm.setString(4, o.getReceiverName());
            stm.setString(5, o.getReceiverPhone());
            stm.setString(6, o.getShippingAddress());
            stm.setLong(7, o.getDiscountAmount());
            stm.setLong(8, o.getShippingFee());
            stm.setString(9, o.getPaymentMethod());
            stm.setString(10, o.getPaymentStatus());
            stm.executeUpdate();
            try (ResultSet keys = stm.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace(); // TRƯỚC ĐÂY nuốt lỗi im lặng — chính vì vậy lỗi 'order_date' không tồn tại đã ẩn mất suốt, không ai biết
        }
        return -1;
    }

   
     public void insertOrderDetail(OrderDetailDTO d) {
        String sql = "INSERT INTO tbOrderDetail (order_id, product_id, product_name_snapshot, "
                + "quantity, unit_price, cost_price_snapshot) VALUES (?,?,?,?,?,?)";
        try (PreparedStatement stm = connection.prepareStatement(sql)) {
            stm.setInt(1, d.getOrderId());
            stm.setInt(2, d.getProductId());
            stm.setString(3, d.getProductNameSnapshot());
            stm.setInt(4, d.getQuantity());
            stm.setLong(5, d.getUnitPrice());
            stm.setLong(6, d.getCostPriceSnapshot());
            stm.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace(); // TRƯỚC ĐÂY nuốt lỗi im lặng — chính vì vậy lỗi 'order_date' không tồn tại đã ẩn mất suốt, không ai biết
        }
    }

    // Dùng cho trang order-success: load lại đơn hàng vừa tạo
    public OrderDTO getOrderById(int orderId, int accountId) {
       String sql = "SELECT o.*, "
                + "(SELECT ISNULL(SUM(quantity * unit_price), 0) FROM tbOrderDetail WHERE order_id = o.order_id) AS subtotal, "
                + "((SELECT ISNULL(SUM(quantity * unit_price), 0) FROM tbOrderDetail WHERE order_id = o.order_id) - o.discount_amount + o.shipping_fee) AS total_amount, "
                + "a.email AS account_email FROM tbOrder o "
                + "JOIN tbAccount a ON a.account_id = o.account_id "
                + "WHERE o.order_id = ? AND o.account_id = ?";
        try (PreparedStatement stm = connection.prepareStatement(sql)) {
            stm.setInt(1, orderId);
            stm.setInt(2, accountId); // chống IDOR: chỉ xem được đơn của chính mình
            try (ResultSet rs = stm.executeQuery()) {
                if (rs.next()) {
                    OrderDTO o = new OrderDTO();
                    o.setOrderId(rs.getInt("order_id"));
                    o.setOrderCode(rs.getString("order_code"));
                    o.setAccountId(rs.getInt("account_id"));
                    o.setStatus(rs.getString("status"));
                    o.setReceiverName(rs.getString("receiver_name"));
                    o.setReceiverPhone(rs.getString("receiver_phone"));
                    o.setShippingAddress(rs.getString("shipping_address"));
                    o.setSubtotal(rs.getLong("subtotal"));
                    o.setDiscountAmount(rs.getLong("discount_amount"));
                    o.setShippingFee(rs.getLong("shipping_fee"));
                    o.setTotalAmount(rs.getLong("total_amount"));
                    o.setPaymentMethod(rs.getString("payment_method"));
                    o.setPaymentStatus(rs.getString("payment_status"));
                    o.setOrderDate(rs.getTimestamp("created_at"));
                    o.setCancelReason(rs.getString("cancel_reason"));
                    o.setReturnStatus(rs.getString("return_status"));
                    o.setAccountEmail(rs.getString("account_email"));
                    return o;
                }
            }
        } catch (Exception e) {
            e.printStackTrace(); // TRƯỚC ĐÂY nuốt lỗi im lặng — chính vì vậy lỗi 'order_date' không tồn tại đã ẩn mất suốt, không ai biết
        }
        return null;
    }

    public List<OrderDTO> getOrdersByAccountId(int accountId) {
        List<OrderDTO> list = new ArrayList<>();
       String sql = "SELECT *, "
                + "(SELECT ISNULL(SUM(quantity * unit_price), 0) FROM tbOrderDetail WHERE order_id = tbOrder.order_id) AS subtotal, "
                + "((SELECT ISNULL(SUM(quantity * unit_price), 0) FROM tbOrderDetail WHERE order_id = tbOrder.order_id) - discount_amount + shipping_fee) AS total_amount "
                + "FROM tbOrder WHERE account_id = ? ORDER BY created_at DESC";
        try (PreparedStatement stm = connection.prepareStatement(sql)) {
            stm.setInt(1, accountId);
            try (ResultSet rs = stm.executeQuery()) {
                while (rs.next()) {
                    OrderDTO o = new OrderDTO();
                    o.setOrderId(rs.getInt("order_id"));
                    o.setOrderCode(rs.getString("order_code"));
                    o.setAccountId(rs.getInt("account_id"));
                    o.setStatus(rs.getString("status"));
                    o.setReceiverName(rs.getString("receiver_name"));
                    o.setReceiverPhone(rs.getString("receiver_phone"));
                    o.setShippingAddress(rs.getString("shipping_address"));
                    o.setSubtotal(rs.getLong("subtotal"));
                    o.setDiscountAmount(rs.getLong("discount_amount"));
                    o.setShippingFee(rs.getLong("shipping_fee"));
                    o.setTotalAmount(rs.getLong("total_amount"));
                    o.setPaymentMethod(rs.getString("payment_method"));
                    o.setPaymentStatus(rs.getString("payment_status"));
                    o.setOrderDate(rs.getTimestamp("created_at"));
                    o.setReturnStatus(rs.getString("return_status"));
                    list.add(o);
                }
            }
        } catch (Exception e) {
            e.printStackTrace(); // TRƯỚC ĐÂY nuốt lỗi im lặng — chính vì vậy lỗi 'order_date' không tồn tại đã ẩn mất suốt, không ai biết
        }
        return list;
    }

    public List<OrderDetailDTO> getOrderDetails(int orderId) {
        List<OrderDetailDTO> list = new ArrayList<>();
       String sql = "SELECT *, (quantity * unit_price) AS line_total FROM tbOrderDetail WHERE order_id = ?";
        try (PreparedStatement stm = connection.prepareStatement(sql)) {
            stm.setInt(1, orderId);
            try (ResultSet rs = stm.executeQuery()) {
                while (rs.next()) {
                    OrderDetailDTO d = new OrderDetailDTO();
                    d.setOrderDetailId(rs.getInt("order_detail_id"));
                    d.setOrderId(rs.getInt("order_id"));
                    d.setProductId(rs.getInt("product_id"));
                    d.setProductNameSnapshot(rs.getString("product_name_snapshot"));
                    d.setQuantity(rs.getInt("quantity"));
                    d.setUnitPrice(rs.getLong("unit_price"));
                    d.setCostPriceSnapshot(rs.getLong("cost_price_snapshot"));
                    d.setLineTotal(rs.getLong("line_total"));
                    list.add(d);
                }
            }
        } catch (Exception e) {
            e.printStackTrace(); // TRƯỚC ĐÂY nuốt lỗi im lặng — chính vì vậy lỗi 'order_date' không tồn tại đã ẩn mất suốt, không ai biết
        }
        return list;
    }

    public Map<Integer, List<OrderDetailDTO>> getOrderDetailsForOrders(List<Integer> orderIds) {
        Map<Integer, List<OrderDetailDTO>> result = new HashMap<>();
        if (orderIds == null || orderIds.isEmpty()) {
            return result;
        }

        // Xây dựng chuỗi placeholder (?,?,?...) tương ứng số lượng orderIds
        StringBuilder placeholders = new StringBuilder();
        for (int i = 0; i < orderIds.size(); i++) {
            placeholders.append(i == 0 ? "?" : ",?");
        }

       String sql = "SELECT *, (quantity * unit_price) AS line_total FROM tbOrderDetail WHERE order_id IN (" + placeholders + ")";
        try (PreparedStatement stm = connection.prepareStatement(sql)) {
            for (int i = 0; i < orderIds.size(); i++) {
                stm.setInt(i + 1, orderIds.get(i));
            }
            try (ResultSet rs = stm.executeQuery()) {
                while (rs.next()) {
                    OrderDetailDTO d = new OrderDetailDTO();
                    d.setOrderDetailId(rs.getInt("order_detail_id"));
                    int orderId = rs.getInt("order_id");
                    d.setOrderId(orderId);
                    d.setProductId(rs.getInt("product_id"));
                    d.setProductNameSnapshot(rs.getString("product_name_snapshot"));
                    d.setQuantity(rs.getInt("quantity"));
                    d.setUnitPrice(rs.getLong("unit_price"));
                    d.setCostPriceSnapshot(rs.getLong("cost_price_snapshot"));
                    d.setLineTotal(rs.getLong("line_total"));

                    result.computeIfAbsent(orderId, k -> new ArrayList<>()).add(d);
                }
            }
        } catch (Exception e) {
            e.printStackTrace(); // TRƯỚC ĐÂY nuốt lỗi im lặng — chính vì vậy lỗi 'order_date' không tồn tại đã ẩn mất suốt, không ai biết
        }
        return result;
    }

    public int cancelOrder(int orderId, int accountId, String cancelReason) {
        try {
            connection.setAutoCommit(false);

            String paymentMethod = null;
            String paymentStatus = null;
            String sqlGetPay = "SELECT payment_method, payment_status FROM tbOrder WHERE order_id = ? AND account_id = ?";
            try (PreparedStatement psPay = connection.prepareStatement(sqlGetPay)) {
                psPay.setInt(1, orderId);
                psPay.setInt(2, accountId);
                try (ResultSet rsPay = psPay.executeQuery()) {
                    if (rsPay.next()) {
                        paymentMethod = rsPay.getString("payment_method");
                        paymentStatus = rsPay.getString("payment_status");
                    }
                }
            }
            boolean needsRefund = "ONLINE_QR".equals(paymentMethod) && "PAID".equals(paymentStatus);

            // Chỉ cho hủy khi status = 'PENDING' — KHÔNG còn cho hủy từ
            // 'PENDING_PAYMENT' nữa (đơn VietQR chưa được Staff xác nhận thanh toán).
            String sqlCancel = needsRefund
                    ? "UPDATE tbOrder SET status = 'CANCELLED', cancel_reason = ?, payment_status = 'REFUND_PENDING' "
                    + "WHERE order_id = ? AND account_id = ? AND status = 'PENDING'"
                    : "UPDATE tbOrder SET status = 'CANCELLED', cancel_reason = ? "
                    + "WHERE order_id = ? AND account_id = ? AND status = 'PENDING'";
            int updated;
            try (PreparedStatement stm = connection.prepareStatement(sqlCancel)) {
                stm.setString(1, cancelReason);
                stm.setInt(2, orderId);
                stm.setInt(3, accountId);
                updated = stm.executeUpdate();
            }

            if (updated == 0) {
                connection.rollback();
                return 0;
            }

            String sqlGetDetails = "SELECT product_id, quantity FROM tbOrderDetail WHERE order_id = ?";
            String sqlRestore = "UPDATE tbProduct SET stock_quantity = stock_quantity + ? "
                    + "WHERE product_id = ?";

            try (PreparedStatement getStm = connection.prepareStatement(sqlGetDetails)) {
                getStm.setInt(1, orderId);
                try (ResultSet rs = getStm.executeQuery(); PreparedStatement restoreStm = connection.prepareStatement(sqlRestore)) {
                    while (rs.next()) {
                        restoreStm.setInt(1, rs.getInt("quantity"));
                        restoreStm.setInt(2, rs.getInt("product_id"));
                        restoreStm.addBatch();
                    }
                    restoreStm.executeBatch();
                }
            }

            connection.commit();
            return needsRefund ? 2 : 1;

        } catch (Exception e) {
            try {
                connection.rollback();
            } catch (Exception ex) {
                /* ignore */ }
            e.printStackTrace();
            return -1;
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (Exception ex) {
                /* ignore */ }
        }
    }

    public int placeOrder(OrderDTO order, List<OrderDetailDTO> details, List<String> outOfStockItems,
            Integer voucherId, int accountIdForVoucher) {
        try {
            connection.setAutoCommit(false);

            // 1) Trừ kho thẳng vào tbProduct.stock_quantity, cost_price_snapshot lấy
            // từ tbProduct.unit_cost tại đúng thời điểm đặt hàng (không đổi về sau
            // dù Manager có sửa giá vốn) — chạy TRÊN CÙNG transaction với việc tạo
            // đơn hàng bên dưới để đảm bảo tính atomic.
            for (OrderDetailDTO d : details) {
                double unitCost = sellFromProductInTransaction(d.getProductId(), d.getQuantity());
                if (unitCost < 0) {
                    outOfStockItems.add(d.getProductNameSnapshot());
                } else {
                    d.setCostPriceSnapshot((long) unitCost);
                }
            }

            if (!outOfStockItems.isEmpty()) {
                connection.rollback();
                return -2;
            }

            // 2) Tạo order (có kèm voucher_id nếu đơn có dùng voucher)
           String sqlOrder = "INSERT INTO tbOrder (order_code, account_id, status, receiver_name, receiver_phone, "
                    + "shipping_address, discount_amount, shipping_fee, "
                    + "payment_method, payment_status, voucher_id) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
            int orderId = -1;
            try (PreparedStatement stm = connection.prepareStatement(sqlOrder, Statement.RETURN_GENERATED_KEYS)) {
                stm.setString(1, order.getOrderCode());
                stm.setInt(2, order.getAccountId());
                stm.setString(3, order.getStatus());
                stm.setString(4, order.getReceiverName());
                stm.setString(5, order.getReceiverPhone());
                stm.setString(6, order.getShippingAddress());
                stm.setLong(7, order.getDiscountAmount());
                stm.setLong(8, order.getShippingFee());
                stm.setString(9, order.getPaymentMethod());
                stm.setString(10, order.getPaymentStatus());
                if (voucherId != null) {
                    stm.setInt(11, voucherId);
                } else {
                    stm.setNull(11, java.sql.Types.INTEGER);
                }
                stm.executeUpdate();
                try (ResultSet keys = stm.getGeneratedKeys()) {
                    if (keys.next()) {
                        orderId = keys.getInt(1);
                    }
                }
            }
            if (orderId == -1) {
                connection.rollback();
                return -1;
            }

            // 2b) Ghi nhận lượt dùng voucher — TRƯỚC ĐÂY không có bước này ở bất kỳ
            // đâu trong code, khiến max_usage_limit/per_customer_limit không bao giờ
            // có tác dụng (voucher dùng lại được vô hạn lần). Giờ ghi ngay trong
            // cùng transaction với đơn hàng: đơn tạo thất bại thì lượt dùng cũng
            // không được tính (rollback chung).
            if (voucherId != null) {
                String sqlVoucherUsage = "INSERT INTO tbVoucherUsage (voucher_id, account_id, order_id) VALUES (?,?,?)";
                try (PreparedStatement stm = connection.prepareStatement(sqlVoucherUsage)) {
                    stm.setInt(1, voucherId);
                    stm.setInt(2, accountIdForVoucher);
                    stm.setInt(3, orderId);
                    stm.executeUpdate();
                }
            }

            // 3) Tạo order detail (batch) — cost_price_snapshot giờ là giá vốn THẬT
            // lấy từ lô FIFO (bước 1), không còn là bản sao của giá bán như trước.
           String sqlDetail = "INSERT INTO tbOrderDetail (order_id, product_id, product_name_snapshot, "
                    + "quantity, unit_price, cost_price_snapshot) VALUES (?,?,?,?,?,?)";
            try (PreparedStatement stm = connection.prepareStatement(sqlDetail)) {
                for (OrderDetailDTO d : details) {
                    d.setOrderId(orderId);
                    stm.setInt(1, d.getOrderId());
                    stm.setInt(2, d.getProductId());
                    stm.setString(3, d.getProductNameSnapshot());
                    stm.setInt(4, d.getQuantity());
                    stm.setLong(5, d.getUnitPrice());
                    stm.setLong(6, d.getCostPriceSnapshot());
                    stm.addBatch();
                }
                stm.executeBatch();
            }

            connection.commit();
            return orderId;

        } catch (Exception e) {
            try {
                connection.rollback();
            } catch (Exception ex) {
                /* ignore */ }
            e.printStackTrace();
            return -1;
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (Exception ex) {
                /* ignore */ }
        }
    }

    /**
     * Trừ kho THẲNG vào tbProduct.stock_quantity (thay cho hệ thống lô FIFO
     * tbSKU cũ đã bỏ) — chạy TRÊN CÙNG connection/transaction của placeOrder()
     * để đảm bảo tính atomic. Trả về unit_cost (>=0) nếu bán thành công, hoặc
     * -1 nếu không đủ hàng.
     *
     * SỬA: TRƯỚC ĐÂY đọc tồn kho bằng 1 câu SELECT riêng, so sánh ở code
     * Java, RỒI MỚI chạy UPDATE — có khe hở race condition (TOCTOU): nếu 2
     * khách đặt cùng 1 sản phẩm CÙNG LÚC, cả 2 request đều có thể đọc thấy
     * "đủ hàng" trước khi request kia kịp trừ kho, dẫn tới trừ vượt quá số
     * tồn kho thật (tồn kho âm). Giờ gộp điều kiện "đủ hàng" NGAY TRONG câu
     * UPDATE — SQL Server tự khóa dòng đang cập nhật, đảm bảo 2 giao dịch
     * cùng lúc không thể cùng đọc-rồi-cùng-trừ vào 1 dòng dữ liệu.
     */
    private double sellFromProductInTransaction(int productId, int quantity) throws Exception {
        String updateQuery = "UPDATE tbProduct SET stock_quantity = stock_quantity - ? "
                + "WHERE product_id = ? AND stock_quantity >= ?";
        int rowsUpdated;
        try (PreparedStatement ps = connection.prepareStatement(updateQuery)) {
            ps.setInt(1, quantity);
            ps.setInt(2, productId);
            ps.setInt(3, quantity);
            rowsUpdated = ps.executeUpdate();
        }

        if (rowsUpdated == 0) {
            return -1; // không tìm thấy sản phẩm, hoặc không đủ hàng để bán (điều kiện >= quantity không khớp)
        }

        Double unitCost = null;
        String findQuery = "SELECT unit_cost FROM tbProduct WHERE product_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(findQuery)) {
            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    unitCost = rs.getDouble("unit_cost");
                }
            }
        }

        return unitCost;
    }

    public int countOrdersByStatus(String status) {
        String sql = "SELECT COUNT(*) FROM tbOrder WHERE status = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
    /**
     * Cập nhật trạng thái đơn hàng — CÓ kiểm tra chuyển trạng thái hợp lệ
     * (state machine), và hoàn kho nếu chuyển sang CANCELLED.
     *
     * TRƯỚC ĐÂY: chỉ có "UPDATE tbOrder SET status = ? WHERE order_id = ?",
     * không kiểm tra trạng thái HIỆN TẠI có cho phép nhảy sang trạng thái MỚI
     * hay không — một đơn đã DELIVERED có thể bị set lại PROCESSING, một đơn đã
     * CANCELLED có thể bị "hồi sinh" thành SHIPPED. Khi Staff hủy đơn qua đây
     * (set CANCELLED), kho ĐƯỢC hoàn lại, giống hệt cancelOrder() (khách tự
     * hủy) — 2 đường tới cùng 1 trạng thái, cùng 1 kết quả, tránh lệch tồn kho
     * giữa 2 luồng.
     *
     * MỚI: thêm SHIPPED -> ON_HOLD (giao thất bại / không liên hệ được khách —
     * "bom hàng"). Từ ON_HOLD, Staff có thể giao lại (-> PROCESSING, không đụng
     * kho) hoặc hủy hẳn (-> CANCELLED, hoàn kho như bình thường).
     */
    private static final java.util.Map<String, java.util.Set<String>> ALLOWED_TRANSITIONS = java.util.Map.of(
            "PENDING_PAYMENT", java.util.Set.of("PENDING", "CANCELLED"),
            "PENDING", java.util.Set.of("PROCESSING", "ON_HOLD", "CANCELLED"),
            "PROCESSING", java.util.Set.of("SHIPPED", "ON_HOLD", "CANCELLED"),
            "ON_HOLD", java.util.Set.of("PROCESSING", "CANCELLED"),
            "SHIPPED", java.util.Set.of("DELIVERED", "ON_HOLD"),
            "DELIVERED", java.util.Set.of(), // trạng thái cuối, không cho chuyển tiếp
            "CANCELLED", java.util.Set.of() // trạng thái cuối, không cho chuyển tiếp
    );

    /**
     * @return null nếu thất bại (không tìm thấy đơn / chuyển trạng thái không
     * hợp lệ); "OK" nếu thành công bình thường; "PHONE_NOT_CONFIRMED" nếu bị
     * chặn giao hàng do chưa xác nhận điện thoại; "REFUND_NEEDED" nếu thành
     * công VÀ đây là đơn cần hoàn tiền thủ công (đã thanh toán online, hủy vì
     * lý do KHÔNG thuộc diện "không hoàn" — xem policy bên dưới).
     */
      public String updateOrderStatus(int orderId, String newStatus) {
        return updateOrderStatus(orderId, newStatus, null);
    }

    /**
     * @param reasonCode Lý do đi kèm — dùng cho 2 trường hợp:
     *   1. Chuyển SHIPPED -> ON_HOLD (giao thất bại): truyền "DELIVERY_FAILED"
     *      để lưu sẵn lý do ngay từ lúc này — vì đơn có thể bị hủy hẳn sau đó
     *      vài ngày, cần biết đơn có nguồn gốc "giao thất bại" để áp dụng
     *      đúng chính sách KHÔNG hoàn tiền lúc hủy hẳn.
     *   2. Có thể truyền null cho các trường hợp chuyển trạng thái thông
     *      thường khác (không cần ghi lý do).
     */
    public String updateOrderStatus(int orderId, String newStatus,String reasonCode) {
       try {
            connection.setAutoCommit(false);

            String currentStatus = null;
            String paymentMethod = null;
            String paymentStatus = null;
            String phoneConfirmStatus = null;
            String sqlGetStatus = "SELECT status, payment_method, payment_status, phone_confirm_status FROM tbOrder WHERE order_id = ?";
            try (PreparedStatement ps = connection.prepareStatement(sqlGetStatus)) {
                ps.setInt(1, orderId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        currentStatus = rs.getString("status");
                        paymentMethod = rs.getString("payment_method");
                        paymentStatus = rs.getString("payment_status");
                        phoneConfirmStatus = rs.getString("phone_confirm_status");
                    }
                }
            }

            if (currentStatus == null) {
                connection.rollback();
                return null; // không tìm thấy đơn hàng
            }

            java.util.Set<String> allowedNext = ALLOWED_TRANSITIONS.getOrDefault(currentStatus, java.util.Set.of());
            if (!allowedNext.contains(newStatus)) {
                connection.rollback();
                return null; // chuyển trạng thái không hợp lệ, chặn ngay tại đây
            }

            // Chặn giao hàng (PROCESSING -> SHIPPED) nếu Staff chưa gọi xác
            // nhận khách thành công — phòng "bom hàng".
            if ("PROCESSING".equals(currentStatus) && "SHIPPED".equals(newStatus)
                    && !"CONFIRMED".equals(phoneConfirmStatus)) {
                connection.rollback();
                return "PHONE_NOT_CONFIRMED";
            }

           // Đơn đã thanh toán online rồi mới bị huỷ -> cần hoàn tiền thủ công,
            // đánh dấu lại payment_status để Manager/Admin biết còn nợ hoàn tiền.
            // SỬA: TRƯỚC ĐÂY thiếu điều kiện newStatus == CANCELLED, khiến MỌI
            // lượt đổi trạng thái tiếp theo của 1 đơn ONLINE_QR đã PAID (kể cả
            // SHIPPED -> DELIVERED, giao thành công bình thường) đều bị gắn
            // nhầm payment_status = REFUND_PENDING.
            //
            // MỚI: đọc thêm cancel_reason ĐÃ LƯU SẴN (nếu đơn từng qua "Giao
            // thất bại" thì cancel_reason đã là DELIVERY_FAILED ngay từ lúc
            // SHIPPED -> ON_HOLD, không phải đợi lúc này mới có).
            String existingReason = null;
            try (PreparedStatement psReason = connection.prepareStatement(
                    "SELECT cancel_reason FROM tbOrder WHERE order_id = ?")) {
                psReason.setInt(1, orderId);
                try (ResultSet rsReason = psReason.executeQuery()) {
                    if (rsReason.next()) {
                        existingReason = rsReason.getString("cancel_reason");
                    }
                }
            }
            String effectiveReason = (reasonCode != null) ? reasonCode : existingReason;

            // Chính sách đã chốt: hủy do KHÔNG LIÊN HỆ ĐƯỢC hoặc GIAO THẤT BẠI
            // (lỗi thuộc về khách) -> KHÔNG hoàn tiền, dù đã thanh toán hay
            // đã ship hay chưa. Hủy vì lý do khác vẫn hoàn tiền như cũ.
            boolean isNoRefundReason = "AUTO_NO_CONTACT".equals(effectiveReason)
                    || "DELIVERY_FAILED".equals(effectiveReason);
            boolean needsRefund = "ONLINE_QR".equals(paymentMethod) && "PAID".equals(paymentStatus)
                    && "CANCELLED".equals(newStatus) && !isNoRefundReason;

            // MỚI (tích hợp VietQR thật — Hướng A): PENDING_PAYMENT -> PENDING
            // nghĩa là Staff vừa đối chiếu sao kê ngân hàng và xác nhận tiền
            // ĐÃ VỀ THẬT — đánh dấu luôn payment_status = PAID tại đây, vì đây
            // là nơi DUY NHẤT chuyển tiếp đơn Online QR ra khỏi PENDING_PAYMENT.
            boolean isPaymentConfirmation = "PENDING_PAYMENT".equals(currentStatus) && "PENDING".equals(newStatus);

           boolean shouldSaveReason = reasonCode != null;
            String sql = needsRefund
                    ? (shouldSaveReason
                        ? "UPDATE tbOrder SET status = ?, payment_status = 'REFUND_PENDING', cancel_reason = ? WHERE order_id = ?"
                        : "UPDATE tbOrder SET status = ?, payment_status = 'REFUND_PENDING' WHERE order_id = ?")
                    : isPaymentConfirmation
                            ? "UPDATE tbOrder SET status = ?, payment_status = 'PAID' WHERE order_id = ?"
                            : (shouldSaveReason
                                ? "UPDATE tbOrder SET status = ?, cancel_reason = ? WHERE order_id = ?"
                                : "UPDATE tbOrder SET status = ? WHERE order_id = ?");
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, newStatus);
                if (shouldSaveReason) {
                    ps.setString(2, reasonCode);
                    ps.setInt(3, orderId);
                } else {
                    ps.setInt(2, orderId);
                }
                if (ps.executeUpdate() == 0) {
                    connection.rollback();
                    return null;
                }
            }
            // Nếu Staff hủy đơn -> hoàn kho, giống hệt logic bên cancelOrder()
            // (khách tự hủy), tránh 2 đường dẫn cho cùng 1 kết quả khác nhau.
            if ("CANCELLED".equals(newStatus)) {
                String sqlGetDetails = "SELECT product_id, quantity FROM tbOrderDetail WHERE order_id = ?";
                String sqlRestore = "UPDATE tbProduct SET stock_quantity = stock_quantity + ? "
                        + "WHERE product_id = ?";

                try (PreparedStatement getStm = connection.prepareStatement(sqlGetDetails)) {
                    getStm.setInt(1, orderId);
                    try (ResultSet rs = getStm.executeQuery(); PreparedStatement restoreStm = connection.prepareStatement(sqlRestore)) {
                        while (rs.next()) {
                            restoreStm.setInt(1, rs.getInt("quantity"));
                            restoreStm.setInt(2, rs.getInt("product_id"));
                            restoreStm.addBatch();
                        }
                        restoreStm.executeBatch();
                    }
                }
            }

            connection.commit();
            return needsRefund ? "REFUND_NEEDED" : "OK";
        } catch (Exception e) {
            try {
                connection.rollback();
            } catch (Exception ex) {
                /* ignore */ }
            e.printStackTrace();
            return null;
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (Exception ex) {
                /* ignore */ }
        }
    }

    /**
     * Khách xác nhận đã nhận hàng — chuyển DELIVERED.
     *
     * TRƯỚC ĐÂY: chỉ đổi status, KHÔNG hề đụng tới payment_status. Với đơn COD,
     * tiền được thu MẶT lúc giao hàng — nhưng payment_status vẫn mãi là
     * 'UNPAID' vì không có bước nào ghi nhận việc thu tiền đó cả. Hậu quả: nếu
     * đơn COD này sau đó bị hủy/hoàn hàng, logic "cần hoàn tiền hay không" (dựa
     * vào payment_status = 'PAID') coi như đơn CHƯA hề thu tiền, nên không đẩy
     * lên Manager xác nhận hoàn tiền dù thực tế khách đã trả tiền mặt rồi. Giờ
     * đánh dấu PAID cho COD ngay khi xác nhận đã giao.
     */
    public boolean confirmDelivered(int orderId, int accountId) {
        String sql = "UPDATE tbOrder SET status = 'DELIVERED', "
                + "payment_status = CASE WHEN payment_method = 'COD' THEN 'PAID' ELSE payment_status END "
                + "WHERE order_id = ? AND account_id = ? AND status = 'SHIPPED'";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            ps.setInt(2, accountId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Khách bấm "Hoàn hàng" — chỉ cho phép khi đơn đang SHIPPED (phải nhận hàng
     * thật thì mới có gì để hoàn). Đơn vẫn chuyển sang DELIVERED (đúng thực tế:
     * hàng đã tới tay khách), NHƯNG đánh dấu thêm return_status = 'REQUESTED'
     * để Staff biết mà chủ động liên hệ xử lý hoàn hàng/hoàn tiền NGOÀI hệ
     * thống — không cần thêm logic gì phức tạp hơn (không có quy trình duyệt
     * nhiều bước như tbReturnRequest cũ đã bỏ).
     */
    public boolean requestReturn(int orderId, int accountId, String reason) {
        // MỚI: đường "Hoàn hàng" cũng tự chuyển SHIPPED -> DELIVERED (khách
        // không bắt buộc phải bấm "Xác nhận đã nhận hàng" trước), nên phải
        // đánh dấu COD đã thu tiền NGAY tại đây luôn — giống hệt lý do đã
        // sửa ở confirmDelivered(), tránh sót đường thứ 2 dẫn tới DELIVERED.
        String sql = "UPDATE tbOrder SET status = 'DELIVERED', return_status = 'REQUESTED', "
                + "return_requested_at = SYSDATETIME(), return_reason = ?, "
                + "payment_status = CASE WHEN payment_method = 'COD' THEN 'PAID' ELSE payment_status END "
                + "WHERE order_id = ? AND account_id = ? AND status = 'SHIPPED'";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, reason);
            ps.setInt(2, orderId);
            ps.setInt(3, accountId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Staff xác nhận ĐÃ TIẾP NHẬN hàng trả về từ khách (hàng vật lý đã về tới
     * kho). Đây là lúc DUY NHẤT hàng được cộng lại vào kho — KHÔNG cộng ngay
     * lúc khách mới bấm "Yêu cầu hoàn hàng" (lúc đó hàng vẫn đang ở chỗ khách).
     *
     * Làm 2 việc trong 1 transaction, ĐÚNG NGUYÊN TẮC giống hệt cancelOrder():
     * 1) Cộng lại số lượng từng sản phẩm trong đơn vào tbProduct.stock_quantity
     * — coi hàng trả về như hàng nhập lại kho. 2) Nếu đơn đã thu tiền thật (COD
     * đã giao / Online QR đã PAID) thì đánh dấu payment_status =
     * 'REFUND_PENDING' — cùng 1 quy tắc hoàn tiền với đơn bị hủy, không phân
     * biệt "hủy" hay "hoàn hàng" nữa.
     *
     * Chỉ cho phép khi đang ở REQUESTED, tránh xác nhận nhầm đơn chưa từng có
     * yêu cầu hoàn hàng, hoặc xác nhận 2 lần (cộng kho 2 lần).
     */
 public boolean confirmReturnCompleted(int orderId) {
        return confirmReturnCompleted(orderId, true);
    }

    /**
     * @param isGoodQuality MỚI: true = hàng còn tốt, cộng lại kho như cũ;
     *   false = hàng lỗi/hư hỏng, KHÔNG cộng lại kho (coi như hàng bỏ đi) —
     *   nhưng vẫn hoàn tiền cho khách bình thường (lỗi hàng hóa không phải
     *   trách nhiệm của khách, không liên quan tới việc có cộng kho hay không).
     */
    public boolean confirmReturnCompleted(int orderId, boolean isGoodQuality) {
        try {
            connection.setAutoCommit(false);

            // 1) Đọc payment_method/payment_status TRƯỚC khi update, để biết
            // đơn này có cần đánh dấu chờ hoàn tiền hay không.
            String paymentMethod = null;
            String paymentStatus = null;
            String sqlGetPay = "SELECT payment_method, payment_status FROM tbOrder "
                    + "WHERE order_id = ? AND return_status = 'REQUESTED'";
            try (PreparedStatement psPay = connection.prepareStatement(sqlGetPay)) {
                psPay.setInt(1, orderId);
                try (ResultSet rsPay = psPay.executeQuery()) {
                    if (rsPay.next()) {
                        paymentMethod = rsPay.getString("payment_method");
                        paymentStatus = rsPay.getString("payment_status");
                    } else {
                        // Không tìm thấy đơn nào đang REQUESTED -> không có gì để xác nhận
                        connection.rollback();
                        return false;
                    }
                }
            }
            // Tiền đã thu thật nếu: Online QR đã PAID, HOẶC COD đã PAID
            // (trường hợp COD đã ghi nhận thu tiền khi giao hàng thành công).
            boolean needsRefund = "PAID".equals(paymentStatus);

            String sqlUpdate = needsRefund
                    ? "UPDATE tbOrder SET return_status = 'COMPLETED', payment_status = 'REFUND_PENDING' "
                    + "WHERE order_id = ? AND return_status = 'REQUESTED'"
                    : "UPDATE tbOrder SET return_status = 'COMPLETED' "
                    + "WHERE order_id = ? AND return_status = 'REQUESTED'";
            int updated;
            try (PreparedStatement ps = connection.prepareStatement(sqlUpdate)) {
                ps.setInt(1, orderId);
                updated = ps.executeUpdate();
            }
            if (updated == 0) {
                connection.rollback();
                return false;
            }

            // 2) Cộng lại kho — CHỈ khi hàng còn tốt (isGoodQuality = true).
            // MỚI: TRƯỚC ĐÂY luôn cộng lại kho vô điều kiện, kể cả hàng trả
            // về đã hư hỏng/lỗi — dẫn tới bán nhầm hàng lỗi cho khách khác.
            if (isGoodQuality) {
                String sqlGetDetails = "SELECT product_id, quantity FROM tbOrderDetail WHERE order_id = ?";
                String sqlRestore = "UPDATE tbProduct SET stock_quantity = stock_quantity + ? "
                        + "WHERE product_id = ?";

                try (PreparedStatement getStm = connection.prepareStatement(sqlGetDetails)) {
                    getStm.setInt(1, orderId);
                    try (ResultSet rs = getStm.executeQuery(); PreparedStatement restoreStm = connection.prepareStatement(sqlRestore)) {
                        while (rs.next()) {
                            restoreStm.setInt(1, rs.getInt("quantity"));
                            restoreStm.setInt(2, rs.getInt("product_id"));
                            restoreStm.addBatch();
                        }
                        restoreStm.executeBatch();
                    }
                }
            }

            connection.commit();
            return true;
        } catch (Exception e) {
            try {
                connection.rollback();
            } catch (Exception ex) {
                /* ignore */ }
            e.printStackTrace();
            return false;
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (Exception ex) {
                /* ignore */ }
        }
    }

    /**
     * Staff TỪ CHỐI yêu cầu hoàn hàng (lý do không hợp lệ, quá hạn, không đúng
     * chính sách đổi trả...) — KHÔNG cộng lại kho, KHÔNG đánh dấu hoàn tiền, vì
     * hàng chưa hề được nhận lại. Chuyển return_status sang 'REJECTED' (trạng
     * thái cuối), lưu kèm lý do từ chối để khách xem lại được vì sao. Chỉ cho
     * phép khi đang ở REQUESTED, tránh từ chối nhầm đơn không có yêu cầu hoàn
     * hàng, hoặc từ chối 2 lần.
     *
     * KHÔNG cần lý do — Staff đã chủ động liên hệ khách để giải quyết ngoài hệ
     * thống trước khi bấm nút này rồi, không cần ghi thêm gì vào DB.
     */
    public boolean rejectReturnRequest(int orderId) {
        String sql = "UPDATE tbOrder SET return_status = 'REJECTED' "
                + "WHERE order_id = ? AND return_status = 'REQUESTED'";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Danh sách đơn đang CHỜ HOÀN TIỀN (payment_status = 'REFUND_PENDING') —
     * sinh ra từ 2 nguồn: (1) đơn bị hủy sau khi đã thanh toán Online QR
     * (cancelOrder / updateOrderStatus), (2) yêu cầu hoàn hàng đã được tiếp
     * nhận xong (confirmReturnCompleted). Dùng cho khu vực "Chờ hoàn tiền" ở
     * Dashboard Manager — Manager là người xác nhận đã chuyển khoản hoàn tiền
     * thật cho khách (việc liên quan tiền bạc, không để Staff tự tick).
     */
    public List<OrderDTO> getPendingRefunds() {
        List<OrderDTO> list = new ArrayList<>();
       String sql = "SELECT o.order_id, o.order_code, o.account_id, o.receiver_name, o.receiver_phone, "
                + "((SELECT ISNULL(SUM(quantity * unit_price), 0) FROM tbOrderDetail WHERE order_id = o.order_id) - o.discount_amount + o.shipping_fee) AS total_amount, o.payment_method, o.status, a.email AS account_email "
                + "FROM tbOrder o JOIN tbAccount a ON a.account_id = o.account_id "
                + "WHERE o.payment_status = 'REFUND_PENDING' ORDER BY o.order_id ASC";
        try (PreparedStatement ps = connection.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                OrderDTO o = new OrderDTO();
                o.setOrderId(rs.getInt("order_id"));
                o.setOrderCode(rs.getString("order_code"));
                o.setAccountId(rs.getInt("account_id"));
                o.setReceiverName(rs.getString("receiver_name"));
                o.setReceiverPhone(rs.getString("receiver_phone"));
                o.setTotalAmount(rs.getLong("total_amount"));
                o.setAccountEmail(rs.getString("account_email"));
                o.setPaymentMethod(rs.getString("payment_method"));
                o.setStatus(rs.getString("status"));
                list.add(o);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * MỚI: Staff ghi nhận kết quả gọi điện xác nhận khách trước khi giao —
     * phòng "bom hàng". Chỉ set được khi đơn đang PENDING/PROCESSING (chưa
     * SHIPPED). status hợp lệ: CONFIRMED (khách xác nhận nhận hàng) hoặc
     * NOT_CONFIRMED (không liên hệ được / khách từ chối).
     *
     * Ghi nhận kết quả gọi điện xác nhận khách. Nếu chọn NOT_CONFIRMED đủ 3
     * lần liên tiếp (không có lần CONFIRMED nào xen giữa, vì CONFIRMED reset
     * lại đếm về 0) thì tự động hủy đơn — coi như "gọi không được là hủy".
     *
     * SỬA: theo chính sách "hủy do không liên hệ được -> KHÔNG hoàn tiền",
     * bỏ hẳn mã trả về "AUTO_CANCELLED_REFUND" (dễ hiểu lầm là có hoàn tiền).
     * Trả về: "OK" | "AUTO_CANCELLED" | "AUTO_CANCELLED_WAS_PAID" | "NOT_FOUND" | "INVALID" | "ERROR"
     * — "AUTO_CANCELLED_WAS_PAID" chỉ mang tính THÔNG TIN (đơn này từng được
     * thanh toán, để Staff chủ động giải thích cho khách vì sao không hoàn),
     * KHÔNG kích hoạt bất kỳ luồng hoàn tiền nào trong hệ thống.
     */
    public String updatePhoneConfirmStatus(int orderId, String status) {
        if (!"CONFIRMED".equals(status) && !"NOT_CONFIRMED".equals(status)) {
            return "INVALID";
        }
        try {
            connection.setAutoCommit(false);

            if ("CONFIRMED".equals(status)) {
                String sql = "UPDATE tbOrder SET phone_confirm_status = 'CONFIRMED', phone_attempt_count = 0 "
                           + "WHERE order_id = ? AND status IN ('PENDING', 'PROCESSING')";
                try (PreparedStatement ps = connection.prepareStatement(sql)) {
                    ps.setInt(1, orderId);
                    int updated = ps.executeUpdate();
                    connection.commit();
                    return updated > 0 ? "OK" : "NOT_FOUND";
                }
            }

            // NOT_CONFIRMED: tăng đếm lên 1, rồi kiểm tra đã đạt ngưỡng 3 chưa
            String sqlIncrement = "UPDATE tbOrder SET phone_confirm_status = 'NOT_CONFIRMED', "
                    + "phone_attempt_count = phone_attempt_count + 1 "
                    + "WHERE order_id = ? AND status IN ('PENDING', 'PROCESSING')";
            int updated;
            try (PreparedStatement ps = connection.prepareStatement(sqlIncrement)) {
                ps.setInt(1, orderId);
                updated = ps.executeUpdate();
            }
            if (updated == 0) {
                connection.rollback();
                return "NOT_FOUND";
            }

            int attemptCount = 0;
            String paymentMethod = null;
            String paymentStatus = null;
            String sqlGet = "SELECT phone_attempt_count, payment_method, payment_status FROM tbOrder WHERE order_id = ?";
            try (PreparedStatement ps = connection.prepareStatement(sqlGet)) {
                ps.setInt(1, orderId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        attemptCount = rs.getInt("phone_attempt_count");
                        paymentMethod = rs.getString("payment_method");
                        paymentStatus = rs.getString("payment_status");
                    }
                }
            }

            if (attemptCount < 3) {
                connection.commit();
                return "OK";
            }

           // Đạt đủ 3 lần không liên hệ được -> tự động hủy, hoàn kho.
            // SỬA: theo chính sách đã chốt — hủy do KHÔNG LIÊN HỆ ĐƯỢC (lỗi
            // thuộc về khách, không phải shop) thì KHÔNG hoàn tiền, bất kể
            // đơn đã thanh toán hay chưa, đã ship hay chưa. payment_status
            // giữ nguyên PAID (nếu đã thanh toán) — không chuyển REFUND_PENDING.
            String sqlCancel = "UPDATE tbOrder SET status = 'CANCELLED', cancel_reason = 'AUTO_NO_CONTACT' WHERE order_id = ?";
            try (PreparedStatement ps = connection.prepareStatement(sqlCancel)) {
                ps.setInt(1, orderId);
                ps.executeUpdate();
            }

            String sqlGetDetails = "SELECT product_id, quantity FROM tbOrderDetail WHERE order_id = ?";
            String sqlRestore = "UPDATE tbProduct SET stock_quantity = stock_quantity + ? WHERE product_id = ?";
            try (PreparedStatement getStm = connection.prepareStatement(sqlGetDetails)) {
                getStm.setInt(1, orderId);
                try (ResultSet rs = getStm.executeQuery();
                     PreparedStatement restoreStm = connection.prepareStatement(sqlRestore)) {
                    while (rs.next()) {
                        restoreStm.setInt(1, rs.getInt("quantity"));
                        restoreStm.setInt(2, rs.getInt("product_id"));
                        restoreStm.addBatch();
                    }
                    restoreStm.executeBatch();
                }
            }

            connection.commit();
            // SỬA: bỏ hẳn nhánh "AUTO_CANCELLED_REFUND" — theo chính sách đã
            // chốt, hủy do không liên hệ được KHÔNG BAO GIỜ hoàn tiền, nên
            // không còn lý do gì để gợi ý có hoàn tiền ở đây. needsRefund giờ
            // chỉ mang tính thông tin (đơn này có từng được thanh toán hay
            // không), không quyết định việc hoàn tiền nữa.
            boolean needsRefund = "ONLINE_QR".equals(paymentMethod) && "PAID".equals(paymentStatus);
            return needsRefund ? "AUTO_CANCELLED_WAS_PAID" : "AUTO_CANCELLED";

        } catch (Exception e) {
            try { connection.rollback(); } catch (Exception ex) { /* ignore */ }
            e.printStackTrace();
            return "ERROR";
        } finally {
            try { connection.setAutoCommit(true); } catch (Exception ex) { /* ignore */ }
        }
    }

    /**
     * Manager xác nhận ĐÃ hoàn tiền thật cho khách (chuyển khoản/tiền mặt ngoài
     * hệ thống) — chuyển payment_status sang trạng thái CUỐI CÙNG 'REFUNDED'.
     * Chỉ cho phép khi đang ở REFUND_PENDING, tránh xác nhận nhầm đơn chưa từng
     * nợ hoàn tiền, hoặc xác nhận 2 lần.
     *
     * MỚI: thêm lớp phòng vệ — chỉ cho xác nhận hoàn tiền khi đơn THẬT SỰ
     * thuộc 1 trong 2 trường hợp hợp lệ: đã bị HỦY, hoặc đã hoàn hàng xong
     * (return_status = COMPLETED). TRƯỚC ĐÂY chỉ check payment_status =
     * REFUND_PENDING — nếu payment_status bị gắn sai (như bug từng gặp ở
     * updateOrderStatus() thiếu check newStatus == CANCELLED, khiến đơn
     * giao thành công bình thường cũng bị gắn nhầm REFUND_PENDING), Manager
     * vẫn có thể lỡ tay xác nhận "hoàn tiền" cho 1 đơn chẳng có gì để hoàn
     * cả. Giờ chặn thêm 1 lớp ở đây, độc lập với chỗ gắn cờ REFUND_PENDING.
     */
    public boolean confirmRefundCompleted(int orderId) {
        String sql = "UPDATE tbOrder SET payment_status = 'REFUNDED' "
                   + "WHERE order_id = ? AND payment_status = 'REFUND_PENDING' "
                   + "AND (status = 'CANCELLED' OR return_status = 'COMPLETED')";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Danh sách đơn đang có yêu cầu hoàn hàng CHỜ Staff xử lý — dùng cho thông
     * báo ở Dashboard Staff, kèm thông tin liên hệ khách hàng.
     */
    public List<OrderDTO> getPendingReturnRequests() {
        List<OrderDTO> list = new ArrayList<>();
       String sql = "SELECT o.order_id, o.order_code, o.account_id, o.receiver_name, o.receiver_phone, "
                + "((SELECT ISNULL(SUM(quantity * unit_price), 0) FROM tbOrderDetail WHERE order_id = o.order_id) - o.discount_amount + o.shipping_fee) AS total_amount, o.return_requested_at, o.return_reason, a.email AS account_email "
                + "FROM tbOrder o JOIN tbAccount a ON a.account_id = o.account_id "
                + "WHERE o.return_status = 'REQUESTED' ORDER BY o.return_requested_at ASC";
        try (PreparedStatement ps = connection.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                OrderDTO o = new OrderDTO();
                o.setOrderId(rs.getInt("order_id"));
                o.setOrderCode(rs.getString("order_code"));
                o.setAccountId(rs.getInt("account_id"));
                o.setReceiverName(rs.getString("receiver_name"));
                o.setReceiverPhone(rs.getString("receiver_phone"));
                o.setTotalAmount(rs.getLong("total_amount"));
                o.setAccountEmail(rs.getString("account_email"));
                o.setReturnReason(rs.getString("return_reason"));
                list.add(o);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Danh sách đơn đã hoàn tất xử lý hoàn hàng (return_status = 'COMPLETED') —
     * dùng cho bảng lịch sử ở trang "Tiếp nhận trả hàng".
     */
    public List<OrderDTO> getCompletedReturnRequests() {
        List<OrderDTO> list = new ArrayList<>();
        String sql = "SELECT o.order_id, o.order_code, o.account_id, o.receiver_name, o.receiver_phone, "
                + "((SELECT ISNULL(SUM(quantity * unit_price), 0) FROM tbOrderDetail WHERE order_id = o.order_id) - o.discount_amount + o.shipping_fee) AS total_amount, o.return_requested_at, o.payment_status, a.email AS account_email "
                + "FROM tbOrder o JOIN tbAccount a ON a.account_id = o.account_id "
                + "WHERE o.return_status = 'COMPLETED' ORDER BY o.return_requested_at DESC";
        try (PreparedStatement ps = connection.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                OrderDTO o = new OrderDTO();
                o.setOrderId(rs.getInt("order_id"));
                o.setOrderCode(rs.getString("order_code"));
                o.setAccountId(rs.getInt("account_id"));
                o.setReceiverName(rs.getString("receiver_name"));
                o.setReceiverPhone(rs.getString("receiver_phone"));
                o.setTotalAmount(rs.getLong("total_amount"));
                o.setAccountEmail(rs.getString("account_email"));
                o.setPaymentStatus(rs.getString("payment_status"));
                list.add(o);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<OrderDTO> getAllOrders(String statusFilter, String keyword) {
        List<OrderDTO> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT o.order_id, o.order_code, o.account_id, o.status, "
                + "       o.receiver_name, o.receiver_phone, o.shipping_address, "
                + "       (SELECT ISNULL(SUM(quantity * unit_price), 0) FROM tbOrderDetail WHERE order_id = o.order_id) AS subtotal, o.discount_amount, o.shipping_fee, ((SELECT ISNULL(SUM(quantity * unit_price), 0) FROM tbOrderDetail WHERE order_id = o.order_id) - o.discount_amount + o.shipping_fee) AS total_amount, "
                + "       o.payment_method, o.payment_status, o.phone_confirm_status,o.phone_attempt_count, o.cancel_reason, o.created_at "
                + "FROM tbOrder o "
        );

        boolean hasStatus = statusFilter != null && !statusFilter.trim().isEmpty();
        boolean hasKeyword = keyword != null && !keyword.trim().isEmpty();

        if (hasStatus) {
            sql.append(" AND o.status = ?");
        }
        if (hasKeyword) {
            sql.append(" AND (o.order_code LIKE ? OR o.receiver_name LIKE ?)");
        }
        sql.append(" ORDER BY o.created_at DESC");

        try (PreparedStatement ps = connection.prepareStatement(sql.toString())) {
            int idx = 1;
            if (hasStatus) {
                ps.setString(idx++, statusFilter.trim());
            }
            if (hasKeyword) {
                String kw = "%" + keyword.trim() + "%";
                ps.setString(idx++, kw);
                ps.setString(idx++, kw);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OrderDTO o = new OrderDTO();
                    o.setOrderId(rs.getInt("order_id"));
                    o.setOrderCode(rs.getString("order_code"));
                    o.setAccountId(rs.getInt("account_id"));
                    o.setStatus(rs.getString("status"));
                    o.setReceiverName(rs.getString("receiver_name"));
                    o.setReceiverPhone(rs.getString("receiver_phone"));
                    o.setShippingAddress(rs.getString("shipping_address"));
                    o.setSubtotal(rs.getLong("subtotal"));
                    o.setDiscountAmount(rs.getLong("discount_amount"));
                    o.setShippingFee(rs.getLong("shipping_fee"));
                    o.setTotalAmount(rs.getLong("total_amount"));
                    o.setPaymentMethod(rs.getString("payment_method"));
                    o.setPaymentStatus(rs.getString("payment_status"));
                    o.setPhoneConfirmStatus(rs.getString("phone_confirm_status"));
                    o.setPhoneAttemptCount(rs.getInt("phone_attempt_count"));
                    o.setCancelReason(rs.getString("cancel_reason"));
                    o.setOrderDate(rs.getTimestamp("created_at"));
                    
                    list.add(o);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }


}
