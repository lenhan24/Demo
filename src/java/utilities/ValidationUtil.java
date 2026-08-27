package utilities;

import java.util.regex.Pattern;

/**
 * Kiểm tra ĐỊNH DẠNG (không phải kiểm tra tồn tại/trùng lặp — việc đó DAO tự
 * lo, ví dụ checkEmailExist()). Dùng chung cho SignUpController,
 * ChangeInfoController, AddAddressController — trước đây cả 3 chỗ này chỉ
 * check không được để trống, không hề check email có đúng dạng
 * "ten@domain.gi" hay số điện thoại có đúng 10 số hay không (mục 3.5 của đề
 * bài yêu cầu tường minh "Format validation: email, phone number...").
 */
public class ValidationUtil {

    // Email dạng chuẩn: ký tự@ký tự.ký tự — không cầu kỳ theo RFC đầy đủ
    // (RFC 5322 rất phức tạp), đủ dùng để chặn lỗi gõ nhầm phổ biến.
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    // SĐT Việt Nam: bắt đầu bằng 0, tổng 10 số (VD: 0912345678)
    // Chấp nhận thêm dạng +84 theo sau 9 số (VD: +84912345678)
    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^(0\\d{9}|\\+84\\d{9})$");

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    public static boolean isValidPhone(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone.trim()).matches();
    }
}
