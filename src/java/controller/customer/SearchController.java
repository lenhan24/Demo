package controller.customer;

import dal.CategoryDAO;
import dal.ProductDAO;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.ProductDTO;

@WebServlet(name="SearchController", urlPatterns={"/search"})
public class SearchController extends HttpServlet {
   
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        
        // Nhận từ khóa tìm kiếm
        String keyword = request.getParameter("keyword");
        if (keyword == null) keyword = "";

        // MỚI: lọc thêm theo danh mục — kết hợp được với từ khóa (categoryId=null nghĩa là không lọc)
        Integer categoryId = null;
        String categoryIdParam = request.getParameter("categoryId");
        if (categoryIdParam != null && !categoryIdParam.isBlank()) {
            try {
                categoryId = Integer.parseInt(categoryIdParam);
            } catch (NumberFormatException e) {
                categoryId = null;
            }
        }

        // MỚI: lọc theo khoảng giá (null = không lọc)
        Double minPrice = parseDoubleSafe(request.getParameter("minPrice"));
        Double maxPrice = parseDoubleSafe(request.getParameter("maxPrice"));

        // Nhận trang hiện tại
        String pageParam = request.getParameter("page");
        int currentPage = 1;
        try {
            if (pageParam != null) currentPage = Integer.parseInt(pageParam);
        } catch (NumberFormatException e) {
            currentPage = 1; 
        }

        int pageSize = 12; 
        ProductDAO productDAO = new ProductDAO();

        // Tính toán phân trang
        int totalProducts = productDAO.getTotalProductsBySearch(keyword, categoryId, minPrice, maxPrice);
        int endPage = totalProducts / pageSize;
        if (totalProducts % pageSize != 0) {
            endPage++;
        }

        // Lấy dữ liệu
        List<ProductDTO> listProducts = productDAO.searchProductsPaging(keyword, categoryId, minPrice, maxPrice, currentPage, pageSize);

        // Đẩy lên JSP
        request.setAttribute("listProducts", listProducts);
        request.setAttribute("endPage", endPage);
        request.setAttribute("totalProducts", totalProducts);
        request.setAttribute("currentPage", currentPage);
        request.setAttribute("currentKeyword", keyword); // Biến quan trọng để phân trang search
        request.setAttribute("isSearchPage", true); // MỚI: cờ rõ ràng để JSP phân biệt "đang ở search" thay vì suy luận qua empty keyword (sai khi keyword rỗng)
        request.setAttribute("currentCategoryId", categoryId); // MỚI: giữ filter danh mục khi chuyển trang
        request.setAttribute("categories", new CategoryDAO().getAllCategories()); // MỚI: cho sidebar filter
        request.setAttribute("currentMinPrice", minPrice);
        request.setAttribute("currentMaxPrice", maxPrice);
        request.setAttribute("pageTitle", "Kết quả tìm kiếm: " + keyword);

        // MỚI: encode keyword đúng chuẩn URL trước khi ghép vào link phân trang —
        // TRƯỚC ĐÂY ghép thẳng keyword thô vào href, từ khóa có dấu cách/ký tự
        // đặc biệt (VD "sách kỹ năng", "C++") sẽ làm link phân trang bị vỡ,
        // chuyển trang mất luôn từ khóa hoặc lỗi 400.
        String encodedKeyword;
        try {
            encodedKeyword = URLEncoder.encode(keyword, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            encodedKeyword = keyword;
        }
        StringBuilder base = new StringBuilder("search?keyword=").append(encodedKeyword);
        if (categoryId != null) base.append("&categoryId=").append(categoryId);
        if (minPrice != null) base.append("&minPrice=").append(minPrice);
        if (maxPrice != null) base.append("&maxPrice=").append(maxPrice);
        request.setAttribute("paginationBaseUrl", base.toString());
        request.setAttribute("currentKeywordEncoded", encodedKeyword);
        
        // Vẫn xài lại trang ProductList.jsp để hiển thị
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
