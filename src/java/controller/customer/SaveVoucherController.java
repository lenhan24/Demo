/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */

package controller.customer;

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
import model.VoucherDTO;

/**
 *
 * @author GiGaByte
 */
@WebServlet(name="SaveVoucherController", urlPatterns={"/saveVoucher"})
public class SaveVoucherController extends HttpServlet {
   
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
            out.println("<title>Servlet SaveVoucherController</title>");  
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet SaveVoucherController at " + request.getContextPath () + "</h1>");
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
        Object accountIdObj = session.getAttribute("accountId");
        int accountId = (accountIdObj != null) ? (Integer) accountIdObj : 0;  
        
        // Nếu chưa đăng nhập (accountId == 0), chuyển hướng người dùng sang trang login
        if (accountId == 0) {
            response.sendRedirect("Login.jsp"); // Thay bằng URL trang login thực tế của bạn
            return;
        }

        // 2. Lấy voucherId truyền từ giao diện sang
        String voucherIdRaw = request.getParameter("voucherId");
        
        if (voucherIdRaw != null && !voucherIdRaw.trim().isEmpty()) {
            try {
                int voucherId = Integer.parseInt(voucherIdRaw);
                
                // 3. Gọi DAO để thực hiện lưu vào cơ sở dữ liệu
                DAOVoucher dao = new DAOVoucher();
                boolean isSaved = dao.saveVoucherForAccount(accountId, voucherId);
                
                if (isSaved) {
      
                    session.setAttribute("msg", "Lưu voucher thành công!");
                } else {
                    session.setAttribute("msgErr", "Không thể lưu mã này!");
                }
                
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        
        // 4. Quay trở về trang home để cập nhật lại danh sách và giao diện
        response.sendRedirect("home");
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
