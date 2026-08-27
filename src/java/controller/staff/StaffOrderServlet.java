package controller.staff;

import dal.DAOOrder;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import model.Account;
import model.OrderDTO;
import model.OrderDetailDTO;

/**
 * Servlet xử lý trang Xử lý đơn hàng cho Staff.
 *
 * URL pattern: /staff/orders
 *
 * GET  → load danh sách tất cả đơn hàng (có lọc theo status và keyword)
 * POST → cập nhật trạng thái đơn (action=updateStatus)
 *
 * Status flow (Staff):
 *   PENDING        → PROCESSING  (xác nhận đơn)
 *   PROCESSING     → SHIPPED     (đã bàn giao vận chuyển)
 *   PENDING/PROCESS→ ON_HOLD     (tạm giữ do hết hàng)
 */
@WebServlet(name = "StaffOrderServlet", urlPatterns = {"/staff/orders"})
public class StaffOrderServlet extends HttpServlet {

    private final DAOOrder orderDAO = new DAOOrder();

    // Các status hợp lệ mà Staff được phép cập nhật sang
    // MỚI: thêm "PENDING" — đây là đích của transition PENDING_PAYMENT ->
    // PENDING (nút "Xác nhận đã nhận tiền" cho đơn VietQR), TRƯỚC ĐÂY set
    // này chỉ có PROCESSING/SHIPPED/ON_HOLD/CANCELLED nên bị chặn nhầm ngay
    // ở đây (báo "Trạng thái không hợp lệ: PENDING") dù DAO đã cho phép.
    private static final java.util.Set<String> ALLOWED_STATUSES = new java.util.HashSet<>(
        java.util.Arrays.asList("PENDING", "PROCESSING", "SHIPPED", "ON_HOLD", "CANCELLED")
    );

