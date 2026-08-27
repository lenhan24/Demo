package controller.admin;

import dal.AccountDAO_1;
import dal.ActivityLogDAO;
import utilities.ValidationUtil;
import model.Account;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import model.Account_1;

@WebServlet(name = "AdminAccountsServlet", urlPatterns = {"/admin/accounts"})
public class AdminAccountsServlet extends HttpServlet {

    // A fresh AccountDAO (and thus a fresh DB connection, since AccountDAO now
    // extends DBContext) per call, rather than one shared field for the whole
    // lifetime of this servlet instance — servlets are long-lived singletons,
    // so holding a single JDBC connection open the entire time would be unsafe
    // across concurrent requests and would eventually die if SQL Server drops it.
    private AccountDAO_1 accountDAO() {
        return new AccountDAO_1();
    }

    private final ActivityLogDAO activityLogDAO = new ActivityLogDAO();

    /** Lấy account_id của Admin đang đăng nhập (người THỰC HIỆN hành động), để ghi log. */
    private int currentAdminId(HttpServletRequest req) {
        jakarta.servlet.http.HttpSession session = req.getSession(false);
        if (session == null) return 0;
        Account acc = (Account) session.getAttribute("account");
        return acc == null ? 0 : acc.getAccountId();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String search = req.getParameter("q");
        String role = req.getParameter("role");
        int page = parsePage(req.getParameter("page"));
        int totalRows = accountDAO().countAll(search, role);
        int totalPages = (int) Math.ceil(totalRows / (double) AccountDAO_1.PAGE_SIZE);
        if (totalPages < 1) totalPages = 1;
        if (page > totalPages) page = totalPages;

        List<Account_1> accounts = accountDAO().getPage(page, search, role);

        req.setAttribute("accounts", accounts);
        req.setAttribute("currentPage", page);
        req.setAttribute("totalPages", totalPages);

        req.getRequestDispatcher("/admin/admin-accounts.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String action = trim(req.getParameter("action"));
        if (action.isEmpty()) action = "create";

        switch (action) {
            case "update":
                handleUpdate(req, resp);
                return;
            case "delete":
                handleDelete(req, resp);
                return;
            default:
                handleCreate(req, resp);
        }
    }

    /** Original "Thêm tài khoản" flow. */
    private void handleCreate(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String fullName = trim(req.getParameter("fullName"));
        String email = trim(req.getParameter("email"));
        String phone = trim(req.getParameter("phone"));
        String role = trim(req.getParameter("role")).toUpperCase();
        String page = req.getParameter("page");

        if (!"STAFF".equals(role) && !"MANAGER".equals(role)) {
            role = "STAFF";
        }

        String error = validateProfileFields(fullName, email, phone);
        if (error == null && accountDAO().emailExists(email)) {
            error = "Email này đã được sử dụng.";
        }

        String tempPassword = null;
        if (error == null) {
            tempPassword = accountDAO().createAccount(fullName, email, phone, role);
            if (tempPassword == null) {
                error = "Không thể tạo tài khoản. Vui lòng thử lại.";
            }
        }

        forwardWithPage(req, resp, page);
        if (error != null) {
            req.setAttribute("createAccountError", error);
        } else {
            req.setAttribute("createAccountSuccessName", fullName);
            req.setAttribute("createAccountSuccessEmail", email);
            req.setAttribute("createAccountSuccessPassword", tempPassword);
            activityLogDAO.insertLog(currentAdminId(req),
                    "Tạo tài khoản mới \"" + fullName + "\" (" + email + "), role " + role);
        }
        req.getRequestDispatcher("/admin/admin-accounts.jsp").forward(req, resp);
    }

    /** "Sửa" (pencil icon) flow — pre-filled edit form, same field rules as create minus password/role restriction. */
    private void handleUpdate(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        int accountId = parseInt(req.getParameter("accountId"));
        String fullName = trim(req.getParameter("fullName"));
        String email = trim(req.getParameter("email"));
        String phone = trim(req.getParameter("phone"));
        String role = trim(req.getParameter("role")).toUpperCase();
        String status = trim(req.getParameter("status")).toUpperCase();
        String lockedUntilRaw = trim(req.getParameter("lockedUntil")); // datetime-local: "yyyy-MM-ddTHH:mm"
        String page = req.getParameter("page");

        if (!"STAFF".equals(role) && !"MANAGER".equals(role) && !"CUSTOMER".equals(role) && !"ADMIN".equals(role)) {
            role = "CUSTOMER";
        }
        if (!"ACTIVE".equals(status) && !"INACTIVE".equals(status) && !"LOCKED".equals(status)) {
            status = "ACTIVE";
        }

        String error = validateProfileFields(fullName, email, phone);
        if (error == null && accountDAO().emailExistsExcluding(email, accountId)) {
            error = "Email này đã được sử dụng bởi tài khoản khác.";
        }

        Timestamp lockedUntil = null;
        if (error == null && "LOCKED".equals(status)) {
            if (lockedUntilRaw.isEmpty()) {
                error = "Vui lòng chọn thời điểm mở khóa cho tài khoản bị Locked.";
            } else {
                try {
                    LocalDateTime parsed = LocalDateTime.parse(lockedUntilRaw);
                    if (!parsed.isAfter(LocalDateTime.now())) {
                        error = "Thời điểm mở khóa phải ở trong tương lai.";
                    } else {
                        lockedUntil = Timestamp.valueOf(parsed);
                    }
                } catch (DateTimeParseException e) {
                    error = "Thời điểm mở khóa không hợp lệ.";
                }
            }
        }

        if (error == null) {
            boolean ok = accountDAO().updateAccount(accountId, fullName, email, phone, role, status, lockedUntil);
            if (!ok) {
                error = "Không thể cập nhật tài khoản. Vui lòng thử lại.";
            } else {
                activityLogDAO.insertLog(currentAdminId(req),
                        "Cập nhật tài khoản #" + accountId + " (" + email + ") — role=" + role + ", status=" + status);
            }
        }

        forwardWithPage(req, resp, page);
        if (error != null) {
            req.setAttribute("editAccountError", error);
            req.setAttribute("editAccountId", accountId);
            req.setAttribute("editFullName", fullName);
            req.setAttribute("editEmail", email);
            req.setAttribute("editPhone", phone);
            req.setAttribute("editRole", role);
            req.setAttribute("editStatus", status);
            req.setAttribute("editLockedUntil", lockedUntilRaw);
        }
        req.getRequestDispatcher("/admin/admin-accounts.jsp").forward(req, resp);
    }

    /** Trash-icon flow — permanently removes the account (see AccountDAO.deleteAccount for FK caveats). */
    private void handleDelete(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        int accountId = parseInt(req.getParameter("accountId"));
        String page = req.getParameter("page");

        String error = accountDAO().deleteAccount(accountId);

        forwardWithPage(req, resp, page);
        if (error != null) {
            req.setAttribute("deleteAccountError", error);
        } else {
            activityLogDAO.insertLog(currentAdminId(req), "Xóa tài khoản #" + accountId);
        }
        req.getRequestDispatcher("/admin/admin-accounts.jsp").forward(req, resp);
    }

    /** Re-loads the accounts list/pagination onto the request so we can forward (not redirect) back to the view. */
    private void forwardWithPage(HttpServletRequest req, HttpServletResponse resp, String pageParam) {
        String search = req.getParameter("q");
        String role = req.getParameter("role");
        int p = parsePage(pageParam);
        int totalRows = accountDAO().countAll(search, role);
        int totalPages = (int) Math.ceil(totalRows / (double) AccountDAO_1.PAGE_SIZE);
        if (totalPages < 1) totalPages = 1;
        if (p > totalPages) p = totalPages;

        req.setAttribute("accounts", accountDAO().getPage(p, search, role));
        req.setAttribute("currentPage", p);
        req.setAttribute("totalPages", totalPages);
    }

    private String validateProfileFields(String fullName, String email, String phone) {
        if (fullName.isEmpty() || fullName.length() < 2 || fullName.length() > 100) {
            return "Họ và tên phải từ 2–100 ký tự.";
        }
        if (email.isEmpty() || !email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            return "Email không hợp lệ.";
        }
        if (!phone.isEmpty() && !ValidationUtil.isValidPhone(phone)) {
           
            return "Số điện thoại không hợp lệ (phải là 10 số bắt đầu bằng 0, hoặc dạng +84xxxxxxxxx).";
        }
        return null;
    }

    private String trim(String s) {
        return s == null ? "" : s.trim();
    }

    private int parseInt(String raw) {
        try {
            return Integer.parseInt(raw);
        } catch (Exception e) {
            return 0;
        }
    }

    private int parsePage(String raw) {
        try {
            int p = Integer.parseInt(raw);
            return p < 1 ? 1 : p;
        } catch (Exception e) {
            return 1;
        }
    }
}
