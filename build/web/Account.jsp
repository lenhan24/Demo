

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="vi">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Tài khoản của tôi — Booky Mart</title>
<link href="https://cdnjs.cloudflare.com/ajax/libs/twitter-bootstrap/5.3.3/css/bootstrap.min.css" rel="stylesheet">
<link href="https://fonts.googleapis.com/css2?family=Fraunces:opsz,wght@9..144,400;9..144,600;9..144,700&family=Inter:wght@400;500;600;700&family=JetBrains+Mono:wght@500&display=swap" rel="stylesheet">
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css">

</head>
<body>


<jsp:include page="assets/header.jsp" />

<div class="container">
  <div class="breadcrumb-shop">
    <a href="home">Trang chủ</a><span class="sep">/</span>
    <span class="current">Tài khoản của tôi</span>
  </div>
</div>

<div class="container pb-5">
  <div class="row g-4">

    <!-- Sidebar -->
    <div class="col-lg-3">
      <div class="panel" style="text-align:center;padding:24px 18px;">
        <div class="account-avatar mx-auto"><svg class="icon"><use href="#ic-user"/></svg></div>
        <div style="font-weight:700;margin-top:12px;">${account.fullName}</div>
        <div style="font-size:.8rem;color:var(--ink-soft);">${account.email}</div>
      </div>

      <div class="side-nav account-tab-list mt-3">
        <a href="#" class="active" onclick="showAccountTab(event,'profile')"><svg class="icon"><use href="#ic-user"/></svg> Hồ sơ cá nhân</a>
        <a href="#" onclick="showAccountTab(event,'address')"><svg class="icon"><use href="#ic-pin"/></svg> Địa chỉ giao hàng</a>
        <a href="#" onclick="showAccountTab(event,'password')"><svg class="icon"><use href="#ic-key"/></svg> Đổi mật khẩu</a>
        <a href="myorders"><svg class="icon"><use href="#ic-package"/></svg> Đơn hàng của tôi</a>
        <a href="#" onclick="showAccountTab(event,'voucher')"><svg class="icon"><use href="#ic-tag"/></svg> Voucher đã lưu</a>
        <a href="logout" style="color:#C24545;"><svg class="icon"><use href="#ic-logout"/></svg> Đăng xuất</a>
      </div>
    </div>

    <!-- Content -->
    <div class="col-lg-9">

      <!-- Tab: Profile -->
      <div id="acc-profile" class="account-tab-panel">

<div class="container mt-5">
    <c:choose>
    <%-- Nếu có thông báo thành công thì hiển thị thông báo này và bỏ qua lỗi --%>
    <c:when test="${not empty successMsg}">
        <div class="alert alert-success" style="color: green; font-weight: bold;">
            ${successMsg}
        </div>
        </c:when>
    
    <%-- Nếu không có thông báo thành công mà có lỗi thì hiển thị lỗi --%>
    <c:when test="${not empty errorMsg}">
        <div class="alert alert-danger" style="color: red; font-weight: bold;">
            ${errorMsg}
        </div>
        </c:when>
</c:choose>

    <form action="changeinfo" method="post">
        <div class="row g-3">
            <div class="col-md-6">
                <label class="form-label-shop">Họ và tên</label>
                <input type="text" name="fullName" class="form-control-shop" value="${sessionScope.account.fullName}" required>
            </div>
            
            <div class="col-md-6">
                <label class="form-label-shop">Số điện thoại</label>
                <input type="text" name="phone" class="form-control-shop" value="${sessionScope.account.phone}">
            </div>
            
            <div class="col-md-6">
                <label class="form-label-shop">Email</label>
                <input type="email" class="form-control-shop" value="${sessionScope.account.email}" disabled style="opacity:.6;">
                <p class="form-hint">Email dùng để đăng nhập, không thể thay đổi</p>
            </div>
        </div>
        <button type="submit" class="btn-terracotta mt-3">Lưu thay đổi</button>
    </form>
</div>
      </div>


<div id="acc-address" class="account-tab-panel" style="display:none;">
    <div class="panel">
        <div class="panel-title">
            <span>Địa chỉ giao hàng</span>
            <button type="button" class="btn-sm-shop btn-outline-ink" onclick="toggleAddAddressForm()">
            + Thêm địa chỉ
        </button>
            </div>
