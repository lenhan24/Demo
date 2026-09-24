<%-- 
    Document   : ProductDetail
    Created on : Jun 30, 2026, 7:37:18 AM
    Author     : GiGaByte
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Booky Mart</title>
<link href="https://cdnjs.cloudflare.com/ajax/libs/twitter-bootstrap/5.3.3/css/bootstrap.min.css" rel="stylesheet">
<link href="https://fonts.googleapis.com/css2?family=Fraunces:opsz,wght@9..144,400;9..144,600;9..144,700&family=Inter:wght@400;500;600;700&family=JetBrains+Mono:wght@500&display=swap" rel="stylesheet">
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css">


</head>
<body>


<jsp:include page="assets/header.jsp" />



<div class="container">
  <c:if test="${not empty flashMessage}">
    <div class="alert ${flashError ? 'alert-danger' : 'alert-success'} mt-3 mb-3" role="alert">${flashMessage}</div>
  </c:if>
  <div class="breadcrumb-shop">
    <a href="home">Trang chủ</a><span class="sep"></span>
    <span class="sep">/</span>
    <span class="current">${product.name}</span>
  </div>
</div>

<div class="container pb-5">
  <div class="row g-5">

    <div class="col-lg-5">
      <div class="gallery-main">
        <img src="${empty product.images ? 'assets/img/products/no-image.svg' : product.images}" alt="${product.name}" class="gallery-main-img">
      </div>
    </div>

    <div class="col-lg-7">
      <a href="category?cid=${product.categoryName}" class="product-cat" style="font-size:.78rem;text-decoration:none;">${product.categoryName}</a>

      <h1 style="font-size:1.9rem;margin-top:6px;">${product.name}</h1>

      <div class="pd-rating-row mt-2">
        <span class="stars">${product.ratingAvg >= 4.5 ? '★★★★★' : (product.ratingAvg >= 3.5 ? '★★★★☆' : (product.ratingAvg >= 2.5 ? '★★★☆☆' : (product.ratingAvg >= 1.5 ? '★★☆☆☆' : '★☆☆☆☆')))}</span>
        <span><fmt:formatNumber value="${product.ratingAvg}" maxFractionDigits="1" minFractionDigits="1"/> (${product.ratingCount} đánh giá)</span>
      </div>

      <div class="pd-price-block">
        <span class="pd-price-now" id="pdPriceNow">
            <fmt:formatNumber value="${product.originalPrice}" pattern="#,###"/>đ
        </span>
      </div>

      <div class="pd-attrs">
        <c:choose>
            <%-- Kiểm tra nếu là Văn phòng phẩm --%>
            <c:when test="${product.productType == 'STATIONERY'}">
                <div><strong>Thương hiệu:</strong> ${product.brandorauthor}</div>
                <div><strong>Thông số kỹ thuật:</strong> ${product.specifications}</div>
            </c:when>

            <%-- Mặc định là Sách --%>
            <c:otherwise>
                <div><strong>Tác giả:</strong> ${product.brandorauthor}</div>
                <div><strong>NXB:</strong> ${product.publisher}</div>
                <div><strong>Năm XB:</strong> ${product.publicationYear}</div>
                <div><strong>ISBN:</strong> ${product.isbn}</div>
            </c:otherwise>
        </c:choose>

        <div><strong>Tồn kho:</strong>
            <span style="color: ${availableStock > 0 ? 'green' : 'red'}; font-weight: bold;">
                ${availableStock > 0 ? availableStock : 'Hết hàng'}
            </span>
        </div>
      </div>

      <!-- PHẦN CHỈNH SỬA: Số lượng và Nút thêm vào giỏ -->
      <div class="d-flex align-items-center gap-3 mt-4">
        <span class="form-label-shop" style="margin:0;">Số lượng</span>
        <div class="qty-stepper" id="qtyStepperWrap">
          <%-- Nếu hết hàng, vô hiệu hóa nút +/- --%>
          <button type="button" id="btnQtyMinus" ${availableStock <= 0 ? 'disabled' : ''}><svg class="icon"><use href="#ic-minus"/></svg></button>
          <input id="qty" type="text" value="${availableStock > 0 ? 1 : 0}" inputmode="numeric" ${availableStock <= 0 ? 'disabled' : ''}>
          <button type="button" id="btnQtyPlus" ${availableStock <= 0 ? 'disabled' : ''}><svg class="icon"><use href="#ic-plus"/></svg></button>
        </div>

        <span id="qtyMaxNote" style="display:none;color:#C24545;font-size:.78rem;"></span>
        <span style="font-size:.85rem;color:var(--ink-soft);">
            Tạm tính: <strong id="pdSubtotal" style="color:var(--ink);">${availableStock > 0 ? '' : '--'}</strong>
        </span>

        <c:if test="${availableStock > 0 && availableStock <= 5}">
          <span class="badge-shop" style="background:#fdecea;color:#d9534f;font-size:.75rem;padding:4px 10px;border-radius:20px;">
            <svg class="icon" style="width:11px;"><use href="#ic-alert-circle"/></svg> Chỉ còn ${availableStock} sản phẩm!
          </span>
        </c:if>
      </div>

      <div class="d-flex gap-3 mt-4">
        <form action="addtocart" method="post" style="flex:1;">
          <input type="hidden" name="productId" value="${product.productId}">
          <input type="hidden" name="quantity" id="qtyHidden" value="${availableStock > 0 ? 1 : 0}">

          <%-- Nút bấm: Đổi màu xám và thêm thuộc tính 'disabled' khi hết hàng --%>
          <button type="submit" class="btn-terracotta"
                  style="background: ${availableStock > 0 ? 'var(--ink)' : '#ccc'}; width:100%; border:none; cursor: ${availableStock > 0 ? 'pointer' : 'not-allowed'};"
                  ${availableStock > 0 ? '' : 'disabled'}>
            ${availableStock > 0 ? 'Thêm vào giỏ' : 'Tạm thời hết hàng'}
          </button>
        </form>
      </div>
      <!-- KẾT THÚC PHẦN CHỈNH SỬA -->
    </div>
  </div>
