<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Sản phẩm - Manager | Booky Mart</title>
<link href="https://cdnjs.cloudflare.com/ajax/libs/twitter-bootstrap/5.3.3/css/bootstrap.min.css" rel="stylesheet">
<link href="https://fonts.googleapis.com/css2?family=Fraunces:opsz,wght@9..144,400;9..144,600;9..144,700&family=Inter:wght@400;500;600;700&family=JetBrains+Mono:wght@500&display=swap" rel="stylesheet">
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css">
</head>
<body>

<%@ include file="/WEB-INF/jspf/icons.jspf" %>

<div class="admin-shell">
  <div class="admin-sidebar">
    <a class="brand" href="${pageContext.request.contextPath}/home">Booky<span class="dot">.</span>Mart</a>
    <div class="group-label">Manager Panel</div>
      <a href="${pageContext.request.contextPath}/manager/dashboard" class=""><svg class="icon"><use href="#ic-dashboard"/></svg> Tổng quan & Báo cáo</a>
      <a href="${pageContext.request.contextPath}/manager/products" class="active"><svg class="icon"><use href="#ic-package"/></svg> Sản phẩm </a>
      <a href="${pageContext.request.contextPath}/manager/promotions"" class=""><svg class="icon"><use href="#ic-percent"/></svg> Khuyến mãi</a>
    <div class="group-label">Khác</div>
      <a href="${pageContext.request.contextPath}/home"><svg class="icon"><use href="#ic-arrow-right"/></svg> Về trang khách hàng</a>
      <a href="${pageContext.request.contextPath}/logout" style="color:#E29B8A;"><svg class="icon"><use href="#ic-logout"/></svg> Đăng xuất</a>
  </div>

  <div class="admin-main">
    <div class="admin-topbar">
      <div style="font-weight:700;color:var(--ink);">Sản phẩm</div>
      <div class="d-flex align-items-center gap-3">
        <button class="icon-btn"><svg class="icon"><use href="#ic-bell"/></svg></button>
        <%@ include file="/WEB-INF/jspf/account-info.jspf" %>
      </div>
    </div>
    <div class="admin-content">
      <div class="page-title-row">
        <div>
          <h1>Sản phẩm</h1>
          <div class="sub">Quản lý danh mục sản phẩm</div>
        </div>
        <!-- MỚI: nút Thêm sản phẩm thay cho link Nhập kho theo lô cũ -->

      </div>

      <div id="prod-list" style="padding:0;">
       <div class="admin-toolbar">
          <form class="admin-search-box" method="get" action="${pageContext.request.contextPath}/manager/products" style="display:flex;gap:10px;">
            <svg class="icon"><use href="#ic-search"/></svg>
            <input type="text" name="keyword" value="${keyword}" placeholder="Tìm theo SKU, tên sản phẩm..." style="color:white">
            <select name="type" class="sort-select" onchange="this.form.submit()">
              <option value="" ${empty typeFilter ? 'selected' : ''}>Tất cả loại</option>
              <option value="BOOK" ${typeFilter == 'BOOK' ? 'selected' : ''}>Sách</option>
              <option value="STATIONERY" ${typeFilter == 'STATIONERY' ? 'selected' : ''}>Văn phòng phẩm</option>
            </select>
          </form>
          
          <a href="${pageContext.request.contextPath}/manager/products?export=excel&keyword=${keyword}" class="btn-outline-ink btn-sm-shop">
  <svg class="icon" style="width:14px;vertical-align:-2px;"><use href="#ic-package"/></svg> Xuất Excel
