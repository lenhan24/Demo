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
import model.Account;

/**
 *
 * @author GiGaByte
 */
@WebServlet(name="SignUpController", urlPatterns={"/register"})
public class SignUpController extends HttpServlet {
   
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
            out.println("<title>Servlet SignUpController</title>");  
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet SignUpController at " + request.getContextPath () + "</h1>");
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
    
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        AccountDAO accountDAO = new AccountDAO();
        // 1. Lấy dữ liệu từ Request (form submit)
        String fullName = request.getParameter("fullName");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        
        // 2. Validation cơ bản ở backend
        if (fullName == null || email == null || password == null || 
            fullName.trim().isEmpty() || email.trim().isEmpty() || password.isEmpty()) {
            
            request.setAttribute("errorMessage", "Vui lòng điền đầy đủ thông tin.");
            request.getRequestDispatcher("SignUp.jsp").forward(request, response);
            return;
        }

        // 2b. Validate ĐỊNH DẠNG email — trước đây chỉ check không rỗng,
        // không check có đúng dạng email hay không (mục 3.5 đề bài).
        if (!utilities.ValidationUtil.isValidEmail(email)) {
            request.setAttribute("errorMessage", "Email không đúng định dạng.");
            request.setAttribute("oldFullName", fullName);
            request.getRequestDispatcher("SignUp.jsp").forward(request, response);
            return;
        }

        // 3. Kiểm tra Email tồn tại
        if (accountDAO.checkEmailExist(email)) {
            
            request.setAttribute("errorMessage", "Email này đã được sử dụng để đăng ký tài khoản khác.");
            // Giữ lại dữ liệu cũ để người dùng không phải nhập lại họ tên
            request.setAttribute("oldFullName", fullName); 
            request.setAttribute("oldEmail", email);
            
            request.getRequestDispatcher("SignUp.jsp").forward(request, response);
            return;
        }

        // 4. Mã hóa mật khẩu rồi mới lưu vào Database — TRƯỚC ĐÂY dòng này bị
        // comment lại, dẫn tới password được lưu thẳng dạng plain text dù cột
        // tên là password_hash.
        String passwordHash = utilities.PasswordUtil.hashPassword(password);
        boolean isRegistered = accountDAO.registerAccount(fullName, email, passwordHash);

        // 5. Điều hướng kết quả
        if (isRegistered) {
            // Đăng ký thành công -> Lưu Session
            HttpSession session = request.getSession();
            session.setMaxInactiveInterval(86400);
            session.setAttribute("userEmail", email);
            Account acc = accountDAO.getAccountByEmail(email);
            session.setAttribute("accountId", acc.getAccountId());
            // Chuyển hướng sang trang home
            response.sendRedirect("home");
        } else {
            // Lỗi database (insert thất bại)
            request.setAttribute("errorMessage", "Lỗi hệ thống. Vui lòng thử lại sau.");
            request.getRequestDispatcher("register.jsp").forward(request, response);
        }
    }
     

    /** 
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */

    /** 
     * Returns a short description of the servlet.
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
