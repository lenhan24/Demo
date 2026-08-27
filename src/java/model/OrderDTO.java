/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author GiGaByte
 */
public class OrderDTO {

    private int orderId;
    private String orderCode;
    private int accountId;
    private String status;
    private String receiverName;
    private java.util.Date orderDate;
    private String receiverPhone;
    private String shippingAddress;
    private long subtotal;
    private long discountAmount;
    private long shippingFee;
    private long totalAmount;
    private String paymentMethod;
    private String paymentStatus;
    private String accountEmail; // email tài khoản khách — dùng để Staff liên hệ khi có yêu cầu hoàn hàng
    private String returnStatus; // null / REQUESTED / COMPLETED / REJECTED
    private String returnReason;
    private String cancelReason;
    private String phoneConfirmStatus; // PENDING / CONFIRMED / NOT_CONFIRMED — MỚI, phòng bom hàng

    private int phoneAttemptCount; //new


    private List<OrderDetailDTO> details = new ArrayList<>();

    // getters & setters
    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public String getOrderCode() {
        return orderCode;
    }

    public void setOrderCode(String orderCode) {
        this.orderCode = orderCode;
    }

    public int getAccountId() {
        return accountId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public String getReceiverPhone() {
        return receiverPhone;
    }

    public void setReceiverPhone(String receiverPhone) {
        this.receiverPhone = receiverPhone;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public long getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(long subtotal) {
        this.subtotal = subtotal;
    }

    public long getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(long discountAmount) {
        this.discountAmount = discountAmount;
    }

    public long getShippingFee() {
        return shippingFee;
    }

    public void setShippingFee(long shippingFee) {
        this.shippingFee = shippingFee;
    }

    public long getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(long totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getAccountEmail() {
        return accountEmail;
    }

    public void setAccountEmail(String accountEmail) {
        this.accountEmail = accountEmail;
    }

    public String getReturnStatus() {
        return returnStatus;
    }

    public void setReturnStatus(String returnStatus) {
        this.returnStatus = returnStatus;
    }

    public String getReturnReason() {
        return returnReason;
    }

    public void setReturnReason(String returnReason) {
        this.returnReason = returnReason;
    }

    public String getCancelReason() {
        return cancelReason;
    }

    public void setCancelReason(String cancelReason) {
        this.cancelReason = cancelReason;
    }

    public String getPhoneConfirmStatus() {
        return phoneConfirmStatus;
    }

    public void setPhoneConfirmStatus(String phoneConfirmStatus) {
        this.phoneConfirmStatus = phoneConfirmStatus;
    }

    public java.util.Date getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(java.util.Date orderDate) {
        this.orderDate = orderDate;
    }

    public List<OrderDetailDTO> getDetails() {
        return details;
    }

    public void setDetails(List<OrderDetailDTO> details) {
        this.details = details;
    }
    public int getPhoneAttemptCount() {
        return phoneAttemptCount;
    }

    public void setPhoneAttemptCount(int phoneAttemptCount) {
        this.phoneAttemptCount = phoneAttemptCount;
    }
    @Override
    public String toString() {
        return "OrderDTO{" + "orderId=" + orderId + ", orderCode=" + orderCode + ", accountId=" + accountId + ", status=" + status + ", receiverName=" + receiverName + ", receiverPhone=" + receiverPhone + ", shippingAddress=" + shippingAddress + ", subtotal=" + subtotal + ", discountAmount=" + discountAmount + ", shippingFee=" + shippingFee + ", totalAmount=" + totalAmount + ", paymentMethod=" + paymentMethod + ", paymentStatus=" + paymentStatus + '}';
    }

}
