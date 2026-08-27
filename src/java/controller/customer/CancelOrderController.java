/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller.customer;

import dal.AccountDAO;
import dal.DAOOrder;
import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Account;
import model.OrderDTO;

/**
 *
 * @author GiGaByte
 */
@WebServlet(name = "CancelOrderController", urlPatterns = {"/cancelOrder"})
public class CancelOrderController extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            /* TODO output your page here. You may use following sample code. */
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet CancelOrderController</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet CancelOrderController at " + request.getContextPath() + "</h1>");
            out.println("</body>");
            out.println("</html>");
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        String email = (String) session.getAttribute("userEmail");
        if (email == null) {
            response.sendRedirect("Login.jsp");
            return;
        }

        AccountDAO daoAccount = new AccountDAO();
        Account account = daoAccount.getAccountByEmail(email);
        if (account == null) {
            response.sendRedirect("Login.jsp");
            return;
        }

        int orderId;
        try {
            orderId = Integer.parseInt(request.getParameter("orderId"));
        } catch (Exception e) {
            response.sendRedirect("myorders");
            return;
        }

        DAOOrder daoOrder = new DAOOrder();

        // Xác nhận đơn tồn tại và thuộc về chính account này trước khi làm gì khác (chống IDOR)
        OrderDTO order = daoOrder.getOrderById(orderId, account.getAccountId());
        if (order == null) {
            session.setAttribute("errorMsg", "Không tìm thấy đơn hàng.");
            response.sendRedirect("myorders");
            return;
        }

        // Đơn đang chờ xác nhận thanh toán VietQR (chưa rõ khách đã chuyển
        // khoản hay chưa) không được tự hủy nữa — báo rõ lý do thay vì để
        // rơi vào thông báo lỗi chung chung.
        if ("PENDING_PAYMENT".equals(order.getStatus())) {
            session.setAttribute("errorMsg", "Đơn đang chờ xác nhận thanh toán, chưa thể tự hủy lúc này. "
                    + "Vui lòng liên hệ hotline hoặc đợi Booky Mart xác nhận thanh toán trước.");
            response.sendRedirect("myorders");
            return;
        }

        int cancelled = daoOrder.cancelOrder(orderId, account.getAccountId(), "CUSTOMER_REQUEST");
        if (cancelled != 1 && cancelled != 2) {
            session.setAttribute("errorMsg", "Đơn hàng không còn ở trạng thái có thể huỷ.");
            response.sendRedirect("myorders");
            return;
        }
        // Ghi log hủy đơn (Customer).
        new dal.ActivityLogDAO().insertLog(account.getAccountId(),
                "Hủy đơn hàng #" + order.getOrderCode());
        session.setAttribute("successMsg", cancelled == 2
                ? "Đã huỷ đơn hàng #" + order.getOrderCode() + " thành công. Đơn đã thanh toán online nên sẽ chuyển sang chờ hoàn tiền, Booky Mart sẽ liên hệ hoàn tiền cho bạn sớm nhất."
                : "Đã huỷ đơn hàng #" + order.getOrderCode() + " thành công.");
        response.sendRedirect("myorders");
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
