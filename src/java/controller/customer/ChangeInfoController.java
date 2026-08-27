/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */

package controller.customer;

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
@WebServlet(name="ChangeInfoController", urlPatterns={"/changeinfo"})
public class ChangeInfoController extends HttpServlet {
   
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
            out.println("<title>Servlet ChangeInfoController</title>");  
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet ChangeInfoController at " + request.getContextPath () + "</h1>");
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
        request.setCharacterEncoding("UTF-8");
        
        // 2. Lấy Session hiện tại và kiểm tra xem người dùng đã đăng nhập chưa
        HttpSession session = request.getSession();
        String email = (String) session.getAttribute("userEmail");
        AccountDAO accdao = new AccountDAO();
        Account currentAccount = accdao.getAccountByEmail(email);
        
        if (currentAccount == null) {
            // Nếu chưa đăng nhập, chuyển hướng về trang login
            response.sendRedirect("Login.jsp");
            return;
        }
        
        // 3. Đọc dữ liệu từ các ô Input thông qua thuộc tính 'name'
        String fullName = request.getParameter("fullName");
        String phone = request.getParameter("phone");
        
        // Validation cơ bản phía Server
        if (fullName == null || fullName.length() < 5 || fullName.trim().isEmpty()) {
            request.setAttribute("errorMsg", "Họ và tên không hợp lệ!");
            request.getRequestDispatcher("Account.jsp").forward(request, response);
            return;
        }

        // Validate ĐỊNH DẠNG số điện thoại — TRƯỚC ĐÂY chỉ check "không có chữ
        // cái" (for-loop từng ký tự), nên vẫn lọt qua số 1-2 chữ số, số quá dài,
        // hay ký tự đặc biệt như "--". Đồng thời sửa luôn 1 lỗi NPE tiềm ẩn:
        // nếu người dùng để trống ô số điện thoại (cột phone cho phép NULL),
        // code cũ gọi thẳng phone.toCharArray() sẽ ném NullPointerException.
        if (phone != null && !phone.trim().isEmpty() && !utilities.ValidationUtil.isValidPhone(phone)) {
            request.setAttribute("errorMsg", "Số điện thoại không đúng định dạng (VD: 0912345678).");
            request.getRequestDispatcher("Account.jsp").forward(request, response);
            return;
        }
               
        
        // 4. Gọi DAO để cập nhật xuống Database SQL Server
        int accountId = currentAccount.getAccountId();
        boolean isSuccess = accdao.updateAccountInfo(accountId, fullName, phone);
        
        if (isSuccess) {
            // CỰC KỲ QUAN TRỌNG: Cập nhật lại đối tượng account trong Session 
            // để trên giao diện JSP đổi theo ngay lập tức mà không cần log out đăng nhập lại
            currentAccount.setFullName(fullName);
            currentAccount.setPhone(phone);
            session.setAttribute("account", currentAccount);
            
            request.setAttribute("successMsg", "Cập nhật thông tin thành công!");
        } else {
            session.setAttribute("errorMsg", "Đã xảy ra lỗi hệ thống, vui lòng thử lại sau!");
        }
        
        // 5. Áp dụng cơ chế Post-Redirect-Get (Chuyển hướng lại về trang changeinfo để tránh trùng lặp dữ liệu khi F5)
       request.getRequestDispatcher("Account.jsp").forward(request, response);
    
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
