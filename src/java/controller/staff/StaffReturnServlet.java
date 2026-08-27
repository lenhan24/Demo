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

/**
 * Servlet xử lý trang "Tiếp nhận trả hàng" cho Staff.
 *
 * URL pattern: /staff/returns
 *
 * VIẾT LẠI HOÀN TOÀN — TRƯỚC ĐÂY servlet này đọc/ghi bảng tbReturnRequest
 * (qua ReturnDAO) với luồng 2 bước Staff→Manager, NHƯNG không hề có bất kỳ
 * luồng khách hàng nào tạo dữ liệu vào bảng đó — khách bấm "Hoàn hàng" ở
 * OrderDetail.jsp thực chất chỉ set cờ tbOrder.return_status = 'REQUESTED'
 * (xem OrderActionController + DAOOrder.requestReturn()). Nghĩa là trang này
 * trước đây LUÔN xử lý dữ liệu "ma", tách biệt hoàn toàn khỏi yêu cầu hoàn
 * hàng thật của khách — và vì không có servlet Manager nào cho bước 2, yêu
 * cầu nào lỡ được "duyệt, chuyển Manager" sẽ bị kẹt vĩnh viễn, không bao giờ
 * ra được trạng thái cuối.
 *
 * Giờ trang này đọc/ghi ĐÚNG một nguồn dữ liệu thật duy nhất: tbOrder.return_status
 * (REQUESTED / COMPLETED), y hệt banner "Yêu cầu hoàn hàng đang chờ xử lý" đã
 * có sẵn ở /staff/orders — cả 2 nơi giờ dùng chung 1 action xử lý
 * (DAOOrder.confirmReturnCompleted), tránh 2 đường cho cùng 1 việc.
 *
 * GET  → danh sách yêu cầu đang REQUESTED + lịch sử đã COMPLETED
 * POST → action=confirmReturn — xác nhận đã tiếp nhận hàng trả về kho:
 *        tự động cộng lại tồn kho + đánh dấu chờ hoàn tiền nếu cần
 *        (xem DAOOrder.confirmReturnCompleted()).
 */
@WebServlet(name = "StaffReturnServlet", urlPatterns = {"/staff/returns"})
public class StaffReturnServlet extends HttpServlet {

    private final DAOOrder orderDAO = new DAOOrder();

    // ─────────────────────────────────────────────────────────
    //  GET: Hiển thị danh sách yêu cầu hoàn hàng
    // ─────────────────────────────────────────────────────────
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!hasStaffAccess(request, response)) return;

        List<OrderDTO> pending   = orderDAO.getPendingReturnRequests();
        List<OrderDTO> completed = orderDAO.getCompletedReturnRequests();

        request.setAttribute("pendingReturns",   pending);
        request.setAttribute("completedReturns", completed);

        loadAndClearFlash(request);

        request.getRequestDispatcher("/staff/staff-returns.jsp").forward(request, response);
    }

    // ─────────────────────────────────────────────────────────
    //  POST: Xác nhận đã tiếp nhận hàng trả về (cộng kho + hoàn tiền nếu cần)
    // ─────────────────────────────────────────────────────────
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        if (!hasStaffAccess(request, response)) return;

        String action = request.getParameter("action");
        HttpSession session = request.getSession();

       if ("confirmReturn".equals(action)) {
            int orderId = parseIntSafe(request.getParameter("orderId"), 0);
            // MỚI: "quality" = "good" (mặc định nếu thiếu, để tương thích ngược)
            // hoặc "bad" — do Staff chọn tương ứng nút bấm.
            boolean isGoodQuality = !"bad".equals(request.getParameter("quality"));
            boolean ok = orderId > 0 && orderDAO.confirmReturnCompleted(orderId, isGoodQuality);
            if (ok) {
                session.setAttribute("flashMessage", isGoodQuality
                        ? "Đã xác nhận tiếp nhận hàng trả về — tồn kho đã được cộng lại."
                        : "Đã xác nhận tiếp nhận hàng trả về — hàng lỗi/hư hỏng, KHÔNG cộng lại kho.");
                session.setAttribute("flashError", false);
            } else {
                session.setAttribute("flashMessage",
                        "Xác nhận thất bại. Đơn có thể đã được xử lý trước đó.");
                session.setAttribute("flashError", true);
            }
        } else if ("rejectReturn".equals(action)) {
            int orderId = parseIntSafe(request.getParameter("orderId"), 0);
            boolean ok = orderId > 0 && orderDAO.rejectReturnRequest(orderId);
            if (ok) {
                session.setAttribute("flashMessage", "Đã từ chối yêu cầu hoàn hàng.");
                session.setAttribute("flashError", false);
            } else {
                session.setAttribute("flashMessage",
                        "Từ chối thất bại. Đơn có thể đã được xử lý trước đó.");
                session.setAttribute("flashError", true);
            }
        } else {
            session.setAttribute("flashMessage", "Hành động không hợp lệ.");
            session.setAttribute("flashError", true);
        }

        response.sendRedirect(request.getContextPath() + "/staff/returns");
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

    private int parseIntSafe(String s, int def) {
        try { return Integer.parseInt(s == null ? "" : s.trim()); }
        catch (Exception e) { return def; }
    }
}