<c:if test="${not empty sessionScope.addrError}">
        <div class="alert alert-danger" style="color:red;">${sessionScope.addrError}</div>
        <c:remove var="addrError" scope="session"/>
    </c:if>
    <c:if test="${not empty sessionScope.addrSuccess}">
        <div class="alert alert-success" style="color:green;">${sessionScope.addrSuccess}</div>
        <c:remove var="addrSuccess" scope="session"/>
    </c:if>

    <!-- Form thêm địa chỉ, mặc định ẩn -->
    <div id="addAddressForm" class="addr-card mb-3" style="display:none;">
        <form action="address-add" method="POST">
            <input type="hidden" name="action" value="add">
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
                <button type="button" class="btn-sm-shop btn-outline-ink" onclick="toggleAddAddressForm()">Hủy</button>
            </div>
        </form>
    </div>
        <%-- Vòng lặp lấy dữ liệu từ addressList --%>
        <c:forEach var="addr" items="${addressList}">
            <%-- Điều chỉnh style một chút nếu không phải địa chỉ mặc định (dựa trên code gốc của bạn) --%>
            <div class="addr-card mb-3" 
                 <c:if test="${!addr.isDefault}">style="border-color:var(--line);background:transparent;"</c:if>>
                 
                <div class="name-row">
                    ${addr.receiverName} &middot; ${addr.receiverPhone}
                    <%-- Nếu là địa chỉ mặc định thì hiện Tag "Mặc định" --%>
                    <c:if test="${addr.isDefault}">
                        <span class="tag-default">Mặc định</span>
                    </c:if>
                </div>
                
                <div class="addr-text">${addr.fullAddress}</div>
                
                <div class="d-flex gap-2 mt-2">
                    <button type="button" class="btn-sm-shop btn-outline-ink edit-address-btn"
                            data-address-id="${addr.addressId}"
                            data-receiver-name="${fn:escapeXml(addr.receiverName)}"
                            data-receiver-phone="${fn:escapeXml(addr.receiverPhone)}"
                            data-full-address="${fn:escapeXml(addr.fullAddress)}">Sửa</button>
                    <%-- Nếu KHÔNG phải địa chỉ mặc định thì hiện nút "Đặt làm mặc định" --%>
                    <c:if test="${!addr.isDefault}">
<!--                        <button class="btn-sm-shop btn-outline-ink">Đặt làm mặc định</button>-->
<form action="address-action" method="POST" style="display: contents;">
            <input type="hidden" name="action" value="setDefault">
            <input type="hidden" name="addressId" value="${addr.addressId}">
            <input type="hidden" name="accountId" value="${addr.accountId}">
            <button type="submit" class="btn-sm-shop btn-outline-ink">Đặt làm mặc định</button>
        </form>
                    </c:if>
<!--                    <button class="btn-sm-shop btn-danger-outline">Xóa</button>-->
<form action="address-action" method="POST" style="display: contents;" 
          onsubmit="return confirm('Bạn có chắc chắn muốn xóa địa chỉ này?');">
        <input type="hidden" name="action" value="delete">
        <input type="hidden" name="addressId" value="${addr.addressId}">
        <button type="submit" class="btn-sm-shop btn-danger-outline">Xóa</button>
    </form>
                </div>
            </div>
        </c:forEach>

        <!-- Modal "Sửa địa chỉ" — MỚI, trước đây nút "Sửa" không có gì để mở -->
        <div class="modal-confirm-backdrop" id="editAddressModal">
          <div class="modal-confirm-box" style="max-width:420px;text-align:left;">
            <h3 style="text-align:center;">Sửa địa chỉ</h3>
            <form action="address-action" method="post">
              <input type="hidden" name="action" value="update">
              <input type="hidden" name="addressId" id="edit_addressId">
              <label class="form-label-shop">Tên người nhận</label>
              <input class="form-control-shop mb-3" name="receiverName" id="edit_receiverName" required>
              <label class="form-label-shop">Số điện thoại</label>
              <input class="form-control-shop mb-3" name="receiverPhone" id="edit_receiverPhone" required>
              <label class="form-label-shop">Địa chỉ</label>
              <textarea class="form-control-shop mb-3" name="fullAddress" id="edit_fullAddress" rows="2" required style="width:100%;font-family:'Inter',sans-serif;padding:10px 12px;border:1px solid var(--line);border-radius:9px;"></textarea>
              <div class="d-flex gap-2">
                <button type="button" class="btn-outline-ink btn-block-shop" onclick="document.getElementById('editAddressModal').classList.remove('open')">Hủy</button>
                <button type="submit" class="btn-terracotta btn-block-shop">Lưu thay đổi</button>
              </div>
            </form>
          </div>
        </div>
        
    </div>
</div>


