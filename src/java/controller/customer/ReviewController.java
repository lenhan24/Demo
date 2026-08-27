package controller.customer;

import dal.AccountDAO;
import dal.ReviewDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import model.Account;

/**
 * Xử lý việc gửi đánh giá sản phẩm (tbReview). Chỉ nhận POST vì đây là hành
 * động ghi dữ liệu — không dùng GET để tránh side-effect ngoài ý muốn, giống
 * các fix trước đó ở AddToCartController/PaymentController.
 */
@WebServlet(name = "ReviewController", urlPatterns = {"/review"})
public class ReviewController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.sendRedirect("home");
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

        String productIdParam = request.getParameter("productId");
        String orderDetailIdParam = request.getParameter("orderDetailId");
        String ratingParam = request.getParameter("rating");
        String comment = request.getParameter("comment");

        // Quay lại đúng trang chi tiết sản phẩm dù thành công hay lỗi
        String backUrl = "productdt?id=" + (productIdParam == null ? "" : productIdParam);

        try {
            int orderDetailId = Integer.parseInt(orderDetailIdParam);
            int rating = Integer.parseInt(ratingParam);

            AccountDAO accountDAO = new AccountDAO();
            Account account = accountDAO.getAccountByEmail(email);

            ReviewDAO reviewDAO = new ReviewDAO();
            String error = reviewDAO.insertReview(account.getAccountId(), orderDetailId, rating, comment);

            if (error != null) {
                session.setAttribute("flashMessage", error);
                session.setAttribute("flashError", true);
            } else {
                session.setAttribute("flashMessage", "Cảm ơn bạn đã đánh giá sản phẩm!");
                session.setAttribute("flashError", false);
            }
            response.sendRedirect(backUrl);
        } catch (NumberFormatException e) {
            session.setAttribute("flashMessage", "Dữ liệu đánh giá không hợp lệ.");
            session.setAttribute("flashError", true);
            response.sendRedirect(backUrl);
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("error.jsp");
        }
    }
}
