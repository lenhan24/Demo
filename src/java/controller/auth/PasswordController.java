/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */

package controller.auth;

import dal.AccountDAO;
import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.websocket.Session;
import model.Account;

/**
 *
 * @author GiGaByte
 */
@WebServlet(name="PasswordController", urlPatterns={"/changepass"})
public class PasswordController extends HttpServlet {
   
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
            out.println("<title>Servlet PasswordController</title>");  
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet PasswordController at " + request.getContextPath () + "</h1>");
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
        processRequest(request, response);
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
        HttpSession session = request.getSession();
    String email = (String) session.getAttribute("userEmail");
    
    // Nếu chưa đăng nhập thì đẩy về trang login
    if (email == null) {
        response.sendRedirect("login"); // Đổi thành URL trang login của bạn
        return;
    }
    
    // 2. Lấy dữ liệu từ form
    String currPass = request.getParameter("currPass");
    String newPass = request.getParameter("newPass");
    String renewPass = request.getParameter("renewPass");
    
    // 3. Kiểm tra tính hợp lệ (Validation)
    if (newPass == null || newPass.length() < 8) {
        session.setAttribute("messError", "Mật khẩu mới phải có tối thiểu 8 ký tự!");
        response.sendRedirect("account"); // Trở về trang account
        return;
    }
    
    if (!newPass.equals(renewPass)) {
        session.setAttribute("messError", "Xác nhận mật khẩu không khớp!");
        response.sendRedirect("account");
        return;
    }
    
    // 4. Lấy thông tin user từ DB để kiểm tra mật khẩu hiện tại
    AccountDAO dao = new AccountDAO();
    Account acc = dao.getAccountByEmail(email);
    
    // Verify mật khẩu hiện tại bằng PasswordUtil — TRƯỚC ĐÂY so sánh
    // acc.getPasswordHash().equals(currPass) là so sánh CHUỖI THÔ, không hề
    // hash gì cả (dù comment ngay phía trên từng nhắc tới việc này).
    if (!utilities.PasswordUtil.verifyPassword(currPass, acc.getPasswordHash())) {
        session.setAttribute("messError", "Mật khẩu hiện tại không chính xác!");
        response.sendRedirect("account");
        return;
    }
    
    // 5. Nếu mọi thứ Ok -> Băm mật khẩu mới rồi mới cập nhật (không lưu plain text)
    String newPasswordHash = utilities.PasswordUtil.hashPassword(newPass);
    boolean isSuccess = dao.changePassword(email, newPasswordHash);
    
    if (isSuccess) {
        session.setAttribute("messSuccess", "Đổi mật khẩu thành công!");
        // Ghi log đổi mật khẩu.
        new dal.ActivityLogDAO().insertLog(acc.getAccountId(), "Đổi mật khẩu");
    } else {
        session.setAttribute("messError", "Có lỗi xảy ra khi cập nhật. Vui lòng thử lại!");
    }
    
    // 6. Quay lại trang tài khoản
    response.sendRedirect("account");
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
