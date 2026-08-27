/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */

package controller.customer;

import dal.AccountDAO;
import dal.AddressDAO;
import dal.CartDAO;
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
import java.util.ArrayList;
import java.util.List;
import model.Account;
import model.Address;
import model.CartItemDTO;
import model.VoucherDTO;

/**
 *
 * @author GiGaByte
 */
@WebServlet(name="CheckoutController", urlPatterns={"/checkout"})
public class CheckoutController extends HttpServlet {
   
    /** 
     * Processes requestuests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param requestuest servlet requestuest
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest requestuest, HttpServletResponse response)
    throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            /* TODO output your page here. You may use following sample code. */
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet CheckoutController</title>");  
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet CheckoutController at " + requestuest.getContextPath () + "</h1>");
            out.println("</body>");
            out.println("</html>");
        }
    } 

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /** 
     * Handles the HTTP <code>GET</code> method.
     * @param requestuest servlet requestuest
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
    throws ServletException, IOException {
      HttpSession session = req.getSession();
        String email = (String) session.getAttribute("userEmail");
        // TRƯỚC ĐÂY có "session.setMaxInactiveInterval(1200)" ở đây — đây là
        // cấu hình CHO CẢ SESSION, không phải riêng request này, nên vô tình
        // rút thời gian sống của session từ 24h (LoginController) xuống còn
        // 20 phút cho toàn bộ phần còn lại của phiên đăng nhập, chỉ vì khách
        // ghé qua trang checkout 1 lần. Đã bỏ hẳn dòng đó.
        // Chưa đăng nhập -> đá về trang login
        if (email == null) {
            resp.sendRedirect("Login.jsp");
            return;
        }

        AccountDAO accountDAO = new AccountDAO();
        Account account = accountDAO.getAccountByEmail(email);

        if (account == null) {
            resp.sendRedirect("Login.jsp");
            return;
        }

        int accountId = account.getAccountId();

        // Lấy danh sách cartItemId được chọn (checkbox) từ form
        String[] selectedIds = req.getParameterValues("selectedItems");

        // TRƯỚC ĐÂY: thiếu selectedItems là lập tức đá về giỏ hàng — nhưng khi
        // khách vừa thêm địa chỉ mới xong rồi quay lại /checkout (không phải
        // submit lại form chọn sản phẩm), sẽ không có selectedItems trên URL,
        // dẫn tới mất hết lựa chọn dù mới chọn xong. Giờ nếu không có
        // selectedItems NHƯNG session đã có sẵn checkoutItems từ lần load
        // trước, dùng lại luôn danh sách đó thay vì đá về giỏ hàng.
        @SuppressWarnings("unchecked")
        List<CartItemDTO> existingCheckoutItems = (List<CartItemDTO>) session.getAttribute("checkoutItems");
        if ((selectedIds == null || selectedIds.length == 0)
                && existingCheckoutItems != null && !existingCheckoutItems.isEmpty()) {
            AddressDAO addDAOReuse = new AddressDAO();
            Address addReuse = addDAOReuse.getAnAddressesByAccountId(accountId);
            req.setAttribute("address", addReuse);
            Long subtotalReuse = (Long) session.getAttribute("grandTotal");
            DAOVoucher voucherDAOReuse = new DAOVoucher();
            req.setAttribute("validVouchers", voucherDAOReuse.getValidSavedVouchers(accountId, subtotalReuse));
            req.getRequestDispatcher("Checkout.jsp").forward(req, resp);
            return;
        }

        if (selectedIds == null || selectedIds.length == 0) {
//            req.setAttribute("error", "Vui lòng chọn ít nhất 1 sản phẩm (BR-CUS02.2)");
//            req.getRequestDispatcher("cart").forward(req, resp);
            resp.sendRedirect("cart");
            return;
        }

        CartDAO cartDAO = new CartDAO();
        AddressDAO addDAO = new AddressDAO();
        ProductDAO productDAO = new ProductDAO();
        Address add = addDAO.getAnAddressesByAccountId(accountId);
        List<CartItemDTO> checkoutItems = new ArrayList<>();
        long grandTotal = 0;

        for (String idStr : selectedIds) {
            int cartItemId = Integer.parseInt(idStr);

            // Truyền accountId -> chỉ lấy được item của chính user này
            CartItemDTO item = cartDAO.getCartItemById(cartItemId, accountId);

            if (item == null) {
                // cartItemId không tồn tại hoặc không thuộc user này -> bỏ qua, không tin client
                continue;
            }

            // Số lượng thực tế lấy từ ô input trên form (name="qty_<cartItemId>")
            String qtyParam = req.getParameter("qty_" + cartItemId);
            if (qtyParam != null) {
                try {
                    int qty = Integer.parseInt(qtyParam);
                    if (qty > 0) {
                        item.setQuantity(qty); // itemTotal tự tính lại trong setter
                        // TRƯỚC ĐÂY: đổi quantity xong không re-check tồn kho —
                        // item.isInStock() vẫn giữ giá trị tính từ SỐ LƯỢNG CŨ
                        // (lúc load từ giỏ hàng). Nếu khách tăng số lượng ngay
                        // tại đây vượt quá tồn kho thật, "còn hàng" sẽ hiển thị
                        // sai. Giờ check lại đúng theo số lượng mới.
                        int activeStock = productDAO.getStockQuantity(item.getProductId());
                        item.setInStock(activeStock >= qty);
                    }
                } catch (NumberFormatException e) {
                    // giữ nguyên quantity đã lưu trong DB nếu param lỗi
                }
            }

            // Không cho checkout sản phẩm hết hàng
            if (!item.isInStock()) {
                continue;
            }

            checkoutItems.add(item);
            grandTotal += item.getItemTotal();
        }

        if (checkoutItems.isEmpty()) {
//            req.setAttribute("error", "Không có sản phẩm hợp lệ để thanh toán");
//            req.getRequestDispatcher("cart").forward(req, resp);
           resp.sendRedirect("cart");
            return;
        }
       

        // Lưu tạm vào session để checkout.jsp / bước tạo order dùng tiếp
        req.setAttribute("address", add);
        session.setAttribute("checkoutItems", checkoutItems);
        session.setAttribute("grandTotal", grandTotal);
        Long subtotal = (Long) session.getAttribute("grandTotal");

     DAOVoucher voucherDAO = new DAOVoucher();
     List<VoucherDTO> validVouchers = voucherDAO.getValidSavedVouchers(accountId, subtotal);
         req.setAttribute("validVouchers", validVouchers);
            req.getRequestDispatcher("Checkout.jsp").forward(req, resp);
//        resp.sendRedirect("Checkout.jsp");
    }
    /** 
     * Handles the HTTP <code>POST</code> method.
     * @param requestuest servlet requestuest
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest requestuest, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(requestuest, response);
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
