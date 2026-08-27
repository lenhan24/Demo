package utilities;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Băm và kiểm tra mật khẩu bằng PBKDF2WithHmacSHA1 — có sẵn trong JDK chuẩn
 * (javax.crypto), KHÔNG cần thư viện ngoài (đề bài không cho phép
 * Maven/Gradle nên không dùng được BCrypt/Argon2 qua Maven Central).
 *
 * TRƯỚC ĐÂY: AccountDAO lưu và so sánh mật khẩu dạng PLAIN TEXT trực tiếp
 * (dù cột tên là "password_hash"). Toàn bộ chỗ gọi registerAccount/
 * changePassword/getAccountByEmail(email, password) cần đổi sang dùng
 * class này thay vì truyền/so sánh chuỗi mật khẩu thô.
 *
 * Định dạng chuỗi lưu trong DB: "iterations:salt(base64):hash(base64)"
 * — tự chứa salt nên không cần thêm cột riêng, vẫn vừa trong
 * password_hash NVARCHAR(255).
 */
public class PasswordUtil {

    private static final int ITERATIONS = 65536;
    private static final int KEY_LENGTH = 256; // bits
    private static final int SALT_LENGTH = 16; // bytes

    /** Băm mật khẩu thô -> chuỗi lưu được vào cột password_hash. */
    public static String hashPassword(String plainPassword) {
        try {
            byte[] salt = new byte[SALT_LENGTH];
            new SecureRandom().nextBytes(salt);

            byte[] hash = pbkdf2(plainPassword.toCharArray(), salt, ITERATIONS, KEY_LENGTH);

            return ITERATIONS + ":" + Base64.getEncoder().encodeToString(salt)
                    + ":" + Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Không thể băm mật khẩu.", e);
        }
    }

    /**
     * So sánh mật khẩu người dùng nhập với chuỗi đã lưu trong DB.
     * @param plainPassword mật khẩu người dùng vừa nhập (form login/đổi mật khẩu)
     * @param storedHash    giá trị lấy từ cột password_hash trong DB
     * @return true nếu khớp
     */
    public static boolean verifyPassword(String plainPassword, String storedHash) {
        if (storedHash == null || plainPassword == null) return false;
        try {
            String[] parts = storedHash.split(":");
            if (parts.length != 3) {
                // Dữ liệu cũ (nếu còn sót lại từ trước khi vá, lưu plain text) —
                // không khớp định dạng mới -> coi như không hợp lệ, bắt buộc
                // phải đổi lại mật khẩu để được băm đúng chuẩn.
                return false;
            }
            int iterations = Integer.parseInt(parts[0]);
            byte[] salt = Base64.getDecoder().decode(parts[1]);
            byte[] expectedHash = Base64.getDecoder().decode(parts[2]);

            byte[] actualHash = pbkdf2(plainPassword.toCharArray(), salt, iterations, expectedHash.length * 8);

            // So sánh theo thời gian cố định để tránh timing attack
            if (actualHash.length != expectedHash.length) return false;
            int diff = 0;
            for (int i = 0; i < actualHash.length; i++) {
                diff |= actualHash[i] ^ expectedHash[i];
            }
            return diff == 0;
        } catch (Exception e) {
            return false;
        }
    }

    private static byte[] pbkdf2(char[] password, byte[] salt, int iterations, int keyLengthBits)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, keyLengthBits);
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        return skf.generateSecret(spec).getEncoded();
    }
}
