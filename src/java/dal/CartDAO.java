/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dal;

/**
 *
 * @author GiGaByte
 */

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import model.CartItemDTO;

public class CartDAO extends DBContext {

    /**
     * Thêm sản phẩm vào giỏ hàng, có kiểm tra số lượng hợp lệ và tồn kho thật.
     *
     * TRƯỚC ĐÂY: hàm này nhận thẳng quantity từ request, không so sánh với
     * tồn kho, và không chặn số 0/âm -> khách có thể thêm số lượng vô hạn,
     * kể cả số âm (dẫn tới cộng ngược tồn kho lúc đặt hàng thành công).
     *
     * @return null nếu thêm thành công; ngược lại trả về thông điệp lỗi để
     *         controller hiển thị cho người dùng (không còn "im lặng" bỏ qua
     *         lỗi như trước).
     */
    public String addToCart(int accountId, int productId, int quantity) {
        if (quantity <= 0) {
            return "Số lượng phải lớn hơn 0.";
        }

        PreparedStatement stmGetCart = null;
        PreparedStatement stmInsertCart = null;
        PreparedStatement stmCheckItem = null;
        PreparedStatement stmUpdateItem = null;
        PreparedStatement stmInsertItem = null;
        ResultSet rs = null;
        String errorMessage = null;

        try {
            connection.setAutoCommit(false);

            int cartId = -1;

            // Số lượng THẬT đang có thể bán ngay — đọc thẳng tbProduct.stock_quantity
            // (không còn khái niệm lô ACTIVE/QUEUED riêng nữa).
            int availableStock = 0;
            String sqlStock = "SELECT stock_quantity FROM tbProduct WHERE product_id = ?";
            try (PreparedStatement stmStock = connection.prepareStatement(sqlStock)) {
                stmStock.setInt(1, productId);
                try (ResultSet rsStock = stmStock.executeQuery()) {
                    if (rsStock.next()) {
                        availableStock = rsStock.getInt("stock_quantity");
                    }
                }
            }

            if (availableStock <= 0) {
                connection.rollback();
                return "Sản phẩm hiện đã hết hàng.";
            }

            // BƯỚC 1: Kiểm tra xem tài khoản này đã có giỏ hàng (tbCart) chưa
            String sqlGetCart = "SELECT cart_id FROM tbCart WHERE account_id = ?";
            stmGetCart = connection.prepareStatement(sqlGetCart);
            stmGetCart.setInt(1, accountId);
            rs = stmGetCart.executeQuery();

            if (rs.next()) {
                // Nếu đã có giỏ hàng, lấy cart_id hiện tại
                cartId = rs.getInt("cart_id");
            } else {
                // Nếu chưa có, tiến hành INSERT giỏ hàng mới cho tài khoản này
                String sqlInsertCart = "INSERT INTO tbCart (account_id) VALUES (?)";
                // Tham số Statement.RETURN_GENERATED_KEYS giúp lấy về ID tự tăng (IDENTITY) vừa sinh ra
                stmInsertCart = connection.prepareStatement(sqlInsertCart, Statement.RETURN_GENERATED_KEYS);
                stmInsertCart.setInt(1, accountId);
                stmInsertCart.executeUpdate();

                ResultSet rsGeneratedKeys = stmInsertCart.getGeneratedKeys();
                if (rsGeneratedKeys.next()) {
                    cartId = rsGeneratedKeys.getInt(1); // Lấy cart_id mới sinh
                }
                rsGeneratedKeys.close();
            }

            // Nếu đã tìm thấy hoặc tạo mới được cartId thành công
            if (cartId != -1) {

                // BƯỚC 2: Kiểm tra xem sản phẩm (productId) này đã có trong giỏ hàng (tbCartItem) chưa
                String sqlCheckItem = "SELECT quantity FROM tbCartItem WHERE cart_id = ? AND product_id = ?";
                stmCheckItem = connection.prepareStatement(sqlCheckItem);
                stmCheckItem.setInt(1, cartId);
                stmCheckItem.setInt(2, productId);
                ResultSet rsItem = stmCheckItem.executeQuery();

                int existingQty = rsItem.next() ? rsItem.getInt("quantity") : 0;
                int newTotalQty = existingQty + quantity;

                if (newTotalQty > availableStock) {
                    // Chặn ở đây thay vì cho thêm vô hạn — số lượng cộng dồn không
                    // được vượt quá số thực sự có thể mua ngay.
                    errorMessage = "Chỉ còn " + availableStock + " sản phẩm có thể mua"
                            + (existingQty > 0 ? " (giỏ hàng đã có " + existingQty + ")." : ".");
                    rsItem.close();
                    connection.rollback();
                    return errorMessage;
                }

                if (existingQty > 0) {
                    // BƯỚC 3a: Nếu sản phẩm ĐÃ CÓ, tiến hành CỘNG DỒN số lượng mới vào số lượng cũ
                    String sqlUpdateItem = "UPDATE tbCartItem SET quantity = quantity + ? WHERE cart_id = ? AND product_id = ?";
                    stmUpdateItem = connection.prepareStatement(sqlUpdateItem);
                    stmUpdateItem.setInt(1, quantity);
                    stmUpdateItem.setInt(2, cartId);
                    stmUpdateItem.setInt(3, productId);
                    stmUpdateItem.executeUpdate();
                } else {
                    // BƯỚC 3b: Nếu sản phẩm CHƯA CÓ, tiến hành INSERT dòng mới (mặc định is_selected = 1)
                    String sqlInsertItem = "INSERT INTO tbCartItem (cart_id, product_id, quantity, is_selected) VALUES (?, ?, ?, 1)";
                    stmInsertItem = connection.prepareStatement(sqlInsertItem);
                    stmInsertItem.setInt(1, cartId);
                    stmInsertItem.setInt(2, productId);
                    stmInsertItem.setInt(3, quantity);
                    stmInsertItem.executeUpdate();
                }
                rsItem.close();
            }

            // Nếu chạy đến đây suôn sẻ, xác nhận thực thi toàn bộ các lệnh trên vào Database
            connection.commit();
            System.out.println("Thêm vào giỏ hàng thành công!");

        } catch (SQLException ex) {
            // Nếu có bất kỳ dòng lệnh nào lỗi, hoàn tác (hủy bỏ) toàn bộ quá trình để tránh rác DB
            try {
                if (connection != null) {
                    connection.rollback();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            ex.printStackTrace();
            errorMessage = "Có lỗi xảy ra, vui lòng thử lại.";
        } finally {
            // Đóng toàn bộ cổng kết nối Statement, ResultSet và bật lại trạng thái AutoCommit
            try {
                if (rs != null) rs.close();
                if (stmGetCart != null) stmGetCart.close();
                if (stmInsertCart != null) stmInsertCart.close();
                if (stmCheckItem != null) stmCheckItem.close();
                if (stmUpdateItem != null) stmUpdateItem.close();
                if (stmInsertItem != null) stmInsertItem.close();
                if (connection != null) connection.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return errorMessage;
    }
 public List<CartItemDTO> getAllCartItem(int accountId) {
    List<CartItemDTO> list = new ArrayList<>();
    
    // SQL JOIN: tbCart, tbCartItem, tbProduct, tbProductImage — đọc tồn kho
    // thẳng từ p.stock_quantity, không còn qua tbSKU/lô ACTIVE nữa.
    String sql = "SELECT ci.cart_item_id, ci.cart_id, ci.product_id, ci.quantity, ci.is_selected, "
               + "p.name AS product_name, "
               + "COALESCE(p.author, p.specifications, p.brand, '') AS product_meta, "
               + "pi.image_url, p.price, p.weight_grams, p.stock_quantity AS active_stock "
               + "FROM tbCart c "
               + "INNER JOIN tbCartItem ci ON c.cart_id = ci.cart_id "
               + "INNER JOIN tbProduct p ON ci.product_id = p.product_id "
               // LEFT JOIN để lấy ảnh thumbnail, nếu sản phẩm chưa có ảnh thì vẫn lấy được dữ liệu ra (tránh bị mất dòng)
               + "LEFT JOIN tbProductImage pi ON p.product_id = pi.product_id AND pi.is_thumbnail = 1 "
               + "WHERE c.account_id = ?";
               
    PreparedStatement stm = null;
    ResultSet rs = null;

    try {
        stm = connection.prepareStatement(sql);
        stm.setInt(1, accountId);
        rs = stm.executeQuery();

        while (rs.next()) {
            int cartItemId = rs.getInt("cart_item_id");
            int cartId = rs.getInt("cart_id");
            int productId = rs.getInt("product_id");
            int quantity = rs.getInt("quantity");
            boolean isSelected = rs.getBoolean("is_selected");
            
            String productName = rs.getString("product_name"); // Đã dùng AS product_name trong SQL
            String productMeta = rs.getString("product_meta"); // Đã dùng AS product_meta trong SQL
            String productImage = rs.getString("image_url");   // Lấy từ tbProductImage
            
            // Ép kiểu DECIMAL(10,2) trong DB thành long (bỏ qua số thập phân nếu là VND)
            long price = rs.getLong("price"); 
            int activeStock = rs.getInt("active_stock");
            
            // Xử lý logic trạng thái kho — TRƯỚC ĐÂY chỉ check "stockQuantity > 0"
            // (có hàng hay không, KHÔNG so với số lượng đang có trong giỏ). Giờ
            // so đúng số lượng cần với đúng tồn kho lô ACTIVE.
            boolean inStock = activeStock >= quantity;

            // Xử lý URL ảnh bị null (nếu DB chưa có ảnh thì để 1 chuỗi rỗng hoặc link ảnh mặc định)
            if (productImage == null) {
                productImage = ""; // Hoặc đường dẫn tới ảnh placeholder "images/default.jpg"
            }

            CartItemDTO item = new CartItemDTO(
                    cartItemId, cartId, productId, quantity, isSelected,
                    productName, productMeta, productImage, price, inStock
            );
            item.setActiveStock(activeStock);
            item.setWeightGrams(rs.getInt("weight_grams"));

            list.add(item);
        }
    } catch (SQLException ex) {
        ex.printStackTrace();
    } finally {
        try {
            if (rs != null) rs.close();
            if (stm != null) stm.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    return list;
}
 public boolean deleteCartItem(int cartItemId) {
    String sql = "DELETE FROM tbCartItem WHERE cart_item_id = ?";
    PreparedStatement stm = null;
    try {
        stm = connection.prepareStatement(sql);
        stm.setInt(1, cartItemId);
        int rowsAffected = stm.executeUpdate();
        return rowsAffected > 0;
    } catch (SQLException ex) {
        ex.printStackTrace();
        return false;
    } finally {
        try {
            if (stm != null) stm.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
 public boolean deleteCartItem(int cartItemId, int accountId) {
    String sql = "DELETE FROM tbCartItem "
               + "WHERE cart_item_id = ? "
               + "AND cart_id IN (SELECT cart_id FROM tbCart WHERE account_id = ?)";
    PreparedStatement stm = null;
    try {
        stm = connection.prepareStatement(sql);
        stm.setInt(1, cartItemId);
        stm.setInt(2, accountId);
        int rowsAffected = stm.executeUpdate();
        return rowsAffected > 0;
    } catch (SQLException ex) {
        ex.printStackTrace();
        return false;
    } finally {
        try {
            if (stm != null) stm.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
 public CartItemDTO getCartItemById(int cartItemId, int accountId) {
    CartItemDTO item = null;

    // Cùng lý do với getAllCartItem(): đọc tồn kho thẳng từ tbProduct.
    String sql = "SELECT ci.cart_item_id, ci.cart_id, ci.product_id, ci.quantity, ci.is_selected, "
               + "p.name AS product_name, "
               + "COALESCE(p.author, p.specifications, p.brand, '') AS product_meta, "
               + "pi.image_url, p.price, p.weight_grams, p.stock_quantity AS active_stock "
               + "FROM tbCart c "
               + "INNER JOIN tbCartItem ci ON c.cart_id = ci.cart_id "
               + "INNER JOIN tbProduct p ON ci.product_id = p.product_id "
               + "LEFT JOIN tbProductImage pi ON p.product_id = pi.product_id AND pi.is_thumbnail = 1 "
               + "WHERE ci.cart_item_id = ? AND c.account_id = ?";

    PreparedStatement stm = null;
    ResultSet rs = null;

    try {
        stm = connection.prepareStatement(sql);
        stm.setInt(1, cartItemId);
        stm.setInt(2, accountId);
        rs = stm.executeQuery();

        if (rs.next()) {
            int cartId = rs.getInt("cart_id");
            int productId = rs.getInt("product_id");
            int quantity = rs.getInt("quantity");
            boolean isSelected = rs.getBoolean("is_selected");

            String productName = rs.getString("product_name");
            String productMeta = rs.getString("product_meta");
            String productImage = rs.getString("image_url");

            long price = rs.getLong("price");
            int activeStock = rs.getInt("active_stock");
            boolean inStock = activeStock >= quantity;

            if (productImage == null) {
                productImage = "";
            }

            item = new CartItemDTO(
                    cartItemId, cartId, productId, quantity, isSelected,
                    productName, productMeta, productImage, price, inStock
            );
            item.setActiveStock(activeStock);
            item.setWeightGrams(rs.getInt("weight_grams"));
        }
    } catch (SQLException ex) {
        ex.printStackTrace();
    } finally {
        try {
            if (rs != null) rs.close();
            if (stm != null) stm.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    return item;
}
  
}
