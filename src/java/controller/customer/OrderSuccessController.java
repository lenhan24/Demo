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
import java.time.LocalDate;
import java.util.Calendar;
//import java.util.Date;
import java.sql.Date;
import java.util.List;
import model.Account;
import model.OrderDTO;
import model.OrderDetailDTO;

/**
 *
 * @author GiGaByte
 */
@WebServlet(name="OrderSuccessController", urlPatterns={"/orderSuccess"})
public class OrderSuccessController extends HttpServlet {
   
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
            out.println("<title>Servlet OrderSuccessController</title>");  
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet OrderSuccessController at " + request.getContextPath () + "</h1>");
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
        String orderid =  request.getParameter("orderId");
        AccountDAO accountdao = new AccountDAO();
        Account account = accountdao.getAccountByEmail(email);
        DAOOrder daoOrder = new DAOOrder();
        OrderDTO order = daoOrder.getOrderById(Integer.parseInt(orderid), account.getAccountId());
if (order == null) {
    response.sendRedirect("home");
    return;
}
List<OrderDetailDTO> details = daoOrder.getOrderDetails(Integer.parseInt(orderid));

// Tính khoảng ngày giao dự kiến dựa trên order_date, không hardcode ở JSP
LocalDate estimatedFromLocal = LocalDate.now();
LocalDate estimatedToLocal = LocalDate.now().plusDays(4);

// Convert sang java.sql.Date để fmt:formatDate dùng được
Date estimatedFrom = Date.valueOf(estimatedFromLocal);
Date estimatedTo = Date.valueOf(estimatedToLocal);

request.setAttribute("order", order);
request.setAttribute("orderDetails", details);
request.setAttribute("estimatedFrom", estimatedFrom);
request.setAttribute("estimatedTo", estimatedTo);
request.getRequestDispatcher("SuccessOrder.jsp").forward(request, response);
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