    // ─────────────────────────────────────────────────────────
    //  GET: Hiển thị danh sách đơn hàng
    // ─────────────────────────────────────────────────────────
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!hasStaffAccess(request, response)) return;

        String statusFilter = request.getParameter("status");
        String keyword      = request.getParameter("keyword");

        // ── Thẻ thống kê nhanh ──
        request.setAttribute("statPendingPayment", orderDAO.countOrdersByStatus("PENDING_PAYMENT"));
        request.setAttribute("statPending",    orderDAO.countOrdersByStatus("PENDING"));
        request.setAttribute("statProcessing", orderDAO.countOrdersByStatus("PROCESSING"));
        request.setAttribute("statShipped",    orderDAO.countOrdersByStatus("SHIPPED"));
        request.setAttribute("statOnHold",     orderDAO.countOrdersByStatus("ON_HOLD"));

        // ── Danh sách đơn hàng ──
        List<OrderDTO> orders = orderDAO.getAllOrders(statusFilter, keyword);

        // Load chi tiết (sản phẩm) cho mỗi đơn để hiển thị số lượng SP
        for (OrderDTO order : orders) {
            List<OrderDetailDTO> details = orderDAO.getOrderDetails(order.getOrderId());
            order.setDetails(details); // xem phần mở rộng OrderDTO bên dưới
        }

        request.setAttribute("orders",       orders);
        request.setAttribute("statusFilter", statusFilter);
        request.setAttribute("keyword",      keyword);

        // Danh sách đơn đang chờ Staff xử lý yêu cầu hoàn hàng (khách bấm
        // "Hoàn hàng" ở trang chi tiết đơn) — kèm thông tin liên hệ để Staff
        // chủ động gọi/nhắn xử lý ngoài hệ thống.
        request.setAttribute("pendingReturns", orderDAO.getPendingReturnRequests());

        // ── Flash message ──
        loadAndClearFlash(request);

        request.getRequestDispatcher("/staff/staff-orders.jsp").forward(request, response);
    }

    // ─────────────────────────────────────────────────────────
    //  POST: Cập nhật trạng thái đơn hàng
    // ─────────────────────────────────────────────────────────
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        if (!hasStaffAccess(request, response)) return;

        String action  = request.getParameter("action");
        HttpSession session = request.getSession();

        if ("updateStatus".equals(action)) {
            handleUpdateStatus(request, session);
        } else if ("confirmPhone".equals(action)) {
            handleConfirmPhone(request, session);
        } else if ("confirmReturn".equals(action)) {
            handleConfirmReturn(request, session);
        } else if ("rejectReturn".equals(action)) {
            handleRejectReturn(request, session);
        } else {
            session.setAttribute("flashMessage", "Hành động không hợp lệ.");
            session.setAttribute("flashError", true);
        }

        // Post-Redirect-Get
        String redirectStatus = request.getParameter("currentFilter");
        String redirectUrl = request.getContextPath() + "/staff/orders";
        if (redirectStatus != null && !redirectStatus.isEmpty()) {
            redirectUrl += "?status=" + redirectStatus;
        }
        response.sendRedirect(redirectUrl);
    }

    // ─────────────────────────────────────────────────────────
    //  HANDLER
    // ─────────────────────────────────────────────────────────

    /**
     * Cập nhật trạng thái một đơn hàng.
     * Params: orderId, newStatus, currentFilter (để redirect về đúng tab)
     *
     * MỚI: thêm tham số tùy chọn "reason" — CHỈ được gửi kèm bởi nút "Giao
     * thất bại" (SHIPPED -> ON_HOLD), với giá trị cố định "DELIVERY_FAILED".
     * Nút "Tạm giữ" thường (PENDING/PROCESSING -> ON_HOLD) KHÔNG gửi tham số
     * này — nhờ vậy phân biệt được 2 nguồn gốc dẫn tới cùng 1 trạng thái
     * ON_HOLD, để áp dụng đúng chính sách "giao thất bại thì không hoàn tiền"
     * khi đơn này bị hủy hẳn sau đó.
     */
    private void handleUpdateStatus(HttpServletRequest request, HttpSession session) {
        int    orderId   = parseIntSafe(request.getParameter("orderId"), 0);
        String newStatus = request.getParameter("newStatus");
        String reason    = request.getParameter("reason"); // null nếu không phải "Giao thất bại"

        if (orderId <= 0) {
            session.setAttribute("flashMessage", "Mã đơn hàng không hợp lệ.");
            session.setAttribute("flashError", true);
            return;
        }
        if (newStatus == null || !ALLOWED_STATUSES.contains(newStatus.trim())) {
            session.setAttribute("flashMessage", "Trạng thái không hợp lệ: " + newStatus);
            session.setAttribute("flashError", true);
            return;
        }

        String result = orderDAO.updateOrderStatus(orderId, newStatus.trim(), reason);

        if ("PHONE_NOT_CONFIRMED".equals(result)) {
            // MỚI: chặn giao hàng nếu Staff chưa gọi xác nhận khách thành công.
            session.setAttribute("flashMessage",
                    "Chưa thể chuyển sang \"Đang giao\" — cần xác nhận đã gọi điện "
                    + "và khách xác nhận nhận hàng trước (xem cột \"Xác nhận điện thoại\").");
            session.setAttribute("flashError", true);
        } else if ("REFUND_NEEDED".equals(result)) {
            // Đơn đã thanh toán online rồi mới bị huỷ (KHÔNG thuộc diện giao
            // thất bại/không liên hệ được) -> không thể tự động hoàn tiền
            // (không có cổng thanh toán thật đứng sau), báo rõ cho Staff biết
            // cần chủ động liên hệ khách xử lý hoàn tiền thủ công.
            session.setAttribute("flashMessage",
                    "Đã huỷ đơn hàng #" + orderId + ". Đơn này đã thanh toán online — "
                    + "shop sẽ liên hệ khách hàng để xử lý hoàn tiền.");
            session.setAttribute("flashError", false);
        } else if (result != null) {
            String label = statusLabel(newStatus.trim());
            String extraNote = "DELIVERY_FAILED".equals(reason) && "ON_HOLD".equals(newStatus.trim())
                    ? " Đơn này sẽ KHÔNG được hoàn tiền nếu sau đó bị hủy hẳn (chính sách giao thất bại)."
                    : "";
            session.setAttribute("flashMessage", "Đơn hàng #" + orderId + " đã chuyển sang \"" + label + "\"." + extraNote);
            session.setAttribute("flashError", false);
        } else {
            session.setAttribute("flashMessage", "Cập nhật trạng thái thất bại.");
            session.setAttribute("flashError", true);
        }
    }

    /**
     * MỚI: Staff ghi nhận kết quả gọi điện xác nhận khách trước khi giao —
     * phòng "bom hàng". Params: orderId, phoneStatus (CONFIRMED/NOT_CONFIRMED).
     *
     * SỬA: đổi theo mã trả về mới của DAOOrder — "AUTO_CANCELLED_REFUND" đã
     * bị bỏ (dễ hiểu lầm là có hoàn tiền), thay bằng "AUTO_CANCELLED_WAS_PAID"
     * (chỉ mang tính thông tin, KHÔNG có ý nghĩa "sẽ hoàn tiền").
     */
    private void handleConfirmPhone(HttpServletRequest request, HttpSession session) {
        int orderId = parseIntSafe(request.getParameter("orderId"), 0);
        String phoneStatus = request.getParameter("phoneStatus");

        if (orderId <= 0) {
            session.setAttribute("flashMessage", "Mã đơn hàng không hợp lệ.");
            session.setAttribute("flashError", true);
            return;
        }

        String result = orderDAO.updatePhoneConfirmStatus(orderId, phoneStatus);

        switch (result) {
            case "AUTO_CANCELLED":
            case "AUTO_CANCELLED_WAS_PAID":
                session.setAttribute("flashMessage",
                        "Đơn #" + orderId + " đã tự động HỦY sau 3 lần không liên hệ được khách."
                        + ("AUTO_CANCELLED_WAS_PAID".equals(result)
                                ? " Đơn này đã thanh toán online — theo chính sách, sẽ KHÔNG hoàn tiền "
                                  + "vì lỗi không liên hệ được thuộc về phía khách hàng."
                                : ""));
                session.setAttribute("flashError", true);
                break;
            case "OK":
                session.setAttribute("flashMessage", "Đã ghi nhận kết quả gọi điện cho đơn #" + orderId + ".");
                session.setAttribute("flashError", false);
                break;
            default:
                session.setAttribute("flashMessage", "Cập nhật thất bại — kiểm tra lại trạng thái đơn hoặc giá trị gửi lên.");
                session.setAttribute("flashError", true);
        }
    }
    /**
     * Staff xác nhận đã xử lý xong hoàn hàng/hoàn tiền — hoàn toàn thủ công
     * (gọi điện, chuyển khoản lại...), hệ thống chỉ ghi nhận lại kết quả.
     */
    private void handleConfirmReturn(HttpServletRequest request, HttpSession session) {
        String orderIdParam = request.getParameter("orderId");
        int orderId;
        try {
            orderId = Integer.parseInt(orderIdParam);
        } catch (NumberFormatException e) {
            session.setAttribute("flashMessage", "Mã đơn hàng không hợp lệ.");
            session.setAttribute("flashError", true);
            return;
        }

        boolean ok = orderDAO.confirmReturnCompleted(orderId);
        session.setAttribute("flashMessage", ok
                ? "Đã xác nhận hoàn tất hoàn hàng/hoàn tiền cho đơn #" + orderId + "."
                : "Không thể xác nhận — đơn này không có yêu cầu hoàn hàng đang chờ.");
        session.setAttribute("flashError", !ok);
    }

    /**
     * Từ chối yêu cầu hoàn hàng — KHÔNG cộng kho, KHÔNG hoàn tiền, vì hàng
     * chưa hề được nhận lại. Không cần lý do — Staff đã chủ động liên hệ
     * khách để giải quyết ngoài hệ thống trước khi bấm nút này rồi.
     */
    private void handleRejectReturn(HttpServletRequest request, HttpSession session) {
        String orderIdParam = request.getParameter("orderId");
        int orderId;
        try {
            orderId = Integer.parseInt(orderIdParam);
        } catch (NumberFormatException e) {
            session.setAttribute("flashMessage", "Mã đơn hàng không hợp lệ.");
            session.setAttribute("flashError", true);
            return;
        }

        boolean ok = orderDAO.rejectReturnRequest(orderId);
        session.setAttribute("flashMessage", ok
                ? "Đã từ chối yêu cầu hoàn hàng cho đơn #" + orderId + "."
                : "Không thể từ chối — đơn này không có yêu cầu hoàn hàng đang chờ.");
        session.setAttribute("flashError", !ok);
    }

    // ─────────────────────────────────────────────────────────
    //  UTILS
    // ─────────────────────────────────────────────────────────

    private boolean hasStaffAccess(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null) { response.sendRedirect(request.getContextPath() + "/login"); return false; }
        Account account = (Account) session.getAttribute("account");
        if (account == null) { response.sendRedirect(request.getContextPath() + "/login"); return false; }
        String role = account.getRole();
        if (!"STAFF".equals(role) && !"MANAGER".equals(role) && !"ADMIN".equals(role)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền truy cập.");
            return false;
        }
        return true;
    }

    private void loadAndClearFlash(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) return;
        Object msg = session.getAttribute("flashMessage");
        if (msg != null) {
            request.setAttribute("flashMessage", msg);
            request.setAttribute("flashError",   session.getAttribute("flashError"));
            session.removeAttribute("flashMessage");
            session.removeAttribute("flashError");
        }
    }

    private String statusLabel(String status) {
        switch (status) {
            case "PENDING":    return "Chờ xử lý";
            case "PROCESSING": return "Đang xử lý";
            case "SHIPPED":    return "Đang giao";
            case "ON_HOLD":    return "Tạm giữ";
            case "CANCELLED":  return "Đã hủy";
            default:           return status;
        }
    }

    private int parseIntSafe(String s, int def) {
        try { return Integer.parseInt(s == null ? "" : s.trim()); }
        catch (Exception e) { return def; }
    }
}
