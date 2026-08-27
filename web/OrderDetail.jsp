<%-- 
    Document   : OrderDetail
    Created on : Jul 2, 2026, 5:51:02 PM
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
<title>Chi tiết đơn hàng — Booky Mart</title>
<link href="https://cdnjs.cloudflare.com/ajax/libs/twitter-bootstrap/5.3.3/css/bootstrap.min.css" rel="stylesheet">
<link href="https://fonts.googleapis.com/css2?family=Fraunces:opsz,wght@9..144,400;9..144,600;9..144,700&family=Inter:wght@400;500;600;700&family=JetBrains+Mono:wght@500&display=swap" rel="stylesheet">
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css">

</head>
<body>


<jsp:include page="assets/header.jsp" />


<div class="container">
  <div class="breadcrumb-shop">
    <a href="home">Trang chủ</a><span class="sep">/</span>
    <a href="myorders">Đơn hàng của tôi</a><span class="sep">/</span>
    <span class="current">${order.orderCode}</span>
  </div>
</div>

<div class="container pb-5">

  <div class="order-detail-head">
    <div>
      <h1>Đơn hàng ${order.orderCode}</h1>
      <div class="odate-lg"><fmt:formatDate value="${order.orderDate}" pattern="dd/MM/yyyy, HH:mm"/></div>
    </div>
    <span class="status-badge status-processing" style="font-size:.82rem;padding:7px 16px;"><span class="dot"></span>Đang xử lý</span>
  </div>

  <div class="row g-4">
    <!-- Left: Timeline + Invoice -->
    <div class="col-lg-8">

  <!-- Banner cho trạng thái đặc biệt: huỷ / tạm giữ / hết hạn thanh toán -->
  <c:if test="${statusRank == -1}">
    <div class="panel" style="border-left:4px solid #d9534f;">
      <c:choose>
        <c:when test="${order.status == 'CANCELLED'}">
          <strong>Đơn hàng đã bị huỷ.</strong>
          <c:if test="${not empty order.cancelReason}">
            <span class="form-hint">Lý do:
              <c:choose>
                <c:when test="${order.cancelReason == 'AUTO_NO_CONTACT'}">Shop không liên hệ được với bạn sau nhiều lần gọi điện</c:when>
                <c:when test="${order.cancelReason == 'CUSTOMER_REQUEST'}">Bạn đã yêu cầu huỷ đơn</c:when>
                <c:otherwise>${order.cancelReason}</c:otherwise>
              </c:choose>
            </span>
          </c:if>
        </c:when>
        <c:when test="${order.status == 'ON_HOLD'}">
          <strong>Đơn hàng đang tạm giữ để kiểm tra thêm.</strong> Vui lòng liên hệ hỗ trợ nếu cần.
        </c:when>
        <c:when test="${order.status == 'PAYMENT_EXPIRED'}">
          <strong>Đơn hàng đã hết hạn thanh toán.</strong> Vui lòng đặt lại đơn hàng mới.
        </c:when>
      </c:choose>
    </div>
  </c:if>

  <!-- Track order -->
  <div class="panel">
    <div class="panel-title"><span><svg class="icon"><use href="#ic-truck"/></svg> Theo dõi đơn hàng</span></div>

    <div class="timeline-track">

      <!-- Bước 1: luôn "done" vì order đã tồn tại -->
      <div class="timeline-step done">
        <div class="tdot"><svg class="icon" style="width:13px;"><use href="#ic-check"/></svg></div>
        <div class="tlabel">Đặt hàng thành công</div>
        <div class="ttime"><fmt:formatDate value="${order.orderDate}" pattern="dd/MM/yyyy, HH:mm"/></div>
      </div>

      <!-- Bước 2: Pending -->
      <div class="timeline-step ${statusRank > 1 ? 'done' : (statusRank == 1 ? 'current' : 'future')}">
        <div class="tdot">
          <c:choose>
            <c:when test="${statusRank > 1}"><svg class="icon" style="width:13px;"><use href="#ic-check"/></svg></c:when>
            <c:otherwise>2</c:otherwise>
          </c:choose>
        </div>
        <div class="tlabel">Chờ xử lý (Pending)</div>
        <c:if test="${statusRank >= 1}">
          <div class="ttime">
            <c:if test="${order.paymentMethod == 'ONLINE_QR' && order.paymentStatus == 'PAID'}">— đã thanh toán online</c:if>
            <c:if test="${order.paymentMethod == 'COD'}">— thanh toán khi nhận hàng</c:if>
          </div>
        </c:if>
      </div>

      <!-- Bước 3: Processing -->
      <div class="timeline-step ${statusRank > 2 ? 'done' : (statusRank == 2 ? 'current' : 'future')}">
        <div class="tdot">
          <c:choose>
            <c:when test="${statusRank > 2}"><svg class="icon" style="width:13px;"><use href="#ic-check"/></svg></c:when>
            <c:otherwise>3</c:otherwise>
          </c:choose>
        </div>
        <div class="tlabel">Đang xử lý (Processing)</div>
        <c:if test="${statusRank == 2}">
          <div class="tnote">Đơn hàng đang được kiểm tra và đóng gói tại kho Booky Mart.</div>
        </c:if>
      </div>

      <!-- Bước 4: Shipped -->
      <div class="timeline-step ${statusRank > 3 ? 'done' : (statusRank == 3 ? 'current' : 'future')}">
        <div class="tdot">
          <c:choose>
            <c:when test="${statusRank > 3}"><svg class="icon" style="width:13px;"><use href="#ic-check"/></svg></c:when>
            <c:otherwise>4</c:otherwise>
          </c:choose>
        </div>
        <div class="tlabel">Đang giao (Shipped)</div>
      </div>

      <!-- Bước 5: Delivered -->
      <div class="timeline-step ${statusRank == 4 ? 'done' : 'future'}">
        <div class="tdot">
          <c:choose>
            <c:when test="${statusRank == 4}"><svg class="icon" style="width:13px;"><use href="#ic-check"/></svg></c:when>
            <c:otherwise>5</c:otherwise>
          </c:choose>
        </div>
        <div class="tlabel">Đã giao (Delivered)</div>
      </div>

    </div>

    <p class="form-hint mt-3 mb-0">
      Trạng thái hiển thị tại đây luôn khớp với trạng thái gốc của đơn hàng —
      không có trường trạng thái riêng cho việc theo dõi.
    </p>
  </div>

  <!-- Invoice -->
  <div class="panel">
    <div class="panel-title"><span><svg class="icon"><use href="#ic-card"/></svg> Chi tiết hoá đơn</span></div>

    <table class="invoice-table-pro">
      <thead>
        <tr>
          <th>Sản phẩm</th>
          <th class="num">SL</th>
          <th class="num">Đơn giá</th>
          <th class="num">Thành tiền</th>
        </tr>
      </thead>
      <tbody>
        <c:forEach var="d" items="${orderDetails}">
          <tr>
            <td>
              <div class="it-name">${d.productNameSnapshot}</div>
              <c:if test="${statusRank == 4}">
                <a href="${pageContext.request.contextPath}/productdt?id=${d.productId}#write-review"
                   style="font-size:.76rem;color:var(--terracotta);text-decoration:none;font-weight:600;">
                  <svg class="icon" style="width:12px;"><use href="#ic-star"/></svg> Viết đánh giá
                </a>
              </c:if>
            </td>
            <td class="it-qty">${d.quantity}</td>
            <td class="it-unit"><fmt:formatNumber value="${d.unitPrice}" pattern="#,###"/>đ</td>
            <td class="it-price"><fmt:formatNumber value="${d.lineTotal}" pattern="#,###"/>đ</td>
          </tr>
        </c:forEach>
      </tbody>
    </table>

    <div class="invoice-summary-block">
      <div class="invoice-summary-row">
        <span class="isr-label"><svg class="icon"><use href="#ic-bag"/></svg> Tạm tính</span>
        <span class="isr-value"><fmt:formatNumber value="${order.subtotal}" pattern="#,###"/>đ</span>
      </div>
      <div class="invoice-summary-row discount">
        <span class="isr-label"><svg class="icon"><use href="#ic-tag"/></svg> Giảm giá</span>
        <span class="isr-value">−<fmt:formatNumber value="${order.discountAmount}" pattern="#,###"/>đ</span>
      </div>
      <div class="invoice-summary-row">
        <span class="isr-label"><svg class="icon"><use href="#ic-truck"/></svg> Phí vận chuyển</span>
        <span class="isr-value"><fmt:formatNumber value="${order.shippingFee}" pattern="#,###"/>đ</span>
      </div>

      <div class="invoice-grand-total">
        <span class="igt-label">
          Tổng ${order.paymentMethod == 'ONLINE_QR' && order.paymentStatus == 'PAID' ? 'đã thanh toán' : 'cần thanh toán'}
        </span>
        <span class="igt-value"><fmt:formatNumber value="${order.totalAmount}" pattern="#,###"/>đ</span>
      </div>
    </div>

    <p class="form-hint mt-3 mb-0">
      <svg class="icon" style="width:13px;vertical-align:-2px;"><use href="#ic-shield"/></svg>
      Các số liệu trên được giữ cố định tại thời điểm đặt hàng, không tính lại theo giá hiện tại của sản phẩm.
    </p>
  </div>

