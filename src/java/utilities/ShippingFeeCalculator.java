package utilities;

/**
 * Tính phí vận chuyển theo TỔNG CÂN NẶNG của đơn hàng (gram) — đơn giản, dựa
 * trên bậc thang cố định, không tích hợp API hãng vận chuyển thật (ngoài
 * phạm vi đề bài).
 *
 * Công thức (khớp 1-1 với JS ở Checkout.jsp — nếu đổi số ở đây, PHẢI đổi
 * đúng y hệt bên JS, vì JS chỉ dùng để hiển thị preview, còn giá trị THẬT
 * tính lại ở đây lúc tạo đơn, không tin số client gửi lên):
 *
 *   - Tổng cân nặng ≤ 2.000g (2kg): phí cố định 30.000đ
 *   - Vượt 2.000g: cộng thêm 15.000đ cho mỗi 1.000g vượt (làm tròn lên)
 *
 * Ví dụ: đơn nặng 3.200g -> vượt 1.200g -> làm tròn lên 2 bậc (2.000g)
 *        -> phí = 30.000 + 2×15.000 = 60.000đ
 */
public class ShippingFeeCalculator {

    private static final int BASE_THRESHOLD_G = 2000;
    private static final long BASE_FEE = 30000;
    private static final int STEP_G = 1000;
    private static final long FEE_PER_STEP = 15000;

    public static long calculate(int totalWeightGrams) {
        if (totalWeightGrams <= BASE_THRESHOLD_G) {
            return BASE_FEE;
        }
        int extraWeight = totalWeightGrams - BASE_THRESHOLD_G;
        int steps = (int) Math.ceil(extraWeight / (double) STEP_G);
        return BASE_FEE + steps * FEE_PER_STEP;
    }
}