</div>

<div class="container mt-5">
  <!-- 1. PHẦN MÔ TẢ -->
  <div class="mb-5">
    <h3 style="font-family:'Fraunces',serif; font-weight:700; margin-bottom:15px;">Mô tả sản phẩm</h3>
    <p style="color:var(--ink-soft); line-height:1.8; word-wrap:break-word; white-space:pre-line;">
      <c:choose>
        <c:when test="${not empty product.description}">${product.description}</c:when>
        <c:otherwise>Sản phẩm hiện chưa có mô tả chi tiết.</c:otherwise>
      </c:choose>
    </p>
  </div>

  <hr class="my-4">

  <!-- 2. PHẦN ĐÁNH GIÁ -->
  <div class="mb-5">
    <h3 style="font-family:'Fraunces',serif; font-weight:700; margin-bottom:20px;">Đánh giá (${ratingCount})</h3>
    <div class="row g-4">
      <div class="col-md-4">
        <div style="font-family:'Fraunces',serif;font-size:2.6rem;font-weight:700;">
          <fmt:formatNumber value="${product.ratingAvg}" maxFractionDigits="1" minFractionDigits="1"/>
        </div>
        <div class="stars mb-3">★★★★★</div>
        <c:forEach var="star" items="${ratingBreakdown}">
          <div class="rating-bar-row" style="display:flex; align-items:center; gap:10px; margin-bottom:5px;">
            <span>${star.key}★</span>
            <div class="rating-bar-track" style="flex:1; background:#eee; height:8px; border-radius:4px;">
              <c:set var="pct" value="${ratingCount > 0 ? (star.value * 100) / ratingCount : 0}"/>
              <div class="rating-bar-fill" style="width:${pct}%; background:#ffc107; height:100%; border-radius:4px;"></div>
            </div>
            <span style="width:20px;">${star.value}</span>
          </div>
        </c:forEach>

        <!-- Form viết đánh giá -->
        <c:if test="${not empty reviewableOrderDetailId}">
          <div class="mt-4 p-3" style="background:#f9f9f9; border-radius:8px;" id="write-review">
            <div style="font-weight:700;margin-bottom:8px;">Viết đánh giá của bạn</div>
            <form action="review" method="post">
              <input type="hidden" name="productId" value="${product.productId}">
              <input type="hidden" name="orderDetailId" value="${reviewableOrderDetailId}">
              <select name="rating" class="form-control-shop mb-2" required>
                <option value="5">★★★★★ (5 sao)</option>
                <option value="4">★★★★☆ (4 sao)</option>
                <option value="3">★★★☆☆ (3 sao)</option>
                <option value="2">★★☆☆☆ (2 sao)</option>
                <option value="1">★☆☆☆☆ (1 sao)</option>
              </select>
              <textarea name="comment" class="form-control-shop mb-2" rows="3" placeholder="Cảm nhận của bạn..."></textarea>
              <button type="submit" class="btn-sm-shop btn-ink">Gửi đánh giá</button>
            </form>
          </div>
        </c:if>
      </div>

      <div class="col-md-8">
        <c:if test="${empty reviews}"><p>Chưa có đánh giá nào.</p></c:if>
        <c:forEach var="rv" items="${reviews}">
          <div class="review-item mb-4 pb-3" style="border-bottom:1px solid #eee;">
            <div class="d-flex gap-3">
              <div class="review-avatar" style="width:40px; height:40px; background:#ddd; border-radius:50%; display:flex; align-items:center; justify-content:center;">${rv.avatarLetter}</div>
              <div>
                <div style="font-weight:700;">${rv.reviewerName}</div>
                <div class="stars" style="font-size:.8rem;">${rv.starsDisplay}</div>
                <p style="margin:5px 0; color:#555;">${rv.comment}</p>
                <div style="font-size:.7rem; color:#999;">${rv.formattedDate}</div>
              </div>
            </div>
          </div>
        </c:forEach>
      </div>
    </div>
  </div>
