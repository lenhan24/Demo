/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */

package controller.customer;

import dal.ProductDAO;
import dal.ReviewDAO;
import dal.AccountDAO;
import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Account;
import model.ProductDTO;
import model.ProductDetailDTO;
import model.ReviewDTO;
import java.util.List;
import java.util.Map;

/**
 *
 * @author GiGaByte
 */
@WebServlet(name="ProductDetailController", urlPatterns={"/productdt"})
public class ProductDetailController extends HttpServlet {
   
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
            out.println("<title>Servlet ProductDetailController</title>");  
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet ProductDetailController at " + request.getContextPath () + "</h1>");
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

System.out.println("=== ProductDetailServlet doGet called ===");

    String idParam = request.getParameter("id");
    System.out.println("idParam = " + idParam);

    if (idParam == null || idParam.isEmpty()) {
        System.out.println("ERROR: id param is null or empty");
        response.sendRedirect("home");
        return;
    }

    try {
        int productId = Integer.parseInt(idParam);
        System.out.println("productId = " + productId);

        ProductDAO dao = new ProductDAO();
        ProductDetailDTO product = dao.getProductById(productId);

        System.out.println("product = " + product); // null hay có object?

        if (product != null) {
            request.setAttribute("product", product);
            // Số lượng THẬT có thể mua ngay — đọc thẳng tbProduct.stock_quantity
            // (không còn khái niệm lô ACTIVE/QUEUED riêng nữa).
            int availableStock = dao.getStockQuantity(productId);
            request.setAttribute("availableStock", availableStock);

            // --- Dữ liệu đánh giá thật (tbReview), thay cho HTML tĩnh cũ ---
            ReviewDAO reviewDAO = new ReviewDAO();
            List<ReviewDTO> reviews = reviewDAO.getVisibleReviewsByProduct(productId);
            Map<Integer, Integer> ratingBreakdown = reviewDAO.getRatingBreakdown(productId);
            request.setAttribute("reviews", reviews);
            request.setAttribute("ratingBreakdown", ratingBreakdown);
            request.setAttribute("ratingCount", reviewDAO.getRatingCount(productId));

            // Nếu khách đã đăng nhập và có 1 lần mua+nhận hàng chưa review ->
            // hiện form viết đánh giá, gắn sẵn đúng order_detail_id đó.
            String email = (String) request.getSession().getAttribute("userEmail");
            if (email != null) {
                Account account = new AccountDAO().getAccountByEmail(email);
                if (account != null) {
                    Integer reviewableOrderDetailId = reviewDAO.getReviewableOrderDetailId(account.getAccountId(), productId);
                    request.setAttribute("reviewableOrderDetailId", reviewableOrderDetailId);
                }
            }

            // Đọc flash message (lỗi thêm giỏ hàng...) rồi xoá khỏi session ngay,
            // tránh hiển thị lặp lại ở lần load trang tiếp theo.
            jakarta.servlet.http.HttpSession session = request.getSession(false);
            if (session != null) {
                Object flash = session.getAttribute("flashMessage");
                if (flash != null) {
                    request.setAttribute("flashMessage", flash);
                    request.setAttribute("flashError", session.getAttribute("flashError"));
                    session.removeAttribute("flashMessage");
                    session.removeAttribute("flashError");
                }
            }

            System.out.println("Forwarding to ProductDetail.jsp");
            request.getRequestDispatcher("ProductDetail.jsp").forward(request, response);
        } else {
            System.out.println("Product null -> redirect home");
            response.sendRedirect("home");
        }

    } catch (NumberFormatException e) {
        System.out.println("NumberFormatException: " + e.getMessage());
        response.sendRedirect("home");
    } catch (Exception e) {
        System.out.println("Unexpected exception: " + e.getMessage());
        e.printStackTrace();
        response.sendError(500, e.getMessage()); // hiện lỗi thay vì trang trắng
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
