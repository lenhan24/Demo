<%-- 
    Document   : Home.jsp
    Created on : Jun 27, 2026, 6:13:00 PM
    Author     : GiGaByte
--%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="vi">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Booky Mart — Siêu thị sách &amp; văn phòng phẩm</title>
<link href="https://cdnjs.cloudflare.com/ajax/libs/bootstrap/5.3.3/css/bootstrap.min.css" rel="stylesheet">
<link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
<link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
<link href="https://fonts.googleapis.com/css2?family=Fraunces:opsz,wght@9..144,400;9..144,600;9..144,700&family=Inter:wght@400;500;600;700&family=JetBrains+Mono:wght@500&display=swap" rel="stylesheet">
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css">

</head>
<body>
 

<jsp:include page="assets/header.jsp" />

<%-- MỚI: banner rõ ràng khi tài khoản nội bộ (Staff/Manager/Admin) đang xem thử
     giao diện khách — trước đây không có dấu hiệu gì, dễ tưởng nhầm là bị đăng
     xuất khi thấy Giỏ hàng/Account Info biến mất khỏi header. --%>
<c:if test="${not empty sessionScope.userRole && sessionScope.userRole != 'CUSTOMER'}">
  <div style="background:var(--ink);color:#fff;text-align:center;padding:10px;font-size:.88rem;">
    Bạn đang xem thử giao diện khách hàng với vai trò <strong>${sessionScope.userRole}</strong> — không mua hàng được từ đây.
    <a href="${pageContext.request.contextPath}/${sessionScope.userRole == 'ADMIN' ? 'admin/roles' : (sessionScope.userRole == 'MANAGER' ? 'manager/products' : 'staff/orders')}"
       style="color:#fff;text-decoration:underline;font-weight:600;margin-left:6px;">Quay lại trang quản trị</a>
  </div>
</c:if>
 
<!-- Hero Dây như kiểu script giới thiệu webitre thôi có 3 cái sản phẩm ở kia như kiểu là besseller
  sẽ để để giới thiệu có thể sửa hoặc không-->
<section class="hero">
  <div class="container">
    <div class="row align-items-center">
      <div class="col-lg-6">
        <div class="hero-eyebrow">Tủ sách &amp; góc làm việc</div>
        <h1 class="mt-3">Mỗi trang sách,<br>mỗi nét bút -<br><em>đều đáng lưu giữ.</em></h1>
        <p class="lead-shop">Thế giới sách và văn phòng phẩm phong phú, giao hàng nhanh chóng tận tay bạn.</p>
        <div class="d-flex gap-3 mt-4">
          <a href="#" class="btn-terracotta" style="text-decoration:none;">Mua sắm ngay</a>
        
        </div>
      </div>
      <div class="col-lg-6">
        <div class="hero-stack">
          <div class="stack-card card-1">
  <img src="https://tgu.edu.vn/upload/images/1_nha%20gia%20kim.jpg" alt="Nhà Giả Kim" class="card-img">
  <div class="tag">Sách văn học</div>
  <div class="ttl">Nhà Giả Kim</div>
  
</div>

<div class="stack-card card-2">
  <img src="https://bizweb.dktcdn.net/thumb/1024x1024/100/567/082/products/de-men-50k-1.jpg?v=1747322973867" alt="Dế mèn phiêu lưu kí" class="card-img">
  <div class="tag">Sách thiếu nhi</div>
  <div class="ttl">Dế mèn phiêu lưu kí</div>
  
</div>

<div class="stack-card card-3">
  <img src="https://down-vn.img.susercontent.com/file/vn-11134207-7ras8-mbg5d7xigpcwca" alt="Hop but 2 tang" class="card-img">
  <div class="tag">Văn phòng phẩm</div>
  <div class="ttl">Hộp bút 2 tầng</div>
 
</div>
        </div>
      </div>
    </div>
  </div>
</section>

