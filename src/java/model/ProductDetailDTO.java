/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import java.math.BigDecimal;
import java.util.List;
import java.util.ArrayList;

// Nếu bạn dùng thư viện Lombok, hãy thêm @Data ở đây để tự động tạo Getters/Setters
// @Data 
public class ProductDetailDTO {

    // ==========================================
    // 1. THÔNG TIN CƠ BẢN (Từ tbProduct & tbCategory)
    // ==========================================
    private Integer productId;
    private String sku;
    private String name;              // Tên sản phẩm (VD: The Great Gatsby)
    private String categoryName;      // Tên danh mục (VD: Tiểu thuyết & Văn học)
    private String productType;       // 'BOOK' hoặc 'STATIONERY'
    private double originalPrice;
    private double unitCost;      // Giá vốn hiện tại (không cộng thuế) — thay cho hệ thống lô tbSKU cũ
    private int stockQuantity;    // Số lượng tồn kho
    private int weightGrams;      // Trọng lượng
    private String publisher;
    private Integer publicationYear;
    private String isbn;
    private String brandorauthor;
    private String images;
    private Integer categoryId;      // cần để <select> danh mục tự chọn đúng khi mở form Sửa
    private String specifications;   // riêng cho Stationery
    private String status;           // ACTIVE / DISCONTINUED

    private String description;
    private double ratingAvg;    // cache đồng bộ từ tbReview
    private int ratingCount;     // cache đồng bộ từ tbReview

    private double priceBeforeTax;

    public ProductDetailDTO() {
    }

    public ProductDetailDTO(Integer productId, String sku, String name, String categoryName, String productType, double originalPrice, int stockQuantity, int weightGrams, String publisher, Integer publicationYear, String isbn, String brandorauthor, String images) {
        this.productId = productId;
        this.sku = sku;
        this.name = name;
        this.categoryName = categoryName;
        this.productType = productType;
        this.originalPrice = originalPrice;
        this.stockQuantity = stockQuantity;
        this.weightGrams = weightGrams;
        this.publisher = publisher;
        this.publicationYear = publicationYear;
        this.isbn = isbn;
        this.brandorauthor = brandorauthor;
        this.images = images;
    }

    public ProductDetailDTO(Integer productId, String sku, String name, String categoryName, String productType, double originalPrice, int stockQuantity, int weightGrams, String publisher, Integer publicationYear, String isbn, String brandorauthor, String images, Integer categoryId, String specifications, String status, String description) {
        this.productId = productId;
        this.sku = sku;
        this.name = name;
        this.categoryName = categoryName;
        this.productType = productType;
        this.originalPrice = originalPrice;
        this.stockQuantity = stockQuantity;
        this.weightGrams = weightGrams;
        this.publisher = publisher;
        this.publicationYear = publicationYear;
        this.isbn = isbn;
        this.brandorauthor = brandorauthor;
        this.images = images;
        this.categoryId = categoryId;
        this.specifications = specifications;
        this.status = status;
        this.description = description;
    }

    public Integer getProductId() {
        return productId;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public String getSpecifications() {
        return specifications;
    }

    public void setSpecifications(String specifications) {
        this.specifications = specifications;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getRatingAvg() {
        return ratingAvg;
    }

    public void setRatingAvg(double ratingAvg) {
        this.ratingAvg = ratingAvg;
    }

    public int getRatingCount() {
        return ratingCount;
    }

    public void setRatingCount(int ratingCount) {
        this.ratingCount = ratingCount;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getProductType() {
        return productType;
    }

    public void setProductType(String productType) {
        this.productType = productType;
    }

    public double getOriginalPrice() {
        return originalPrice;
    }

    public void setOriginalPrice(double originalPrice) {
        this.originalPrice = originalPrice;
    }

    public double getUnitCost() {
        return unitCost;
    }

    public void setUnitCost(double unitCost) {
        this.unitCost = unitCost;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public int getWeightGrams() {
        return weightGrams;
    }

    public void setWeightGrams(int weightGrams) {
        this.weightGrams = weightGrams;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public Integer getPublicationYear() {
        return publicationYear;
    }

    public void setPublicationYear(Integer publicationYear) {
        this.publicationYear = publicationYear;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getBrandorauthor() {
        return brandorauthor;
    }

    public void setBrandorauthor(String brandorauthor) {
        this.brandorauthor = brandorauthor;
    }

    public String getImages() {
        return images;
    }

    public void setImages(String images) {
        this.images = images;
    }

    public double getPriceBeforeTax() {
        return priceBeforeTax;
    }

    public void setPriceBeforeTax(double priceBeforeTax) {
        this.priceBeforeTax = priceBeforeTax;
    }

    @Override
    public String toString() {
        return "ProductDetailDTO{" + "productId=" + productId + ", sku=" + sku + ", name=" + name + ", categoryName=" + categoryName + ", productType=" + productType + ", originalPrice=" + originalPrice + ", stockQuantity=" + stockQuantity + ", weightGrams=" + weightGrams + ", publisher=" + publisher + ", publicationYear=" + publicationYear + ", isbn=" + isbn + ", brandorauthor=" + brandorauthor + ", images=" + images + '}';
    }

}