<div id="acc-password" class="account-tab-panel" style="display:none;">
    <div class="panel" style="max-width:480px;">
        <form action="changepass" method="post">
            <div class="panel-title">Đổi mật khẩu</div>
            
            <label class="form-label-shop">Mật khẩu hiện tại</label>
            <input type="password" class="form-control-shop mb-3" name="currPass" placeholder="Nhập mật khẩu hiện tại" required>
            
            <label class="form-label-shop">Mật khẩu mới</label>
            <input type="password" class="form-control-shop mb-3" name="newPass" placeholder="Tối thiểu 8 ký tự" required>
            
            <label class="form-label-shop">Xác nhận mật khẩu mới</label>
            <input type="password" class="form-control-shop mb-3" name="renewPass" placeholder="Nhập lại mật khẩu mới" required>
            
            <%-- Khu vực hiển thị thông báo lỗi / thành công --%>
            <c:if test="${not empty sessionScope.messError}">
                <div class="alert alert-danger mb-3" style="color: red;">
                    ${sessionScope.messError}
                </div>
                <c:remove var="messError" scope="session"/>
            </c:if>
            <c:if test="${not empty sessionScope.messSuccess}">
                <div class="alert alert-success mb-3" style="color: green;">
                    ${sessionScope.messSuccess}
                </div>
                <c:remove var="messSuccess" scope="session"/>
            </c:if>

            <button type="submit" class="btn-terracotta btn-block-shop">Cập nhật mật khẩu</button>
        </form>
    </div>
</div>

      <!-- Tab: Voucher đã lưu -->
      <div id="acc-voucher" class="account-tab-panel" style="display:none;">
        <div class="panel-title" style="margin:0 0 12px;">Voucher đã lưu (${fn:length(savedVouchersFull)})</div>
        <c:if test="${empty savedVouchersFull}">
          <p style="color:var(--ink-soft);font-size:.9rem;">Bạn chưa lưu voucher nào. Ghé trang chủ để lưu mã ưu đãi nhé!</p>
        </c:if>
        <div class="row g-3">
          <c:forEach var="v" items="${savedVouchersFull}">
            <div class="col-md-6">
              <div class="voucher-card" style="${v.status != 'ACTIVE' ? 'opacity:.55;' : ''}">
                <div class="voucher-notch-top"></div>
                <div class="voucher-notch-bottom"></div>
                <div class="voucher-stub">
                  <c:choose>
                    <c:when test="${v.discountType == 'PERCENTAGE'}">
                      <div class="pct">${v.discountValue}<small>%</small></div>
                    </c:when>
                    <c:otherwise>
                      <div class="pct"><fmt:formatNumber value="${v.discountValue / 1000}" maxFractionDigits="0"/><small>K</small></div>
                    </c:otherwise>
                  </c:choose>
                  <div class="off-label">Giảm</div>
                </div>
                <div class="voucher-body">
                  <div class="title">Đơn từ <fmt:formatNumber value="${v.minOrderValue}" pattern="#,###"/>đ</div>
                  <div class="cond">
                    <c:choose>
                      <c:when test="${v.status == 'ACTIVE'}">Còn hiệu lực đến <fmt:formatDate value="${v.endDate}" pattern="dd/MM/yyyy"/></c:when>
                      <c:when test="${v.status == 'EXPIRED'}">Đã hết hạn</c:when>
                      <c:otherwise>Ngừng áp dụng</c:otherwise>
                    </c:choose>
                  </div>
                  <div class="voucher-footer">
                    <span class="voucher-code">${v.code}</span>
                  </div>
                </div>
              </div>
            </div>
          </c:forEach>
        </div>
      </div>

    </div>
  </div>
</div>

<script>
function showAccountTab(e, name){
  e.preventDefault();
  document.querySelectorAll('.account-tab-list a').forEach(a=>a.classList.remove('active'));
  e.currentTarget.classList.add('active');
  document.querySelectorAll('.account-tab-panel').forEach(p=>p.style.display='none');
  document.getElementById('acc-'+name).style.display='block';
}
function toggleAddAddressForm(){
  var f = document.getElementById('addAddressForm');
  f.style.display = (f.style.display === 'none' || f.style.display === '') ? 'block' : 'none';
}

// MỚI: mở modal "Sửa địa chỉ", đổ dữ liệu hiện có của địa chỉ vào form.
// Đọc qua data-* thay vì nhét thẳng vào onclick("...") để khỏi vỡ chuỗi
// khi tên/địa chỉ chứa dấu nháy đơn/kép.
document.querySelectorAll('.edit-address-btn').forEach(function(btn){
  btn.addEventListener('click', function(){
    document.getElementById('edit_addressId').value = btn.dataset.addressId;
    document.getElementById('edit_receiverName').value = btn.dataset.receiverName;
    document.getElementById('edit_receiverPhone').value = btn.dataset.receiverPhone;
    document.getElementById('edit_fullAddress').value = btn.dataset.fullAddress;
    document.getElementById('editAddressModal').classList.add('open');
  });
});
</script>

<!-- Footer -->
<jsp:include page="assets/footer.jsp"></jsp:include>

</body>
</html>