<section class="section-pad pb-0" id="voucher-section">
  <div class="container">
    <div class="eyebrow-line" style="min-width:160px;"><span>Ưu đãi hôm nay</span><div class="ln"></div></div>
    <div class="d-flex justify-content-between align-items-end mb-4">
      <h2 class="section-title">Bóc mã, giảm liền tay</h2>
      <span class="mono" style="font-size:.82rem;color:var(--ink-soft);">
        <%-- Hiển thị số lượng mã đã lưu / tổng số mã --%>
        Đã lưu <strong style="color:var(--terracotta);">${fn:length(savedVoucherIds)}</strong>/${fn:length(voucherList)} mã
      </span>
    </div>

    <div class="row g-4">
      <%-- Lặp qua danh sách voucherList được gửi từ Servlet --%>
      <c:forEach items="${voucherList}" var="v" varStatus="vs">
        <div class="col-md-6 col-lg-3 voucher-extra-item" style="${vs.index >= 4 ? 'display:none;' : ''}">
          <div class="voucher-card">
            <div class="voucher-notch-top"></div>
            <div class="voucher-notch-bottom"></div>
            <div class="voucher-stub">
              <%-- Xử lý hiển thị % hoặc số tiền (Ví dụ: 30% hoặc 50K) --%>
              <c:choose>
                <c:when test="${v.discountType == 'PERCENTAGE'}">
                  <div class="pct">${v.discountValue}<small>%</small></div>
                </c:when>
                <c:otherwise>
                  <%-- Hiển thị dạng K (ví dụ 50.000 -> 50K) --%>
                  <div class="pct">
                    <fmt:formatNumber value="${v.discountValue / 1000}" maxFractionDigits="0"/>
                    <small>K</small>
                  </div>
                </c:otherwise>
              </c:choose>
              <div class="off-label">Giảm</div>
            </div>
            
            <div class="voucher-body">
              <%-- Tiêu đề mã (Dùng mã code làm tiêu đề hoặc bạn có thể tùy chỉnh) --%>
              <div class="title">Ưu đãi giảm giá ${v.code}</div>
              
              <%-- Xử lý điều kiện đơn tối thiểu và giảm tối đa --%>
              <div class="cond">
                Đơn từ <fmt:formatNumber value="${v.minOrderValue}" pattern="#,###"/>đ
                <c:choose>
                  <c:when test="${not empty v.maxDiscountCap}">
                    · Tối đa <fmt:formatNumber value="${v.maxDiscountCap}" pattern="#,###"/>đ
                  </c:when>
                  <c:otherwise>
                    · Không giới hạn
                  </c:otherwise>
                </c:choose>
              </div>
              
              <div class="voucher-footer">
                <span class="voucher-code">${v.code}</span>
                
                <%-- Kiểm tra xem voucherId này đã nằm trong danh sách savedVoucherIds hay chưa --%>
                <c:set var="isSaved" value="false" />
                <c:forEach items="${savedVoucherIds}" var="savedId">
                  <c:if test="${savedId == v.voucherId}">
                    <c:set var="isSaved" value="true" />
                  </c:if>
                </c:forEach>
                
                <%-- Render nút tương ứng --%>
                <c:choose>
                  <c:when test="${isSaved}">
                    <button class="voucher-save-btn saved" disabled>
                      <svg class="icon"><use href="#ic-check"/></svg> Đã lưu
                    </button>
                  </c:when>
                  <c:otherwise>
                    <a href="saveVoucher?voucherId=${v.voucherId}" class="voucher-save-btn" style="text-decoration: none; text-align: center; display: inline-block;">
      Lưu mã
    </a>
                  </c:otherwise>
                </c:choose>
              </div>
            </div>
          </div>
        </div>
      </c:forEach>
      
    </div>

    <c:if test="${fn:length(voucherList) > 4}">
      <div class="text-center mt-3">
        <button type="button" class="btn-outline-ink btn-sm-shop" id="btnShowMoreVouchers" onclick="toggleMoreVouchers()">
          Xem thêm (${fn:length(voucherList) - 4} mã)
        </button>
      </div>
    </c:if>
    <script>
      function toggleMoreVouchers(){
        var extras = document.querySelectorAll('.voucher-extra-item');
        var btn = document.getElementById('btnShowMoreVouchers');
        var expanded = btn.dataset.expanded === 'true';
        extras.forEach(function(el){ el.style.display = expanded ? 'none' : ''; });
        btn.dataset.expanded = expanded ? 'false' : 'true';
        btn.textContent = expanded ? 'Xem thêm (${fn:length(voucherList) - 4} mã)' : 'Thu gọn';
      }
    </script>
  </div>
