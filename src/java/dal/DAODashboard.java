package dal;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import model.DashboardSummaryDTO;
import model.RevenueByCategoryDTO;
import model.RevenueByDateDTO;
import model.TopProductDTO;


/**
 * To�n b? query ph�n t�ch kinh doanh cho Manager Dashboard.
 *
 * QUY ??C QUAN TR?NG (?� ch?t v?i Manager tr??c khi code): - Ch? t�nh ??n h�ng
 * status = 'DELIVERED' (?� ho�n t?t) l� doanh thu th?t. - "Doanh thu" (s? t?ng
 * quan + bi?u ?? theo ng�y) = SUM(subtotal - discount_amount), t?c l� ?� TR?
 * voucher ch�nh x�c ? c?p ??n h�ng � kh�ng c?ng ph� ship v�o doanh thu. - "Top
 * s?n ph?m" / "Doanh thu theo danh m?c" t�nh theo doanh thu GROSS
 * (SUM(unit_price * quantity) t? tbOrderDetail) � CH?A ph�n b? voucher xu?ng
 * t?ng d�ng, theo ?�ng quy?t ??nh ??n gi?n h�a ?� th?ng nh?t v?i Manager.
 */
public class DAODashboard extends DBContext {

    private static final String COUNTED_STATUS = "DELIVERED";

