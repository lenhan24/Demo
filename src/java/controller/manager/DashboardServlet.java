package controller.manager;

import dal.DAODashboard;
import dal.DAOOrder;
import dal.ProductDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import model.DashboardSummaryDTO;
import model.OrderDTO;
import model.ProductDetailDTO;
import model.RevenueByCategoryDTO;
import model.RevenueByDateDTO;
import model.TopProductDTO;

@WebServlet(name = "DashboardServlet", urlPatterns = {"/manager/dashboard"})
public class DashboardServlet extends HttpServlet {

    // SUA (BUG-02): truoc day 3 DAO nay la FIELD cua servlet. Servlet trong
    // Tomcat la singleton -> ca app dung chung dung 1 Connection, song tu luc
    // load den luc tat. Connection bi SQL Server dong do idle la moi query sau
    // do chet im lang. Gio moi request tu tao DAO rieng trong try-with-resources.
    private static final int LOW_STOCK_THRESHOLD = 10;
    private static final int TOP_PRODUCT_LIMIT = 5;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try (DAODashboard dashboardDAO = new DAODashboard(); ProductDAO productDAO = new ProductDAO(); DAOOrder orderDAO = new DAOOrder()) {

            int rangeDays = parseIntSafe(request.getParameter("range"), 30);

            Calendar cal = Calendar.getInstance();
            Date to = cal.getTime();
            cal.add(Calendar.DAY_OF_MONTH, -rangeDays);
            Date from = cal.getTime();

            DashboardSummaryDTO summary = dashboardDAO.getSummaryStats(from, to);
            List<TopProductDTO> topProducts = dashboardDAO.getTopSellingProducts(from, to, TOP_PRODUCT_LIMIT);
            List<RevenueByCategoryDTO> revenueByCategory = dashboardDAO.getRevenueByCategory(from, to);
            List<RevenueByDateDTO> revenueByDate = dashboardDAO.getRevenueByDate(from, to);
            List<ProductDetailDTO> lowStockProducts = productDAO.getLowStockProducts(LOW_STOCK_THRESHOLD);

            // Xuất báo cáo kinh doanh ra CSV (Excel mở trực tiếp được, không cần
            // thêm thư viện gì) — gộp các bảng chính vào 1 file, có dòng phân cách
            // giữa từng phần cho dễ đọc.
            if ("excel".equals(request.getParameter("export"))) {
                List<String[]> rows = new java.util.ArrayList<>();
                rows.add(new String[]{"TỔNG QUAN", ""});
                rows.add(new String[]{"Tổng doanh thu", String.valueOf(summary.getTotalRevenue())});
                rows.add(new String[]{"Tổng số đơn hàng (đã giao)", String.valueOf(summary.getTotalOrders())});
                rows.add(new String[]{"Giá trị đơn trung bình (AOV)", String.valueOf(summary.getAvgOrderValue())});
                rows.add(new String[]{"Lợi nhuận gộp", String.valueOf(summary.getGrossProfit())});
                rows.add(new String[]{"Tổng tiền giảm giá (voucher)", String.valueOf(summary.getTotalVoucherDiscount())});
                rows.add(new String[]{"", ""});

                rows.add(new String[]{"DOANH THU THEO NGÀY", ""});
                for (RevenueByDateDTO d : revenueByDate) {
                    rows.add(new String[]{d.getDateLabel(), String.valueOf(d.getRevenue())});
                }
                rows.add(new String[]{"", ""});

                rows.add(new String[]{"TOP SẢN PHẨM", "", "", ""});
                for (TopProductDTO p : topProducts) {
                    rows.add(new String[]{p.getSku(), p.getProductName(),
                        String.valueOf(p.getTotalQuantitySold()), String.valueOf(p.getTotalRevenue())});
                }
                rows.add(new String[]{"", ""});

                rows.add(new String[]{"DOANH THU THEO DANH MỤC", ""});
                for (RevenueByCategoryDTO c : revenueByCategory) {
                    rows.add(new String[]{c.getCategoryName(), String.valueOf(c.getTotalRevenue())});
                }
                rows.add(new String[]{"", ""});

                rows.add(new String[]{"SẮP HẾT HÀNG", "", ""});
                for (ProductDetailDTO p : lowStockProducts) {
                    rows.add(new String[]{p.getSku(), p.getName(), String.valueOf(p.getStockQuantity())});
                }

                String fileName = "BaoCao_KinhDoanh_" + new SimpleDateFormat("yyyyMMdd").format(to) + ".csv";
                utilities.ExcelExportUtil.exportCsv(response, fileName,
                        new String[]{"Mục", "Giá trị", "Số lượng", "Doanh thu"}, rows);
                return;
            }

            // Doanh thu theo tháng — cố định 12 tháng gần nhất, không phụ thuộc bộ lọc range ngày
            Calendar monthCal = Calendar.getInstance();
            Date monthTo = monthCal.getTime();
            monthCal.add(Calendar.MONTH, -12);
            Date monthFrom = monthCal.getTime();
            List<RevenueByDateDTO> revenueByMonth = dashboardDAO.getRevenueByMonth(monthFrom, monthTo);

            // MỚI: đơn đang chờ hoàn tiền (sinh ra từ đơn hủy đã thanh toán online,
            // hoặc yêu cầu hoàn hàng Staff đã tiếp nhận xong) — Manager là người
            // xác nhận đã chuyển khoản hoàn tiền thật cho khách.
            List<OrderDTO> pendingRefunds = orderDAO.getPendingRefunds();

            request.setAttribute("summary", summary);
            request.setAttribute("topProducts", topProducts);
            request.setAttribute("revenueByCategory", revenueByCategory);
            request.setAttribute("revenueByDate", revenueByDate);
            request.setAttribute("revenueByMonth", revenueByMonth);
            request.setAttribute("lowStockProducts", lowStockProducts);
            request.setAttribute("rangeDays", rangeDays);
            request.setAttribute("pendingRefunds", pendingRefunds);

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            request.setAttribute("fromLabel", sdf.format(from));
            request.setAttribute("toLabel", sdf.format(to));

            loadAndClearFlash(request);

            request.getRequestDispatcher("/manager/manager-dashboard.jsp").forward(request, response);
        }
    }

    // ─────────────────────────────────────────────────────────
    //  POST: Manager xác nhận đã hoàn tiền xong cho 1 đơn
    // ─────────────────────────────────────────────────────────
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try (DAOOrder orderDAO = new DAOOrder()) {

            HttpSession session = request.getSession();
            String action = request.getParameter("action");

            if ("confirmRefund".equals(action)) {
                int orderId = parseIntSafe(request.getParameter("orderId"), 0);
                boolean ok = orderId > 0 && orderDAO.confirmRefundCompleted(orderId);
                if (ok) {
                    session.setAttribute("flashMessage", "Đã xác nhận hoàn tiền xong cho đơn.");
                    session.setAttribute("flashError", false);
                } else {
                    session.setAttribute("flashMessage", "Xác nhận thất bại. Đơn có thể đã được xử lý trước đó.");
                    session.setAttribute("flashError", true);
                }
            } else {
                session.setAttribute("flashMessage", "Hành động không hợp lệ.");
                session.setAttribute("flashError", true);
            }

            response.sendRedirect(request.getContextPath() + "/manager/dashboard");
        }
    }

    private void loadAndClearFlash(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return;
        }
        Object msg = session.getAttribute("flashMessage");
        if (msg != null) {
            request.setAttribute("flashMessage", msg);
            request.setAttribute("flashError", session.getAttribute("flashError"));
            session.removeAttribute("flashMessage");
            session.removeAttribute("flashError");
        }
    }

    private int parseIntSafe(String s, int fallback) {
        try {
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            return fallback;
        }
    }
}