</section>
 
<!-- Categories  chỗ này tương tự cũng đổ dữ liệu -->
<section class="section-pad">
  <div class="container">
    <div class="eyebrow-line"><span>Danh mục</span><div class="ln"></div></div>
    <h2 class="section-title mb-4">Bạn đang tìm gì hôm nay?</h2>

<div class="row g-3">
  <c:forEach items="${categories}" var="c">
    <div class="col-6 col-md-2">
      <a href="category?cid=${c.categoryId}" class="cat-card" style="text-decoration: none; color: inherit;">
        <div class="cat-icon">
            <c:choose>
                <c:when test="${c.name eq 'Tiểu thuyết & Văn học'}">
                    <svg class="icon"><use href="#ic-book"/></svg>
                </c:when>
                <c:when test="${c.name eq 'Sách Kỹ năng & Kinh doanh'}">
                    <svg class="icon"><use href="#ic-grad"/></svg>
                </c:when>
                <c:when test="${c.name eq 'Sách Thiếu nhi'}">
                    <svg class="icon"><use href="#ic-child"/></svg>
                </c:when>
                <c:when test="${c.name eq 'Bút & Mực'}">
                    <svg class="icon"><use href="#ic-pen"/></svg>
                </c:when>
                <c:when test="${c.name eq 'Sổ tay & Giấy in'}">
                    <svg class="icon"><use href="#ic-note"/></svg>
                </c:when>
                <c:when test="${c.name eq 'Dụng cụ văn phòng'}">
                    <svg class="icon"><use href="#ic-briefcase"/></svg>
                </c:when>
                <c:otherwise>
                    <svg class="icon"><use href="#ic-grid"/></svg>
                </c:otherwise>
            </c:choose>
        </div>
        <div class="name">${c.name}</div>
        <div class="count">${c.productCount} sản phẩm</div>
      </a>
    </div>
  </c:forEach>
</div>
  </div>
  </div>
</section>
 
<!-- Best sellers cũng đổ dữ liệu để hiển thị khi tính toán đc sản phẩm là bestseller -->
<section class="section-pad pt-0">
  <div class="container">
    <div class="d-flex justify-content-between align-items-end mb-2">
      <div>
        <div class="eyebrow-line" style="min-width:160px;"><span>Bán chạy</span><div class="ln"></div></div>
        <h2 class="section-title">Được yêu thích nhất tuần này</h2>
      </div>
      <a href="search?keyword=" class="nav-link-shop" style="border:1px solid var(--line);border-radius:8px;">Xem tất cả <svg class="icon"><use href="#ic-arrow-right"/></svg></a>
    </div>
 

