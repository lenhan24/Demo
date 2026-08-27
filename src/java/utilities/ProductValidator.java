package utilities;

import dal.CategoryDAO;
import java.time.Year;

public class ProductValidator {

    private static final int MAX_NAME_LENGTH = 200;
    private static final int MAX_SKU_LENGTH = 30;
    private static final int MAX_AUTHOR_PUBLISHER_LENGTH = 150;
    private static final int MAX_BRAND_LENGTH = 100;
    private static final int MAX_SPECS_IMAGE_LENGTH = 255;
    private static final int MAX_DESCRIPTION_LENGTH = 2000;
    private static final int MIN_PUBLICATION_YEAR = 1900;

    public static String validate(ProductInput input, CategoryDAO categoryDAO) {
        if (!"BOOK".equals(input.type) && !"STATIONERY".equals(input.type)) {
            return "Loại sản phẩm không hợp lệ.";
        }
        boolean isBook = "BOOK".equals(input.type);

        if (input.requireSku) {
            if (isBlank(input.sku)) return "SKU không được để trống.";
            if (input.sku.trim().length() > MAX_SKU_LENGTH)
                return "SKU không được vượt quá " + MAX_SKU_LENGTH + " ký tự.";
            // MỚI: SKU phải đúng định dạng BK/ST + 6 chữ số, khớp với loại sản phẩm đang chọn
            String expectedPrefix = isBook ? "BK" : "ST";
            if (!input.sku.trim().matches("^" + expectedPrefix + "-\\d{4}$")) {
                return "SKU phải đúng định dạng " + expectedPrefix + " + 4 chữ số (VD: " + expectedPrefix + "-1234).";
            }
        }

        if (isBlank(input.name)) return "Tên sản phẩm không được để trống.";
        if (input.name.trim().length() > MAX_NAME_LENGTH)
            return "Tên sản phẩm không được vượt quá " + MAX_NAME_LENGTH + " ký tự.";

        Double price = parseDoubleSafe(input.priceRaw);
        if (price == null) return "Giá bán không hợp lệ, vui lòng chỉ nhập số.";
        if (price <= 0) return "Giá bán phải lớn hơn 0.";

        // MỚI: số lượng tồn kho + giá vốn nhập thẳng ở form Thêm/Sửa (thay cho phiếu nhập kho cũ)
        Integer quantity = parseIntSafe(input.quantityRaw);
        if (quantity == null) return "Số lượng không hợp lệ, vui lòng chỉ nhập số nguyên.";
        if (quantity < 0) return "Số lượng không được âm.";

        Double unitCost = parseDoubleSafe(input.unitCostRaw);
        if (unitCost == null) return "Giá vốn không hợp lệ, vui lòng chỉ nhập số.";
        if (unitCost < 0) return "Giá vốn không được âm.";

        Integer weight = parseIntSafe(input.weightRaw);
        if (weight == null) return "Cân nặng không hợp lệ, vui lòng chỉ nhập số nguyên.";
        if (weight <= 0) return "Cân nặng phải lớn hơn 0.";

        Integer categoryId = parseIntSafe(input.categoryIdRaw);
        if (categoryId == null) return "Danh mục không hợp lệ.";
        if (categoryDAO.getCategoryById(categoryId) == null)
            return "Danh mục không tồn tại, vui lòng chọn lại.";

        if (input.imageUrl != null && input.imageUrl.trim().length() > MAX_SPECS_IMAGE_LENGTH)
            return "URL ảnh không được vượt quá " + MAX_SPECS_IMAGE_LENGTH + " ký tự.";

        if (input.description != null && input.description.trim().length() > MAX_DESCRIPTION_LENGTH)
            return "Mô tả sản phẩm không được vượt quá " + MAX_DESCRIPTION_LENGTH + " ký tự.";

        if (isBook) {
            if (isBlank(input.isbn)) return "ISBN không được để trống với sản phẩm loại Sách.";
            if (!isValidIsbn(input.isbn)) return "ISBN không hợp lệ (sai checksum ISBN-13).";
            if (isBlank(input.author)) return "Tác giả không được để trống với sản phẩm loại Sách.";
            if (input.author.trim().length() > MAX_AUTHOR_PUBLISHER_LENGTH)
                return "Tên tác giả không được vượt quá " + MAX_AUTHOR_PUBLISHER_LENGTH + " ký tự.";
            if (isBlank(input.publisher)) return "Nhà xuất bản không được để trống với sản phẩm loại Sách.";
            if (input.publisher.trim().length() > MAX_AUTHOR_PUBLISHER_LENGTH)
                return "Tên nhà xuất bản không được vượt quá " + MAX_AUTHOR_PUBLISHER_LENGTH + " ký tự.";
            if (isBlank(input.publicationYearRaw)) return "Năm xuất bản không được để trống với sản phẩm loại Sách.";
            Integer year = parseIntSafe(input.publicationYearRaw);
            int currentYear = Year.now().getValue();
            if (year == null || year < MIN_PUBLICATION_YEAR || year > currentYear)
                return "Năm xuất bản không hợp lệ (từ " + MIN_PUBLICATION_YEAR + " đến " + currentYear + ").";
        } else {
            if (isBlank(input.brand)) return "Thương hiệu không được để trống với sản phẩm loại Văn phòng phẩm.";
            if (input.brand.trim().length() > MAX_BRAND_LENGTH)
                return "Thương hiệu không được vượt quá " + MAX_BRAND_LENGTH + " ký tự.";
            if (isBlank(input.specifications)) return "Thông số không được để trống với sản phẩm loại Văn phòng phẩm.";
            if (input.specifications.trim().length() > MAX_SPECS_IMAGE_LENGTH)
                return "Thông số không được vượt quá " + MAX_SPECS_IMAGE_LENGTH + " ký tự.";
        }
        return null;
    }

