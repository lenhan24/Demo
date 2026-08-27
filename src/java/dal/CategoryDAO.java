/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import model.CategoryDTO;
/**
 *
 * @author GiGaByte
 */
public class CategoryDAO extends DBContext {

    public List<CategoryDTO> getAllCategories() {
        List<CategoryDTO> list = new ArrayList<>();
        String query = "SELECT c.category_id, c.name, COUNT(p.product_id) AS total " +
                       "FROM tbCategory c LEFT JOIN tbProduct p ON c.category_id = p.category_id " +
                       "GROUP BY c.category_id, c.name";
        
        // Không cần tạo lại biến connection, dùng luôn biến 'connection' từ DBContext
        try (PreparedStatement ps = connection.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                CategoryDTO c = new CategoryDTO();
                c.setCategoryId(rs.getInt("category_id"));
                c.setName(rs.getString("name"));
                c.setProductCount(rs.getInt("total"));
                list.add(c);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * MỚI: chỉ lấy danh mục THUỘC ĐÚNG loại sản phẩm (BOOK/STATIONERY) —
     * tbCategory không có cột product_type riêng (1 bảng dùng chung cho cả 2
     * loại), nên phải INNER JOIN qua tbProduct để lọc đúng. Dùng cho sidebar
     * khi đang browse theo type (/products?type=...), tránh hiện lẫn danh mục
     * Sách khi đang xem Văn phòng phẩm và ngược lại.
     */
    public List<CategoryDTO> getCategoriesByType(String productType) {
        List<CategoryDTO> list = new ArrayList<>();
        String query = "SELECT c.category_id, c.name, COUNT(p.product_id) AS total " +
                       "FROM tbCategory c INNER JOIN tbProduct p ON c.category_id = p.category_id " +
                       "WHERE p.product_type = ? AND p.status = 'ACTIVE' " +
                       "GROUP BY c.category_id, c.name";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, productType);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    CategoryDTO c = new CategoryDTO();
                    c.setCategoryId(rs.getInt("category_id"));
                    c.setName(rs.getString("name"));
                    c.setProductCount(rs.getInt("total"));
                    list.add(c);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
     public CategoryDTO getCategoryById(int categoryId) {
        CategoryDTO c = null;
        String query = "SELECT category_id, name FROM tbCategory WHERE category_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, categoryId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    c = new CategoryDTO();
                    c.setCategoryId(rs.getInt("category_id"));
                    c.setName(rs.getString("name"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return c;
    }
     /**
 * MỚI: xác định 1 category_id thuộc loại BOOK hay STATIONERY — cần cho
 * CategoryController để biết nên lọc sidebar theo type nào (categories
 * không có cột product_type riêng, phải suy ra từ sản phẩm thực tế).
 */
public String getCategoryProductType(int categoryId) {
    String query = "SELECT TOP 1 product_type FROM tbProduct WHERE category_id = ? AND status = 'ACTIVE'";
    try (PreparedStatement ps = connection.prepareStatement(query)) {
        ps.setInt(1, categoryId);
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getString("product_type");
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
    return null; // category rỗng (chưa có SP nào) — không xác định được type
}
}
