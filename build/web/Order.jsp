<%-- 
    Document   : Order
    Created on : Jul 2, 2026, 5:41:35 PM
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
<title>Đơn hàng của tôi — Booky Mart</title>
<link href="https://cdnjs.cloudflare.com/ajax/libs/twitter-bootstrap/5.3.3/css/bootstrap.min.css" rel="stylesheet">
<link href="https://fonts.googleapis.com/css2?family=Fraunces:opsz,wght@9..144,400;9..144,600;9..144,700&family=Inter:wght@400;500;600;700&family=JetBrains+Mono:wght@500&display=swap" rel="stylesheet">
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css">

</head>
<body>


<jsp:include page="assets/header.jsp" />


<div class="container">
  <div class="breadcrumb-shop">
    <a href="home">Trang chủ</a><span class="sep">/</span>
    <span class="current">Đơn hàng của tôi</span>
  </div>
</div>

<div class="container pb-5">
  <h1 style="font-size:1.6rem;margin-bottom:18px;">Đơn hàng của tôi</h1>
  <c:if test="${not empty sessionScope.successMsg}">
  <div class="alert alert-success">${sessionScope.successMsg}</div>
  <c:remove var="successMsg" scope="session" />
</c:if>
<c:if test="${not empty sessionScope.errorMsg}">
  <div class="alert alert-danger">${sessionScope.errorMsg}</div>
  <c:remove var="errorMsg" scope="session" />
</c:if>
  <!-- Status tabs -->
  <div class="order-tabs">
    <div class="order-tab active" data-filter="all">Tất cả <span class="ct">(${totalCount})</span></div>
    <div class="order-tab" data-filter="paying">Chờ thanh toán <span class="ct">(${payingCount})</span></div>
    <div class="order-tab" data-filter="processing">Đang xử lý <span class="ct">(${processingCount})</span></div>
    <div class="order-tab" data-filter="shipped">Đang giao <span class="ct">(${shippedCount})</span></div>
    <div class="order-tab" data-filter="delivered">Đã giao <span class="ct">(${deliveredCount})</span></div>
    <div class="order-tab" data-filter="cancelled">Đã huỷ <span class="ct">(${cancelledCount})</span></div>
  </div>

  <c:if test="${empty orders}">
    <p class="form-hint">Bạn chưa có đơn hàng nào.</p>
  </c:if>

  <c:forEach var="o" items="${orders}">
    <c:set var="details" value="${detailsMap[o.orderId]}" />

    <!-- Quy đổi status -> category cho JS lọc + label/màu badge hiển thị -->
    <c:choose>
      <c:when test="${o.status == 'PENDING_PAYMENT'}">
        <c:set var="category" value="paying" />
        <c:set var="badgeClass" value="status-paying" />
        <c:set var="badgeLabel" value="Chờ thanh toán" />
      </c:when>
      <c:when test="${o.status == 'PENDING'}">
        <c:set var="category" value="processing" />
        <c:set var="badgeClass" value="status-processing" />
        <c:set var="badgeLabel" value="Chờ xử lý" />
      </c:when>
      <c:when test="${o.status == 'PROCESSING'}">
        <c:set var="category" value="processing" />
        <c:set var="badgeClass" value="status-processing" />
        <c:set var="badgeLabel" value="Đang xử lý" />
      </c:when>
      <c:when test="${o.status == 'ON_HOLD'}">
        <c:set var="category" value="processing" />
        <c:set var="badgeClass" value="status-processing" />
        <c:set var="badgeLabel" value="Tạm giữ" />
      </c:when>
      <c:when test="${o.status == 'SHIPPED'}">
        <c:set var="category" value="shipped" />
        <c:set var="badgeClass" value="status-shipped" />
        <c:set var="badgeLabel" value="Đang giao" />
      </c:when>
      <c:when test="${o.status == 'DELIVERED'}">
        <c:set var="category" value="delivered" />
        <c:set var="badgeClass" value="status-delivered" />
        <c:set var="badgeLabel" value="Đã giao" />
      </c:when>
      <c:when test="${o.status == 'CANCELLED'}">
        <c:set var="category" value="cancelled" />
        <c:set var="badgeClass" value="status-cancelled" />
        <c:set var="badgeLabel" value="Đã huỷ" />
      </c:when>
      <c:when test="${o.status == 'PAYMENT_EXPIRED'}">
        <c:set var="category" value="cancelled" />
        <c:set var="badgeClass" value="status-cancelled" />
        <c:set var="badgeLabel" value="Hết hạn thanh toán" />
      </c:when>
    </c:choose>

    <div class="order-card" data-category="${category}">
      <div class="order-card-head">
        <div>
          <div class="ocode">#${o.orderCode}</div>
          <div class="odate"><fmt:formatDate value="${o.orderDate}" pattern="dd/MM/yyyy, HH:mm"/></div>
        </div>
        <span class="status-badge ${badgeClass}"><span class="dot"></span>${badgeLabel}</span>
      </div>

      <div class="order-items-preview">
        <c:forEach var="d" items="${details}" varStatus="st" begin="0" end="1">
          <span class="oitem-name">${d.productNameSnapshot}<c:if test="${d.quantity > 1}"> x${d.quantity}</c:if></span>
        </c:forEach>
        <c:if test="${fn:length(details) > 2}">
          <span class="oitem-more">+${fn:length(details) - 2} sản phẩm khác</span>
        </c:if>
      </div>

      <div class="order-card-foot">
        <div class="order-total-mini">
          ${fn:length(details)} sản phẩm &middot; Tổng
          <strong><fmt:formatNumber value="${o.totalAmount}" pattern="#,###"/>đ</strong>
        </div>
        <div class="order-actions">
          <a href="orders?id=${o.orderId}" class="btn-outline-ink btn-sm-shop">
            <c:choose>
              <c:when test="${category == 'shipped'}">Theo dõi đơn</c:when>
              <c:otherwise>Xem chi tiết</c:otherwise>
            </c:choose>
          </a>
         <c:if test="${o.status == 'PENDING'}">
            <form action="cancelOrder" method="post" style="display:inline;"
        onsubmit="return confirm('Bạn có chắc muốn huỷ đơn hàng #${o.orderCode}?');">
    <input type="hidden" name="orderId" value="${o.orderId}">
    <button type="submit" class="btn-danger-outline btn-sm-shop" title="Chỉ huỷ được khi đơn đang chờ xử lý">Huỷ đơn</button>
  </form>
          </c:if>
        </div>
      </div>
    </div>
  </c:forEach>
</div>

<!-- Footer -->
<jsp:include page="assets/footer.jsp"></jsp:include>
<script>
document.querySelectorAll('.order-tab').forEach(function (tab) {
    tab.addEventListener('click', function () {
        document.querySelectorAll('.order-tab').forEach(function (t) { t.classList.remove('active'); });
        tab.classList.add('active');

        var filter = tab.getAttribute('data-filter');
        document.querySelectorAll('.order-card').forEach(function (card) {
            var category = card.getAttribute('data-category');
            card.style.display = (filter === 'all' || category === filter) ? '' : 'none';
        });
    });
});
</script>
</body>
</html>
