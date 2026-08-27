/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import java.util.Date;

/**
 *
 * @author GiGaByte
 */
public class VoucherDTO {
    private int voucherId;
    private String code;
    private String discountType;   // "PERCENTAGE" hoặc "FIXED_AMOUNT"
    private long discountValue;
    private Long maxDiscountCap;   // có thể null
    private long minOrderValue;
    private Date startDate;
    private Date endDate;
    private int maxUsageLimit;
    private int perCustomerLimit;
     private String status; 
    public VoucherDTO() {}

    public VoucherDTO(int voucherId, String code, String discountType, long discountValue,
                       Long maxDiscountCap, long minOrderValue, Date startDate, Date endDate,
                       int maxUsageLimit, int perCustomerLimit) {
        this.voucherId = voucherId;
        this.code = code;
        this.discountType = discountType;
        this.discountValue = discountValue;
        this.maxDiscountCap = maxDiscountCap;
        this.minOrderValue = minOrderValue;
        this.startDate = startDate;
        this.endDate = endDate;
        this.maxUsageLimit = maxUsageLimit;
        this.perCustomerLimit = perCustomerLimit;
    }

    public VoucherDTO(int voucherId, String code, String discountType, long discountValue, Long maxDiscountCap, long minOrderValue, Date startDate, Date endDate, int maxUsageLimit, int perCustomerLimit, String status) {
        this.voucherId = voucherId;
        this.code = code;
        this.discountType = discountType;
        this.discountValue = discountValue;
        this.maxDiscountCap = maxDiscountCap;
        this.minOrderValue = minOrderValue;
        this.startDate = startDate;
        this.endDate = endDate;
        this.maxUsageLimit = maxUsageLimit;
        this.perCustomerLimit = perCustomerLimit;
        this.status = status;
    }

    public void setVoucherId(int voucherId) {
        this.voucherId = voucherId;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setDiscountType(String discountType) {
        this.discountType = discountType;
    }

    public void setDiscountValue(long discountValue) {
        this.discountValue = discountValue;
    }

    public void setMaxDiscountCap(Long maxDiscountCap) {
        this.maxDiscountCap = maxDiscountCap;
    }

    public void setMinOrderValue(long minOrderValue) {
        this.minOrderValue = minOrderValue;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public void setMaxUsageLimit(int maxUsageLimit) {
        this.maxUsageLimit = maxUsageLimit;
    }

    public void setPerCustomerLimit(int perCustomerLimit) {
        this.perCustomerLimit = perCustomerLimit;
    }

    // --- Getters/Setters ---
    public int getVoucherId() { return voucherId; }
    public String getCode() { return code; }
    public String getDiscountType() { return discountType; }
    public long getDiscountValue() { return discountValue; }
    public Long getMaxDiscountCap() { return maxDiscountCap; }
    public long getMinOrderValue() { return minOrderValue; }
    public Date getStartDate() { return startDate; }
    public Date getEndDate() { return endDate; }
    public int getMaxUsageLimit() { return maxUsageLimit; }
    public int getPerCustomerLimit() { return perCustomerLimit; }

    /** Tính số tiền giảm dựa trên tạm tính đơn hàng */
    public long calculateDiscount(long orderSubtotal) {
        long discount = "PERCENTAGE".equals(discountType)
                ? orderSubtotal * discountValue / 100
                : discountValue;

        if (maxDiscountCap != null && discount > maxDiscountCap) {
            discount = maxDiscountCap;
        }
        if (discount > orderSubtotal) {
            discount = orderSubtotal;
        }
        return discount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    /** Nhãn hiển thị trong dropdown, VD: "BOOKY30 — Giảm 30% (tối đa 50.000đ)" */
    public String getDisplayLabel() {
        StringBuilder sb = new StringBuilder(code).append(" — Giảm ");
        if ("PERCENTAGE".equals(discountType)) {
            sb.append(discountValue).append("%");
            if (maxDiscountCap != null) {
                sb.append(" (tối đa ").append(String.format("%,d", maxDiscountCap)).append("đ)");
            }
        } else {
            sb.append(String.format("%,d", discountValue)).append("đ");
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return "VoucherDTO{" + "voucherId=" + voucherId + ", code=" + code + ", discountType=" + discountType + ", discountValue=" + discountValue + ", maxDiscountCap=" + maxDiscountCap + ", minOrderValue=" + minOrderValue + ", startDate=" + startDate + ", endDate=" + endDate + ", maxUsageLimit=" + maxUsageLimit + ", perCustomerLimit=" + perCustomerLimit + '}';
    }
    
}
