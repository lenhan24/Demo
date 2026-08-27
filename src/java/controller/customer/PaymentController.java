/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller.customer;

import dal.AccountDAO;
import dal.AddressDAO;
import dal.CartDAO;
import dal.DAOOrder;
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
import model.OrderDTO;
import model.OrderDetailDTO;
import model.VoucherDTO;

/**
 *
 * @author GiGaByte
 */
@WebServlet(name = "PaymentController", urlPatterns = {"/placeOrder"})
public class PaymentController extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
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
            out.println("<title>Servlet PaymentController</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet PaymentController at " + request.getContextPath() + "</h1>");
            out.println("</body>");
            out.println("</html>");
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method. KHÔNG còn xử lý đặt hàng ở đây
     * — tạo đơn + trừ kho là hành động làm thay đổi dữ liệu, bắt buộc phải là
     * POST theo chuẩn HTTP (GET phải "an toàn", không side-effect — trước đây
     * để ở GET có thể bị trigger ngoài ý muốn bởi prefetch của trình duyệt, F5,
     * hay crawler).
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.sendRedirect("checkout");
    }

    /**
     * Handles the HTTP <code>POST</code> method — nơi xử lý đặt hàng thật.
     *
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
        if (email == null) {
            response.sendRedirect("Login.jsp");
            return; // BẮT BUỘC — thiếu dòng này sẽ NPE ở phía dưới
        }
        AccountDAO daoAccount = new AccountDAO();
        Account account = daoAccount.getAccountByEmail(email);
        String voucherIdParam = request.getParameter("voucherId");
        String paymentMethod = request.getParameter("paymentMethod");
        // TRƯỚC ĐÂY: đọc paymentMethod xong rồi... bỏ đó, dòng validate bị
        // comment lại, và lúc tạo đơn luôn ép cứng "COD" (xem phía dưới) —
        // nghĩa là khách chọn "Thanh toán online qua QR" trên giao diện nhưng
        // đơn tạo ra vẫn luôn là COD/UNPAID. Giờ validate và dùng đúng lựa
        // chọn thật của khách.
        if (!"COD".equals(paymentMethod) && !"ONLINE_QR".equals(paymentMethod)) {
            paymentMethod = "COD";
        }
        AddressDAO daoaddress = new AddressDAO();
        List<CartItemDTO> listCartItem = (List<CartItemDTO>) session.getAttribute("checkoutItems");
        Address address = daoaddress.getAnAddressesByAccountId(account.getAccountId());

        if (listCartItem == null || listCartItem.isEmpty() || address == null) {
            // TRƯỚC ĐÂY dùng session.setAttribute("errorMsg", ...) — nhưng
            // Cart.jsp/CartController KHÔNG hề đọc biến "errorMsg" ở bất kỳ
            // đâu, nên khách bị đá về giỏ hàng mà không thấy lý do gì cả.
            // Đổi sang đúng cặp "flashMessage"/"flashError" đã dùng thống
            // nhất ở mọi nơi khác trong app (AddToCart, Review, Checkout...).
            session.setAttribute("flashMessage", "Phiên đặt hàng đã hết hạn, vui lòng thử lại.");
            session.setAttribute("flashError", true);
            response.sendRedirect("cart");
            return;
        }

        Integer voucherId = null;
        if (voucherIdParam != null && !voucherIdParam.trim().isEmpty()) {
            try {
                voucherId = Integer.parseInt(voucherIdParam.trim());
            } catch (NumberFormatException ignored) {
            }
        }

        DAOOrder daoOrder = new DAOOrder();
        ProductDAO daoProduct = new ProductDAO();
        DAOVoucher daoVoucher = new DAOVoucher();

        if (account == null) {
            throw new IllegalStateException("Không tìm thấy tài khoản.");
        }
        int accountId = account.getAccountId();

        // --- Recompute giá + tên sản phẩm + giá vốn từ DB, không tin session/client ---
        long subtotal = 0;
        int totalWeightGrams = 0;
        List<OrderDetailDTO> details = new ArrayList<>();

        for (CartItemDTO item : listCartItem) {

            long lineTotal = item.getPrice() * item.getQuantity();
            subtotal += lineTotal;
            totalWeightGrams += item.getWeightGrams() * item.getQuantity();

            OrderDetailDTO d = new OrderDetailDTO();
            d.setProductId(item.getProductId());
            d.setProductNameSnapshot(item.getProductName());
            d.setQuantity(item.getQuantity());
            d.setUnitPrice(item.getPrice());
            // KHÔNG set costPriceSnapshot ở đây nữa — trước đây gán bằng
            // item.getPrice() (giá BÁN) là sai hoàn toàn, cost_price_snapshot
            // phải là giá VỐN thật. Giờ DAOOrder.placeOrder() tự lấy đúng giá
            // vốn từ tbProduct.unit_cost (sellFromProductInTransaction) và gán
            // lại trước khi insert, nên để trống ở đây.
            d.setLineTotal(lineTotal);
            details.add(d);
        }

        // Phí ship tính theo TỔNG CÂN NẶNG thật của đơn — không còn hardcode
        // 30.000đ cố định cho mọi đơn nữa. Tính lại ở server (không tin số
        // JS gửi lên) để không ai sửa được phí ship qua request giả mạo.
        long shippingFee = utilities.ShippingFeeCalculator.calculate(totalWeightGrams);

        long discountAmount = 0;
        Integer appliedVoucherId = null; // chỉ khác null nếu voucher THẬT SỰ hợp lệ và được áp dụng
        if (voucherIdParam != null) {
            VoucherDTO voucher = null;
            if (voucherId != null) {
                voucher = daoVoucher.getVoucherByAccountAndId(accountId, voucherId, subtotal);
                if (voucher != null) {
                    if ("PERCENTAGE".equalsIgnoreCase(voucher.getDiscountType())) {
                        discountAmount = subtotal * voucher.getDiscountValue() / 100;
                        if (voucher.getMaxDiscountCap() != null) {
                            discountAmount = Math.min(discountAmount, voucher.getMaxDiscountCap());
                        }
                    } else {
                        discountAmount = voucher.getDiscountValue();
                    }
                    discountAmount = Math.min(discountAmount, subtotal);
                    appliedVoucherId = voucherId;
                }
                // voucher == null -> hết hạn/hết lượt/không thuộc account này -> tự bỏ qua giảm giá
            }
        }
        System.out.println(discountAmount);

        long totalAmount = Math.max(0, subtotal + shippingFee - discountAmount);

        OrderDTO order = new OrderDTO();
        order.setOrderCode(daoOrder.generateOrderCode());
        order.setAccountId(accountId);
        order.setStatus("ONLINE_QR".equals(paymentMethod) ? "PENDING_PAYMENT" : "PENDING");
        order.setReceiverName(address.getReceiverName());
        order.setReceiverPhone(address.getReceiverPhone());
        order.setShippingAddress(address.getFullAddress());
        order.setSubtotal(subtotal);
        order.setDiscountAmount(discountAmount);
        order.setShippingFee(shippingFee);
        order.setTotalAmount(totalAmount);
        order.setPaymentMethod(paymentMethod);
        // MỚI (tích hợp VietQR thật — Hướng A): KHÔNG còn tự động đánh dấu
        // PAID ngay khi đặt hàng nữa. Mã QR hiển thị giờ là VietQR THẬT
        // (chứa đúng số tài khoản ngân hàng thật của shop, quét bằng app
        // ngân hàng bất kỳ sẽ tự điền đúng số tiền + nội dung chuyển
        // khoản) — nhưng API VietQR miễn phí KHÔNG có webhook báo tiền đã
        // về, nên đơn phải chờ Staff tự đối chiếu sao kê ngân hàng rồi
        // xác nhận thủ công (xem StaffOrderServlet.handleConfirmPayment).
        // COD vẫn giữ nguyên UNPAID, chỉ thu tiền khi giao hàng.
        order.setPaymentStatus("UNPAID");

//            int orderId = daoOrder.createOrder( order);
//            order.setOrderId(orderId);
// Ghi log đặt hàng (Customer).
        new dal.ActivityLogDAO().insertLog(accountId,
                "Đặt hàng #" + order.getOrderCode() + " (" + (long) order.getTotalAmount() + "đ)");
//            for (OrderDetailDTO d : details) {
//                d.setOrderId(orderId);
//                daoOrder.insertOrderDetail(d);
//            }
        List<String> outOfStockItems = new ArrayList<>();
        int orderId = daoOrder.placeOrder(order, details, outOfStockItems, appliedVoucherId, accountId);

        if (orderId == -2) {
            session.setAttribute("flashMessage",
                    "Các sản phẩm sau không đủ số lượng tồn kho: " + String.join(", ", outOfStockItems)
                    + ". Vui lòng cập nhật lại giỏ hàng.");
            session.setAttribute("flashError", true);
            response.sendRedirect("cart");
            return;
        } else if (orderId == -1) {
            session.setAttribute("flashMessage", "Có lỗi xảy ra khi đặt hàng, vui lòng thử lại.");
            session.setAttribute("flashError", true);
            response.sendRedirect("cart");
            return;
        }
        order.setOrderId(orderId);

        // Xoá đúng các dòng giỏ hàng VỪA ĐẶT khỏi tbCartItem — TRƯỚC ĐÂY
        // chỉ xoá session ("checkoutItems"...), chưa từng xoá dữ liệu thật
        // trong DB, nên giỏ hàng vẫn còn nguyên y hệt sau khi đặt hàng
        // thành công. Chỉ xoá đúng những item đã chọn lúc checkout, không
        // đụng tới các sản phẩm khác chưa chọn còn lại trong giỏ.
        CartDAO cartDAO = new CartDAO();
        for (CartItemDTO item : listCartItem) {
            cartDAO.deleteCartItem(item.getCartItemId(), accountId);
        }

        // Dọn session để tránh đặt trùng đơn khi người dùng F5 lại trang
        session.removeAttribute("checkoutItems");
        session.removeAttribute("grandTotal");
        session.removeAttribute("selectedVoucher");

        // Post/Redirect/Get sang trang xác nhận, JSP tự load lại theo orderId
        response.sendRedirect("orderSuccess?orderId=" + orderId);

    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
