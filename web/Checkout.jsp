<%-- 
    Document   : Checkout
    Created on : Jun 30, 2026, 6:58:44 PM
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
<title>Thanh toán — Booky Mart</title>
<link href="https://cdnjs.cloudflare.com/ajax/libs/twitter-bootstrap/5.3.3/css/bootstrap.min.css" rel="stylesheet">
<link href="https://fonts.googleapis.com/css2?family=Fraunces:opsz,wght@9..144,400;9..144,600;9..144,700&family=Inter:wght@400;500;600;700&family=JetBrains+Mono:wght@500&display=swap" rel="stylesheet">
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css">

</head>
<body>


<jsp:include page="assets/header.jsp" />


<div class="container pb-5 pt-4">

  <!-- Stepper -->
  <div class="stepper">
    <div class="step-item done">
      <div class="step-circle"><svg class="icon" style="width:16px;"><use href="#ic-check"/></svg></div>
      <span class="step-label">Giỏ hàng</span>
    </div>
    <div class="step-divider"></div>
    <div class="step-item active">
      <div class="step-circle">2</div>
      <span class="step-label">Thanh toán</span>
    </div>
    <div class="step-divider"></div>
    <div class="step-item">
      <div class="step-circle">3</div>
      <span class="step-label">Xác nhận</span>
    </div>
  </div>

  <div class="row g-4">
    <!-- Left: form steps -->
    <div class="col-lg-8">

      <!-- Item selection (UC-CUS-02) -->
      <div class="panel">
  <div class="panel-title">
    <span>
      <svg class="icon"><use href="#ic-bag"/></svg>
      Sản phẩm thanh toán (${fn:length(checkoutItems)} món)
    </span>
  </div>

  <c:forEach var="item" items="${checkoutItems}" varStatus="status">
    <div class="cart-line checkout-line-item" style="margin-bottom:${status.last ? '0' : '10px'};"
         data-weight="${item.weightGrams}" data-quantity="${item.quantity}">

      <!-- Đã chọn từ bước giỏ hàng, chỉ hiển thị lại, không cho đổi ở bước checkout -->
      <input type="checkbox" class="cart-check" checked disabled>

      <div class="cart-thumb thumb-fiction">
        <c:choose>
          <c:when test="${not empty item.productImage}">
            <img src="${empty item.productImage ? 'assets/img/products/no-image.svg' : item.productImage}" alt="${item.productName}"
                 style="width:100%; height:100%; object-fit:cover; border-radius:8px;">
          </c:when>
          <c:otherwise>
            <svg class="icon" style="width:1.6rem;"><use href="#ic-book-open"/></svg>
          </c:otherwise>
        </c:choose>
      </div>

      <div class="cart-line-info">
        <div class="cart-line-name">${item.productName}</div>
        <div class="cart-line-meta">SL: ${item.quantity}</div>
      </div>

      <div class="cart-line-price">
        <fmt:formatNumber value="${item.itemTotal}" pattern="#,###"/>đ
      </div>

    </div>
  </c:forEach>
