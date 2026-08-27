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

@WebServlet(name = "CategoryController", urlPatterns = {"/category"})
public class CategoryController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. Lấy category_id (cid) từ URL
        String cidParam = request.getParameter("cid");
        int cid = 0;
        try {
            if (cidParam != null) {
                cid = Integer.parseInt(cidParam);
            }
        } catch (NumberFormatException e) {
            cid = 0;
        }

        // 2. Lấy số trang hiện tại
        String pageParam = request.getParameter("page");
        int currentPage = 1;
        try {
            if (pageParam != null) {
                currentPage = Integer.parseInt(pageParam);
            }
        } catch (NumberFormatException e) {
            currentPage = 1;
        }

        // MỚI: lọc theo khoảng giá (null = không lọc)
        Double minPrice = parseDoubleSafe(request.getParameter("minPrice"));
        Double maxPrice = parseDoubleSafe(request.getParameter("maxPrice"));

        int pageSize = 10;
        ProductDAO productDAO = new ProductDAO();

        // 3. Tính toán phân trang
        int totalProducts = productDAO.getTotalProductsByCategory(cid, minPrice, maxPrice);

        int endPage = totalProducts / pageSize;
        if (totalProducts % pageSize != 0) {
            endPage++;
        }

        // 4. Lấy dữ liệu
        List<ProductDTO> listProducts = productDAO.getProductsByCategoryPaging(cid, minPrice, maxPrice, currentPage, pageSize);

        // Lấy tên danh mục làm tiêu đề trang (lấy từ sản phẩm đầu tiên nếu có)
        String pageTitle = "Danh Mục Sản Phẩm";
        if (!listProducts.isEmpty()) {
            pageTitle = listProducts.get(0).getCategoryName();
        }

        // 5. Truyền dữ liệu lên JSP
        request.setAttribute("listProducts", listProducts);
        request.setAttribute("endPage", endPage);
        request.setAttribute("totalProducts", totalProducts);
        request.setAttribute("currentPage", currentPage);
        request.setAttribute("pageTitle", pageTitle);
        request.setAttribute("currentCid", cid);
// SỬA: getAllCategories() trả về CẢ 6 danh mục (Sách lẫn VPP trộn lẫn) —
// giờ xác định đúng type của category đang xem (cid) rồi lọc sidebar theo
// đúng type đó.
        CategoryDAO categoryDAO = new CategoryDAO();
        String categoryProductType = categoryDAO.getCategoryProductType(cid);
        request.setAttribute("categories", categoryProductType != null
                ? categoryDAO.getCategoriesByType(categoryProductType)
                : categoryDAO.getAllCategories()); // category rỗng, không xác định được type -> fallback hiện hết
        request.setAttribute("currentMinPrice", minPrice);
        request.setAttribute("currentMaxPrice", maxPrice);
        request.setAttribute("currentType", categoryProductType != null ? categoryProductType.toLowerCase() : null);
        StringBuilder base = new StringBuilder("category?cid=").append(cid);
        if (minPrice != null) {
            base.append("&minPrice=").append(minPrice);
        }
        if (maxPrice != null) {
            base.append("&maxPrice=").append(maxPrice);
        }
        request.setAttribute("paginationBaseUrl", base.toString());

        request.getRequestDispatcher("ProductList.jsp").forward(request, response);
    }

    private Double parseDoubleSafe(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return Double.parseDouble(raw.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
