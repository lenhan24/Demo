/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */

package controller.auth;

import dal.AccountDAO;
import dal.DAOVoucher;
import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.util.Date;
import java.util.List;
import model.Account;
import model.VoucherDTO;

/**
 *
 * @author GiGaByte
 */
@WebServlet(name="LoginController", urlPatterns={"/login"})
public class LoginController extends HttpServlet {
   
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
            out.println("<title>Servlet LoginController</title>");  
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet LoginController at " + request.getContextPath () + "</h1>");
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
         request.getRequestDispatcher("Login.jsp").forward(request, response);
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
        response.setCharacterEncoding("UTF-8");

        String email = request.getParameter("email");
        String password = request.getParameter("password");
        AccountDAO accountDAO = new AccountDAO();
        // Validate cơ bản
        if (email == null || password == null || email.isEmpty() || password.isEmpty()) {
            request.setAttribute("errorMessage", "Vui lòng nhập đầy đủ email và mật khẩu.");
            request.getRequestDispatcher("Login.jsp").forward(request, response);
            return;
        }

        Account acc = accountDAO.getAccountByEmail(email,password);

        // Xử lý nếu tài khoản không tồn tại
        if (acc == null) {
            request.setAttribute("errorMessage", "Email hoặc mật khẩu không đúng hoặc tài khoản đã bị khóa. Vui lòng kiểm tra lại.");
            request.setAttribute("oldEmail", email);
            request.getRequestDispatcher("Login.jsp").forward(request, response);
            return;
            
        } else {
            // Đăng nhập thành công -> Reset số lần sai
            
            // Tạo Session
            HttpSession session = request.getSession();
            session.setMaxInactiveInterval(86400);
            session.setAttribute("userEmail", acc.getEmail());
            session.setAttribute("accountId", acc.getAccountId());
            session.setAttribute("userRole", accountDAO.getRoleByAccount(acc));
            
            // --- ĐOẠN SỬA ĐỔI: Điều hướng dựa theo Role ---
            String role = accountDAO.getRoleByAccount(acc); // Lấy role từ đối tượng account (CUSTOMER, STAFF, MANAGER, ADMIN)
            acc.setRole(role);
            session.setAttribute("account", acc);
            if (role != null) {
                switch (role) {
                    case "ADMIN":
                        response.sendRedirect("admin/roles"); // Hoặc trang admin_home.jsp tùy project của bạn
                        break;
                    case "MANAGER":
                        response.sendRedirect("manager/products"); // Trang dành cho Quản lý
                        break;
                    case "STAFF":
                        // SỬA: staff/inventory đã bị xóa (dồn hết vào staff/orders),
                        // trước đây trỏ vào route chết -> Staff đăng nhập xong bị lỗi.
                        response.sendRedirect("staff/orders"); // Trang dành cho Nhân viên
                        break;
                    case "CUSTOMER":
                        response.sendRedirect("home"); // Khách hàng chuẩn thì về trang home chung
                        break;
                    default:
                        // Trường hợp role lạ không khớp với constraint trong DB
                        response.sendRedirect("home");
                        break;
                }
            } else {
                // Nếu role bị null (đề phòng lỗi dữ liệu)
                response.sendRedirect("home");
            }
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
