package controller.manager;

import dal.DAOVoucher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import model.VoucherDTO;
import utilities.ExcelExportUtil;
import utilities.PromotionValidator;

@WebServlet(name = "PromotionManagerServlet", urlPatterns = {"/manager/promotions"})
public class PromotionManagerServlet extends HttpServlet {

    private final DAOVoucher voucherDAO = new DAOVoucher();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Sub-endpoint check mã voucher trùng, gọi qua fetch() từ JS
        String checkVoucherCode = request.getParameter("checkVoucherCode");
        if (checkVoucherCode != null) {
            boolean exists = voucherDAO.voucherCodeExists(checkVoucherCode.trim());
            response.setContentType("text/plain;charset=UTF-8");
            response.getWriter().write(exists ? "EXISTS" : "OK");
            return;
        }

        voucherDAO.expireOverdueVouchers();   // MỚI: tự động cập nhật status thật trong DB trước khi lấy danh sách
        List<VoucherDTO> vouchers = voucherDAO.getAllVouchers();
        request.setAttribute("vouchers", vouchers);
        if ("excel".equals(request.getParameter("export"))) {
            exportVouchersToCsv(response);
            return;
        }
        HttpSession session = request.getSession();
        Object flash = session.getAttribute("flashMessage");
        if (flash != null) {
            request.setAttribute("flashMessage", flash);
            request.setAttribute("flashError", session.getAttribute("flashError"));
            session.removeAttribute("flashMessage");
            session.removeAttribute("flashError");
        }