    private static boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }

    private static Double parseDoubleSafe(String s) {
        if (isBlank(s)) return null;
        try { return Double.parseDouble(s.replace(".", "").replace(",", "").trim()); }
        catch (Exception e) { return null; }
    }

    private static Integer parseIntSafe(String s) {
        if (isBlank(s)) return null;
        try { return Integer.parseInt(s.trim()); }
        catch (Exception e) { return null; }
    }

    public static boolean isValidIsbn(String isbn) {
        if (isbn == null) return false;
        String digits = isbn.replaceAll("[\\- ]", "");
        if (!digits.matches("\\d{13}")) return false;
        int sum = 0;
        for (int i = 0; i < 12; i++) {
            int d = digits.charAt(i) - '0';
            sum += (i % 2 == 0) ? d : d * 3;
        }
        int check = (10 - (sum % 10)) % 10;
        return check == (digits.charAt(12) - '0');
    }

    /** MỚI: validate riêng 3 field SL/giá vốn/giá bán — dùng cho form Nhập kho */
    public static String validateStockFields(String quantityRaw, String unitCostRaw, String sellingPriceRaw) {
        Integer qty = parseIntSafe(quantityRaw);
        if (qty == null || qty <= 0) return "Số lượng phải là số nguyên lớn hơn 0.";

        Double cost = parseDoubleSafe(unitCostRaw);
        if (cost == null || cost <= 0) return "Giá nhập phải là số lớn hơn 0.";

        Double price = parseDoubleSafe(sellingPriceRaw);
        if (price == null || price <= 0) return "Giá bán phải là số lớn hơn 0.";

        if (price <= cost) return "Giá bán phải cao hơn giá nhập (đang lỗ nếu bán đúng giá này).";

        return null;
    }

    public static class ProductInput {
        public String type, sku, name, priceRaw, weightRaw, categoryIdRaw, imageUrl,
                isbn, author, publisher, publicationYearRaw, brand, specifications, description,
                quantityRaw, unitCostRaw;
        public boolean requireSku;
    }
}