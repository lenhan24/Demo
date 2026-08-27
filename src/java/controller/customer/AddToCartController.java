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
@WebServlet(name="AddToCartController", urlPatterns={"/addtocart"})
public class AddToCartController extends HttpServlet {
   
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
            out.println("<title>Servlet AddToCartController</title>");  
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet AddToCartController at " + request.getContextPath () + "</h1>");
            out.println("</body>");
            out.println("</html>");
        }
    } 

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /** 
     * Handles the HTTP <code>GET</code> method.
     * KHÔNG còn xử lý logic ở đây nữa — thêm giỏ hàng là hành động làm THAY ĐỔI
     * dữ liệu (trừ liên quan tới tồn kho), theo chuẩn HTTP phải là POST, không
     * phải GET (GET phải "an toàn", không được có side-effect).
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        response.sendRedirect("home");
    } 

    /** 
     * Handles the HTTP <code>POST</code> method — nơi xử lý thêm giỏ hàng thật.
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

        String rawProductId = request.getParameter("productId");
        // Nơi khách bị chuyển về khi có lỗi — quay lại đúng trang chi tiết sản
        // phẩm thay vì about:home, để không mất ngữ cảnh đang xem sản phẩm nào.
        String backUrl = "productdt?id=" + (rawProductId == null ? "" : rawProductId);

        try {
            int productId = Integer.parseInt(rawProductId);
            // Integer.parseInt cho phép số âm ("-5" là hợp lệ) -> phải tự chặn
            // thêm ở đây, không được dựa vào parseInt để lọc số âm.
            int quantity = Integer.parseInt(request.getParameter("quantity"));

            AccountDAO accountDAO = new AccountDAO(); 
            Account account = accountDAO.getAccountByEmail(email);

            CartDAO cartDAO = new CartDAO();
            // addToCart giờ trả về null nếu thành công, hoặc thông điệp lỗi cụ
            // thể nếu số lượng không hợp lệ / vượt tồn kho thật — không còn
            // "âm thầm" chấp nhận mọi số lượng như trước.
            String error = cartDAO.addToCart(account.getAccountId(), productId, quantity);

            if (error != null) {
                session.setAttribute("flashMessage", error);
                session.setAttribute("flashError", true);
                response.sendRedirect(backUrl);
                return;
            }

            response.sendRedirect("cart");
        } catch (NumberFormatException e) {
            e.printStackTrace();
            session.setAttribute("flashMessage", "Số lượng không hợp lệ.");
            session.setAttribute("flashError", true);
            response.sendRedirect(backUrl);
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("error.jsp");
        }
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