<div class="row g-4 mt-3">
  <c:forEach items="${products}" var="p">
    <div class="col-6 col-md-3">
      <div class="product-card">
        <div class="product-thumb thumb-fiction">
          <span class="badge-new">Mới</span>
          <button class="wish-btn"><svg class="icon"><use href="#ic-heart"/></svg></button>
          <c:choose>
              <c:when test="${not empty p.thumbnailUrl}">
                  <img src="${p.thumbnailUrl}" alt="${p.name}" class="card-img-fit" />
              </c:when>
              <c:otherwise>
                  <svg class="icon"><use href="#ic-book-open"/></svg>
              </c:otherwise>
          </c:choose>
        </div>
        <div class="product-body">
          <div class="product-cat">${p.categoryName}</div>
          <div class="product-name">${p.name}</div>
          <div class="product-author">${p.authorOrBrand != null ? p.authorOrBrand : 'Đang cập nhật'}</div>
          <div class="stars">★★★★★ <span class="count"></span></div>
          <div class="price-row">
            <div>
              <span class="price-now">
                <fmt:formatNumber value="${p.price}" type="currency" pattern="###,###" />đ
              </span>
            </div>
              <a href="productdt?id=${p.productId}" class="add-btn"><svg class="icon"><use href="#ic-plus"/></svg></a>
          </div>
        </div>
      </div>
    </div>
  </c:forEach>
</div>
  </div>
</section>
 
<!-- Promo strip cái này cũng đổ dữ liệu, nó kiểu là 1 loại promotion đặc biệt ấy, promotion khác với voucher nha -->
<section class="container section-pad pt-0">
  <div class="promo-strip">
    <div>
      <h3>Lấy mã giảm giá ngay!</h3>
      <p>Chỉ một cú click để nhận ưu đãi độc quyền cho đơn hàng của bạn.</p>
    </div>
      <a href="#" class="btn-terracotta flex-shrink-0" style="text-decoration:none;">Săn ưu đãi</a>
  </div>
</section>
 
<!-- Stationery row  cũng cần đổ dữ liệu còn đây chỉ là demo-->
<section class="section-pad pt-0">
  <div class="container">
    <div class="eyebrow-line" style="min-width:200px;"><span>Văn phòng phẩm</span><div class="ln"></div></div>
    <h2 class="section-title mb-4">Góc học tập gọn gàng hơn</h2>

<div class="row g-4">
  <c:forEach items="${officeSupplies}" var="p">
    <div class="col-6 col-md-3">
      <div class="product-card">
        <div class="product-thumb">
          <c:if test="${not empty p.thumbnailUrl}">
             <img src="${p.thumbnailUrl}" style="width:100%; height:200px; object-fit:contain;" alt="${p.name}"/>
          </c:if>
          <button class="wish-btn"><svg class="icon"><use href="#ic-heart"/></svg></button>
        </div>
        
        <div class="product-body">
          <div class="product-cat">${p.categoryName}</div>
          <div class="product-name">${p.name}</div>
          <div class="product-author">${p.authorOrBrand}</div>
          <div class="stars">★★★★★ <span class="count">(0)</span></div>
          <div class="price-row">
            <div>
              <span class="price-now">
                 <fmt:formatNumber value="${p.price}" type="currency" pattern="###,###" />đ
              </span>
            </div>
              <a href="productdt?id=${p.productId}" class="add-btn"><svg class="icon"><use href="#ic-plus"/></svg></a>
          </div>
        </div>
      </div>
    </div>
  </c:forEach>
</div>
  </div>
</section>
 
<!-- Newsletter  điều hướng đến cửa sỏ đăng kí-->
<section class="container section-pad pt-0">
  <div class="newsletter" style="background: #1e2632; padding: 40px; border-radius: 20px; display: flex; align-items: center; justify-content: space-around; flex-wrap: wrap; gap: 20px;">
    
    <!-- Thêm mô tả để khối trông đầy đặn hơn -->
    <div style="color: #fff; max-width: 500px;">
      <h3 style="margin-bottom: 10px;">Tham gia cộng đồng Booky Mart</h3>
      <p style="margin: 0; opacity: 0.9;">Đăng ký ngay để nhận ưu đãi độc quyền và cập nhật những cuốn sách mới nhất mỗi tuần!</p>
    </div>
    
    <!-- Nút đăng ký -->
  <a href="SignUp.jsp" class="btn-letter">
    Đăng ký ngay
</a>
  </div>
</section>
 
<!-- Footer -->
<jsp:include page="assets/footer.jsp"></jsp:include>

</body>
</html>
