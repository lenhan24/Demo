/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 *
 * @author GiGaByte
 */
public class ProductDTO {
    private int productId;
    private String name;
    private String categoryName;
    private String authorOrBrand; 
    private double price;
    private String thumbnailUrl;

    public ProductDTO() {
    }

    public ProductDTO(int productId, String name, String categoryName, String authorOrBrand, double price, String thumbnailUrl) {
        this.productId = productId;
        this.name = name;
        this.categoryName = categoryName;
        this.authorOrBrand = authorOrBrand;
        this.price = price;
        this.thumbnailUrl = thumbnailUrl;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
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

    public String getAuthorOrBrand() {
        return authorOrBrand;
    }

    public void setAuthorOrBrand(String authorOrBrand) {
        this.authorOrBrand = authorOrBrand;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    @Override
    public String toString() {
        return "ProductDTO{" + "productId=" + productId + ", name=" + name + ", categoryName=" + categoryName + ", authorOrBrand=" + authorOrBrand + ", price=" + price + ", thumbnailUrl=" + thumbnailUrl + '}';
    }
    
    
}
