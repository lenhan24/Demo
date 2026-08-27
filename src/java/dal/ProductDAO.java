/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dal;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import model.OrderDTO;
import model.OrderDetailDTO;
import model.ProductDTO;
import model.ProductDetailDTO;

public class ProductDAO extends DBContext {

    public List<ProductDTO> getTopProducts() {
        List<ProductDTO> list = new ArrayList<>();
        String query = "SELECT TOP 4 p.product_id, p.name, p.price, "
                + "COALESCE(p.author, p.brand) AS author_brand, c.name AS cat_name, i.image_url "
                + "FROM tbProduct p "
                + "INNER JOIN tbCategory c ON p.category_id = c.category_id "
                + "LEFT JOIN tbProductImage i ON p.product_id = i.product_id AND i.is_thumbnail = 1 "
                + "WHERE p.status = 'ACTIVE' "
                + "ORDER BY p.created_at DESC";

        // Sử dụng trực tiếp biến 'connection' được thừa kế từ cha
        try (PreparedStatement ps = connection.prepareStatement(query); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                ProductDTO p = new ProductDTO();
                p.setProductId(rs.getInt("product_id"));
                p.setName(rs.getString("name"));
                p.setPrice(rs.getDouble("price"));
                p.setAuthorOrBrand(rs.getString("author_brand"));
                p.setCategoryName(rs.getString("cat_name"));
                p.setThumbnailUrl(rs.getString("image_url"));
                list.add(p);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<ProductDTO> getProductsByCategoryId(int categoryId) {
        List<ProductDTO> list = new ArrayList<>();
        String query = "SELECT top 4 p.product_id, p.name, p.price, "
                + "COALESCE(p.author, p.brand) AS author_brand, "
                + // Lấy tác giả hoặc thương hiệu
                "c.name AS cat_name, i.image_url "
                + "FROM tbProduct p "
                + "INNER JOIN tbCategory c ON p.category_id = c.category_id "
                + "LEFT JOIN tbProductImage i ON p.product_id = i.product_id AND i.is_thumbnail = 1 "
                + "WHERE p.category_id = ? AND p.status = 'ACTIVE'"
                + "ORDER BY p.created_at DESC";;

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, categoryId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ProductDTO p = new ProductDTO();
                    p.setProductId(rs.getInt("product_id"));
                    p.setName(rs.getString("name"));
                    p.setPrice(rs.getDouble("price"));
                    p.setAuthorOrBrand(rs.getString("author_brand"));
                    p.setCategoryName(rs.getString("cat_name"));
                    p.setThumbnailUrl(rs.getString("image_url"));
                    list.add(p);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<ProductDTO> getProductsByCategoryName(String name) {
        List<ProductDTO> list = new ArrayList<>();
        String query = "SELECT top 4 p.product_id, p.name, p.price, "
                + "COALESCE(p.author, p.brand) AS author_brand, "
                + // Lấy tác giả hoặc thương hiệu
                "c.name AS cat_name, i.image_url "
                + "FROM tbProduct p "
                + "INNER JOIN tbCategory c ON p.category_id = c.category_id "
                + "LEFT JOIN tbProductImage i ON p.product_id = i.product_id AND i.is_thumbnail = 1 "
                + "WHERE c.name = ? AND p.status = 'ACTIVE'"
                + "ORDER BY p.created_at DESC";;

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ProductDTO p = new ProductDTO();
                    p.setProductId(rs.getInt("product_id"));
                    p.setName(rs.getString("name"));
                    p.setPrice(rs.getDouble("price"));
                    p.setAuthorOrBrand(rs.getString("author_brand"));
                    p.setCategoryName(rs.getString("cat_name"));
                    p.setThumbnailUrl(rs.getString("image_url"));
                    list.add(p);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public ProductDetailDTO getProductById(int productId) {
        ProductDetailDTO p = null;

        // Cập nhật câu query để lấy đầy đủ các trường cần thiết cho ProductDetailDTO
        String query = "SELECT p.product_id, p.sku, p.name, c.name AS cat_name, "
                + "p.product_type, p.price, p.stock_quantity, p.weight_grams, "
                + "p.publisher, p.publication_year, p.isbn, p.description, p.specifications, "
                + // Đã thêm ở đây
                "COALESCE(p.author, p.brand) AS brand_or_author, i.image_url, "
                + "ISNULL((SELECT AVG(CAST(rating AS DECIMAL(3,2))) FROM tbReview WHERE product_id = p.product_id AND status = 'VISIBLE'), 0) AS rating_avg, "
                + "ISNULL((SELECT COUNT(*) FROM tbReview WHERE product_id = p.product_id AND status = 'VISIBLE'), 0) AS rating_count "
                + "FROM tbProduct p "
                + "INNER JOIN tbCategory c ON p.category_id = c.category_id "
                + "LEFT JOIN tbProductImage i ON p.product_id = i.product_id AND i.is_thumbnail = 1 "
                + "WHERE p.product_id = ? AND p.status = 'ACTIVE'";

        // Sử dụng trực tiếp biến 'connection' được thừa kế từ cha
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    p = new ProductDetailDTO();

                    // Map dữ liệu từ ResultSet vào các thuộc tính của DTO
                    p.setProductId(rs.getInt("product_id"));
                    p.setSku(rs.getString("sku"));
                    p.setName(rs.getString("name"));
                    p.setCategoryName(rs.getString("cat_name"));
                    p.setProductType(rs.getString("product_type"));
                    p.setOriginalPrice(rs.getDouble("price"));
                    p.setStockQuantity(rs.getInt("stock_quantity"));
                    p.setWeightGrams(rs.getInt("weight_grams"));
                    p.setPublisher(rs.getString("publisher"));
                    p.setIsbn(rs.getString("isbn"));
                    p.setBrandorauthor(rs.getString("brand_or_author"));
                    p.setRatingAvg(rs.getDouble("rating_avg"));
                    p.setRatingCount(rs.getInt("rating_count"));
                    p.setDescription(rs.getString("description"));
                    p.setSpecifications(rs.getString("specifications"));
                    // Gán URL của ảnh (image_url) vào trường 'images' kiểu String của bạn
                    p.setImages(rs.getString("image_url"));

                    // Xử lý riêng cho publication_year vì nó có thể NULL trong database
                    int pubYear = rs.getInt("publication_year");
                    if (rs.wasNull()) {
                        p.setPublicationYear(null);
                    } else {
                        p.setPublicationYear(pubYear);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace(); // Trong thực tế nên dùng Logger thay vì printStackTrace
        }

        return p; // Sẽ trả về null nếu không tìm thấy ID tương ứng
    }

    //Lấy TOÀN BỘ sản phẩm theo loại (Book / Stationery)
    public List<ProductDTO> getProductsByType(String productType) {
        List<ProductDTO> list = new ArrayList<>();
        // Lưu ý: Đã bỏ "TOP 4" để lấy toàn bộ danh sách
        String query = "SELECT p.product_id, p.name, p.price, "
                + "COALESCE(p.author, p.brand) AS author_brand, c.name AS cat_name, i.image_url "
                + "FROM tbProduct p "
                + "INNER JOIN tbCategory c ON p.category_id = c.category_id "
                + "LEFT JOIN tbProductImage i ON p.product_id = i.product_id AND i.is_thumbnail = 1 "
                + "WHERE p.product_type = ? AND p.status = 'ACTIVE' "
                + "ORDER BY p.created_at DESC";

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, productType);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ProductDTO p = new ProductDTO();
                    p.setProductId(rs.getInt("product_id"));
                    p.setName(rs.getString("name"));
                    p.setPrice(rs.getDouble("price"));
                    p.setAuthorOrBrand(rs.getString("author_brand"));
                    p.setCategoryName(rs.getString("cat_name"));
                    p.setThumbnailUrl(rs.getString("image_url"));
                    list.add(p);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    //Lấy tổng số sản phẩm theo loại (Book hoặc Stationery) để tính số trang
    public int getTotalProductsByType(String productType, Double minPrice, Double maxPrice) {
        String query = "SELECT COUNT(*) FROM tbProduct p "
                + "WHERE p.product_type = ? AND p.status = 'ACTIVE' "
                + "AND (? IS NULL OR p.price >= ?) AND (? IS NULL OR p.price <= ?)";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, productType);
            setNullableDouble(ps, 2, minPrice);
            setNullableDouble(ps, 3, minPrice);
            setNullableDouble(ps, 4, maxPrice);
            setNullableDouble(ps, 5, maxPrice);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1); // Trả về con số tổng
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Helper set tham số giá lọc (nullable) — dùng chung cho
     * search/type/category.
     */
    private void setNullableDouble(PreparedStatement ps, int idx, Double value) throws java.sql.SQLException {
        if (value == null) {
            ps.setNull(idx, java.sql.Types.DOUBLE);
        } else {
            ps.setDouble(idx, value);
        }
    }

    //Lấy danh sách sản phẩm có phân trang (10 mục/trang), lọc thêm được khoảng giá (null = không lọc)
    public List<ProductDTO> getProductsByTypePaging(String productType, Double minPrice, Double maxPrice, int page, int pageSize) {
        List<ProductDTO> list = new ArrayList<>();
        // Sử dụng OFFSET và FETCH NEXT của T-SQL để phân trang
        String query = "SELECT p.product_id, p.name, p.price, "
                + "COALESCE(p.author, p.brand) AS author_brand, c.name AS cat_name, i.image_url "
                + "FROM tbProduct p "
                + "INNER JOIN tbCategory c ON p.category_id = c.category_id "
                + "LEFT JOIN tbProductImage i ON p.product_id = i.product_id AND i.is_thumbnail = 1 "
                + "WHERE p.product_type = ? AND p.status = 'ACTIVE' "
                + "AND (? IS NULL OR p.price >= ?) AND (? IS NULL OR p.price <= ?) "
                + "ORDER BY p.created_at DESC "
                + "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, productType);
            setNullableDouble(ps, 2, minPrice);
            setNullableDouble(ps, 3, minPrice);
            setNullableDouble(ps, 4, maxPrice);
            setNullableDouble(ps, 5, maxPrice);

            // Tính toán vị trí bắt đầu lấy dữ liệu (OFFSET)
            int offset = (page - 1) * pageSize;
            ps.setInt(6, offset);
            ps.setInt(7, pageSize); // Số lượng lấy (FETCH NEXT)

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ProductDTO p = new ProductDTO();
                    p.setProductId(rs.getInt("product_id"));
                    p.setName(rs.getString("name"));
                    p.setPrice(rs.getDouble("price"));
                    p.setAuthorOrBrand(rs.getString("author_brand"));
                    p.setCategoryName(rs.getString("cat_name"));
                    p.setThumbnailUrl(rs.getString("image_url"));
                    list.add(p);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // 1. Đếm tổng sản phẩm theo danh mục (phục vụ chia trang), lọc thêm được khoảng giá
    public int getTotalProductsByCategory(int categoryId, Double minPrice, Double maxPrice) {
        String query = "SELECT COUNT(*) FROM tbProduct p "
                + "WHERE p.category_id = ? AND p.status = 'ACTIVE' "
                + "AND (? IS NULL OR p.price >= ?) AND (? IS NULL OR p.price <= ?)";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, categoryId);
            setNullableDouble(ps, 2, minPrice);
            setNullableDouble(ps, 3, minPrice);
            setNullableDouble(ps, 4, maxPrice);
            setNullableDouble(ps, 5, maxPrice);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    //Lấy danh sách sản phẩm theo danh mục (có phân trang), lọc thêm được khoảng giá
    public List<ProductDTO> getProductsByCategoryPaging(int categoryId, Double minPrice, Double maxPrice, int page, int pageSize) {
        List<ProductDTO> list = new ArrayList<>();
        String query = "SELECT p.product_id, p.name, p.price, "
                + "COALESCE(p.author, p.brand) AS author_brand, c.name AS cat_name, i.image_url "
                + "FROM tbProduct p "
                + "INNER JOIN tbCategory c ON p.category_id = c.category_id "
                + "LEFT JOIN tbProductImage i ON p.product_id = i.product_id AND i.is_thumbnail = 1 "
                + "WHERE p.category_id = ? AND p.status = 'ACTIVE' "
                + "AND (? IS NULL OR p.price >= ?) AND (? IS NULL OR p.price <= ?) "
                + "ORDER BY p.created_at DESC "
                + "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, categoryId);
            setNullableDouble(ps, 2, minPrice);
            setNullableDouble(ps, 3, minPrice);
            setNullableDouble(ps, 4, maxPrice);
            setNullableDouble(ps, 5, maxPrice);
            ps.setInt(6, (page - 1) * pageSize);
            ps.setInt(7, pageSize);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ProductDTO p = new ProductDTO();
                    p.setProductId(rs.getInt("product_id"));
                    p.setName(rs.getString("name"));
                    p.setPrice(rs.getDouble("price"));
                    p.setAuthorOrBrand(rs.getString("author_brand"));
                    p.setCategoryName(rs.getString("cat_name"));
                    p.setThumbnailUrl(rs.getString("image_url"));
                    list.add(p);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // Đếm số lượng kết quả tìm kiếm
    public int getTotalProductsBySearch(String keyword, Integer categoryId, Double minPrice, Double maxPrice) {
        String query = "SELECT COUNT(*) FROM tbProduct p "
                + "WHERE p.name LIKE ? AND p.status = 'ACTIVE' "
                + "AND (? IS NULL OR p.category_id = ?) "
                + "AND (? IS NULL OR p.price >= ?) AND (? IS NULL OR p.price <= ?)";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            // Thêm % vào 2 đầu để tìm kiếm từ khóa chứa ở bất kỳ đâu trong tên
            ps.setString(1, "%" + keyword + "%");
            if (categoryId == null) {
                ps.setNull(2, java.sql.Types.INTEGER);
                ps.setNull(3, java.sql.Types.INTEGER);
            } else {
                ps.setInt(2, categoryId);
                ps.setInt(3, categoryId);
            }
            setNullableDouble(ps, 4, minPrice);
            setNullableDouble(ps, 5, minPrice);
            setNullableDouble(ps, 6, maxPrice);
            setNullableDouble(ps, 7, maxPrice);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    //Lấy danh sách kết quả tìm kiếm (có phân trang), lọc thêm được theo danh mục + khoảng giá (null nghĩa là không lọc)
    public List<ProductDTO> searchProductsPaging(String keyword, Integer categoryId, Double minPrice, Double maxPrice, int page, int pageSize) {
        List<ProductDTO> list = new ArrayList<>();
        String query = "SELECT p.product_id, p.name, p.price, "
                + "COALESCE(p.author, p.brand) AS author_brand, c.name AS cat_name, i.image_url "
                + "FROM tbProduct p "
                + "INNER JOIN tbCategory c ON p.category_id = c.category_id "
                + "LEFT JOIN tbProductImage i ON p.product_id = i.product_id AND i.is_thumbnail = 1 "
                + "WHERE p.name LIKE ? AND p.status = 'ACTIVE' "
                + "AND (? IS NULL OR p.category_id = ?) "
                + "AND (? IS NULL OR p.price >= ?) AND (? IS NULL OR p.price <= ?) "
                + "ORDER BY p.created_at DESC "
                + "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, "%" + keyword + "%");
            if (categoryId == null) {
                ps.setNull(2, java.sql.Types.INTEGER);
                ps.setNull(3, java.sql.Types.INTEGER);
            } else {
                ps.setInt(2, categoryId);
                ps.setInt(3, categoryId);
            }
            setNullableDouble(ps, 4, minPrice);
            setNullableDouble(ps, 5, minPrice);
            setNullableDouble(ps, 6, maxPrice);
            setNullableDouble(ps, 7, maxPrice);
            ps.setInt(8, (page - 1) * pageSize);
            ps.setInt(9, pageSize);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ProductDTO p = new ProductDTO();
                    p.setProductId(rs.getInt("product_id"));
                    p.setName(rs.getString("name"));
                    p.setPrice(rs.getDouble("price"));
                    p.setAuthorOrBrand(rs.getString("author_brand"));
                    p.setCategoryName(rs.getString("cat_name"));
                    p.setThumbnailUrl(rs.getString("image_url"));
                    list.add(p);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<ProductDetailDTO> getAllProductsForManager(String keyword, String productType, int page, int pageSize) {
        List<ProductDetailDTO> list = new ArrayList<>();
        String query = "SELECT p.product_id, p.sku, p.name, p.price, (p.price / 1.1) AS price_before_tax, p.unit_cost, p.stock_quantity, p.weight_grams, "
                + "p.product_type, p.status, p.category_id, c.name AS cat_name, "
                + "p.isbn, p.author, p.publisher, p.publication_year, p.brand, p.specifications, "
                + "p.description, i.image_url "
                + "FROM tbProduct p INNER JOIN tbCategory c ON p.category_id = c.category_id "
                + "LEFT JOIN tbProductImage i ON p.product_id = i.product_id AND i.is_thumbnail = 1 "
                + "WHERE (? IS NULL OR p.name LIKE ? OR p.sku LIKE ? OR p.isbn LIKE ?) "
                + "AND (? IS NULL OR p.product_type = ?) "
                + "ORDER BY p.created_at DESC "
                + "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            String kw = (keyword == null || keyword.trim().isEmpty()) ? null : keyword.trim();
            String like = (kw == null) ? null : "%" + kw + "%";
            String pt = (productType == null || productType.trim().isEmpty()) ? null : productType.trim();
            ps.setString(1, kw);
            ps.setString(2, like);
            ps.setString(3, like);
            ps.setString(4, like);
            ps.setString(5, pt);
            ps.setString(6, pt);
            ps.setInt(7, (page - 1) * pageSize);
            ps.setInt(8, pageSize);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ProductDetailDTO p = new ProductDetailDTO();
                    p.setProductId(rs.getInt("product_id"));
                    p.setSku(rs.getString("sku"));
                    p.setName(rs.getString("name"));
                    p.setOriginalPrice(rs.getDouble("price"));
                    p.setPriceBeforeTax(rs.getDouble("price_before_tax"));
                    p.setUnitCost(rs.getDouble("unit_cost"));
                    p.setStockQuantity(rs.getInt("stock_quantity"));
                    p.setWeightGrams(rs.getInt("weight_grams"));
                    p.setProductType(rs.getString("product_type"));
                    p.setStatus(rs.getString("status"));
                    p.setCategoryId(rs.getInt("category_id"));
                    p.setCategoryName(rs.getString("cat_name"));
                    p.setIsbn(rs.getString("isbn"));
                    p.setPublisher(rs.getString("publisher"));
                    int year = rs.getInt("publication_year");
                    p.setPublicationYear(rs.wasNull() ? null : year);
                    p.setBrandorauthor("BOOK".equals(p.getProductType())
                            ? rs.getString("author") : rs.getString("brand"));
                    p.setSpecifications(rs.getString("specifications"));
                    p.setDescription(rs.getString("description"));
                    p.setImages(rs.getString("image_url"));
                    list.add(p);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

   public int countProductsForManager(String keyword, String productType) {
        String query = "SELECT COUNT(*) FROM tbProduct p "
                + "WHERE (? IS NULL OR p.name LIKE ? OR p.sku LIKE ? OR p.isbn LIKE ?) "
                + "AND (? IS NULL OR p.product_type = ?)";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            String kw = (keyword == null || keyword.trim().isEmpty()) ? null : keyword.trim();
            String like = (kw == null) ? null : "%" + kw + "%";
            String pt = (productType == null || productType.trim().isEmpty()) ? null : productType.trim();
            ps.setString(1, kw);
            ps.setString(2, like);
            ps.setString(3, like);
            ps.setString(4, like);
            ps.setString(5, pt);
            ps.setString(6, pt);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Thêm sản phẩm mới. Trả về product_id vừa tạo, hoặc -1 nếu lỗi.
     */
    public int insertProduct(ProductDetailDTO p) {
        // Mã trả về: >0 = product_id mới tạo | -1 = lỗi DB | -2 = ISBN rỗng
// -3 = ISBN sai checksum | -4 = ISBN đã tồn tại
        boolean isBook = "BOOK".equals(p.getProductType());

        if (isBook) {
            if (p.getIsbn() == null || p.getIsbn().trim().isEmpty()) {
                return -2;
            }
            if (!utilities.ProductValidator.isValidIsbn(p.getIsbn())) {
                return -3;
            }
            if (isbnExists(p.getIsbn())) {
                return -4;
            }
        }

        String query = "INSERT INTO tbProduct "
                + "(sku, name, price, unit_cost, stock_quantity, weight_grams, category_id, product_type, status, "
                + "isbn, author, publisher, publication_year, brand, specifications, description) "
                + "VALUES (?,?,?,?,?,?,?,?,'ACTIVE',?,?,?,?,?,?,?)";

        try (PreparedStatement ps = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, p.getSku());
            ps.setString(2, p.getName());
            ps.setDouble(3, p.getOriginalPrice() * 1.1); // giá nhập là giá trước thuế
            ps.setDouble(4, p.getUnitCost()); // giá vốn không cộng thuế
            ps.setInt(5, p.getStockQuantity());
            ps.setInt(6, p.getWeightGrams());
            ps.setInt(7, p.getCategoryId());
            ps.setString(8, p.getProductType());

            ps.setString(9, isBook ? p.getIsbn() : null);
            ps.setString(10, isBook ? p.getBrandorauthor() : null); // author
            ps.setString(11, isBook ? p.getPublisher() : null);

            if (isBook && p.getPublicationYear() != null) {
                ps.setInt(12, p.getPublicationYear());
            } else {
                ps.setNull(12, java.sql.Types.INTEGER);
            }
            ps.setString(13, isBook ? null : p.getBrandorauthor()); // brand
            ps.setString(14, isBook ? null : p.getSpecifications());
            ps.setString(15, p.getDescription());
            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        return keys.getInt(1);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Cập nhật thông tin sản phẩm, gồm cả tồn kho + giá vốn (không còn quản lý
     * qua phiếu nhập kho theo lô nữa — Manager sửa trực tiếp tại đây).
     */
    public int updateProduct(ProductDetailDTO p) {
        // Mã trả về: 1 = thành công | 0 = không tìm thấy product | -1 = lỗi DB
// -2 = ISBN rỗng | -3 = ISBN sai checksum | -4 = ISBN trùng (của sản phẩm khác)

        boolean isBook = "BOOK".equals(p.getProductType());

        if (isBook) {
            if (p.getIsbn() == null || p.getIsbn().trim().isEmpty()) {
                return -2;
            }
            if (!utilities.ProductValidator.isValidIsbn(p.getIsbn())) {
                return -3;
            }
            if (isbnExistsExceptProduct(p.getIsbn(), p.getProductId())) {
                return -4;
            }
        }
        String query = "UPDATE tbProduct SET name=?, price=?, unit_cost=?, stock_quantity=?, weight_grams=?, category_id=?, "
                + "isbn=?, author=?, publisher=?, publication_year=?, brand=?, specifications=?, description=? "
                + "WHERE product_id=?";

        try (PreparedStatement ps = connection.prepareStatement(query)) {

            ps.setString(1, p.getName());
            ps.setDouble(2, p.getOriginalPrice() * 1.1);
            ps.setDouble(3, p.getUnitCost());
            ps.setInt(4, p.getStockQuantity());
            ps.setInt(5, p.getWeightGrams());
            ps.setInt(6, p.getCategoryId());
            ps.setString(7, isBook ? p.getIsbn() : null);
            ps.setString(8, isBook ? p.getBrandorauthor() : null);
            ps.setString(9, isBook ? p.getPublisher() : null);
            if (isBook && p.getPublicationYear() != null) {
                ps.setInt(10, p.getPublicationYear());
            } else {
                ps.setNull(10, java.sql.Types.INTEGER);
            }
            ps.setString(11, isBook ? null : p.getBrandorauthor());
            ps.setString(12, isBook ? null : p.getSpecifications());
            ps.setString(13, p.getDescription());
            ps.setInt(14, p.getProductId());

            return ps.executeUpdate() > 0 ? 1 : 0;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Sản phẩm đã từng có trong đơn hàng nào chưa (quyết định soft/hard
     * delete).
     */
    public boolean hasOrderHistory(int productId) {
        String query = "SELECT COUNT(*) FROM tbOrderDetail WHERE product_id=?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true; // an toàn: nếu lỗi thì coi như có lịch sử, tránh xóa nhầm
    }

    /**
     * Soft delete: chuyển trạng thái (dùng khi sản phẩm đã có lịch sử đơn
     * hàng).
     */
    public boolean updateProductStatus(int productId, String status) {
        String query = "UPDATE tbProduct SET status=? WHERE product_id=?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, status);
            ps.setInt(2, productId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Hard delete: chỉ gọi khi chưa có lịch sử đơn hàng.
     */
    public boolean deleteProductHard(int productId) {
        try (PreparedStatement clearCart = connection.prepareStatement(
                "DELETE FROM tbCartItem WHERE product_id=?")) {
            clearCart.setInt(1, productId);
            clearCart.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
        String query = "DELETE FROM tbProduct WHERE product_id=?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, productId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            // Nếu còn bị FK ràng buộc (combo, phiếu nhập, báo cáo lỗi...), fallback về soft delete
            e.printStackTrace();
            return updateProductStatus(productId, "DISCONTINUED");
        }
    }

    /**
     * Kiểm tra trùng SKU trước khi insert (SKU là UNIQUE trong tbProduct).
     */
    public boolean skuExists(String sku) {
        String query = "SELECT COUNT(*) FROM tbProduct WHERE sku=?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, sku);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
//check xem isbn ddax toonf tai chua

    public boolean isbnExists(String isbn) {
        if (isbn == null || isbn.trim().isEmpty()) {
            return false;
        }
        String query = "SELECT COUNT(*) FROM tbProduct WHERE isbn=?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, isbn);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean isbnExistsExceptProduct(String isbn, int productId) {
        if (isbn == null || isbn.trim().isEmpty()) {
            return false;
        }
        String query = "SELECT COUNT(*) FROM tbProduct WHERE isbn=? AND product_id<>?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, isbn);
            ps.setInt(2, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Thêm ảnh thumbnail cho sản phẩm mới tạo
     */
    public boolean insertThumbnail(int productId, String imageUrl) {
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            return true; // cho phép bỏ trống, không coi là lỗi
        }
        String query = "INSERT INTO tbProductImage (product_id, image_url, display_order, is_thumbnail) "
                + "VALUES (?, ?, 0, 1)";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, productId);
            ps.setString(2, imageUrl.trim());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Cập nhật ảnh thumbnail đã có (nếu chưa có thì insert mới)
     */
    public boolean updateThumbnail(int productId, String imageUrl) {
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            return true;
        }
        String checkQuery = "SELECT COUNT(*) FROM tbProductImage WHERE product_id=? AND is_thumbnail=1";
        try (PreparedStatement check = connection.prepareStatement(checkQuery)) {
            check.setInt(1, productId);
            try (ResultSet rs = check.executeQuery()) {
                boolean exists = rs.next() && rs.getInt(1) > 0;
                String query = exists
                        ? "UPDATE tbProductImage SET image_url=? WHERE product_id=? AND is_thumbnail=1"
                        : "INSERT INTO tbProductImage (product_id, image_url, display_order, is_thumbnail) VALUES (?,?,0,1)";
                try (PreparedStatement ps = connection.prepareStatement(query)) {
                    if (exists) {
                        ps.setString(1, imageUrl.trim());
                        ps.setInt(2, productId);
                    } else {
                        ps.setInt(1, productId);
                        ps.setString(2, imageUrl.trim());
                    }
                    return ps.executeUpdate() > 0;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Lấy số lượng tồn kho hiện tại của 1 sản phẩm — đọc thẳng tbProduct, thay
     * cho getActiveLotStock() cũ (đọc qua tbSKU, đã bỏ).
     */
    public int getStockQuantity(int productId) {
        String query = "SELECT stock_quantity FROM tbProduct WHERE product_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("stock_quantity");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public List<ProductDetailDTO> getLowStockProducts(int threshold) {
        List<ProductDetailDTO> list = new ArrayList<>();
        String query = "SELECT p.product_id, p.sku, p.name, p.stock_quantity, p.price, "
                + "p.product_type, p.status, c.name AS cat_name "
                + "FROM tbProduct p INNER JOIN tbCategory c ON p.category_id = c.category_id "
                + "WHERE p.status = 'ACTIVE' AND p.stock_quantity <= ? "
                + "ORDER BY p.stock_quantity ASC";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, threshold);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ProductDetailDTO p = new ProductDetailDTO();
                    p.setProductId(rs.getInt("product_id"));
                    p.setSku(rs.getString("sku"));
                    p.setName(rs.getString("name"));
                    p.setStockQuantity(rs.getInt("stock_quantity"));
                    p.setOriginalPrice(rs.getDouble("price"));
                    p.setProductType(rs.getString("product_type"));
                    p.setStatus(rs.getString("status"));
                    p.setCategoryName(rs.getString("cat_name"));
                    list.add(p);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

}