</a>
          <button type="button" class="btn-terracotta btn-sm-shop" onclick="openAddModal()">
            <svg class="icon" style="width:14px;vertical-align:-2px;"><use href="#ic-plus"/></svg> Thêm sản phẩm
          </button>
          
          
        </div>

        <table class="table-shop">
          <thead><tr><th>Sản phẩm</th><th>SKU</th><th>Loại</th><th>Giá bán</th><th>Tồn kho</th><th>Trạng thái</th><th></th></tr></thead>
          <tbody>
            <c:forEach var="p" items="${products}">
              <tr style="${p.status == 'DISCONTINUED' ? 'opacity:.6;' : ''}">
                <td><div class="d-flex align-items-center gap-2">
                  <div class="table-thumb ${p.productType == 'BOOK' ? 'thumb-fiction' : 'thumb-vpp'}">
                    <svg class="icon" style="width:18px;"><use href="${p.productType == 'BOOK' ? '#ic-book-open' : '#ic-book'}"/></svg>
                  </div> ${p.name}
                </div></td>
                <td class="mono">${p.sku}</td>
                <td>${p.productType == 'BOOK' ? 'Sách' : 'VPP'}</td>
                <td class="mono">${p.status == 'DISCONTINUED' ? '—' : p.originalPrice}đ</td>
                <td class="mono">${p.stockQuantity}</td>
                <td><span class="status-badge ${p.status == 'ACTIVE' ? 'status-delivered' : 'status-cancelled'}">${p.status}</span></td>
                <td>
                  <c:if test="${p.status == 'ACTIVE'}">
                    <button type="button" class="row-action-btn edit-product-btn"
                      data-id="${p.productId}" data-type="${p.productType}" data-cat="${p.categoryId}" data-sku="${p.sku}"
                 data-name="${p.name}" data-price="<fmt:formatNumber value='${p.priceBeforeTax}' pattern='0' groupingUsed='false'/>" data-weight="${p.weightGrams}"
                      data-quantity="${p.stockQuantity}" data-unitcost="${p.unitCost}"
                      data-isbn="${p.isbn}" data-brandorauthor="${p.brandorauthor}" data-publisher="${p.publisher}"
                      data-year="${p.publicationYear}" data-specs="${p.specifications}"
                      data-image="${p.images}" data-description="${p.description}">
                      <svg class="icon"><use href="#ic-edit"/></svg>
                    </button>
                    <form method="post" action="${pageContext.request.contextPath}/manager/products" style="display:inline"
                          onsubmit="return confirm('Xóa sản phẩm \'${p.name}\'?');">
                      <input type="hidden" name="action" value="delete">
                      <input type="hidden" name="productId" value="${p.productId}">
                      <button type="submit" class="row-action-btn"><svg class="icon"><use href="#ic-trash"/></svg></button>
                    </form>
                  </c:if>
                  <c:if test="${p.status != 'ACTIVE'}">
                    <button type="button" class="row-action-btn"><svg class="icon"><use href="#ic-eye"/></svg></button>
                  </c:if>
                </td>
              </tr>
            </c:forEach>
          </tbody>
        </table>
          <c:if test="${totalPages > 1}">
  <div class="d-flex justify-content-center gap-2 mt-3">
    <c:forEach begin="1" end="${totalPages}" var="i">
      <a href="?keyword=${keyword}&type=${typeFilter}&page=${i}"
         class="btn-sm-shop ${i == currentPage ? 'btn-terracotta' : 'btn-outline-ink'}">${i}</a>
    </c:forEach>
  </div>
