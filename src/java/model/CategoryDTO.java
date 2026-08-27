/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 *
 * @author GiGaByte
 */
public class CategoryDTO {
    private int categoryId;
    private String name;
    private int productCount;

    public CategoryDTO() {
    }

    public CategoryDTO(int categoryId, String name, int productCount) {
        this.categoryId = categoryId;
        this.name = name;
        this.productCount = productCount;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getProductCount() {
        return productCount;
    }

    public void setProductCount(int productCount) {
        this.productCount = productCount;
    }

    @Override
    public String toString() {
        return "CategoryDTO{" + "categoryId=" + categoryId + ", name=" + name + ", productCount=" + productCount + '}';
    }
    
}
