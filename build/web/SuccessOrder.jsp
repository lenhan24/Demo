<%-- 
    Document   : SuccessOrder
    Created on : Jul 2, 2026, 10:00:07 AM
    Author     : GiGaByte
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="vi">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Đặt hàng thành công — Booky Mart</title>
<link href="https://cdnjs.cloudflare.com/ajax/libs/twitter-bootstrap/5.3.3/css/bootstrap.min.css" rel="stylesheet">
<link href="https://fonts.googleapis.com/css2?family=Fraunces:opsz,wght@9..144,400;9..144,600;9..144,700&family=Inter:wght@400;500;600;700&family=JetBrains+Mono:wght@500&display=swap" rel="stylesheet">
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css">
</head>
<body>


<jsp:include page="assets/header.jsp" />
<div class="container pb-5 pt-5">
  <div class="row justify-content-center">
    <div class="col-lg-7">

      <!-- Success hero -->
      <div class="success-hero">
        <div class="success-checkmark">
          <svg class="icon" style="width:40px;height:40px;color:#fff;"><use href="#ic-check"/></svg>
        </div>
        <h1>Đặt hàng thành công!</h1>
        <p>Cảm ơn bạn đã mua sắm tại Booky Mart. Đơn hàng của bạn đang được chuẩn bị.</p>
        <div class="success-order-code">#${order.orderCode}</div>

        <div class="success-steps-row">
          <div class="step-item done">
            <div class="step-circle"><svg class="icon" style="width:14px;"><use href="#ic-check"/></svg></div>
            <span class="step-label" style="color:#B9C0CE;">Đặt hàng</span>
          </div>
          <div class="step-divider" style="background:rgba(255,255,255,.15);"></div>

          <c:set var="isProcessingDone" value="${order.status == 'PROCESSING' || order.status == 'SHIPPED' || order.status == 'DELIVERED'}" />
          <div class="step-item ${isProcessingDone ? 'done' : ''}">
            <div class="step-circle" style="${isProcessingDone ? '' : 'background:rgba(255,255,255,.12);color:#B9C0CE;border-color:rgba(255,255,255,.2);'}">
              <c:choose>
                <c:when test="${isProcessingDone}"><svg class="icon" style="width:14px;"><use href="#ic-check"/></svg></c:when>
                <c:otherwise>2</c:otherwise>
              </c:choose>
            </div>
            <span class="step-label" style="color:#B9C0CE;">Xử lý</span>
          </div>
          <div class="step-divider" style="background:rgba(255,255,255,.15);"></div>

          <c:set var="isShippedDone" value="${order.status == 'SHIPPED' || order.status == 'DELIVERED'}" />
          <div class="step-item ${isShippedDone ? 'done' : ''}">
            <div class="step-circle" style="${isShippedDone ? '' : 'background:rgba(255,255,255,.12);color:#B9C0CE;border-color:rgba(255,255,255,.2);'}">
              <c:choose>
                <c:when test="${isShippedDone}"><svg class="icon" style="width:14px;"><use href="#ic-check"/></svg></c:when>
                <c:otherwise>3</c:otherwise>
              </c:choose>
            </div>
            <span class="step-label" style="color:#B9C0CE;">Giao hàng</span>
          </div>
        </div>
      </div>

      <%-- MỚI (tích hợp VietQR thật — Hướng A): hiện mã VietQR THẬT ngay khi
           đã có order.orderCode thật (khác hẳn Checkout.jsp trước đây phải
           tự bịa mã tạm vì lúc đó đơn chưa được tạo). Chỉ hiện khi đơn Online
           QR CHƯA được Staff xác nhận thanh toán (còn PENDING_PAYMENT/UNPAID).
           API ảnh miễn phí img.vietqr.io — chuẩn EMVCo/Napas, quét được bằng
           MỌI app ngân hàng Việt Nam, tự động điền đúng số tiền + nội dung. --%>
      <c:if test="${order.paymentMethod == 'ONLINE_QR' && order.paymentStatus == 'UNPAID'}">
        <div class="panel mt-4" style="text-align:center;">
          <div class="panel-title" style="justify-content:center;"><span><svg class="icon"><use href="#ic-card"/></svg> Quét mã VietQR để chuyển khoản</span></div>
          <%-- ⚠️ CẤU HÌNH: thay 3 giá trị mẫu dưới bằng thông tin ngân hàng
               thật của shop. Tra mã BIN tại https://api.vietqr.io/v2/banks --%>
          <c:set var="shopBankBin" value="970436" /><%-- mẫu: Vietcombank --%>
          <c:set var="shopBankAccountNo" value="1234567890" />
          <c:set var="shopBankAccountName" value="CONG TY BOOKYMART" />
          <c:set var="transferContent" value="BOOKYMART ${order.orderCode}" />
          <img src="https://img.vietqr.io/image/${shopBankBin}-${shopBankAccountNo}-compact2.png?amount=${order.totalAmount}&addInfo=${transferContent}&accountName=${shopBankAccountName}"
               alt="VietQR thanh toán" style="width:220px;height:220px;border-radius:12px;border:1px solid var(--line);" />
          <div style="font-size:1.3rem;font-weight:700;color:var(--terracotta);margin-top:12px;">
            <fmt:formatNumber value="${order.totalAmount}" pattern="#,###"/>đ
          </div>
          <div class="form-hint">Nội dung chuyển khoản: <strong>BOOKYMART ${order.orderCode}</strong></div>
          <p class="form-hint mt-2" style="max-width:420px;margin:8px auto 0;">
            Mở app ngân hàng bất kỳ, quét mã trên — số tiền và nội dung sẽ tự điền sẵn.
            Đơn sẽ chuyển sang "Đang xử lý" khi shop đối chiếu sao kê và xác nhận đã nhận được tiền
            (thường trong vài giờ làm việc). Bạn có thể xem lại mã này bất cứ lúc nào ở trang chi tiết đơn hàng.
          </p>
        </div>
      </c:if>

      <!-- Order summary card -->
      <div class="panel mt-4">
        <div class="panel-title"><span><svg class="icon"><use href="#ic-bag"/></svg> Tóm tắt đơn hàng</span></div>

        <div class="delivery-info-row">
          <svg class="icon"><use href="#ic-pin"/></svg>
          <div>
            <div class="dl">Giao đến</div>
            <div class="dv">${order.receiverName} — ${order.receiverPhone} — ${order.shippingAddress}</div>
          </div>
        </div>

        <div class="delivery-info-row">
          <svg class="icon"><use href="#ic-card"/></svg>
          <div>
            <div class="dl">Thanh toán</div>
            <div class="dv">
              <c:choose>
                <c:when test="${order.paymentMethod == 'ONLINE_QR'}">
                  Online qua QR &mdash;
                  <c:choose>
                    <c:when test="${order.paymentStatus == 'PAID'}">Đã thanh toán</c:when>
                    <c:otherwise>Chưa thanh toán</c:otherwise>
                  </c:choose>
                </c:when>
                <c:otherwise>
                  Thanh toán khi nhận hàng (COD) &mdash; Thu tiền khi giao
                </c:otherwise>
              </c:choose>
            </div>
          </div>
        </div>

        <div class="delivery-info-row">
          <svg class="icon"><use href="#ic-clock"/></svg>
          <div>
            <div class="dl">Dự kiến giao</div>
            <div class="dv">
              <fmt:formatDate value="${estimatedFrom}" pattern="dd/MM/yyyy"/> &mdash;
              <fmt:formatDate value="${estimatedTo}" pattern="dd/MM/yyyy"/>
            </div>
          </div>
        </div>

        <div class="panel-divider"></div>

        <div class="summary-row">
          <span>Tạm tính (${fn:length(orderDetails)} sản phẩm)</span>
          <span class="val"><fmt:formatNumber value="${order.subtotal}" pattern="#,###"/>đ</span>
        </div>
        <div class="summary-row">
          <span>Giảm giá</span>
          <span class="val">−<fmt:formatNumber value="${order.discountAmount}" pattern="#,###"/>đ</span>
        </div>
        <div class="summary-row">
          <span>Phí vận chuyển</span>
          <span class="val"><fmt:formatNumber value="${order.shippingFee}" pattern="#,###"/>đ</span>
        </div>
        <div class="summary-row total">
          <span>Tổng ${order.paymentMethod == 'ONLINE_QR' && order.paymentStatus == 'PAID' ? 'đã thanh toán' : 'cần thanh toán'}</span>
          <span class="val"><fmt:formatNumber value="${order.totalAmount}" pattern="#,###"/>đ</span>
        </div>
      </div>

      <div class="d-flex gap-3 mt-4">
        <a href="orders?id=${order.orderId}" class="btn-outline-ink btn-block-shop" style="text-align:center;text-decoration:none;">
          <svg class="icon" style="vertical-align:-2px;"><use href="#ic-clipboard"/></svg> Theo dõi đơn hàng
        </a>
        <a href="home" class="btn-terracotta btn-block-shop" style="text-align:center;text-decoration:none;">
          Tiếp tục mua sắm
        </a>
      </div>

    </div>
  </div>
</div>

<!-- Footer -->
<jsp:include page="assets/footer.jsp"></jsp:include>

</body>
</html>
