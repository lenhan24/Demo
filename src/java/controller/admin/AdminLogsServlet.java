package controller.admin;

import dal.ActivityLogDAO;
import model.ActivityLog;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet(name = "AdminLogsServlet", urlPatterns = {"/admin/logs"})
public class AdminLogsServlet extends HttpServlet {

    // Fresh ActivityLogDAO (and thus a fresh DB connection) per call — see the
    // same comment in AdminAccountsServlet for why this isn't a shared instance field.
    private ActivityLogDAO logDAO() {
        return new ActivityLogDAO();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        int page = parsePage(req.getParameter("page"));
        int totalRows = logDAO().countAll();
        int totalPages = (int) Math.ceil(totalRows / (double) ActivityLogDAO.PAGE_SIZE);
        if (totalPages < 1) totalPages = 1;
        if (page > totalPages) page = totalPages;

        List<ActivityLog> logs = logDAO().getPage(page);

        req.setAttribute("logs", logs);
        req.setAttribute("currentPage", page);
        req.setAttribute("totalPages", totalPages);

        req.getRequestDispatcher("/admin/admin-logs.jsp").forward(req, resp);
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
