package controller.admin;

import dal.AccountDAO_1;
import dal.ActivityLogDAO;
import model.Account;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;
import model.Account_1;

@WebServlet(name = "AdminRolesServlet", urlPatterns = {"/admin/roles"})
public class AdminRolesServlet extends HttpServlet {

    // Fresh AccountDAO (and thus a fresh DB connection) per call — see the same
    // comment in AdminAccountsServlet for why this isn't a shared instance field.
    private AccountDAO_1 accountDAO() {
        return new AccountDAO_1();
    }

    private final ActivityLogDAO activityLogDAO = new ActivityLogDAO();

    private int currentAdminId(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
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
        if (totalPages < 1) {
            totalPages = 1;
        }
        if (page > totalPages) {
            page = totalPages;
        }

        List<Account_1> accounts = accountDAO().getPage(page, search, role);

        req.setAttribute("accounts", accounts);
        req.setAttribute("currentPage", page);
        req.setAttribute("totalPages", totalPages);

        req.getRequestDispatcher("/admin/admin-roles.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        int accountId = Integer.parseInt(req.getParameter("accountId"));
        String newRole = req.getParameter("newRole");
        if (newRole != null) {
            newRole = newRole.trim().toUpperCase();
        }
        if ("CUSTOMER".equals(newRole) || "STAFF".equals(newRole) || "MANAGER".equals(newRole)) {
            accountDAO().updateRole(accountId, newRole);
            activityLogDAO.insertLog(currentAdminId(req),
                    "Đổi role tài khoản #" + accountId + " thành " + newRole);
        }

        String page = req.getParameter("page");
        StringBuilder redirect = new StringBuilder(req.getContextPath())
                .append("/admin/roles?page=").append(page == null ? "1" : page);
        if (req.getParameter("q") != null && !req.getParameter("q").isBlank()) {
            redirect.append("&q=").append(req.getParameter("q"));
        }
        if (req.getParameter("role") != null && !req.getParameter("role").isBlank()) {
            redirect.append("&role=").append(req.getParameter("role"));
        }
        resp.sendRedirect(redirect.toString());
    }

    private int parsePage(String raw) {
        if (raw == null) {
            return 1;
        }
        try {
            int p = Integer.parseInt(raw);
            return p < 1 ? 1 : p;
        } catch (NumberFormatException e) {
            return 1;
        }
    }
}