</div>

      <!-- Shipping address (UC-CUS-04) -->
      <div class="panel">
        <div class="panel-title">
          <span><svg class="icon"><use href="#ic-pin"/></svg> Địa chỉ giao hàng</span>
          <c:if test="${not empty address}">
            <button type="button" class="btn-sm-shop btn-outline-ink" onclick="toggleAddAddressForm()">Đổi</button>
          </c:if>
        </div>

        <c:choose>
          <c:when test="${not empty address}">
            <div class="addr-card">
              <div class="name-row">${address.receiverName} &middot; ${address.receiverPhone} <span class="tag-default">Mặc định</span></div>
              <div class="addr-text">${address.fullAddress}</div>
            </div>
          </c:when>
          <c:otherwise>
            <!-- TRƯỚC ĐÂY: nếu khách chưa từng có địa chỉ nào, phần này im lặng
                 hiển thị trống trơn, không giải thích gì. Giờ báo rõ + luôn hiện
                 sẵn form thêm địa chỉ (không cần bấm "Đổi" vì đang thiếu, không
                 phải đang có sẵn để đổi). -->
            <div class="addr-card" style="border-color:#d9534f; margin-bottom: 10px" >
              <div class="name-row" style="color:#d9534f;">Bạn chưa có địa chỉ giao hàng nào.</div>
              <div class="addr-text">Vui lòng thêm địa chỉ bên dưới để tiếp tục đặt hàng.</div>
            </div>
          </c:otherwise>
        </c:choose>

       <!-- Form thêm địa chỉ ngay tại Checkout — tái dùng đúng pattern đã có ở
             Account.jsp, không cần rời trang checkout để thêm địa chỉ mới. -->
        <%-- MỚI: trước đây trang này chưa từng hiển thị addrError/addrSuccess dù
             AddAddressController đã set session — lỗi nhập địa chỉ (SĐT sai
             định dạng, thiếu trường...) trôi qua âm thầm, khách không biết vì
             sao form "im lặng" không lưu được. --%>
        <c:if test="${not empty sessionScope.addrError}">
          <div class="alert alert-danger" style="color:red;">${sessionScope.addrError}</div>
          <c:remove var="addrError" scope="session"/>
        </c:if>
        <c:if test="${not empty sessionScope.addrSuccess}">
          <div class="alert alert-success" style="color:green;">${sessionScope.addrSuccess}</div>
          <c:remove var="addrSuccess" scope="session"/>
        </c:if>
        <div id="addAddressForm" class="addr-card mb-3" style="${empty address ? '' : 'display:none;'}">
          <form action="address-add" method="POST">
            <input type="hidden" name="action" value="add">
            <input type="hidden" name="returnUrl" value="checkout">
            <div class="mb-2">
              <label class="form-label-shop">Họ và tên người nhận</label>
              <input type="text" name="receiverName" class="form-control-shop" required>
            </div>
            <div class="mb-2">
              <label class="form-label-shop">Số điện thoại</label>
              <input type="text" name="receiverPhone" class="form-control-shop"
                     pattern="\d{9,11}" placeholder="VD: 0901234567" required>
            </div>
            <div class="mb-2">
              <label class="form-label-shop">Địa chỉ cụ thể</label>
              <textarea name="fullAddress" class="form-control-shop" rows="3" required
                        placeholder="Số nhà, đường, phường/xã, quận/huyện, tỉnh/thành"></textarea>
            </div>
            <div class="d-flex gap-2 mt-2">
              <button type="submit" class="btn-sm-shop btn-terracotta">Lưu địa chỉ</button>
              <c:if test="${not empty address}">
                <button type="button" class="btn-sm-shop btn-outline-ink" onclick="toggleAddAddressForm()">Hủy</button>
              </c:if>
            </div>
          </form>
        </div>
      </div>
      <script>
        function toggleAddAddressForm(){
          var f = document.getElementById('addAddressForm');
          f.style.display = (f.style.display === 'none') ? 'block' : 'none';
        }
      </script>

      <!-- Payment method (UC-CUS-06/07/08/09) -->
     <div class="panel">
  <div class="panel-title">
    <span><svg class="icon"><use href="#ic-card"/></svg> Phương thức thanh toán</span>
  </div>

  <label class="pay-method-option selected" onclick="selectPayment(this,'ONLINE_QR')">
    <input type="radio" name="payDisplay" checked>
    <div class="pm-icon"><svg class="icon"><use href="#ic-qr"/></svg></div>
    <div class="flex-grow-1">
      <div class="pm-name">Thanh toán online qua QR</div>
      <div class="pm-desc">Quét mã chuyển khoản — xác nhận tức thì</div>
    </div>
  </label>

  <label class="pay-method-option" onclick="selectPayment(this,'COD')">
    <input type="radio" name="payDisplay">
    <div class="pm-icon"><svg class="icon"><use href="#ic-truck"/></svg></div>
    <div class="flex-grow-1">
      <div class="pm-name">Thanh toán khi nhận hàng (COD)</div>
      <div class="pm-desc">Trả tiền mặt cho shipper khi giao</div>
    </div>
  </label>

  <p class="form-hint mt-3 mb-0">
    <svg class="icon" style="width:13px;vertical-align:-2px;"><use href="#ic-clock"/></svg>
    Mã VietQR thật sẽ hiện ở trang kế tiếp sau khi đặt hàng thành công. Đơn sẽ được xác nhận khi shop đối chiếu sao kê ngân hàng.
  </p>