</c:if>
      </div>

      <!-- Modal Sửa sản phẩm — cấu trúc y hệt form Thêm, chỉ khác SKU readonly -->
     <div class="modal-confirm-backdrop" id="editProductModal">
  <div class="modal-confirm-box" style="text-align:left;max-width:520px;">
    <h3 style="text-align:center;">Sửa sản phẩm</h3>
    <form id="editProductForm" method="post" action="${pageContext.request.contextPath}/manager/products"
          enctype="multipart/form-data" onsubmit="return validateProductForm();">
      <input type="hidden" name="action" value="update">
      <input type="hidden" name="productId" id="ep_id">

      <label class="form-label-shop">SKU</label>
      <input class="form-control-shop mb-3" id="ep_sku" readonly disabled
             style="background:var(--paper-deep);color:var(--ink-soft);">

      <label class="form-label-shop">Loại sản phẩm</label>
      <select class="form-select-shop mb-3" name="type" id="ep_type" onchange="toggleProductTypeFields('ep')">
        <option value="BOOK">Sách (Book)</option>
        <option value="STATIONERY">Văn phòng phẩm (Stationery)</option>
      </select>

      <label class="form-label-shop">Tên sản phẩm</label>
      <input class="form-control-shop mb-1" name="name" id="ep_name" placeholder="Tên sản phẩm" required>
      <div class="field-error" id="ep_name_error"></div>

      <div class="row g-2 mb-1">
        <div class="col-6">
          <label class="form-label-shop">Giá bán (trước thuế)</label>
          <input class="form-control-shop" name="price" id="ep_price" placeholder="₫" required
                 oninput="updateTaxedPricePreview('ep')">
          <small id="ep_price_taxed" style="display:block;margin-top:4px;color:var(--ink-soft);font-size:.78rem;"></small>
          <div class="field-error" id="ep_price_error"></div>
        </div>
        <div class="col-6">
          <label class="form-label-shop">Cân nặng (gram)</label>
          <input class="form-control-shop" name="weight" id="ep_weight" placeholder="g" required>
          <div class="field-error" id="ep_weight_error"></div>
        </div>
      </div>

      <div class="row g-2 mb-3">
        <div class="col-6">
          <label class="form-label-shop">Số lượng tồn kho</label>
          <input class="form-control-shop" name="quantity" id="ep_quantity" placeholder="0" required>
          <div class="field-error" id="ep_quantity_error"></div>
        </div>
        <div class="col-6">
          <label class="form-label-shop">Giá vốn</label>
          <input class="form-control-shop" name="unitCost" id="ep_unitCost" placeholder="₫" required>
          <div class="field-error" id="ep_unitCost_error"></div>
        </div>
      </div>

      <label class="form-label-shop">Danh mục</label>
      <select class="form-select-shop mb-3" name="categoryId" id="ep_cat">
        <c:forEach var="cat" items="${categories}">
          <option value="${cat.categoryId}">${cat.name}</option>
        </c:forEach>
      </select>

      <label class="form-label-shop">Ảnh sản phẩm</label>
      <div class="mb-3">
        <img id="ep_image_preview" src="" alt="" style="display:none;width:80px;height:80px;object-fit:cover;border-radius:8px;margin-bottom:8px;">
        <input class="form-control-shop" type="file" id="ep_image" name="image" accept="image/jpeg,image/png">
        <small style="display:block;margin-top:4px;color:var(--ink-soft);font-size:.78rem;">Bỏ trống nếu không muốn đổi ảnh. Chỉ nhận .jpg/.png, tối đa 2MB.</small>
      </div>

      <label class="form-label-shop">Mô tả sản phẩm</label>
      <textarea class="form-control-shop mb-1" id="ep_description" name="description" rows="4" maxlength="2000" required
                placeholder="Mô tả ngắn gọn về sản phẩm (tối đa 2000 ký tự)..."></textarea>
      <div class="field-error" id="ep_description_error"></div>

      <div id="ep_book_fields">
        <label class="form-label-shop">ISBN</label>
        <input class="form-control-shop mb-1" name="isbn" id="ep_isbn" required>
        <div class="field-error" id="ep_isbn_error"></div>

        <label class="form-label-shop">Tác giả</label>
        <input class="form-control-shop mb-1" name="author" id="ep_author" required>
        <div class="field-error" id="ep_author_error"></div>

        <label class="form-label-shop">Nhà xuất bản</label>
        <input class="form-control-shop mb-1" name="publisher" id="ep_publisher" required>
        <div class="field-error" id="ep_publisher_error"></div>

        <label class="form-label-shop">Năm xuất bản</label>
        <input class="form-control-shop mb-1" name="publicationYear" id="ep_year" required>
        <div class="field-error" id="ep_year_error"></div>
      </div>
      <div id="ep_stationery_fields" style="display:none;">
        <label class="form-label-shop">Thương hiệu</label>
        <input class="form-control-shop mb-1" name="brand" id="ep_brand" required>
        <div class="field-error" id="ep_brand_error"></div>

        <label class="form-label-shop">Thông số</label>
        <input class="form-control-shop mb-1" name="specifications" id="ep_specs" required>
        <div class="field-error" id="ep_specs_error"></div>
      </div>

      <div class="d-flex gap-2 mt-2">
        <button type="button" class="btn-outline-ink btn-block-shop" onclick="document.getElementById('editProductModal').classList.remove('open')">Hủy</button>
        <button type="submit" class="btn-terracotta btn-block-shop">Lưu thay đổi</button>
      </div>
    </form>
  </div>
</div>