</div>

<footer style="background-color: #1e2632; color: #d1cfcb; width: 100%; display: block; padding-top: 40px; padding-bottom: 20px;">
  <div class="container">
    <div class="row g-4">
      <!-- Cột Hỗ trợ -->
      <div class="col-6 col-md-6">
        <h6 style="font-weight: 700; margin-bottom: 15px; color: #ffffff;">Hỗ trợ</h6>
        <div class="d-flex flex-column gap-2">
          <a href="#" style="text-decoration:none; color: #bdc3c7;">Chính sách đổi trả</a>
          <a href="#" style="text-decoration:none; color: #bdc3c7;">Vận chuyển</a>
          <a href="#" style="text-decoration:none; color: #bdc3c7;">Câu hỏi thường gặp</a>
        </div>
      </div>

      <!-- Cột Liên hệ -->
      <div class="col-6 col-md-6">
        <h6 style="font-weight: 700; margin-bottom: 15px; color: #ffffff;">Liên hệ</h6>
        <div class="d-flex flex-column gap-2">
          <a href="#" style="text-decoration:none; color: #bdc3c7;">
            <svg class="icon" style="fill: #bdc3c7;"><use href="#ic-pin"/></svg> Hà Nội, Việt Nam
          </a>
          <a href="#" style="text-decoration:none; color: #bdc3c7;">
            <svg class="icon" style="fill: #bdc3c7;"><use href="#ic-mail"/></svg> hotro@bookymart.vn
          </a>
          <a href="#" style="text-decoration:none; color: #bdc3c7;">
            <svg class="icon" style="fill: #bdc3c7;"><use href="#ic-phone"/></svg> 1900 1234
          </a>
        </div>
      </div>
    </div>

    <!-- Phần chân trang -->
    <div class="footer-bottom mt-4 pt-3 border-top" style="border-color: #34495e !important; color: #95a5a6; font-size: 0.85rem; text-align: center;">
      <span>© 2026 Booky Mart - Hệ thống phân phối sách & văn phòng phẩm</span>
    </div>
  </div>
</footer>

