/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 *
 * @author GiGaByte
 */

public class CartItemDTO {
    // 1. Thông tin gốc từ bảng tbCartItem
    private int cartItemId;
    private int cartId;
    private int productId;
    private int quantity;
    private boolean isSelected; // Ánh xạ từ cột is_selected (BIT)

    // 2. Thông tin JOIN từ bảng tbProduct (Phục vụ hiển thị UI)
    private String productName;   // Tên sản phẩm (VD: "Rừng Na-uy", "Set giấy note 5 màu")
    private String productMeta;   // Thông tin phụ (VD: "Haruki Murakami", "Hộp 500 tờ")
    private String productImage;  // Đường dẫn ảnh thumbnail hoặc tên class icon
    private long price;           // Giá bán lẻ của 1 sản phẩm
    private boolean inStock;      // Trạng thái kho (VD: true = "Còn hàng")
    private int activeStock;      // Số lượng THẬT có thể mua ngay (lô FIFO đang ACTIVE) — dùng làm giới hạn max ở Cart/ProductDetail
    private int weightGrams;      // Khối lượng 1 sản phẩm (gram) — dùng tính phí ship theo cân nặng tổng đơn

    // 3. Thông tin tính toán thêm (Tiện lợi cho việc hiển thị tổng tiền từng dòng trên JSP)
    private long itemTotal;       // Tổng tiền của dòng này (= price * quantity)

    // --- Constructors ---
    
    public CartItemDTO() {
    }

    public CartItemDTO(int cartItemId, int cartId, int productId, int quantity, boolean isSelected, 
                       String productName, String productMeta, String productImage, 
                       long price, boolean inStock) {
        this.cartItemId = cartItemId;
        this.cartId = cartId;
        this.productId = productId;
        this.quantity = quantity;
        this.isSelected = isSelected;
        this.productName = productName;
        this.productMeta = productMeta;
        this.productImage = productImage;
        this.price = price;
        this.inStock = inStock;
        this.itemTotal = this.price * this.quantity; // Tự động tính tổng tiền của dòng
    }

    // --- Getters và Setters ---

    public int getCartItemId() {
        return cartItemId;
    }

    public void setCartItemId(int cartItemId) {
        this.cartItemId = cartItemId;
    }

    public int getCartId() {
        return cartId;
    }

    public void setCartId(int cartId) {
        this.cartId = cartId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
        this.itemTotal = this.price * this.quantity; // Cập nhật lại tổng tiền nếu số lượng thay đổi
    }

    public boolean getIsSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductMeta() {
        return productMeta;
    }

    public void setProductMeta(String productMeta) {
        this.productMeta = productMeta;
    }

    public String getProductImage() {
        return productImage;
    }

    public void setProductImage(String productImage) {
        this.productImage = productImage;
    }

    public long getPrice() {
        return price;
    }

    public void setPrice(long price) {
        this.price = price;
        this.itemTotal = this.price * this.quantity; // Cập nhật lại tổng tiền nếu giá thay đổi
    }

    public boolean isInStock() {
        return inStock;
    }

    public int getActiveStock() {
        return activeStock;
    }

    public void setActiveStock(int activeStock) {
        this.activeStock = activeStock;
    }

    public int getWeightGrams() {
        return weightGrams;
    }

    public void setWeightGrams(int weightGrams) {
        this.weightGrams = weightGrams;
    }

    public void setInStock(boolean inStock) {
        this.inStock = inStock;
    }

    public long getItemTotal() {
        return itemTotal;
    }

    // --- Phương thức hỗ trợ ---

    @Override
    public String toString() {
        return "CartItemDTO{" + "cartItemId=" + cartItemId + ", cartId=" + cartId + ", productId=" + productId + ", quantity=" + quantity + ", isSelected=" + isSelected + ", productName=" + productName + ", productMeta=" + productMeta + ", productImage=" + productImage + ", price=" + price + ", inStock=" + inStock + ", itemTotal=" + itemTotal + '}';
    }

    
}