<!-- Modal Thêm sản phẩm mới -->
<div class="modal-confirm-backdrop" id="addProductModal">
  <div class="modal-confirm-box" style="text-align:left;max-width:520px;">
    <h3 style="text-align:center;">Thêm sản phẩm</h3>
    <form id="addProductForm" method="post" action="${pageContext.request.contextPath}/manager/products"
          enctype="multipart/form-data" onsubmit="return validateAddProductForm();">
      <input type="hidden" name="action" value="create">

      <label class="form-label-shop">SKU</label>
      <input class="form-control-shop mb-1" name="sku" id="ap_sku" placeholder="VD: BK-1022 hoặc ST-2015" required>
      <div class="field-error" id="ap_sku_error"></div>

      <label class="form-label-shop">Loại sản phẩm</label>
      <select class="form-select-shop mb-3" name="type" id="ap_type" onchange="toggleProductTypeFields('ap')">
        <option value="BOOK">Sách (Book)</option>
        <option value="STATIONERY">Văn phòng phẩm (Stationery)</option>
      </select>

      <label class="form-label-shop">Tên sản phẩm</label>
      <input class="form-control-shop mb-1" name="name" id="ap_name" placeholder="Tên sản phẩm" required>
      <div class="field-error" id="ap_name_error"></div>

      <div class="row g-2 mb-1">
        <div class="col-6">
          <label class="form-label-shop">Giá bán (trước thuế)</label>
          <input class="form-control-shop" name="price" id="ap_price" placeholder="₫" required
                 oninput="updateTaxedPricePreview('ap')">
          <small id="ap_price_taxed" style="display:block;margin-top:4px;color:var(--ink-soft);font-size:.78rem;"></small>
          <div class="field-error" id="ap_price_error"></div>
        </div>
        <div class="col-6">
          <label class="form-label-shop">Cân nặng (gram)</label>
          <input class="form-control-shop" name="weight" id="ap_weight" placeholder="g" required>
          <div class="field-error" id="ap_weight_error"></div>
        </div>
      </div>

      <div class="row g-2 mb-3">
        <div class="col-6">
          <label class="form-label-shop">Số lượng tồn kho</label>
          <input class="form-control-shop" name="quantity" id="ap_quantity" placeholder="0" required>
          <div class="field-error" id="ap_quantity_error"></div>
        </div>
        <div class="col-6">
          <label class="form-label-shop">Giá vốn</label>
          <input class="form-control-shop" name="unitCost" id="ap_unitCost" placeholder="₫" required>
          <div class="field-error" id="ap_unitCost_error"></div>
        </div>
      </div>

      <label class="form-label-shop">Danh mục</label>
      <select class="form-select-shop mb-3" name="categoryId" id="ap_cat">
        <c:forEach var="cat" items="${categories}">
          <option value="${cat.categoryId}">${cat.name}</option>
        </c:forEach>
      </select>

      <label class="form-label-shop">Ảnh sản phẩm</label>
      <div class="mb-3">
        <input class="form-control-shop" type="file" id="ap_image" name="image" accept="image/jpeg,image/png">
        <small style="display:block;margin-top:4px;color:var(--ink-soft);font-size:.78rem;">Có thể bỏ trống, thêm ảnh sau qua nút Sửa. Chỉ nhận .jpg/.png, tối đa 2MB.</small>
      </div>

      <label class="form-label-shop">Mô tả sản phẩm</label>
      <textarea class="form-control-shop mb-1" id="ap_description" name="description" rows="4" maxlength="2000" required
                placeholder="Mô tả ngắn gọn về sản phẩm (tối đa 2000 ký tự)..."></textarea>
      <div class="field-error" id="ap_description_error"></div>

      <div id="ap_book_fields">
        <label class="form-label-shop">ISBN</label>
        <input class="form-control-shop mb-1" name="isbn" id="ap_isbn" required>
        <div class="field-error" id="ap_isbn_error"></div>

        <label class="form-label-shop">Tác giả</label>
        <input class="form-control-shop mb-1" name="author" id="ap_author" required>
        <div class="field-error" id="ap_author_error"></div>

        <label class="form-label-shop">Nhà xuất bản</label>
        <input class="form-control-shop mb-1" name="publisher" id="ap_publisher" required>
        <div class="field-error" id="ap_publisher_error"></div>

        <label class="form-label-shop">Năm xuất bản</label>
        <input class="form-control-shop mb-1" name="publicationYear" id="ap_year" required>
        <div class="field-error" id="ap_year_error"></div>
      </div>
      <div id="ap_stationery_fields" style="display:none;">
        <label class="form-label-shop">Thương hiệu</label>
        <input class="form-control-shop mb-1" name="brand" id="ap_brand" required>
        <div class="field-error" id="ap_brand_error"></div>

        <label class="form-label-shop">Thông số</label>
        <input class="form-control-shop mb-1" name="specifications" id="ap_specs" required>
        <div class="field-error" id="ap_specs_error"></div>
      </div>

      <div class="d-flex gap-2 mt-2">
        <button type="button" class="btn-outline-ink btn-block-shop" onclick="document.getElementById('addProductModal').classList.remove('open')">Hủy</button>
        <button type="submit" class="btn-terracotta btn-block-shop">Thêm sản phẩm</button>
      </div>
    </form>
  </div>
