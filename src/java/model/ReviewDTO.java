package model;

import java.time.LocalDateTime;

/**
 * DTO cho 1 dòng đánh giá sản phẩm (tbReview), kèm tên người đánh giá lấy
 * join từ tbAccount để hiển thị trực tiếp, không cần query riêng ở JSP.
 */
public class ReviewDTO {
    private int reviewId;
    private int productId;
    private int accountId;
    private int orderDetailId;
    private String reviewerName;
    private int rating;
    private String comment;
    private String status;
    private LocalDateTime createdAt;

    public int getReviewId() { return reviewId; }
    public void setReviewId(int reviewId) { this.reviewId = reviewId; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public int getAccountId() { return accountId; }
    public void setAccountId(int accountId) { this.accountId = accountId; }

    public int getOrderDetailId() { return orderDetailId; }
    public void setOrderDetailId(int orderDetailId) { this.orderDetailId = orderDetailId; }

    public String getReviewerName() { return reviewerName; }
    public void setReviewerName(String reviewerName) { this.reviewerName = reviewerName; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    /** Chữ cái đầu tên, dùng làm avatar chữ (giống UI cũ: "M", "T"...) */
    public String getAvatarLetter() {
        if (reviewerName == null || reviewerName.isEmpty()) return "?";
        return reviewerName.substring(0, 1).toUpperCase();
    }

    /** Ngày viết review, định dạng sẵn dd/MM/yyyy để JSP dùng thẳng, không cần thêm taglib. */
    public String getFormattedDate() {
        if (createdAt == null) return "";
        return String.format("%02d/%02d/%04d", createdAt.getDayOfMonth(), createdAt.getMonthValue(), createdAt.getYear());
    }

    /** Chuỗi "★★★★☆" theo đúng số sao, dùng render trực tiếp trong JSP. */
    public String getStarsDisplay() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 5; i++) sb.append(i < rating ? "★" : "☆");
        return sb.toString();
    }
}
