package model;

public class DashboardSummaryDTO {

    private long totalRevenue;      // Doanh thu (?� tr? voucher, ch?a g?m ph� ship)
    private int totalOrders;        // T?ng s? ??n h�ng (trong kho?ng th?i gian, ?� DELIVERED)
    private long avgOrderValue;     // Gi� tr? ??n h�ng trung b�nh (AOV) = totalRevenue / totalOrders
    private long grossProfit;       // L?i nhu?n g?p = totalRevenue - t?ng gi� v?n h�ng b�n

    private long totalVoucherDiscount; // Tổng tiền đã giảm giá qua voucher (chi phí khuyến mãi)

    public DashboardSummaryDTO() {
    }

    public long getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(long totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public int getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(int totalOrders) {
        this.totalOrders = totalOrders;
    }

    public long getAvgOrderValue() {
        return avgOrderValue;
    }

    public void setAvgOrderValue(long avgOrderValue) {
        this.avgOrderValue = avgOrderValue;
    }

    public long getGrossProfit() {
        return grossProfit;
    }

    public void setGrossProfit(long grossProfit) {
        this.grossProfit = grossProfit;
    }

    public long getTotalVoucherDiscount() {
        return totalVoucherDiscount;
    }

    public void setTotalVoucherDiscount(long totalVoucherDiscount) {
        this.totalVoucherDiscount = totalVoucherDiscount;
    }
}
