package utilities;

import dal.DAOVoucher;
import java.sql.Timestamp;

public class PromotionValidator {

    private static final int MAX_CODE_LENGTH = 30;

    public static String validateVoucher(VoucherInput input, DAOVoucher voucherDAO) {
        if (isBlank(input.code)) return "Mã voucher không được để trống.";
        if (input.code.trim().length() > MAX_CODE_LENGTH)
            return "Mã voucher không được vượt quá " + MAX_CODE_LENGTH + " ký tự.";
        if (!input.code.trim().matches("^[A-Z0-9_-]+$"))
            return "Mã voucher chỉ được chứa chữ in hoa, số, dấu gạch ngang/gạch dưới.";

        if (input.requireUniqueCheck) {
            boolean duplicate = input.voucherId == null
                    ? voucherDAO.voucherCodeExists(input.code.trim())
                    : voucherDAO.voucherCodeExistsExcept(input.code.trim(), input.voucherId);
            if (duplicate) return "Mã voucher \"" + input.code + "\" đã tồn tại.";
        }

        if (!"PERCENTAGE".equals(input.discountType) && !"FIXED_AMOUNT".equals(input.discountType))
            return "Loại giảm giá không hợp lệ.";
        boolean isPercentage = "PERCENTAGE".equals(input.discountType);

        Long discountValue = parseLongSafe(input.discountValueRaw);
        if (discountValue == null) return "Giá trị giảm không hợp lệ, vui lòng chỉ nhập số.";
        if (discountValue <= 0) return "Giá trị giảm phải lớn hơn 0.";
        if (isPercentage && discountValue > 100) return "Giảm theo % không được vượt quá 100.";

        if (isPercentage) {
            if (isBlank(input.maxDiscountCapRaw)) return "Voucher giảm theo % phải nhập mức giảm tối đa.";
            Long cap = parseLongSafe(input.maxDiscountCapRaw);
            if (cap == null || cap <= 0) return "Mức giảm tối đa không hợp lệ.";
        }

        Long minOrderValue = parseLongSafe(input.minOrderValueRaw);
        if (minOrderValue == null || minOrderValue < 0) return "Giá trị đơn hàng tối thiểu không hợp lệ.";

        Timestamp start = parseDateSafe(input.startDateRaw);
        Timestamp end = parseDateSafe(input.endDateRaw);
        if (start == null || end == null) return "Ngày bắt đầu/kết thúc không hợp lệ.";
        if (!start.before(end)) return "Ngày bắt đầu phải trước ngày kết thúc.";

        Integer maxUsage = parseIntSafe(input.maxUsageLimitRaw);
        if (maxUsage == null || maxUsage <= 0) return "Giới hạn lượt dùng phải là số nguyên lớn hơn 0.";

        Integer perCustomer = parseIntSafe(input.perCustomerLimitRaw);
        if (perCustomer == null || perCustomer <= 0) return "Giới hạn dùng/khách phải là số nguyên lớn hơn 0.";
        if (perCustomer > maxUsage) return "Giới hạn dùng/khách không được lớn hơn tổng lượt dùng.";

        return null;
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static Long parseLongSafe(String s) {
        if (isBlank(s)) return null;
        try {
            return Long.parseLong(s.replace(".", "").replace(",", "").trim());
        } catch (Exception e) {
            return null;
        }
    }

    private static Integer parseIntSafe(String s) {
        if (isBlank(s)) return null;
        try {
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            return null;
        }
    }

    /** input type="datetime-local" trả về dạng "2026-07-05T10:30" */
    private static Timestamp parseDateSafe(String s) {
        if (isBlank(s)) return null;
        try {
            String normalized = s.trim().replace("T", " ");
            if (normalized.length() == 16) normalized += ":00";
            return Timestamp.valueOf(normalized);
        } catch (Exception e) {
            return null;
        }
    }

    public static class VoucherInput {
        public Integer voucherId;
        public boolean requireUniqueCheck = true;
        public String code;
        public String discountType;
        public String discountValueRaw;
        public String maxDiscountCapRaw;
        public String minOrderValueRaw;
        public String startDateRaw;
        public String endDateRaw;
        public String maxUsageLimitRaw;
        public String perCustomerLimitRaw;
    }
}