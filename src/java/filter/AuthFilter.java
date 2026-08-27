package filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Set;
import model.Account;

/**
 * AuthFilter — chốt chặn phân quyền TẬP TRUNG cho toàn bộ route quản trị,
 * VÀ chặn tài khoản nội bộ (Staff/Manager/Admin) khỏi các route tự-phục-vụ
 * của Customer (sửa thông tin cá nhân, giỏ hàng, thanh toán...).
 *
 * TRƯỚC ĐÂY: mỗi servlet (ProductManagerServlet, PromotionManagerServlet,
 * AdminAccountsServlet, AdminRolesServlet, AdminLogsServlet, DashboardServlet...)
 * không hề tự kiểm tra login/role -> ai cũng vào thẳng
 * được, kể cả chưa đăng nhập. Filter này thay thế toàn bộ các đoạn check
 * rải rác đó bằng MỘT nơi duy nhất, đúng yêu cầu "Authorization Filter"
 * trong đề bài (mục 3.8 + cấu trúc project mục 2 yêu cầu có package `filter`).
 *
 * MỚI: tài khoản nội bộ (STAFF/MANAGER/ADMIN) không được tự sửa thông tin
 * tài khoản của chính mình (đổi tên/email/phone/mật khẩu/địa chỉ) và không
 * được mua hàng (giỏ hàng, thanh toán, đặt hàng, hủy/hoàn đơn, dùng voucher,
 * đánh giá sản phẩm). Toàn bộ thay đổi thông tin của các tài khoản này chỉ
 * được thực hiện bởi Admin qua /admin/accounts. Vì vậy các route tự-phục-vụ
 * của Customer bên dưới chỉ cho phép role CUSTOMER đi qua; STAFF/MANAGER/ADMIN
 * bị chặn 403 dù đã đăng nhập.
 *
 * Cách hoạt động: match theo tiền tố URL (khu vực quản trị) hoặc theo path
 * chính xác (route tự-phục-vụ của Customer), mỗi nhóm ứng với 1 tập role
 * được phép. Không nằm trong nhóm nào thì cho qua (route công khai, ví dụ
 * /home, /products, /login...).
 *
 * Login đã lưu sẵn session.setAttribute("account", acc) với acc.getRole()
 * đã set (xem LoginController) — filter dựa hoàn toàn vào đó, không cần
 * sửa gì thêm ở phía Login.
 */
@WebFilter(urlPatterns = {
    "/manager/*", "/admin/*", "/staff/*",
    "/account", "/changeinfo", "/changepass", "/address-add", "/address-action",
    "/cart", "/addtocart", "/deletecart", "/checkout", "/placeOrder", "/saveVoucher",
    "/review", "/cancelOrder", "/orderaction", "/myorders", "/orders", "/orderSuccess"
})
public class AuthFilter implements Filter {

    // Tiền tố URL -> tập role được phép truy cập tiền tố đó.
    // ADMIN được thêm mặc định vào mọi tiền tố quản trị vì Admin có quyền cao nhất.
    private static final String[] MANAGER_ROLES  = {"MANAGER", "ADMIN"};
    private static final String[] STAFF_ROLES    = {"STAFF", "MANAGER", "ADMIN"};
    private static final String[] ADMIN_ROLES    = {"ADMIN"};
    // Route tự-phục-vụ / mua hàng của Customer — chỉ CUSTOMER được đi qua.
    private static final String[] CUSTOMER_ROLES = {"CUSTOMER"};

    private static final Set<String> CUSTOMER_ONLY_PATHS = Set.of(
            "/account", "/changeinfo", "/changepass", "/address-add", "/address-action",
            "/cart", "/addtocart", "/deletecart", "/checkout", "/placeOrder", "/saveVoucher",
            "/review", "/cancelOrder", "/orderaction", "/myorders", "/orders", "/orderSuccess"
    );

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request   = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        String path = request.getRequestURI().substring(request.getContextPath().length());

        Set<String> allowedRoles;
        boolean isCustomerOnlyRoute = false;
        if (path.startsWith("/manager/")) {
            allowedRoles = Set.of(MANAGER_ROLES);
        } else if (path.startsWith("/staff/")) {
            allowedRoles = Set.of(STAFF_ROLES);
        } else if (path.startsWith("/admin/")) {
            allowedRoles = Set.of(ADMIN_ROLES);
        } else if (CUSTOMER_ONLY_PATHS.contains(path)) {
            allowedRoles = Set.of(CUSTOMER_ROLES);
            isCustomerOnlyRoute = true;
        } else {
            // Không thuộc nhóm route nào ở trên -> cho qua, không phải việc của filter này
            chain.doFilter(req, res);
            return;
        }

        HttpSession session = request.getSession(false);
        if (session == null) {
            // Route tự-phục-vụ của Customer thì cứ để servlet đích tự lo phần
            // "chưa đăng nhập -> đá về Login" như code cũ (một số nơi có logic
            // riêng, ví dụ quay lại đúng trang sản phẩm đang xem thay vì Login
            // trắng); filter chỉ đứng ra chặn role SAI, không giành việc redirect.
            if (isCustomerOnlyRoute) {
                chain.doFilter(req, res);
                return;
            }
            redirectToLogin(request, response);
            return;
        }

        Account account = (Account) session.getAttribute("account");
        if (account == null || account.getRole() == null) {
            if (isCustomerOnlyRoute) {
                chain.doFilter(req, res);
                return;
            }
            redirectToLogin(request, response);
            return;
        }

        if (!allowedRoles.contains(account.getRole())) {
            if (isCustomerOnlyRoute) {
                // Đã đăng nhập bằng tài khoản nội bộ (Staff/Manager/Admin) nhưng
                // cố vào route mua hàng / tự sửa thông tin -> chặn hẳn, không cho
                // qua. Thông tin của các tài khoản này chỉ Admin được sửa.
                response.sendRedirect(request.getContextPath() + "/login?blocked=internal");
                return;
            }
            // Đã đăng nhập nhưng sai role -> 403, KHÔNG redirect về login
            // (redirect về login sẽ gây hiểu lầm "chưa đăng nhập", trong khi
            //  thực ra là "đã đăng nhập nhưng không đủ quyền")
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Bạn không có quyền truy cập trang này.");
            return;
        }

        // Đủ quyền -> cho request đi tiếp tới servlet đích
        chain.doFilter(req, res);
    }

    private void redirectToLogin(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.sendRedirect(request.getContextPath() + "/login");
    }

    @Override
    public void init(FilterConfig filterConfig) { /* không cần khởi tạo gì thêm */ }

    @Override
    public void destroy() { /* không cần giải phóng gì thêm */ }
}