</div>

<!-- Right: delivery info + actions -->
<div class="col-lg-4">
  <div class="panel">
    <div class="panel-title"><span><svg class="icon"><use href="#ic-pin"/></svg> Thông tin giao hàng</span></div>
    <div class="delivery-info-row">
      <svg class="icon"><use href="#ic-user"/></svg>
      <div><div class="dl">Người nhận</div><div class="dv">${order.receiverName}</div></div>
    </div>
    <div class="delivery-info-row">
      <svg class="icon"><use href="#ic-phone"/></svg>
      <div><div class="dl">Điện thoại</div><div class="dv">${order.receiverPhone}</div></div>
    </div>
    <div class="delivery-info-row">
      <svg class="icon"><use href="#ic-pin"/></svg>
      <div><div class="dl">Địa chỉ</div><div class="dv">${order.shippingAddress}</div></div>
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
                <c:when test="${order.paymentStatus == 'REFUND_PENDING'}">
                  <span style="color:#d9534f;">Đơn đã huỷ — đang chờ shop hoàn tiền</span>
                </c:when>
                <c:when test="${order.paymentStatus == 'REFUNDED'}">
                  <span style="color:var(--moss);">Đã hoàn tiền</span>
                </c:when>
                <c:when test="${order.paymentStatus == 'UNPAID' && order.status == 'PENDING_PAYMENT'}">
                  <span style="color:#B8762E;">Chưa thanh toán — đang chờ shop xác nhận</span>
                </c:when>
                <c:otherwise>Chưa thanh toán</c:otherwise>
              </c:choose>
            </c:when>
            <c:otherwise>Thanh toán khi nhận hàng (COD)</c:otherwise>
          </c:choose>
        </div>
      </div>
    </div>

    <%-- MỚI (tích hợp VietQR thật — Hướng A): cho khách xem lại mã VietQR
         nếu đơn vẫn đang chờ xác nhận thanh toán — trước đây mã QR chỉ hiện
         thoáng qua lúc vừa đặt hàng, thoát ra là mất luôn, không xem lại được. --%>
    <c:if test="${order.paymentMethod == 'ONLINE_QR' && order.paymentStatus == 'UNPAID' && order.status == 'PENDING_PAYMENT'}">
      <div class="mt-3 mb-2" style="text-align:center;padding:16px;background:var(--paper);border-radius:12px;">
        <c:set var="shopBankBin" value="970436" /><%-- ⚠️ mẫu: thay bằng mã BIN ngân hàng thật của shop --%>
        <c:set var="shopBankAccountNo" value="1234567890" />
        <c:set var="shopBankAccountName" value="CONG TY BOOKYMART" />
        <img src="https://img.vietqr.io/image/${shopBankBin}-${shopBankAccountNo}-compact2.png?amount=${order.totalAmount}&addInfo=BOOKYMART ${order.orderCode}&accountName=${shopBankAccountName}"
             alt="VietQR thanh toán" style="width:180px;height:180px;border-radius:10px;border:1px solid var(--line);" />
        <div class="form-hint mt-2">Nội dung CK: <strong>BOOKYMART ${order.orderCode}</strong> — số tiền: <strong><fmt:formatNumber value="${order.totalAmount}" pattern="#,###"/>đ</strong></div>
      </div>
    </c:if>
  </div>

  <div class="panel">
    <div class="panel-title">Hành động</div>

    <c:if test="${not empty flashMessage}">
      <div class="alert ${flashError ? 'alert-danger' : 'alert-success'} mb-3" role="alert">${flashMessage}</div>
    </c:if>

    <c:if test="${order.returnStatus == 'REQUESTED'}">
      <div class="alert alert-warning mb-3">
        <svg class="icon" style="width:13px;vertical-align:-2px;"><use href="#ic-clock"/></svg>
        Yêu cầu hoàn hàng của bạn đang chờ xử lý — shop sẽ liên hệ lại sớm nhất.
      </div>
    </c:if>
    <c:if test="${order.returnStatus == 'COMPLETED'}">
      <div class="alert alert-success mb-3">
        <svg class="icon" style="width:13px;vertical-align:-2px;"><use href="#ic-check"/></svg>
        Yêu cầu hoàn hàng/hoàn tiền đã được shop xử lý xong.
      </div>
    </c:if>
    <c:if test="${order.returnStatus == 'REJECTED'}">
      <div class="alert alert-danger mb-3">
        <svg class="icon" style="width:13px;vertical-align:-2px;"><use href="#ic-alert-circle"/></svg>
        Yêu cầu hoàn hàng của bạn đã bị từ chối. Vui lòng liên hệ shop nếu cần thêm thông tin.
      </div>
    </c:if>

    <c:choose>
       <c:when test="${statusRank == 1}">
       <p class="form-hint mt-0" style="margin-top:-6px;">
       Đơn đang ở trạng thái <strong>Chờ xử lý</strong> — bạn có thể yêu cầu huỷ đơn.
       </p>
      <form action="cancelOrder" method="post" onsubmit="return confirm('Bạn có chắc muốn huỷ đơn hàng #${order.orderCode}?');">
       <input type="hidden" name="orderId" value="${order.orderId}">
       <button type="submit" class="btn-danger-outline btn-block-shop">Huỷ đơn hàng</button>
      </form>
  </c:when>
  <c:when test="${statusRank == 0}">
    <p class="form-hint mt-0" style="margin-top:-6px;">
      Đơn đang chờ xác nhận thanh toán — chưa thể tự hủy lúc này. Vui lòng liên hệ hotline nếu cần hỗ trợ.
    </p>
  </c:when>
      <c:when test="${statusRank == 3 && empty order.returnStatus}">
        <p class="form-hint mt-0" style="margin-top:-6px;">
          Đơn đang <strong>Đang giao (Shipped)</strong> — xác nhận khi bạn đã nhận được hàng.
        </p>
        <form action="orderaction" method="post" class="mb-2"
              onsubmit="return confirm('Xác nhận bạn đã nhận được hàng cho đơn #${order.orderCode}? Sau khi xác nhận, đơn sẽ được đóng và bạn sẽ không thể yêu cầu hoàn hàng qua bước này nữa.');">
          <input type="hidden" name="action" value="confirmDelivered">
          <input type="hidden" name="orderId" value="${order.orderId}">
          <button type="submit" class="btn-terracotta btn-block-shop">Xác nhận đã nhận hàng</button>
        </form>
        <button type="button" class="btn-outline-ink btn-block-shop" onclick="toggleReturnReasonForm()">Hoàn hàng</button>
        <!-- MỚI: bắt buộc nhập lý do trước khi gửi yêu cầu hoàn hàng — TRƯỚC
             ĐÂY bấm là gửi thẳng, Staff/Manager nhận yêu cầu mà không biết
             lý do gì (lỗi sản phẩm? giao nhầm? đổi ý?). -->
        <div id="returnReasonBox" style="display:none;margin-top:10px;">
          <form action="orderaction" method="post"
                onsubmit="return validateReturnReason();">
            <input type="hidden" name="action" value="requestReturn">
            <input type="hidden" name="orderId" value="${order.orderId}">
            <label class="form-label-shop">Lý do hoàn hàng *</label>
            <textarea name="reason" id="returnReasonInput" class="form-control-shop mb-2" rows="3"
                      placeholder="VD: Sản phẩm bị lỗi/hư hỏng, giao nhầm sản phẩm, không đúng mô tả..." required></textarea>
            <div class="d-flex gap-2">
              <button type="button" class="btn-outline-ink btn-block-shop" onclick="toggleReturnReasonForm()">Hủy</button>
              <button type="submit" class="btn-terracotta btn-block-shop">Gửi yêu cầu hoàn hàng</button>
            </div>
          </form>
        </div>
      </c:when>
      <c:when test="${statusRank == -1}">
        <p class="form-hint mt-0" style="margin-top:-6px;">Đơn hàng không còn ở trạng thái xử lý bình thường.</p>
      </c:when>
      <c:otherwise>
        <p class="form-hint mt-0" style="margin-top:-6px;">
          Đơn đang ở trạng thái <strong>${order.status}</strong>
          Vui lòng liên hệ hỗ trợ nếu cần thay đổi.
        </p>
      </c:otherwise>
    </c:choose>
    
  </div>
</div>
  </div>
</div>

<!-- Footer -->
<jsp:include page="assets/footer.jsp"></jsp:include>

<script>
function toggleReturnReasonForm(){
  var box = document.getElementById('returnReasonBox');
  box.style.display = (box.style.display === 'none' || box.style.display === '') ? 'block' : 'none';
}
function validateReturnReason(){
  var reason = document.getElementById('returnReasonInput').value.trim();
  if (reason === '') {
    alert('Vui lòng nhập lý do hoàn hàng.');
    return false;
  }
  return confirm('Xác nhận gửi yêu cầu hoàn hàng? Shop sẽ liên hệ lại bạn để xử lý.');
}
</script>
</body>
</html>