</div>

<!-- TRƯỚC ĐÂY: thiếu hẳn phần tử này dù JS đã gọi document.getElementById('resultToast')
     -> lỗi "Cannot read properties of null" mỗi khi có flash message. Thêm lại
     đúng theo cấu trúc đã dùng ở manager-promotions.jsp. -->
<div class="result-toast" id="resultToast">
    <div class="rt-icon"><svg class="icon" style="width:18px;" id="resultToastIcon"><use href="#ic-check"/></svg></div>
    <div class="rt-text" id="resultToastText"></div>
</div>

<script>
// MỚI: bật/tắt required cho cả nhóm field (Sách / VPP) khi chuyển loại sản phẩm.
// Tránh xung đột giữa validate JS tự viết và validate required của trình duyệt
// (đặc biệt vì modal dùng position:fixed nên offsetParent luôn null -> check cũ sai).
function setGroupRequired(groupEl, isActive){
  groupEl.querySelectorAll('input, textarea').forEach(function(el){
    if (isActive) {
      el.setAttribute('required', 'required');
    } else {
      el.removeAttribute('required');
      el.classList.remove('invalid');
      const err = document.getElementById(el.id + '_error');
      if (err) { err.textContent = ''; err.classList.remove('show'); }
    }
  });
}

function toggleProductTypeFields(prefix){
  const type = document.getElementById(prefix + '_type').value;
  const bookFields = document.getElementById(prefix + '_book_fields');
  const stationeryFields = document.getElementById(prefix + '_stationery_fields');
  const isBook = type === 'BOOK';

  bookFields.style.display = isBook ? 'block' : 'none';
  stationeryFields.style.display = isBook ? 'none' : 'block';

  setGroupRequired(bookFields, isBook);
  setGroupRequired(stationeryFields, !isBook);
}

function updateTaxedPricePreview(prefix){
  const priceInput = document.getElementById(prefix + '_price');
  const hint = document.getElementById(prefix + '_price_taxed');
  const raw = parseFloat(priceInput.value.replace(/[.,]/g, ''));
  if (!raw || raw <= 0){ hint.textContent = ''; return; }
  const taxed = raw * 1.10;
  hint.textContent = 'Giá sau thuế: ' + taxed.toLocaleString('vi-VN') + 'đ';
}

function openEditModal(id, type, catId, sku, name, price, weight, quantity, unitCost, isbn, brandOrAuthor, publisher, year, specs, imageUrl, description){
  document.getElementById('ep_id').value = id;
  document.getElementById('ep_sku').value = sku;
  document.getElementById('ep_type').value = type;
  document.getElementById('ep_cat').value = catId;
  document.getElementById('ep_name').value = name;
  document.getElementById('ep_price').value = price;
  document.getElementById('ep_weight').value = weight;
  document.getElementById('ep_quantity').value = quantity;
  document.getElementById('ep_unitCost').value = unitCost;
  document.getElementById('ep_image').value = '';
  document.getElementById('ep_description').value = description;

  const preview = document.getElementById('ep_image_preview');
  if (imageUrl) {
    preview.src = '${pageContext.request.contextPath}/' + imageUrl;
    preview.style.display = 'block';
  } else {
    preview.style.display = 'none';
  }

  if (type === 'BOOK'){
    document.getElementById('ep_isbn').value = isbn;
    document.getElementById('ep_author').value = brandOrAuthor;
    document.getElementById('ep_publisher').value = publisher;
    document.getElementById('ep_year').value = year;
  } else {
    document.getElementById('ep_brand').value = brandOrAuthor;
    document.getElementById('ep_specs').value = specs;
  }
  toggleProductTypeFields('ep'); // sẽ tự bật/tắt required đúng theo loại sản phẩm
  updateTaxedPricePreview('ep');
  document.getElementById('editProductModal').classList.add('open');
}