<script>
function showTab(btn, name){
  document.querySelectorAll('.tab-btn').forEach(b=>b.classList.remove('active'));
  document.querySelectorAll('.tab-panel').forEach(p=>p.classList.remove('active'));
  btn.classList.add('active');
  document.getElementById('tab-'+name).classList.add('active');
}

// Khách bấm "Viết đánh giá" từ trang chi tiết đơn hàng (OrderDetail.jsp)
// sẽ được điều hướng tới đây kèm #write-review — trước đây tab "Đánh giá"
// không tự mở, khách phải tự tìm và bấm tab, rất dễ bỏ lỡ luôn.
if (window.location.hash === '#write-review') {
  const reviewTabBtn = document.querySelector('.tab-btn[onclick*="reviews"]');
  if (reviewTabBtn) showTab(reviewTabBtn, 'reviews');
  const target = document.getElementById('write-review');
  if (target) target.scrollIntoView({ behavior: 'smooth', block: 'center' });
}
</script>


<script>
  (function () {
    var availableStock = ${availableStock};
    var unitPrice = ${product.originalPrice}; // giá gốc từ server, dùng để tính lại theo số lượng
    var qtyInput = document.getElementById('qty');
    var qtyHidden = document.getElementById('qtyHidden');
    var subtotalEl = document.getElementById('pdSubtotal');
    var qtyMaxNote = document.getElementById('qtyMaxNote');

    // Hết hàng: không có gì để tính và không có gì để cảnh báo "chỉ còn X",
    // vì X = 0. Giữ nguyên UI đã disabled từ server, thoát sớm luôn.
    if (availableStock <= 0) {
      subtotalEl.textContent = '--';
      return;
    }

    var btnMinus = document.getElementById('btnQtyMinus');
    var btnPlus = document.getElementById('btnQtyPlus');
    var MIN_QTY = 1;
    // Lấy đúng số lượng CÓ THỂ MUA NGAY từ server (đã tính theo lô FIFO đang ACTIVE).
    // Không ép tối thiểu bằng 1 nữa — nếu còn hàng thì availableStock luôn >= 1 ở đây rồi.
    var MAX_QTY = availableStock;
    var noteHideTimer = null;

    function formatPrice(num) {
      return num.toLocaleString('vi-VN') + 'đ';
    }

    function getQty() {
      var v = parseInt(qtyInput.value, 10);
      if (isNaN(v)) v = MIN_QTY;
      return v;
    }

    function setQty(v) {
      var hitLimit = false;
      if (v < MIN_QTY) { v = MIN_QTY; hitLimit = true; }
      if (v > MAX_QTY) { v = MAX_QTY; hitLimit = true; }
      qtyInput.value = v;
      qtyHidden.value = v;
      // Ô giá chính (pdPriceNow) giữ nguyên cố định là đơn giá, chỉ ô "Tạm tính"
      // riêng biệt mới đổi theo số lượng.
      subtotalEl.textContent = formatPrice(unitPrice * v);

      // Bấm "+" chạm MAX_QTY thì hiện cảnh báo rõ ràng trong chốc lát rồi tự ẩn,
      // thay vì ghim âm thầm không rõ lý do.
      if (qtyMaxNote) {
        if (hitLimit && v >= MAX_QTY) {
          qtyMaxNote.textContent = 'Chỉ còn ' + MAX_QTY + ' sản phẩm trong kho.';
          qtyMaxNote.style.display = 'inline';
          clearTimeout(noteHideTimer);
          noteHideTimer = setTimeout(function () { qtyMaxNote.style.display = 'none'; }, 2500);
        } else {
          qtyMaxNote.style.display = 'none';
        }
      }
    }

    btnMinus.addEventListener('click', function () {
      setQty(getQty() - 1);
    });

    btnPlus.addEventListener('click', function () {
      setQty(getQty() + 1);
    });

    qtyInput.addEventListener('input', function () {
      // chỉ giữ số
      this.value = this.value.replace(/[^0-9]/g, '');
    });

    qtyInput.addEventListener('blur', function () {
      setQty(getQty());
    });

    // khởi tạo giá ban đầu đúng theo qty=1
    setQty(getQty());
  })();
</script>
</body>
</html>
