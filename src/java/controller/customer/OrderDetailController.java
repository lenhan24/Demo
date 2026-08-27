package controller.customer;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */


//import .*;
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
import java.util.List;
import model.Account;
import model.OrderDTO;
import model.OrderDetailDTO;

/**
 *
 * @author GiGaByte
 */
@WebServlet(urlPatterns={"/orders"})
public class OrderDetailController extends HttpServlet {
   
    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
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
            out.println("<title>Servlet OrderDetailController</title>");  
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet OrderDetailController at " + request.getContextPath () + "</h1>");
            out.println("</body>");
            out.println("</html>");
        }
    } 

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /** 
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
          HttpSession session = request.getSession();
        String email = (String) session.getAttribute("userEmail");
        if (email == null) {
            response.sendRedirect("Login.jsp");
            return;
        }

        String idParam = request.getParameter("id");
        int orderId;
        try {
            orderId = Integer.parseInt(idParam);
        } catch (Exception e) {
            response.sendRedirect("home");
            return;
        }

        AccountDAO daoAccount = new AccountDAO();
        Account account = daoAccount.getAccountByEmail(email);
        if (account == null) {
            response.sendRedirect("Login.jsp");
            return;
        }

        DAOOrder daoOrder = new DAOOrder();

        // getOrderById đã lọc theo account_id -> khách A không xem được đơn của khách B (chống IDOR)
        OrderDTO order = daoOrder.getOrderById(orderId, account.getAccountId());
        if (order == null) {
            response.sendRedirect("home");
            return;
        }

        // Chỉ gọi sau khi đã xác nhận order thuộc về account này ở bước trên
        List<OrderDetailDTO> details = daoOrder.getOrderDetails(orderId);
         System.out.println(order);
        // Quy đổi status -> mức độ (rank) để JSP so sánh dễ dàng, tránh so chuỗi rải rác nhiều nơi
        int statusRank = mapStatusToRank(order.getStatus());
        System.out.println(statusRank);
        System.out.println(order);
        for (OrderDetailDTO detail : details) {
            System.out.println(detail);
        }
        request.setAttribute("order", order);
        request.setAttribute("orderDetails", details);
        request.setAttribute("statusRank", statusRank);

        // Đọc + xoá flash message (kết quả của xác nhận nhận hàng / yêu cầu
        // hoàn hàng vừa thực hiện ở OrderActionController) ngay lập tức, tránh
        // hiện lặp lại ở lần load trang tiếp theo.
        Object flash = session.getAttribute("flashMessage");
        if (flash != null) {
            request.setAttribute("flashMessage", flash);
            request.setAttribute("flashError", session.getAttribute("flashError"));
            session.removeAttribute("flashMessage");
            session.removeAttribute("flashError");
        }

        request.getRequestDispatcher("OrderDetail.jsp").forward(request, response);
    } 
private int mapStatusToRank(String status) {
        switch (status) {
            case "PENDING_PAYMENT": return 0;
            case "PENDING": return 1;
            case "PROCESSING": return 2;
            case "SHIPPED": return 3;
            case "DELIVERED": return 4;
            default: return -1;
        }
    }
    /** 
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
     * Returns a short description of the servlet.
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
