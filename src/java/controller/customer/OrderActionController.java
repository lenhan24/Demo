package controller.customer;

import dal.AccountDAO;
import dal.DAOOrder;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import model.Account;

/**
 * Xử lý 2 hành động khách hàng có thể làm khi đơn đang ở trạng thái SHIPPED:
 * - "Xác nhận đã nhận hàng" -> chuyển DELIVERED bình thường
 * - "Yêu cầu hoàn hàng"     -> cũng chuyển DELIVERED (đã nhận hàng thật), kèm
 *   đánh dấu return_status = REQUESTED để Staff biết mà liên hệ xử lý ngoài
 *   hệ thống (không cần quy trình duyệt nhiều bước).
 *
 * Chỉ nhận POST vì đây là hành động ghi dữ liệu.
 */
@WebServlet(name = "OrderActionController", urlPatterns = {"/orderaction"})
public class OrderActionController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.sendRedirect("myorders");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        String email = (String) session.getAttribute("userEmail");
        if (email == null) {
            response.sendRedirect("Login.jsp");
            return;
        }

        String action = request.getParameter("action");
        String orderIdParam = request.getParameter("orderId");

        try {
            int orderId = Integer.parseInt(orderIdParam);
            AccountDAO accountDAO = new AccountDAO();
            Account account = accountDAO.getAccountByEmail(email);
            DAOOrder orderDAO = new DAOOrder();

            if ("confirmDelivered".equals(action)) {
                boolean ok = orderDAO.confirmDelivered(orderId, account.getAccountId());
                session.setAttribute("flashMessage", ok
                        ? "Cảm ơn bạn đã xác nhận nhận hàng!"
                        : "Không thể xác nhận — đơn không ở trạng thái phù hợp.");
                session.setAttribute("flashError", !ok);

            } else if ("requestReturn".equals(action)) {
                String reason = request.getParameter("reason");
                boolean ok = orderDAO.requestReturn(orderId, account.getAccountId(),
                        reason != null ? reason.trim() : null);
                // Message theo đúng yêu cầu: báo khách shop sẽ liên hệ lại qua
                // đúng SĐT/email hiện có trong tài khoản — không thu thập gì thêm.
                session.setAttribute("flashMessage", ok
                        ? "Yêu cầu hoàn hàng đã được ghi nhận. Chúng tôi sẽ liên hệ lại bạn qua số điện thoại "
                          + account.getPhone() + " hoặc email " + account.getEmail() + " để xử lý."
                        : "Không thể gửi yêu cầu — đơn không ở trạng thái phù hợp.");
                session.setAttribute("flashError", !ok);
                if (ok) {
                    // Ghi log yêu cầu hoàn hàng.
                    new dal.ActivityLogDAO().insertLog(account.getAccountId(),
                            "Yêu cầu hoàn hàng đơn #" + orderId);
                }

            } else {
                session.setAttribute("flashMessage", "Hành động không hợp lệ.");
                session.setAttribute("flashError", true);
            }
        } catch (NumberFormatException e) {
            session.setAttribute("flashMessage", "Dữ liệu không hợp lệ.");
            session.setAttribute("flashError", true);
        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("flashMessage", "Có lỗi xảy ra, vui lòng thử lại.");
            session.setAttribute("flashError", true);
        }

        response.sendRedirect("orders?id=" + orderIdParam);
    }
}