        request.getRequestDispatcher("/manager/manager-promotions.jsp").forward(request, response);
    }

    private void exportVouchersToCsv(HttpServletResponse response) throws IOException {
        List<VoucherDTO> all = voucherDAO.getAllVouchers();
        String[] headers = {"Mã voucher", "Loại giảm giá", "Giá trị", "Đơn tối thiểu", "Bắt đầu", "Kết thúc", "Giới hạn/khách", "Tổng giới hạn", "Trạng thái"};
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        List<String[]> rows = new ArrayList<>();
        for (VoucherDTO v : all) {
            rows.add(new String[]{
                v.getCode(),
                "PERCENTAGE".equals(v.getDiscountType()) ? "Theo %" : "Số tiền cố định",
                String.valueOf(v.getDiscountValue()),
                String.valueOf(v.getMinOrderValue()),
                sdf.format(v.getStartDate()),
                sdf.format(v.getEndDate()),
                String.valueOf(v.getPerCustomerLimit()),
                String.valueOf(v.getMaxUsageLimit()),
                v.getStatus()
            });
        }
        ExcelExportUtil.exportCsv(response, "danh_sach_voucher.csv", headers, rows);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");
        HttpSession session = request.getSession();

        try {
            switch (action == null ? "" : action) {
                case "createVoucher":
                    handleCreateVoucher(request, session);
                    break;
                case "updateVoucher":
                    handleUpdateVoucher(request, session);
                    break;
                case "deactivateVoucher":
                    handleToggleVoucherStatus(request, session, "INACTIVE");
                    break;
                case "activateVoucher":
                    handleToggleVoucherStatus(request, session, "ACTIVE");
                    break;
                default:
                    session.setAttribute("flashMessage", "Hành động không hợp lệ.");
                    session.setAttribute("flashError", true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("flashMessage", "Có lỗi xảy ra: " + e.getMessage());
            session.setAttribute("flashError", true);
        }

        response.sendRedirect(request.getContextPath() + "/manager/promotions");
    }

    private PromotionValidator.VoucherInput readVoucherInput(HttpServletRequest request) {
        PromotionValidator.VoucherInput input = new PromotionValidator.VoucherInput();
        String idRaw = request.getParameter("voucherId");
        input.voucherId = (idRaw == null || idRaw.isEmpty()) ? null : Integer.parseInt(idRaw);
        input.code = request.getParameter("code");
        input.discountType = request.getParameter("discountType");
        input.discountValueRaw = request.getParameter("discountValue");
        input.maxDiscountCapRaw = request.getParameter("maxDiscountCap");
        input.minOrderValueRaw = request.getParameter("minOrderValue");
        input.startDateRaw = request.getParameter("startDate");
        input.endDateRaw = request.getParameter("endDate");
        input.maxUsageLimitRaw = request.getParameter("maxUsageLimit");
        input.perCustomerLimitRaw = request.getParameter("perCustomerLimit");
        return input;
    }

    /**
     * Dựng VoucherDTO qua no-arg constructor + setter (constructor 10 tham số
     * của Customer không tiện dùng ở đây vì dữ liệu tới rời rạc từ
     * request.getParameter())
     */
    private VoucherDTO buildVoucherDto(PromotionValidator.VoucherInput input) {
        VoucherDTO v = new VoucherDTO();
        v.setCode(input.code == null ? null : input.code.trim().toUpperCase());
        v.setDiscountType(input.discountType);
        v.setDiscountValue(Long.parseLong(input.discountValueRaw.replace(".", "").replace(",", "")));
        if (input.maxDiscountCapRaw != null && !input.maxDiscountCapRaw.isEmpty()) {
            v.setMaxDiscountCap(Long.parseLong(input.maxDiscountCapRaw.replace(".", "").replace(",", "")));
        }
        v.setMinOrderValue(Long.parseLong(input.minOrderValueRaw.replace(".", "").replace(",", "")));
        v.setStartDate(parseTimestamp(input.startDateRaw)); // Timestamp extends Date -> gán trực tiếp được
        v.setEndDate(parseTimestamp(input.endDateRaw));
        v.setMaxUsageLimit(Integer.parseInt(input.maxUsageLimitRaw.trim()));
        v.setPerCustomerLimit(Integer.parseInt(input.perCustomerLimitRaw.trim()));
        return v;
    }

    private Timestamp parseTimestamp(String s) {
        String normalized = s.trim().replace("T", " ");
        if (normalized.length() == 16) {
            normalized += ":00";
        }
        return Timestamp.valueOf(normalized);
    }

    private int currentManagerId(HttpSession session) {
        model.Account acc = (model.Account) session.getAttribute("account");
        return acc == null ? 0 : acc.getAccountId();
    }

    private void handleCreateVoucher(HttpServletRequest request, HttpSession session) {
        PromotionValidator.VoucherInput input = readVoucherInput(request);
        String error = PromotionValidator.validateVoucher(input, voucherDAO);
        if (error != null) {
            session.setAttribute("flashMessage", error);
            session.setAttribute("flashError", true);
            return;
        }
        VoucherDTO v = buildVoucherDto(input);
        int result = voucherDAO.insertVoucher(v);
        session.setAttribute("flashMessage", result > 0
                ? "Đã tạo voucher \"" + v.getCode() + "\" thành công."
                : "Tạo voucher thất bại.");
        session.setAttribute("flashError", result <= 0);
        if (result > 0) {
            new dal.ActivityLogDAO().insertLog(currentManagerId(session),
                    "Tạo voucher \"" + v.getCode() + "\"");
        }
    }

    private void handleUpdateVoucher(HttpServletRequest request, HttpSession session) {
        PromotionValidator.VoucherInput input = readVoucherInput(request);
        String error = PromotionValidator.validateVoucher(input, voucherDAO);
        if (error != null) {
            session.setAttribute("flashMessage", error);
            session.setAttribute("flashError", true);
            return;
        }
        VoucherDTO v = buildVoucherDto(input);
        v.setVoucherId(input.voucherId);
        boolean ok = voucherDAO.updateVoucher(v);
        session.setAttribute("flashMessage", ok
                ? "Đã cập nhật voucher \"" + v.getCode() + "\"."
                : "Cập nhật voucher thất bại.");
        session.setAttribute("flashError", !ok);
        if (ok) {
            new dal.ActivityLogDAO().insertLog(currentManagerId(session),
                    "Cập nhật voucher \"" + v.getCode() + "\"");
        }
    }

    private void handleToggleVoucherStatus(HttpServletRequest request, HttpSession session, String newStatus) {
        int voucherId = Integer.parseInt(request.getParameter("voucherId"));
        boolean ok = voucherDAO.updateVoucherStatus(voucherId, newStatus);
        session.setAttribute("flashMessage", ok
                ? ("ACTIVE".equals(newStatus) ? "Đã kích hoạt lại voucher." : "Đã tạm ngưng voucher.")
                : "Thao tác thất bại.");
        session.setAttribute("flashError", !ok);
        if (ok) {
            new dal.ActivityLogDAO().insertLog(currentManagerId(session),
                    (("ACTIVE".equals(newStatus)) ? "Kích hoạt lại" : "Tạm ngưng") + " voucher #" + voucherId);
        }
    }
    
}
