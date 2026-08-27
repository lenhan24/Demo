package model;

public class VoucherUsageStatsDTO {

    private int voucherId;
    private String code;
    private String discountType;
    private int maxUsageLimit;
    private int usageCount;
    private long totalDiscountGiven;

    public VoucherUsageStatsDTO() {
    }

    public int getVoucherId() {
        return voucherId;
    }

    public void setVoucherId(int voucherId) {
        this.voucherId = voucherId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDiscountType() {
        return discountType;
    }

    public void setDiscountType(String discountType) {
        this.discountType = discountType;
    }

    public int getMaxUsageLimit() {
        return maxUsageLimit;
    }

    public void setMaxUsageLimit(int maxUsageLimit) {
        this.maxUsageLimit = maxUsageLimit;
    }

    public int getUsageCount() {
        return usageCount;
    }

    public void setUsageCount(int usageCount) {
        this.usageCount = usageCount;
    }

    public long getTotalDiscountGiven() {
        return totalDiscountGiven;
    }

    public void setTotalDiscountGiven(long totalDiscountGiven) {
        this.totalDiscountGiven = totalDiscountGiven;
    }

    /** Tỷ lệ đã dùng so với giới hạn tối đa — hữu ích để biết voucher nào "hot" */
    public double getUsagePercent() {
        return maxUsageLimit > 0 ? (usageCount * 100.0 / maxUsageLimit) : 0;
    }
}
