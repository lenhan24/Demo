package controller.auth;

import dal.AccountDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import model.Account;

/**
 * MỚI: "Quên mật khẩu" tự khôi phục — vì project chưa có hạ tầng gửi email
 * (không JavaMail/SMTP), khách xác minh bằng Email + Số điện thoại khớp DB
 * thay vì link reset qua email. Bảo mật yếu hơn cách gửi email thật (số điện
 * thoại không hẳn là bí mật), nhưng không cần thêm thư viện/tài khoản SMTP
 * nào — đây là đánh đổi ALOHA đã chọn.
 */
@WebServlet(name = "ForgotPasswordController", urlPatterns = {"/forgotpassword"})
public class ForgotPasswordController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("ForgotPassword.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String email = request.getParameter("email");
        String phone = request.getParameter("phone");
        String newPassword = request.getParameter("newPassword");
        String confirmPassword = request.getParameter("confirmPassword");

        request.setAttribute("oldEmail", email);
        request.setAttribute("oldPhone", phone);

        if (email == null || email.isBlank() || phone == null || phone.isBlank()) {
            request.setAttribute("errorMessage", "Vui lòng nhập đầy đủ Email và Số điện thoại.");
            request.getRequestDispatcher("ForgotPassword.jsp").forward(request, response);
            return;
        }

        if (newPassword == null || newPassword.length() < 8) {
            request.setAttribute("errorMessage", "Mật khẩu mới phải có tối thiểu 8 ký tự.");
            request.getRequestDispatcher("ForgotPassword.jsp").forward(request, response);
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            request.setAttribute("errorMessage", "Xác nhận mật khẩu không khớp.");
            request.getRequestDispatcher("ForgotPassword.jsp").forward(request, response);
            return;
        }

        AccountDAO accountDAO = new AccountDAO();
        Account account = accountDAO.getAccountByEmail(email.trim());

        // Cố tình dùng CHUNG 1 thông báo lỗi cho cả 2 trường hợp "email không
        // tồn tại" và "số điện thoại không khớp" — không được để lộ ra là
        // email nào có tồn tại trong hệ thống hay không (tránh dò email).
        if (account == null || account.getPhone() == null
                || !account.getPhone().trim().equals(phone.trim())) {
            request.setAttribute("errorMessage", "Email hoặc Số điện thoại không khớp với tài khoản nào.");
            request.getRequestDispatcher("ForgotPassword.jsp").forward(request, response);
            return;
        }

        String newPasswordHash = utilities.PasswordUtil.hashPassword(newPassword);
        boolean ok = accountDAO.changePassword(email.trim(), newPasswordHash);

        if (ok) {
            request.setAttribute("successMessage", "Đặt lại mật khẩu thành công! Bạn có thể đăng nhập bằng mật khẩu mới.");
            request.setAttribute("oldEmail", null);
            request.setAttribute("oldPhone", null);
        } else {
            request.setAttribute("errorMessage", "Có lỗi xảy ra, vui lòng thử lại.");
        }
        request.getRequestDispatcher("ForgotPassword.jsp").forward(request, response);
    }
    //// new version
}