</div>

    </div>

    <!-- Right: order summary -->
    <div class="col-lg-4">
      <div class="panel">
        <div class="panel-title"><span><svg class="icon"><use href="#ic-tag"/></svg> Mã giảm giá</span></div>
       <c:choose>
    <c:when test="${not empty validVouchers}">
      <select class="voucher-select" id="voucherSelect" onchange="renderSummary()">
        <option value="">-- Không dùng mã giảm giá --</option>
        <c:forEach var="v" items="${validVouchers}">
          <option value="${v.voucherId}"
                  data-code="${v.code}"
                  data-type="${v.discountType}"
                  data-value="${v.discountValue}"
                  data-cap="${v.maxDiscountCap}"
                  ${not empty selectedVoucher && selectedVoucher.voucherId == v.voucherId ? 'selected' : ''}>
            ${v.displayLabel}
          </option>
        </c:forEach>
      </select>
      <p class="form-hint">Chỉ được áp dụng 1 mã giảm giá cho mỗi đơn hàng</p>
    </c:when>
    <c:otherwise>
      <p class="form-hint">Bạn chưa có mã giảm giá khả dụng cho đơn hàng này</p>
    </c:otherwise>
  </c:choose>
      </div>


<div class="summary-card-pretty">
  <div class="sc-title"><svg class="icon" style="width:18px;"><use href="#ic-bag"/></svg> Tóm tắt thanh toán</div>

  <div class="sc-row">
    <span>Tạm tính (${fn:length(checkoutItems)} sản phẩm)</span>
    <span class="val" id="subtotal-val"><fmt:formatNumber value="${grandTotal}" pattern="#,###"/>đ</span>
  </div>

  <div class="sc-row">
    <span>Phí vận chuyển</span>
    <span class="val" id="shipping-val">--</span>
  </div>

  <div class="sc-row discount" id="discount-row" style="${empty selectedVoucher ? 'display:none;' : ''}">
    <span>Giảm giá &middot; <span id="discount-code">${selectedVoucher.code}</span></span>
    <span class="val" id="discount-val">−0đ</span>
  </div>

  <div class="sc-divider"></div>

  <div class="sc-total">
    <span class="lbl">Tổng thanh toán</span>
    <span class="amt" id="final-total">0đ</span>
  </div>

  <span class="sc-savings" id="savings-badge" style="display:none;">
    <svg class="icon" style="width:13px;"><use href="#ic-check"/></svg>
    Bạn đã tiết kiệm <span id="savings-val">0đ</span>
  </span>

  <!-- Voucher chọn sẽ được gửi kèm khi bấm xác nhận, KHÔNG gửi ngay lúc chọn -->
 <form id="confirmOrderForm" action="placeOrder" method="post">
  <input type="hidden" name="voucherId" id="voucherIdHidden"
         value="${not empty selectedVoucher ? selectedVoucher.voucherId : ''}">
  <input type="hidden" name="paymentMethod" id="paymentMethodHidden" value="ONLINE_QR">

  <button type="submit" class="btn-confirm-order" id="confirmBtn">
    <svg class="icon" style="width:18px;"><use href="#ic-shield"/></svg>
    <span id="confirmBtnText">Xác nhận đặt hàng</span>
  </button>
</form>
</div>
    </div>
  </div>
</div>

<script>
function selectPayment(el, type){
  document.querySelectorAll('.pay-method-option').forEach(o=>o.classList.remove('selected'));
  el.classList.add('selected');
  el.querySelector('input').checked = true;
   document.getElementById('paymentMethodHidden').value = type;
}