    public DashboardSummaryDTO getSummaryStats(Date from, Date to) {
        DashboardSummaryDTO summary = new DashboardSummaryDTO();

        // SUA (BUG-01): truoc day doanh thu va gia von la 2 query rieng, moi cai
        // nam trong 1 try/catch chi goi printStackTrace() roi di tiep. Neu query
        // doanh thu chet thi totalRevenue giu nguyen 0 trong khi totalCost van
        // tinh duoc -> grossProfit = 0 - totalCost = SO AM GIA, ma web khong he
        // bao loi. Gio gop 2 query lam 1: hai con so luon tinh tren CUNG tap don
        // hang, khong the lech nhau duoc nua.
        String query = "SELECT COUNT(*) AS total_orders, "
                + "ISNULL(SUM(sub.order_subtotal - o.discount_amount), 0) AS total_revenue, "
                + "ISNULL(SUM(sub.order_cogs), 0) AS total_cost "
                + "FROM tbOrder o "
                + "CROSS APPLY (SELECT ISNULL(SUM(quantity * unit_price), 0) AS order_subtotal, "
                + "                    ISNULL(SUM(quantity * cost_price_snapshot), 0) AS order_cogs "
                + "             FROM tbOrderDetail WHERE order_id = o.order_id) sub "
                + "WHERE o.status = ? AND o.created_at BETWEEN ? AND ?";

        long totalRevenue = 0;
        long totalCost = 0;
        int totalOrders = 0;
        boolean queryOk = false;

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, COUNTED_STATUS);
            ps.setTimestamp(2, new Timestamp(from.getTime()));
            ps.setTimestamp(3, new Timestamp(to.getTime()));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    totalOrders = rs.getInt("total_orders");
                    totalRevenue = rs.getLong("total_revenue");
                    totalCost = rs.getLong("total_cost");
                    queryOk = true;
                }
            }
} catch (Exception e) {
            System.out.println("[DASHBOARD] LOI query tong quan: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("[DASHBOARD] orders=" + totalOrders
                + " revenue=" + totalRevenue + " cost=" + totalCost
                + " profit=" + (totalRevenue - totalCost) + " ok=" + queryOk);

        summary.setTotalRevenue(totalRevenue);
        summary.setTotalOrders(totalOrders);
        summary.setAvgOrderValue(totalOrders > 0 ? totalRevenue / totalOrders : 0);
        // Query chet thi de 0, KHONG de lot ra so am vo nghia
        summary.setGrossProfit(queryOk ? (totalRevenue - totalCost) : 0);
        long totalVoucherDiscount = getTotalVoucherDiscount(from, to);
        summary.setTotalVoucherDiscount(totalVoucherDiscount);
        return summary;
    }

    public List<TopProductDTO> getTopSellingProducts(Date from, Date to, int limit) {
        List<TopProductDTO> list = new ArrayList<>();
        String query = "SELECT TOP (?) od.product_id, p.name, p.sku, "
                + "SUM(od.quantity) AS total_qty, SUM(od.unit_price * od.quantity) AS total_revenue "
                + "FROM tbOrderDetail od "
                + "INNER JOIN tbOrder o ON od.order_id = o.order_id "
                + "INNER JOIN tbProduct p ON od.product_id = p.product_id "
                + "WHERE o.status = ? AND o.created_at BETWEEN ? AND ? "
                + "GROUP BY od.product_id, p.name, p.sku "
                + "ORDER BY total_revenue DESC";

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, limit);
            ps.setString(2, COUNTED_STATUS);
            ps.setTimestamp(3, new Timestamp(from.getTime()));
            ps.setTimestamp(4, new Timestamp(to.getTime()));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    TopProductDTO t = new TopProductDTO();
                    t.setProductId(rs.getInt("product_id"));
                    t.setProductName(rs.getString("name"));
                    t.setSku(rs.getString("sku"));
                    t.setTotalQuantitySold(rs.getInt("total_qty"));
                    t.setTotalRevenue(rs.getLong("total_revenue"));
                    list.add(t);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<RevenueByCategoryDTO> getRevenueByCategory(Date from, Date to) {
        List<RevenueByCategoryDTO> list = new ArrayList<>();
        String query = "SELECT c.category_id, c.name, SUM(od.unit_price * od.quantity) AS total_revenue "
                + "FROM tbOrderDetail od "
                + "INNER JOIN tbOrder o ON od.order_id = o.order_id "
                + "INNER JOIN tbProduct p ON od.product_id = p.product_id "
                + "INNER JOIN tbCategory c ON p.category_id = c.category_id "
+ "WHERE o.status = ? AND o.created_at BETWEEN ? AND ? "
                + "GROUP BY c.category_id, c.name "
                + "ORDER BY total_revenue DESC";

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, COUNTED_STATUS);
            ps.setTimestamp(2, new Timestamp(from.getTime()));
            ps.setTimestamp(3, new Timestamp(to.getTime()));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    RevenueByCategoryDTO r = new RevenueByCategoryDTO();
                    r.setCategoryId(rs.getInt("category_id"));
                    r.setCategoryName(rs.getString("name"));
                    r.setTotalRevenue(rs.getLong("total_revenue"));
                    list.add(r);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Doanh thu theo ng�y � group ? c?p ??N H�NG (kh�ng c?n ph�n b? d�ng), n�n
     * tr? voucher ch�nh x�c lu�n
     */
    public List<RevenueByDateDTO> getRevenueByDate(Date from, Date to) {
        List<RevenueByDateDTO> list = new ArrayList<>();
        String query = "SELECT CAST(o.created_at AS DATE) AS order_day, "
                + "SUM(sub.order_subtotal - o.discount_amount) AS total_revenue "
                + "FROM tbOrder o "
                + "CROSS APPLY (SELECT ISNULL(SUM(quantity * unit_price), 0) AS order_subtotal "
                + "             FROM tbOrderDetail WHERE order_id = o.order_id) sub "
                + "WHERE o.status = ? AND o.created_at BETWEEN ? AND ? "
                + "GROUP BY CAST(o.created_at AS DATE) "
                + "ORDER BY order_day ASC";

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM");

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, COUNTED_STATUS);
            ps.setTimestamp(2, new Timestamp(from.getTime()));
            ps.setTimestamp(3, new Timestamp(to.getTime()));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    RevenueByDateDTO r = new RevenueByDateDTO();
                    r.setDateLabel(sdf.format(rs.getDate("order_day")));
                    r.setRevenue(rs.getLong("total_revenue"));
                    list.add(r);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Doanh thu theo THÁNG — dùng cho biểu đồ dài hạn (12 tháng gần nhất)
     */
    public List<RevenueByDateDTO> getRevenueByMonth(Date from, Date to) {
        List<RevenueByDateDTO> list = new ArrayList<>();
        String query = "SELECT YEAR(o.created_at) AS yr, MONTH(o.created_at) AS mo, "
                + "SUM(sub.order_subtotal - o.discount_amount) AS total_revenue "
                + "FROM tbOrder o "
+ "CROSS APPLY (SELECT ISNULL(SUM(quantity * unit_price), 0) AS order_subtotal "
                + "             FROM tbOrderDetail WHERE order_id = o.order_id) sub "
                + "WHERE o.status = ? AND o.created_at BETWEEN ? AND ? "
                + "GROUP BY YEAR(o.created_at), MONTH(o.created_at) "
                + "ORDER BY yr ASC, mo ASC";

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, COUNTED_STATUS);
            ps.setTimestamp(2, new Timestamp(from.getTime()));
            ps.setTimestamp(3, new Timestamp(to.getTime()));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    RevenueByDateDTO r = new RevenueByDateDTO();
                    r.setDateLabel(String.format("%02d/%d", rs.getInt("mo"), rs.getInt("yr")));
                    r.setRevenue(rs.getLong("total_revenue"));
                    list.add(r);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Tổng tiền đã giảm giá qua voucher (chỉ tính đơn có dùng voucher thật, qua
     * tbVoucherUsage)
     */
    public long getTotalVoucherDiscount(Date from, Date to) {
        String query = "SELECT ISNULL(SUM(o.discount_amount), 0) AS total_discount "
                + "FROM tbOrder o "
                + "INNER JOIN tbVoucherUsage vu ON o.order_id = vu.order_id "
                + "WHERE o.status = ? AND o.created_at BETWEEN ? AND ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, COUNTED_STATUS);
            ps.setTimestamp(2, new Timestamp(from.getTime()));
            ps.setTimestamp(3, new Timestamp(to.getTime()));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("total_discount");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Thống kê từng voucher đã release: đã dùng bao nhiêu lần, giảm tổng bao
     * nhiêu tiền. LEFT JOIN để voucher CHƯA từng được dùng vẫn hiện ra
     * (usage_count=0), không bị ẩn mất.
     */
    public List<model.VoucherUsageStatsDTO> getVoucherUsageStats(Date from, Date to) {
        List<model.VoucherUsageStatsDTO> list = new ArrayList<>();
        String query = "SELECT v.voucher_id, v.code, v.discount_type, v.max_usage_limit, "
                + "COUNT(vu.usage_id) AS usage_count, ISNULL(SUM(o.discount_amount), 0) AS total_discount "
                + "FROM tbVoucher v "
                + "LEFT JOIN tbVoucherUsage vu ON v.voucher_id = vu.voucher_id "
                + "LEFT JOIN tbOrder o ON vu.order_id = o.order_id AND o.status = ? AND o.created_at BETWEEN ? AND ? "
                + "GROUP BY v.voucher_id, v.code, v.discount_type, v.max_usage_limit "
+ "ORDER BY total_discount DESC";

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, COUNTED_STATUS);
            ps.setTimestamp(2, new Timestamp(from.getTime()));
            ps.setTimestamp(3, new Timestamp(to.getTime()));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    model.VoucherUsageStatsDTO v = new model.VoucherUsageStatsDTO();
                    v.setVoucherId(rs.getInt("voucher_id"));
                    v.setCode(rs.getString("code"));
                    v.setDiscountType(rs.getString("discount_type"));
                    v.setMaxUsageLimit(rs.getInt("max_usage_limit"));
                    v.setUsageCount(rs.getInt("usage_count"));
                    v.setTotalDiscountGiven(rs.getLong("total_discount"));
                    list.add(v);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}
