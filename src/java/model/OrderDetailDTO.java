/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 *
 * @author GiGaByte
 */
// OrderDetailDTO.java
public class OrderDetailDTO {
    private int orderDetailId;
    private int orderId;
    private int productId;
    private String productNameSnapshot;
    private int quantity;
    private long unitPrice;
    private long costPriceSnapshot;
    private long lineTotal;

    // getters & setters
    public int getOrderDetailId() { return orderDetailId; }
    public void setOrderDetailId(int orderDetailId) { this.orderDetailId = orderDetailId; }
    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }
    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }
    public String getProductNameSnapshot() { return productNameSnapshot; }
    public void setProductNameSnapshot(String productNameSnapshot) { this.productNameSnapshot = productNameSnapshot; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public long getUnitPrice() { return unitPrice; }
    public void setUnitPrice(long unitPrice) { this.unitPrice = unitPrice; }
    public long getCostPriceSnapshot() { return costPriceSnapshot; }
    public void setCostPriceSnapshot(long costPriceSnapshot) { this.costPriceSnapshot = costPriceSnapshot; }
    public long getLineTotal() { return lineTotal; }
    public void setLineTotal(long lineTotal) { this.lineTotal = lineTotal; }

    @Override
    public String toString() {
        return "OrderDetailDTO{" + "orderDetailId=" + orderDetailId + ", orderId=" + orderId + ", productId=" + productId + ", productNameSnapshot=" + productNameSnapshot + ", quantity=" + quantity + ", unitPrice=" + unitPrice + ", costPriceSnapshot=" + costPriceSnapshot + ", lineTotal=" + lineTotal + '}';
    }
    
    
}