document.querySelectorAll('.edit-product-btn').forEach(function(btn){
  btn.addEventListener('click', function(){
    const d = btn.dataset;
    openEditModal(d.id, d.type, d.cat, d.sku, d.name, d.price, d.weight, d.quantity, d.unitcost, d.isbn, d.brandorauthor, d.publisher, d.year, d.specs, d.image, d.description);
  });
});

function openAddModal(){
  document.getElementById('addProductForm').reset();
  document.getElementById('ap_sku_error').textContent = '';
  document.getElementById('ap_sku_error').classList.remove('show');
  document.getElementById('ap_sku').classList.remove('invalid');
  toggleProductTypeFields('ap');
  document.getElementById('ap_price_taxed').textContent = '';
  document.getElementById('addProductModal').classList.add('open');
}

// MỚI: check SKU trùng real-time qua AJAX, gọi khi rời khỏi ô SKU (blur)
let skuCheckTimer = null;
document.getElementById('ap_sku').addEventListener('blur', function(){
  const el = this;
  const val = el.value.trim();
  const errorEl = document.getElementById('ap_sku_error');
  if (!val) return;

  clearTimeout(skuCheckTimer);
  skuCheckTimer = setTimeout(function(){
    fetch('${pageContext.request.contextPath}/manager/products?checkSku=' + encodeURIComponent(val))
      .then(function(res){ return res.text(); })
      .then(function(text){
        if (text.trim() === 'EXISTS') {
          el.classList.add('invalid');
          errorEl.textContent = 'SKU này đã tồn tại, vui lòng chọn mã khác.';
          errorEl.classList.add('show');
        } else {
          el.classList.remove('invalid');
          errorEl.textContent = '';
          errorEl.classList.remove('show');
        }
      })
      .catch(function(){ /* lỗi mạng thì bỏ qua, server vẫn re-check lúc submit */ });
  }, 300);
});

function isValidISBN13(raw){
  const isbn = (raw || '').replace(/[-\s]/g, '');
  if (!/^\d{13}$/.test(isbn)) return false;
  let sum = 0;
  for (let i = 0; i < 13; i++){
    const digit = parseInt(isbn[i], 10);
    sum += (i % 2 === 0) ? digit : digit * 3;
  }
  return sum % 10 === 0;
}

// Validate 1 field, hiện/ẩn lỗi ngay tại chỗ
function validateField(el){
  const errorEl = document.getElementById(el.id + '_error');
  if (!errorEl) return true; // field không có khung lỗi (VD: danh mục, ảnh) thì bỏ qua

  let message = '';
  const val = el.value.trim();

  if (el.hasAttribute('required') && val === '') {
    message = 'Không được để trống.';
  } else if (el.id.endsWith('_isbn') && val !== '' && !isValidISBN13(val)) {
    message = 'ISBN không hợp lệ (sai checksum ISBN-13).';
  } else if (el.id.endsWith('_price') && val !== '' && (isNaN(parseFloat(val.replace(/[.,]/g,''))) || parseFloat(val.replace(/[.,]/g,'')) <= 0)) {
    message = 'Giá bán phải là số lớn hơn 0.';
  } else if (el.id.endsWith('_unitCost') && val !== '' && (isNaN(parseFloat(val.replace(/[.,]/g,''))) || parseFloat(val.replace(/[.,]/g,'')) < 0)) {
    message = 'Giá vốn phải là số từ 0 trở lên.';
  } else if (el.id.endsWith('_quantity') && val !== '' && (!/^\d+$/.test(val) || parseInt(val, 10) < 0)) {
    message = 'Số lượng phải là số nguyên từ 0 trở lên.';
  } else if (el.id === 'ap_sku' && val !== '' && !/^(BK|ST)-\d{4}$/.test(val)) {
    message = 'SKU phải đúng định dạng BK-XXXX (Sách) hoặc ST-XXXX (VPP), VD: BK-1022.';
  } else if (el.id.endsWith('_weight') && val !== '' && (isNaN(parseFloat(val)) || parseFloat(val) <= 0)) {
    message = 'Cân nặng phải là số lớn hơn 0.';
  } else if (el.id.endsWith('_year') && val !== '') {
    const y = parseInt(val, 10);
    const current = new Date().getFullYear();
    if (isNaN(y) || y < 1900 || y > current) {
      message = 'Năm xuất bản phải từ 1900 đến ' + current + '.';
    }
  } else if (el.id.endsWith('_description') && val.length > 2000) {
    message = 'Mô tả không được vượt quá 2000 ký tự.';
  } else if (el.id.endsWith('_name') && val.length > 200) {
    message = 'Tên sản phẩm không được vượt quá 200 ký tự.';
  } else if ((el.id.endsWith('_author') || el.id.endsWith('_publisher')) && val.length > 150) {
    message = 'Không được vượt quá 150 ký tự.';
  } else if (el.id.endsWith('_brand') && val.length > 100) {
    message = 'Thương hiệu không được vượt quá 100 ký tự.';
  } else if (el.id.endsWith('_specs') && val.length > 255) {
    message = 'Thông số không được vượt quá 255 ký tự.';
  }

  if (message) {
    el.classList.add('invalid');
    errorEl.textContent = message;
    errorEl.classList.add('show');
    return false;
  } else {
    el.classList.remove('invalid');
    errorEl.classList.remove('show');
    return true;
  }
}

