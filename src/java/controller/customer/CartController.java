/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */

package controller.customer;

import dal.AccountDAO;
import dal.CartDAO;
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
import model.CartItemDTO;

/**
 *
 * @author GiGaByte
 */
@WebServlet(name="CartController", urlPatterns={"/cart"})
public class CartController extends HttpServlet {
   
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
            out.println("<title>Servlet CartController</title>");  
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet CartController at " + request.getContextPath () + "</h1>");
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

         AccountDAO accountDAO = new AccountDAO(); 
            Account account = accountDAO.getAccountByEmail(email); // Bạn cần tự viết hàm này trong AccountDAO

            // 4. Gọi DAO để Insert/Update vào giỏ hàng
            CartDAO cartDAO = new CartDAO();
        List<CartItemDTO> cartItems = cartDAO.getAllCartItem(account.getAccountId());
        request.setAttribute("cartItems", cartItems);
          request.setAttribute("totalItems", cartItems.size());

            // Đọc + xoá flash message (ví dụ lỗi hết hàng lúc đặt hàng thất bại
            // ở PaymentController) — trước đây dùng "errorMsg" nhưng trang này
            // chưa từng đọc nó, khiến lỗi bị mất hoàn toàn không hiển thị.
            Object flash = session.getAttribute("flashMessage");
            if (flash != null) {
                request.setAttribute("flashMessage", flash);
                request.setAttribute("flashError", session.getAttribute("flashError"));
                session.removeAttribute("flashMessage");
                session.removeAttribute("flashError");
            }

            // 7. Chuyển tiếp (forward) sang trang cart.jsp để hiển thị giao diện
           request.getRequestDispatcher("Cart.jsp").forward(request, response);
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
