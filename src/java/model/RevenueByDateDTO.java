package model;

public class RevenueByDateDTO {

    private String dateLabel;  // dạng "dd/MM" hoặc "MM/yyyy" để hiện trực tiếp trên trục X của Chart.js
    private long revenue;      // NET — đã trừ voucher, vì đây là group theo đơn hàng (không cần phân bổ dòng)

    public RevenueByDateDTO() {
    }

    public String getDateLabel() {
        return dateLabel;
    }

    public void setDateLabel(String dateLabel) {
        this.dateLabel = dateLabel;
    }

    public long getRevenue() {
        return revenue;
    }

    public void setRevenue(long revenue) {
        this.revenue = revenue;
    }
}
