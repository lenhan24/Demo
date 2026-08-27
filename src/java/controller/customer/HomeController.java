/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */

package controller.customer;

import dal.CategoryDAO;
import dal.DAOVoucher;
import dal.ProductDAO;
import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import model.CategoryDTO;
import model.ProductDTO;
import model.VoucherDTO;

/**
 *
 * @author GiGaByte
 */
@WebServlet(name="HomeController", urlPatterns={"/home"})
public class HomeController extends HttpServlet {
   
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
            out.println("<title>Servlet HomeController</title>");  
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet HomeController at " + request.getContextPath () + "</h1>");
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
        CategoryDAO categoryDAO = new CategoryDAO();
        ProductDAO productDAO = new ProductDAO();
        HttpSession session = request.getSession();
        session.setMaxInactiveInterval(86400);
        Object accountIdObj = session.getAttribute("accountId");
    int accountId = (accountIdObj != null) ? (Integer) accountIdObj : 0;  
        
//        DAOVoucher voucherDAO = new DAOVoucher();
        // 1. Lấy dữ liệu từ DB
        List<CategoryDTO> listCat = categoryDAO.getAllCategories();
        List<ProductDTO> listPro = productDAO.getTopProducts();
        List<ProductDTO> officeSupplies = productDAO.getProductsByCategoryId(6);
        DAOVoucher dao = new DAOVoucher();
        List<VoucherDTO> voucherList = dao.getAllActiveVouchers(accountId);
        List<Integer> savedVoucherIds = dao.getSavedVoucherIds(accountId); // hàm có sẵn, không cần sửa DAO

        request.setAttribute("voucherList", voucherList);
        request.setAttribute("savedVoucherIds", savedVoucherIds);
        request.setAttribute("officeSupplies", officeSupplies);
        
        // 2. Set thuộc tính lên request để JSP có thể đọc
        request.setAttribute("categories", listCat);
        request.setAttribute("products", listPro);
        
        // 3. Forward sang file index.jsp
        request.getRequestDispatcher("Home.jsp").forward(request, response);
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
