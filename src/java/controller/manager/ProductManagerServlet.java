// Servlet điều phối cho trang manager-products.jsp: đọc danh sách sản phẩm/danh mục
// cho GET, xử lý Thêm/Sửa/Xóa cho POST (action=create|update|delete), theo mô hình
// Post-Redirect-Get để tránh submit trùng khi người dùng F5 trang.
package controller.manager;

import dal.CategoryDAO;
import dal.ProductDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.List;
import java.util.UUID;
import model.CategoryDTO;
import model.ProductDetailDTO;
import utilities.ProductValidator;
import utilities.ExcelExportUtil;
import java.util.ArrayList;

@WebServlet(name = "ProductManagerServlet", urlPatterns = {"/manager/products"})
// MỚI: cho phép nhận multipart/form-data (upload ảnh) — giới hạn 2MB/file,
// 5MB/request, không giới hạn ổ đĩa tạm (-1L = mặc định container quyết định).
@MultipartConfig(
        maxFileSize = 2 * 1024 * 1024,      // 2MB / file
        maxRequestSize = 5 * 1024 * 1024,   // 5MB / request
        fileSizeThreshold = 1024 * 1024
)
public class ProductManagerServlet extends HttpServlet {

    private final ProductDAO productDAO = new ProductDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();

    // Ảnh sản phẩm lưu trong chính webapp, dưới thư mục assets/uploads/products/
    // — đơn giản, không cần cấu hình đường dẫn tuyệt đối ngoài server. Lưu ý:
    // clean-build trong NetBeans có thể xóa mất ảnh cũ (chấp nhận được cho
    // project sinh viên deploy local, không phải môi trường production thật).
    private static final String UPLOAD_SUBDIR = "assets/uploads/products";
    private static final long MAX_IMAGE_BYTES = 2 * 1024 * 1024;
    private static final java.util.Set<String> ALLOWED_EXT = java.util.Set.of("jpg", "jpeg", "png");

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // MỚI: sub-endpoint check SKU trùng, gọi qua fetch() từ JS — trả text thuần, không forward JSP
        String checkSku = request.getParameter("checkSku");
        if (checkSku != null) {
            boolean exists = productDAO.skuExists(checkSku.trim());
            response.setContentType("text/plain;charset=UTF-8");
            response.getWriter().write(exists ? "EXISTS" : "OK");
            return;
        }
        String export = request.getParameter("export");
        if ("excel".equals(export)) {
            exportProductsToCsv(request, response);
            return;
        }

       String keyword = request.getParameter("keyword");
        String typeFilter = request.getParameter("type"); // MỚI: lọc theo Sách (BOOK) / VPP (STATIONERY)

        int pageSize = 10;
        int page = parseInt(request.getParameter("page"));
        if (page <= 0) {
            page = 1;
        }

        int totalProducts = productDAO.countProductsForManager(keyword, typeFilter);
        int totalPages = (int) Math.ceil((double) totalProducts / pageSize);
        List<ProductDetailDTO> products = productDAO.getAllProductsForManager(keyword, typeFilter, page, pageSize);

        request.setAttribute("products", products);
        request.setAttribute("typeFilter", typeFilter);
        request.setAttribute("currentPage", page);
        request.setAttribute("totalPages", totalPages);
        List<CategoryDTO> categories = categoryDAO.getAllCategories();

        request.setAttribute("categories", categories);
        request.setAttribute("products", products);
        request.setAttribute("keyword", keyword);
        request.setAttribute("keyword", keyword);

        // Lấy flash message (thông báo kết quả Thêm/Sửa/Xóa) từ session sau khi redirect
        HttpSession session = request.getSession();
        Object flash = session.getAttribute("flashMessage");
        if (flash != null) {
            request.setAttribute("flashMessage", flash);
            request.setAttribute("flashError", session.getAttribute("flashError"));
            session.removeAttribute("flashMessage");
            session.removeAttribute("flashError");
        }

