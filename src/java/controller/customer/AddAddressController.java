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
import jakarta.servlet.http.HttpSession;
import model.Account;

/**
 *
 * @author GiGaByte
 */
@WebServlet(name="AddAddressController", urlPatterns={"/address-add"})
public class AddAddressController extends HttpServlet {
   
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
            out.println("<title>Servlet AddAddressController</title>");  
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet AddAddressController at " + request.getContextPath () + "</h1>");
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
    HttpSession session = request.getSession();
    Object accIdObj = session.getAttribute("accountId");
if (accIdObj == null) {
    response.sendRedirect("Login.jsp");
    return;
}
int accountid = (Integer) accIdObj;
        System.out.println(accountid);
request.getServletContext().log(String.valueOf(accountid));
    String action = request.getParameter("action");
    AddressDAO dao = new AddressDAO();

    switch (action) {
        case "add": {
            String name = request.getParameter("receiverName");
            String phone = request.getParameter("receiverPhone");
            String fullAddress = request.getParameter("fullAddress");

            // MỚI: TRƯỚC ĐÂY 2 nhánh lỗi bên dưới luôn redirect cứng về "account",
            // bỏ qua hẳn returnUrl -> nếu form được gọi từ Checkout, khách nhập
            // sai SĐT sẽ bị đá sang trang Tài khoản, MẤT LUÔN đơn hàng đang đặt
            // dở. Giờ đọc đúng returnUrl ngay tại đây, quay lại đúng trang đang
            // đứng để thấy cảnh báo tại chỗ, không mất ngữ cảnh.
            String returnPage = "checkout".equals(request.getParameter("returnUrl")) ? "checkout" : "account";

            if (name == null || name.trim().isEmpty()
                    || phone == null || phone.trim().isEmpty()
                    || fullAddress == null || fullAddress.trim().isEmpty()) {
                session.setAttribute("addrError", "Vui lòng điền đầy đủ thông tin địa chỉ.");
                response.sendRedirect(returnPage);
                return;
            }
            if (!utilities.ValidationUtil.isValidPhone(phone)) {
                session.setAttribute("addrError", "Số điện thoại không đúng định dạng (VD: 0912345678).");
                response.sendRedirect(returnPage);
                return;
            }
             // MỚI: chặn địa chỉ quá ngắn (khả năng cao là gõ nhầm — VD gõ nhầm
            // mật khẩu/tên/SĐT vào ô này) — địa chỉ thật luôn có ít nhất số nhà +
            // tên đường + phường/quận, hiếm khi ngắn hơn 15 ký tự.
            if (fullAddress.trim().length() < 15) {
                session.setAttribute("addrError", "Địa chỉ cụ thể quá ngắn, vui lòng nhập đầy đủ số nhà/đường/phường/quận.");
                response.sendRedirect(returnPage);
                return;
            }
            boolean ok = dao.insertAddress(name, phone, fullAddress, String.valueOf(accountid));
            session.setAttribute(ok ? "addrSuccess" : "addrError",
                    ok ? "Thêm địa chỉ mới thành công!" : "Có lỗi khi thêm địa chỉ, vui lòng thử lại.");
            break;
        }
        default:
            break;
    }

    // TRƯỚC ĐÂY luôn redirect cứng về "account" — nếu form được gọi từ trang
    // Checkout (để thêm địa chỉ ngay tại chỗ), khách sẽ bị đá khỏi checkout
    // giữa chừng. Giờ đọc "returnUrl" do form gửi lên, chỉ chấp nhận 2 giá trị
    // biết trước (whitelist) để tránh open-redirect nếu ai đó chỉnh tay param.
    String returnUrl = request.getParameter("returnUrl");
    if ("checkout".equals(returnUrl)) {
        response.sendRedirect("checkout");
    } else {
        response.sendRedirect("account");
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
