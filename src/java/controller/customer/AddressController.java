/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */

package controller.customer;

import dal.AddressDAO;
import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 *
 * @author GiGaByte
 */
@WebServlet(name="AddressController", urlPatterns={"/address-action"})
public class AddressController extends HttpServlet {
   
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
            out.println("<title>Servlet AddressController</title>");  
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet AddressController at " + request.getContextPath () + "</h1>");
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
       String action = request.getParameter("action");
    AddressDAO dao = new AddressDAO();

    if ("delete".equals(action)) {
        int id = Integer.parseInt(request.getParameter("addressId"));
        dao.deleteAddress(id);
    } else if ("setDefault".equals(action)) {
        int accId = Integer.parseInt(request.getParameter("accountId"));
        int addrId = Integer.parseInt(request.getParameter("addressId"));
        dao.setDefaultAddress(accId, addrId);
    } else if ("update".equals(action)) {
        // MỚI: xử lý nút "Sửa" địa chỉ — TRƯỚC ĐÂY route/action này chưa
        // tồn tại nên nút "Sửa" trên Account.jsp không có tác dụng gì.
        int addrId = Integer.parseInt(request.getParameter("addressId"));
        String name = request.getParameter("receiverName");
        String phone = request.getParameter("receiverPhone");
        String fullAddress = request.getParameter("fullAddress");
        dao.updateAddress(addrId, name, phone, fullAddress);
    }

    // Sau khi xử lý xong, chuyển hướng về trang account để load lại danh sách
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