// Gắn sự kiện validate real-time cho mọi input có required trong form Sửa
document.querySelectorAll('#editProductForm [required]').forEach(function(el){
  el.addEventListener('blur', function(){ validateField(el); });
  el.addEventListener('input', function(){
    if (el.classList.contains('invalid')) validateField(el);
  });
});

// Validate toàn bộ form trước khi submit.
// Giờ [required] chỉ còn tồn tại đúng ở các field đang thực sự hiển thị
// (nhờ setGroupRequired ở trên), nên không cần đoán bằng offsetParent nữa.
function validateProductForm(){
  try {
    const form = document.getElementById('editProductForm');
    let allValid = true;
    let firstInvalid = null;

    form.querySelectorAll('[required]').forEach(function(el){
      if (!validateField(el)) {
        allValid = false;
        if (!firstInvalid) firstInvalid = el;
      }
    });

    if (!allValid && firstInvalid) {
      firstInvalid.scrollIntoView({ behavior: 'smooth', block: 'center' });
      firstInvalid.focus();
      return false;
    }
    if (allValid) {
      return confirm('Xác nhận lưu thay đổi cho sản phẩm này?');
    }
    return allValid;
  } catch (err) {
    alert('Có lỗi JavaScript xảy ra: ' + err.message + '\nMở Console (F12) để xem chi tiết.');
    console.error(err);
    return false;
  }
}

// Gắn sự kiện validate real-time cho mọi input có required trong form Thêm

// Gắn sự kiện validate real-time cho mọi input có required trong form Thêm
document.querySelectorAll('#addProductForm [required]').forEach(function(el){
  el.addEventListener('blur', function(){ validateField(el); });
  el.addEventListener('input', function(){
    if (el.classList.contains('invalid')) validateField(el);
  });
});

function validateAddProductForm(){
  try {
    const form = document.getElementById('addProductForm');
    let allValid = true;
    let firstInvalid = null;

    form.querySelectorAll('[required]').forEach(function(el){
      if (!validateField(el)) {
        allValid = false;
        if (!firstInvalid) firstInvalid = el;
      }
    });

    // Chặn submit nếu SKU đang báo trùng (do lần check AJAX gần nhất)
    const skuEl = document.getElementById('ap_sku');
    if (skuEl.classList.contains('invalid')) {
      allValid = false;
      if (!firstInvalid) firstInvalid = skuEl;
    }

  if (!allValid && firstInvalid) {
      firstInvalid.scrollIntoView({ behavior: 'smooth', block: 'center' });
      firstInvalid.focus();
      return false;
    }
    if (allValid) {
      return confirm('Xác nhận thêm sản phẩm mới này vào hệ thống?');
    }
    return allValid;
  } catch (err) {
    alert('Có lỗi JavaScript xảy ra: ' + err.message + '\nMở Console (F12) để xem chi tiết.');
    console.error(err);
    return false;
  }
}

function showResultToast(text, isError){
  const toast = document.getElementById('resultToast');
  document.getElementById('resultToastText').textContent = text;
  toast.classList.toggle('error', !!isError);
  const iconUse = document.querySelector('#resultToastIcon use');
  iconUse.setAttribute('href', isError ? '#ic-alert-circle' : '#ic-check');
  toast.classList.add('show');
  setTimeout(()=> toast.classList.remove('show'), 3500);
}

<c:if test="${not empty flashMessage}">
window.addEventListener('DOMContentLoaded', function(){
  showResultToast('${flashMessage}', ${flashError});
});
</c:if>
</script>
</body>
</html>