// TRƯỚC ĐÂY: nút "Xác nhận đặt hàng" là type="submit" nằm thẳng trong form,
// bấm là submit ngay lập tức — modal QR phía trên tồn tại trong HTML nhưng
// KHÔNG BAO GIỜ được mở ra, vì hàm confirmOrder() cũ đã bị comment lại và
// không có gì gọi tới nó nữa. Chọn "Online QR" hay "COD" trên giao diện vì
// vậy không tạo ra khác biệt gì khi demo. Giờ chặn lại đúng lúc submit: nếu
// chọn Online QR thì mở modal QR thật (ảnh QR sinh động theo đúng số tiền
// vừa chốt) trước, chỉ submit thật sự khi người dùng bấm xác nhận trong
// modal; nếu chọn COD thì submit thẳng như cũ, không cần qua modal.
// MỚI (tích hợp VietQR thật — Hướng A): TRƯỚC ĐÂY chặn submit để hiện QR
// ngay tại đây — nhưng QR THẬT cần đúng order_code thật (sinh ra sau khi đơn
// đã được tạo trong DB), nên không thể hiện QR chính xác TRƯỚC khi submit
// được nữa. Giờ để cả COD và Online QR submit thẳng như nhau; mã QR thật
// (với đúng order_code + số tiền) được hiển thị ở trang "Đặt hàng thành công"
// (SuccessOrder.jsp) ngay sau khi đơn đã có mã thật, và có thể xem lại bất
// cứ lúc nào ở trang chi tiết đơn hàng.
document.getElementById('confirmOrderForm').addEventListener('submit', function(e){
  const paymentMethod = document.getElementById('paymentMethodHidden').value;
  if (paymentMethod === 'ONLINE_QR') {
    if (!confirm('Đặt hàng xong bạn sẽ được đưa tới trang chứa mã VietQR để chuyển khoản. Tiếp tục?')) {
      e.preventDefault();
      return;
    }
  }
  const confirmBtn = document.getElementById('confirmBtn');
  const confirmBtnText = document.getElementById('confirmBtnText');
  confirmBtn.disabled = true;
  confirmBtnText.textContent = 'Đang xử lý...';
});
const SUBTOTAL = ${grandTotal};
  // TRƯỚC ĐÂY: phí ship cố định 30.000đ cho mọi đơn, không quan tâm cân nặng.
  // Giờ tính theo TỔNG CÂN NẶNG thật (data-weight × data-quantity trên mỗi
  // dòng sản phẩm) — công thức PHẢI khớp y hệt utilities/ShippingFeeCalculator.java
  // (bên server tính lại số thật, JS ở đây chỉ để xem trước trực quan).
  function calculateShippingFee() {
    var totalWeight = 0;
    document.querySelectorAll('.checkout-line-item').forEach(function (el) {
      var w = parseInt(el.dataset.weight, 10) || 0;
      var q = parseInt(el.dataset.quantity, 10) || 0;
      totalWeight += w * q;
    });

    var BASE_THRESHOLD_G = 2000;
    var BASE_FEE = 30000;
    var STEP_G = 1000;
    var FEE_PER_STEP = 15000;

    if (totalWeight <= BASE_THRESHOLD_G) return BASE_FEE;
    var extraWeight = totalWeight - BASE_THRESHOLD_G;
    var steps = Math.ceil(extraWeight / STEP_G);
    return BASE_FEE + steps * FEE_PER_STEP;
  }
  const SHIPPING_FEE = calculateShippingFee();

  function formatVND(n) {
    return Math.round(n).toLocaleString('vi-VN') + 'đ';
  }

  function renderSummary() {
    const select = document.getElementById('voucherSelect');
    const selectedOption = select ? select.options[select.selectedIndex] : null;

    document.getElementById('shipping-val').textContent = formatVND(SHIPPING_FEE);

    const discountRow = document.getElementById('discount-row');
    const discountCode = document.getElementById('discount-code');
    const discountVal = document.getElementById('discount-val');
    const finalTotal = document.getElementById('final-total');
    const savingsBadge = document.getElementById('savings-badge');
    const savingsVal = document.getElementById('savings-val');
    const voucherIdHidden = document.getElementById('voucherIdHidden');

    let discount = 0;

    if (selectedOption && selectedOption.value !== '') {
      const type = selectedOption.dataset.type;
      const value = parseFloat(selectedOption.dataset.value) || 0;
      const capRaw = selectedOption.dataset.cap;
      const cap = (capRaw && capRaw.trim() !== '') ? parseFloat(capRaw) : null;

      if (type === 'PERCENTAGE') {
        discount = SUBTOTAL * value / 100;
        if (cap !== null && discount > cap) discount = cap;
      } else {
        discount = value;
      }
      if (discount > SUBTOTAL) discount = SUBTOTAL;

      discountRow.style.display = '';
      discountCode.textContent = selectedOption.dataset.code;
      discountVal.textContent = '−' + formatVND(discount);
      voucherIdHidden.value = selectedOption.value;
    } else {
      discountRow.style.display = 'none';
      voucherIdHidden.value = '';
    }

    const total = SUBTOTAL + SHIPPING_FEE - discount;
    finalTotal.textContent = formatVND(total);
    window.currentOrderTotal = total; // để modal QR đọc đúng số tiền hiện tại

    if (discount > 0) {
      savingsBadge.style.display = '';
      savingsVal.textContent = formatVND(discount);
    } else {
      savingsBadge.style.display = 'none';
    }
  }
   document.addEventListener('DOMContentLoaded', renderSummary);
</script>



</body>
</html>

