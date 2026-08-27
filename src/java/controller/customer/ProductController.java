package controller.customer;

import dal.CategoryDAO;
import dal.ProductDAO;
import java.io.IOException;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.ProductDTO;

@WebServlet(name="ProductController", urlPatterns={"/products"})
public class ProductController extends HttpServlet {
   
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        
        String type = request.getParameter("type");
        // Đảm bảo type không rỗng, mặc định là book nếu user truy cập sai link
        if (type == null || type.isEmpty()) {
            type = "book";
        }

        String dbType = "stationery".equalsIgnoreCase(type) ? "STATIONERY" : "BOOK";

        // Lấy tham số trang hiện tại từ URL (VD: ?type=book&page=2)
        String pageParam = request.getParameter("page");
        int currentPage = 1; // Mặc định là trang 1
        if (pageParam != null && !pageParam.isEmpty()) {
            try {
                currentPage = Integer.parseInt(pageParam);
            } catch (NumberFormatException e) {
                currentPage = 1; // Nếu người dùng nhập linh tinh (?page=abc), đưa về trang 1
            }
        }

        // MỚI: lọc theo khoảng giá (null = không lọc)
        Double minPrice = parseDoubleSafe(request.getParameter("minPrice"));
        Double maxPrice = parseDoubleSafe(request.getParameter("maxPrice"));

        int pageSize = 12; // Quy định 12 sản phẩm 1 trang
        ProductDAO productDAO = new ProductDAO();

        // 1. Đếm tổng số lượng sản phẩm để tính số trang
        int totalProducts = productDAO.getTotalProductsByType(dbType, minPrice, maxPrice);
        
        // Thuật toán tính tổng số trang: (Tổng SP / 10) làm tròn lên
        int endPage = totalProducts / pageSize;
        if (totalProducts % pageSize != 0) {
            endPage++;
        }

        // 2. Lấy dữ liệu sản phẩm của đúng trang đó
        List<ProductDTO> listProducts = productDAO.getProductsByTypePaging(dbType, minPrice, maxPrice, currentPage, pageSize);

        // 3. Đẩy mọi thứ lên JSP để giao diện render
        request.setAttribute("listProducts", listProducts);
        request.setAttribute("endPage", endPage);           // Gửi biến endPage cho vòng lặp in nút
        request.setAttribute("totalProducts", totalProducts);
        request.setAttribute("currentPage", currentPage);   // Gửi biến currentPage để làm đậm nút đang chọn
        request.setAttribute("currentType", type);          // Gửi type để giữ nguyên loại sản phẩm khi bấm chuyển trang
        // SỬA: TRƯỚC ĐÂY dùng getAllCategories() -> sidebar hiện lẫn cả danh mục
        // Sách khi đang xem Văn phòng phẩm (và ngược lại), vì tbCategory dùng
        // chung 1 bảng cho cả 2 loại, không tự tách theo type. Giờ lọc đúng
        // theo dbType đang browse.
        request.setAttribute("categories", new CategoryDAO().getCategoriesByType(dbType));
        request.setAttribute("currentMinPrice", minPrice);
        request.setAttribute("currentMaxPrice", maxPrice);

        // MỚI: tính sẵn phần URL gốc cho link phân trang ở JSP — TRƯỚC ĐÂY logic
        // này nằm trong 1 biểu thức EL lồng nhau rất khó đọc/dễ sai ngay trong
        // JSP, giờ tính 1 lần ở server, JSP chỉ cần nối thêm "&page=N".
        StringBuilder base = new StringBuilder("products?type=").append(type);
        if (minPrice != null) base.append("&minPrice=").append(minPrice);
        if (maxPrice != null) base.append("&maxPrice=").append(maxPrice);
        request.setAttribute("paginationBaseUrl", base.toString());

        // Setup tiêu đề trang
        String pageTitle = "book".equals(type) ? "Toàn Bộ Sách" : "Văn Phòng Phẩm";
        request.setAttribute("pageTitle", pageTitle);

        request.getRequestDispatcher("ProductList.jsp").forward(request, response);
    }

    private Double parseDoubleSafe(String raw) {
        if (raw == null || raw.isBlank()) return null;
        try {
            return Double.parseDouble(raw.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