        request.getRequestDispatcher("/manager/manager-products.jsp").forward(request, response);

    }

   private void exportProductsToCsv(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String keyword = request.getParameter("keyword");
        String typeFilter = request.getParameter("type");
        List<ProductDetailDTO> all = productDAO.getAllProductsForManager(keyword, typeFilter, 1, Integer.MAX_VALUE);

        String[] headers = {"SKU", "Tên sản phẩm", "Loại", "Giá bán (đã VAT)", "Tồn kho", "Cân nặng (g)", "Danh mục", "Trạng thái"};
        List<String[]> rows = new ArrayList<>();
        for (ProductDetailDTO p : all) {
            rows.add(new String[]{
                p.getSku(), p.getName(),
                "BOOK".equals(p.getProductType()) ? "Sách" : "VPP",
                String.valueOf(p.getOriginalPrice()),
                String.valueOf(p.getStockQuantity()),
                String.valueOf(p.getWeightGrams()),
                p.getCategoryName(),
                p.getStatus()
            });
        }
        ExcelExportUtil.exportCsv(response, "danh_sach_san_pham.csv", headers, rows);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");
        HttpSession session = request.getSession();

        try {
            switch (action == null ? "" : action) {
                case "create":
                    handleCreate(request, session);
                    break;
                case "update":
                    handleUpdate(request, session);
                    break;
                case "delete":
                    handleDelete(request, session);
                    break;
                default:
                    session.setAttribute("flashMessage", "Hành động không hợp lệ.");
                    session.setAttribute("flashError", true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("flashMessage", "Có lỗi xảy ra: " + e.getMessage());
            session.setAttribute("flashError", true);
        }

        // Post-Redirect-Get: quay lại doGet để load danh sách mới nhất, tránh resubmit khi F5
        response.sendRedirect(request.getContextPath() + "/manager/products");
    }

    /**
     * Gom toàn bộ tham số request thành ProductInput để đưa vào
     * ProductValidator
     */
    private ProductValidator.ProductInput readInput(HttpServletRequest request, boolean requireSku) {
        ProductValidator.ProductInput input = new ProductValidator.ProductInput();
        input.type = request.getParameter("type");
        input.sku = request.getParameter("sku");
        input.requireSku = requireSku;
        input.name = request.getParameter("name");
        input.priceRaw = request.getParameter("price");
        input.weightRaw = request.getParameter("weight");
        input.categoryIdRaw = request.getParameter("categoryId");
        input.imageUrl = request.getParameter("imageUrl");
        input.isbn = request.getParameter("isbn");
        input.author = request.getParameter("author");
        input.publisher = request.getParameter("publisher");
        input.publicationYearRaw = request.getParameter("publicationYear");
        input.brand = request.getParameter("brand");
        input.specifications = request.getParameter("specifications");
        input.description = request.getParameter("description"); // MỚI
        input.quantityRaw = request.getParameter("quantity");
        input.unitCostRaw = request.getParameter("unitCost");
        return input;
    }

    /**
     * Dựng ProductDetailDTO từ ProductInput đã validate — dùng chung cho cả
     * Create/Update
     */
    private ProductDetailDTO buildDtoFromInput(ProductValidator.ProductInput input) {
        ProductDetailDTO p = new ProductDetailDTO();
        p.setProductType(input.type);
        p.setSku(input.sku);
        p.setName(input.name);
        p.setOriginalPrice(parseDouble(input.priceRaw));
        p.setUnitCost(parseDouble(input.unitCostRaw));
        p.setStockQuantity(parseInt(input.quantityRaw));
        p.setWeightGrams(parseInt(input.weightRaw));
        p.setCategoryId(parseInt(input.categoryIdRaw));
        p.setDescription(input.description); // MỚI: dùng chung cho cả 2 loại

        if ("BOOK".equals(input.type)) {
            p.setIsbn(input.isbn);
            p.setBrandorauthor(input.author);
            p.setPublisher(input.publisher);
            p.setPublicationYear(input.publicationYearRaw == null || input.publicationYearRaw.isEmpty()
                    ? null : Integer.parseInt(input.publicationYearRaw.trim()));
        } else {
            p.setBrandorauthor(input.brand);
            p.setSpecifications(input.specifications);
        }
        return p;
    }

    private void handleCreate(HttpServletRequest request, HttpSession session) throws IOException, ServletException {
        // true: Create BẮT BUỘC phải có SKU do Manager tự gõ (đã check trùng
        // real-time qua AJAX ở JS, nhưng vẫn re-check ở server phòng race condition).
        ProductValidator.ProductInput input = readInput(request, true);

        String error = ProductValidator.validate(input, categoryDAO);
        if (error != null) {
            session.setAttribute("flashMessage", error);
            session.setAttribute("flashError", true);
            return;
        }

        if (productDAO.skuExists(input.sku.trim())) {
            session.setAttribute("flashMessage", "SKU \"" + input.sku + "\" đã tồn tại, vui lòng chọn mã khác.");
            session.setAttribute("flashError", true);
            return;
        }

        ProductDetailDTO p = buildDtoFromInput(input);

        int productId = productDAO.insertProduct(p);
        String message;
        boolean isError = true;

        if (productId > 0) {
            message = "Đã thêm sản phẩm \"" + p.getName() + "\" thành công.";
            isError = false;
            // Ảnh là optional lúc thêm mới — không bắt buộc phải có ngay
            String savedImagePath = saveUploadedImage(request, "image");
            if (savedImagePath != null) {
                productDAO.insertThumbnail(productId, savedImagePath);
            }
        } else {
            message = mapErrorCode(productId, input.isbn);
        }

        session.setAttribute("flashMessage", message);
        session.setAttribute("flashError", isError);
    }

    /**
     * Đọc file ảnh từ field multipart tên `fieldName`, lưu vào
     * webapp/assets/uploads/products/ với tên file random (tránh trùng/đè),
     * trả về đường dẫn tương đối để lưu vào DB (image_url), hoặc null nếu
     * người dùng không chọn file nào / file không hợp lệ.
     */
    private String saveUploadedImage(HttpServletRequest request, String fieldName) throws IOException, ServletException {
        Part filePart;
        try {
            filePart = request.getPart(fieldName);
        } catch (Exception e) {
            return null; // request không phải multipart, hoặc field không tồn tại
        }
        if (filePart == null || filePart.getSize() <= 0) {
            return null; // Manager không chọn ảnh nào
        }
        if (filePart.getSize() > MAX_IMAGE_BYTES) {
            return null; // vượt 2MB — bị @MultipartConfig chặn từ trước, phòng thêm 1 lớp
        }

        String submittedFileName = filePart.getSubmittedFileName();
        String ext = "";
        int dot = submittedFileName == null ? -1 : submittedFileName.lastIndexOf('.');
        if (dot >= 0) {
            ext = submittedFileName.substring(dot + 1).toLowerCase();
        }
        if (!ALLOWED_EXT.contains(ext)) {
            return null; // chỉ nhận .jpg/.jpeg/.png
        }

        String newFileName = UUID.randomUUID().toString() + "." + ext;

        String uploadRealPath = getServletContext().getRealPath("/" + UPLOAD_SUBDIR);
        File uploadDir = new File(uploadRealPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        File targetFile = new File(uploadDir, newFileName);
        try (InputStream in = filePart.getInputStream();
             OutputStream out = Files.newOutputStream(targetFile.toPath())) {
            in.transferTo(out);
        }

        // Đường dẫn tương đối, tính từ context root — dùng thẳng trong <img src="...">
        return UPLOAD_SUBDIR + "/" + newFileName;
    }

    private void handleUpdate(HttpServletRequest request, HttpSession session) throws IOException, ServletException {
        // false: Update KHÔNG bắt buộc SKU vì SKU không được sửa (input SKU trên form là readonly/disabled,
        // productDAO.updateProduct() cũng không UPDATE cột sku dù có bị bypass gửi lên)
        ProductValidator.ProductInput input = readInput(request, false);

        String error = ProductValidator.validate(input, categoryDAO);
        if (error != null) {
            session.setAttribute("flashMessage", error);
            session.setAttribute("flashError", true);
            return;
        }

        ProductDetailDTO p = buildDtoFromInput(input);
        p.setProductId(parseInt(request.getParameter("productId")));

        int result = productDAO.updateProduct(p);
        String message;
        boolean isError = true;

        switch (result) {
            case 1:
                message = "Đã cập nhật sản phẩm \"" + p.getName() + "\" thành công.";
                isError = false;
                // Ảnh mới là optional lúc sửa — không chọn file mới thì giữ nguyên ảnh cũ
                String savedImagePath = saveUploadedImage(request, "image");
                if (savedImagePath != null) {
                    productDAO.updateThumbnail(p.getProductId(), savedImagePath);
                }
                break;
            case 0:
                message = "Không tìm thấy sản phẩm để cập nhật.";
                break;
            default:
                message = mapErrorCode(result, input.isbn);
        }

        session.setAttribute("flashMessage", message);
        session.setAttribute("flashError", isError);
    }

    private void handleDelete(HttpServletRequest request, HttpSession session) {
        int productId = parseInt(request.getParameter("productId"));
        boolean ok = productDAO.updateProductStatus(productId, "DISCONTINUED");

        session.setAttribute("flashMessage", ok
                ? "Sản phẩm đã được chuyển sang Discontinued."
                : "Xóa sản phẩm thất bại.");
        session.setAttribute("flashError", !ok);
    }

    /**
     * Dịch mã lỗi số từ ProductDAO (-2/-3/-4/-1) thành message tiếng Việt
     */
    private String mapErrorCode(int code, String isbn) {
        switch (code) {
            case -2:
                return "ISBN không được để trống với sản phẩm loại Sách.";
            case -3:
                return "ISBN không hợp lệ (sai checksum ISBN-13).";
            case -4:
                return "ISBN \"" + isbn + "\" đã tồn tại cho sản phẩm khác.";
            default:
                return "Thao tác thất bại, vui lòng thử lại.";
        }
    }

    private int parseInt(String s) {
        try {
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            return 0;
        }
    }

    private double parseDouble(String s) {
        try {
            return Double.parseDouble(s.replace(".", "").replace(",", "").trim());
        } catch (Exception e) {
            return 0;
        }
    }
}
