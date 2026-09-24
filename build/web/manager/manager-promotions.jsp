<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Khuyến mãi — Manager | Booky Mart</title>
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
                <a href="${pageContext.request.contextPath}/manager/products" class=""><svg class="icon"><use href="#ic-package"/></svg> Sản phẩm</a>
                <a href="${pageContext.request.contextPath}/manager/promotions" class="active"><svg class="icon"><use href="#ic-percent"/></svg> Khuyến mãi</a>
                <div class="group-label">Khác</div>
                <a href="${pageContext.request.contextPath}/home"><svg class="icon"><use href="#ic-arrow-right"/></svg> Về trang khách hàng</a>
                <a href="${pageContext.request.contextPath}/logout" style="color:#E29B8A;"><svg class="icon"><use href="#ic-logout"/></svg> Đăng xuất</a>
            </div>

            <div class="admin-main">
                <div class="admin-topbar">
                    <div style="font-weight:700;color:var(--ink);">Khuyến mãi</div>
                    <div class="d-flex align-items-center gap-3">
                        <button class="icon-btn"><svg class="icon"><use href="#ic-bell"/></svg></button>
                        <%@ include file="/WEB-INF/jspf/account-info.jspf" %>
                    </div>
                </div>

                <div class="admin-content">
                    <div class="page-title-row">
                        <div>
                            <h1>Voucher</h1>
                            <div class="sub">Quản lý mã giảm giá áp dụng cho đơn hàng</div>
                        </div>
                        <a href="${pageContext.request.contextPath}/manager/promotions?export=excel" class="btn-outline-ink btn-sm-shop">Xuất Excel</a>
                        <button class="btn-terracotta btn-sm-shop" onclick="document.getElementById('newVoucherModal').classList.add('open')">
                            <svg class="icon" style="width:14px;vertical-align:-2px;"><use href="#ic-plus"/></svg> Tạo voucher
                        </button>
                    </div>

                    <table class="table-shop">
                        <thead>
                            <tr>
                                <th>Mã</th><th>Loại giảm</th><th>Giá trị</th><th>Đơn tối thiểu</th>
                                <th>Hiệu lực</th><th>Giới hạn dùng</th><th>Trạng thái</th><th></th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="v" items="${vouchers}">
                                <tr style="${v.status == 'INACTIVE' ? 'opacity:.6;' : ''}">
                                    <td class="mono">${v.code}</td>
                                    <td>${v.discountType == 'PERCENTAGE' ? 'Theo %' : 'Số tiền cố định'}</td>
                                    <td class="mono">
                                        ${v.discountType == 'PERCENTAGE' ? v.discountValue.intValue() : v.discountValue}${v.discountType == 'PERCENTAGE' ? '%' : 'đ'}
                                        <c:if test="${v.discountType == 'PERCENTAGE' and v.maxDiscountCap != null}">
                                            <br><span style="font-size:.72rem;color:var(--ink-soft);">(tối đa ${v.maxDiscountCap}đ)</span>
                                        </c:if>
                                    </td>
                                    <td class="mono">${v.minOrderValue}đ</td>
                                    <td style="font-size:.78rem;">
                                        <fmt:formatDate value="${v.startDate}" pattern="dd/MM/yyyy"/> —
                                        <fmt:formatDate value="${v.endDate}" pattern="dd/MM/yyyy"/>
                                    </td>
                                    <td class="mono">${v.perCustomerLimit}/khách · ${v.maxUsageLimit} tổng</td>
                                    <td><span class="status-badge ${v.status == 'ACTIVE' ? 'status-delivered' : 'status-cancelled'}">${v.status}</span></td>
                                    <td>
                                        <button type="button" class="row-action-btn edit-voucher-btn"
                                                data-id="${v.voucherId}" data-code="${v.code}" data-type="${v.discountType}"
                                                data-value="${v.discountValue}" data-cap="${v.maxDiscountCap}" data-min="${v.minOrderValue}"
                                                data-start="<fmt:formatDate value="${v.startDate}" pattern="yyyy-MM-dd'T'HH:mm"/>"
                                                data-end="<fmt:formatDate value="${v.endDate}" pattern="yyyy-MM-dd'T'HH:mm"/>"
                                                data-maxusage="${v.maxUsageLimit}" data-percustomer="${v.perCustomerLimit}">
                                            <svg class="icon"><use href="#ic-edit"/></svg>
                                        </button>
                                        <form method="post" action="${pageContext.request.contextPath}/manager/promotions" style="display:inline"
                                              onsubmit="return confirm('${v.status == "ACTIVE" ? "Tạm ngưng" : "Kích hoạt lại"} voucher \'${v.code}\'?');">
                                            <input type="hidden" name="action" value="${v.status == 'ACTIVE' ? 'deactivateVoucher' : 'activateVoucher'}">
                                            <input type="hidden" name="voucherId" value="${v.voucherId}">
                                            <button type="submit" class="row-action-btn">
                                                <svg class="icon"><use href="${v.status == 'ACTIVE' ? '#ic-x-circle' : '#ic-check'}"/></svg>
                                            </button>
                                        </form>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>

                    <!-- Modal Tạo voucher -->
                    <div class="modal-confirm-backdrop" id="newVoucherModal">
                        <div class="modal-confirm-box" style="text-align:left;max-width:480px;">
                            <h3 style="text-align:center;">Tạo voucher mới</h3>
                            <form id="createVoucherForm" method="post" action="${pageContext.request.contextPath}/manager/promotions"
                                  onsubmit="return validateVoucherForm('nv');">
                                <input type="hidden" name="action" value="createVoucher">

                                <label class="form-label-shop">Mã voucher</label>
                                <input class="form-control-shop mb-1" id="nv_code" name="code" placeholder="VD: SUMMER2026"
                                       style="text-transform:uppercase;" required>
                                <div class="field-error" id="nv_code_error"></div>

                                <label class="form-label-shop">Loại giảm giá</label>
                                <select class="form-select-shop mb-3" id="nv_discountType" name="discountType" onchange="toggleVoucherCapField('nv')">
                                    <option value="PERCENTAGE">Theo phần trăm (%)</option>
                                    <option value="FIXED_AMOUNT">Số tiền cố định (đ)</option>
                                </select>

                                <div class="row g-2 mb-1">
                                    <div class="col-6">
                                        <label class="form-label-shop">Giá trị giảm</label>
                                        <input class="form-control-shop" id="nv_discountValue" name="discountValue" placeholder="VD: 20" required>
                                        <div class="field-error" id="nv_discountValue_error"></div>
                                    </div>
                                    <div class="col-6" id="nv_cap_wrap">
                                        <label class="form-label-shop">Giảm tối đa (đ)</label>
                                        <input class="form-control-shop" id="nv_maxDiscountCap" name="maxDiscountCap" placeholder="VD: 50000" required>
                                        <div class="field-error" id="nv_maxDiscountCap_error"></div>
                                    </div>
                                </div>

                                <label class="form-label-shop">Giá trị đơn hàng tối thiểu (đ)</label>
                                <input class="form-control-shop mb-1" id="nv_minOrderValue" name="minOrderValue" placeholder="VD: 200000" required>
                                <div class="field-error" id="nv_minOrderValue_error"></div>

                                <div class="row g-2 mb-1">
                                    <div class="col-6">
                                        <label class="form-label-shop">Ngày bắt đầu</label>
                                        <input type="datetime-local" class="form-control-shop" id="nv_startDate" name="startDate" required>
                                        <div class="field-error" id="nv_startDate_error"></div>
                                    </div>
                                    <div class="col-6">
                                        <label class="form-label-shop">Ngày kết thúc</label>
                                        <input type="datetime-local" class="form-control-shop" id="nv_endDate" name="endDate" required>
                                        <div class="field-error" id="nv_endDate_error"></div>
                                    </div>
                                </div>

                                <div class="row g-2 mb-1">
                                    <div class="col-6">
                                        <label class="form-label-shop">Tổng lượt dùng tối đa</label>
                                        <input class="form-control-shop" id="nv_maxUsageLimit" name="maxUsageLimit" placeholder="VD: 100" required>
                                        <div class="field-error" id="nv_maxUsageLimit_error"></div>
                                    </div>
                                    <div class="col-6">
                                        <label class="form-label-shop">Giới hạn dùng/khách</label>
                                        <input class="form-control-shop" id="nv_perCustomerLimit" name="perCustomerLimit" placeholder="VD: 1" required>
                                        <div class="field-error" id="nv_perCustomerLimit_error"></div>
                                    </div>
                                </div>

                                <div class="d-flex gap-2 mt-2">
                                    <button type="button" class="btn-outline-ink btn-block-shop" onclick="document.getElementById('newVoucherModal').classList.remove('open')">Hủy</button>
                                    <button type="submit" class="btn-terracotta btn-block-shop">Tạo voucher</button>
                                </div>
                            </form>
                        </div>
                    </div>

                    <!-- Modal Sửa voucher -->
                    <div class="modal-confirm-backdrop" id="editVoucherModal">
                        <div class="modal-confirm-box" style="text-align:left;max-width:480px;">
                            <h3 style="text-align:center;">Sửa voucher</h3>
                            <form id="editVoucherForm" method="post" action="${pageContext.request.contextPath}/manager/promotions"
                                  onsubmit="return validateVoucherForm('ev');">
                                <input type="hidden" name="action" value="updateVoucher">
                                <input type="hidden" name="voucherId" id="ev_id">

                                <label class="form-label-shop">Mã voucher</label>
                                <input class="form-control-shop mb-3" id="ev_code" readonly disabled
                                       style="background:var(--paper-deep);color:var(--ink-soft);">

                                <label class="form-label-shop">Loại giảm giá</label>
                                <select class="form-select-shop mb-3" id="ev_discountType" name="discountType" onchange="toggleVoucherCapField('ev')">
                                    <option value="PERCENTAGE">Theo phần trăm (%)</option>
                                    <option value="FIXED_AMOUNT">Số tiền cố định (đ)</option>
                                </select>

                                <div class="row g-2 mb-1">
                                    <div class="col-6">
                                        <label class="form-label-shop">Giá trị giảm</label>
                                        <input class="form-control-shop" id="ev_discountValue" name="discountValue" required>
                                        <div class="field-error" id="ev_discountValue_error"></div>
                                    </div>
                                    <div class="col-6" id="ev_cap_wrap">
                                        <label class="form-label-shop">Giảm tối đa (đ)</label>
                                        <input class="form-control-shop" id="ev_maxDiscountCap" name="maxDiscountCap" required>
                                        <div class="field-error" id="ev_maxDiscountCap_error"></div>
                                    </div>
                                </div>

                                <label class="form-label-shop">Giá trị đơn hàng tối thiểu (đ)</label>
                                <input class="form-control-shop mb-1" id="ev_minOrderValue" name="minOrderValue" required>
                                <div class="field-error" id="ev_minOrderValue_error"></div>

                                <div class="row g-2 mb-1">
                                    <div class="col-6">
                                        <label class="form-label-shop">Ngày bắt đầu</label>
                                        <input type="datetime-local" class="form-control-shop" id="ev_startDate" name="startDate" required>
                                        <div class="field-error" id="ev_startDate_error"></div>
                                    </div>
                                    <div class="col-6">
                                        <label class="form-label-shop">Ngày kết thúc</label>
                                        <input type="datetime-local" class="form-control-shop" id="ev_endDate" name="endDate" required>
                                        <div class="field-error" id="ev_endDate_error"></div>
                                    </div>
                                </div>

                                <div class="row g-2 mb-1">
                                    <div class="col-6">
                                        <label class="form-label-shop">Tổng lượt dùng tối đa</label>
                                        <input class="form-control-shop" id="ev_maxUsageLimit" name="maxUsageLimit" required>
                                        <div class="field-error" id="ev_maxUsageLimit_error"></div>
                                    </div>
                                    <div class="col-6">
                                        <label class="form-label-shop">Giới hạn dùng/khách</label>
                                        <input class="form-control-shop" id="ev_perCustomerLimit" name="perCustomerLimit" required>
                                        <div class="field-error" id="ev_perCustomerLimit_error"></div>
                                    </div>
                                </div>

                                <div class="d-flex gap-2 mt-2">
                                    <button type="button" class="btn-outline-ink btn-block-shop" onclick="document.getElementById('editVoucherModal').classList.remove('open')">Hủy</button>
                                    <button type="submit" class="btn-terracotta btn-block-shop">Lưu thay đổi</button>
                                </div>
                            </form>
                        </div>
                    </div>

                    <div class="result-toast" id="resultToast">
                        <div class="rt-icon"><svg class="icon" style="width:18px;" id="resultToastIcon"><use href="#ic-check"/></svg></div>
                        <div class="rt-text" id="resultToastText"></div>
                    </div>
                </div>
            </div>
        </div>

        <script>
            function toggleVoucherCapField(prefix) {
                const type = document.getElementById(prefix + '_discountType').value;
                const wrap = document.getElementById(prefix + '_cap_wrap');
                const capInput = document.getElementById(prefix + '_maxDiscountCap');
                if (type === 'PERCENTAGE') {
                    wrap.style.display = 'block';
                    capInput.setAttribute('required', 'required');
                } else {
                    wrap.style.display = 'none';
                    capInput.removeAttribute('required'); // FIXED_AMOUNT không cần mức giảm tối đa
                }
            }

            function openEditVoucherModal(id, code, type, value, cap, min, start, end, maxUsage, perCustomer) {
                document.getElementById('ev_id').value = id;
                document.getElementById('ev_code').value = code;
                document.getElementById('ev_discountType').value = type;
                document.getElementById('ev_discountValue').value = value;
                document.getElementById('ev_maxDiscountCap').value = cap;
                document.getElementById('ev_minOrderValue').value = min;
                document.getElementById('ev_startDate').value = start;
                document.getElementById('ev_endDate').value = end;
                document.getElementById('ev_maxUsageLimit').value = maxUsage;
                document.getElementById('ev_perCustomerLimit').value = perCustomer;
                toggleVoucherCapField('ev');
                document.getElementById('editVoucherModal').classList.add('open');
            }

            document.querySelectorAll('.edit-voucher-btn').forEach(function (btn) {
                btn.addEventListener('click', function () {
                    const d = btn.dataset;
                    openEditVoucherModal(d.id, d.code, d.type, d.value, d.cap, d.min, d.start, d.end, d.maxusage, d.percustomer);
                });
            });

            // MỚI: validate 1 field voucher, hiện lỗi ngay tại chỗ — cùng pattern với validateField() bên Product
            function validateVoucherField(el) {
                const errorEl = document.getElementById(el.id + '_error');
                if (!errorEl)
                    return true;

                let message = '';
                const val = el.value.trim();
                const prefix = el.id.startsWith('nv') ? 'nv' : 'ev';
                const type = document.getElementById(prefix + '_discountType').value;

                if (el.hasAttribute('required') && val === '') {
                    message = 'Không được để trống.';
                } else if (el.id.endsWith('_code') && val !== '' && !/^[A-Za-z0-9_-]+$/.test(val)) {
                    message = 'Mã voucher chỉ được chứa chữ, số, dấu gạch ngang/gạch dưới.';
                } else if (el.id.endsWith('_discountValue') && val !== '') {
                    const num = parseFloat(val.replace(/[.,]/g, ''));
                    if (isNaN(num) || num <= 0) {
                        message = 'Giá trị giảm phải là số lớn hơn 0.';
                    } else if (type === 'PERCENTAGE' && num > 100) {
                        message = 'Giảm theo % không được vượt quá 100.';
                    }
                } else if (el.id.endsWith('_maxDiscountCap') && el.hasAttribute('required') && val !== '') {
                    const num = parseFloat(val.replace(/[.,]/g, ''));
                    if (isNaN(num) || num <= 0)
                        message = 'Mức giảm tối đa phải là số lớn hơn 0.';
                } else if (el.id.endsWith('_minOrderValue') && val !== '') {
                    const num = parseFloat(val.replace(/[.,]/g, ''));
                    if (isNaN(num) || num < 0)
                        message = 'Giá trị đơn hàng tối thiểu không hợp lệ.';
                } else if (el.id.endsWith('_maxUsageLimit') && val !== '') {
                    const num = parseInt(val, 10);
                    if (isNaN(num) || num <= 0)
                        message = 'Phải là số nguyên lớn hơn 0.';
                } else if (el.id.endsWith('_perCustomerLimit') && val !== '') {
                    const num = parseInt(val, 10);
                    const maxUsage = parseInt(document.getElementById(prefix + '_maxUsageLimit').value, 10);
                    if (isNaN(num) || num <= 0) {
                        message = 'Phải là số nguyên lớn hơn 0.';
                    } else if (!isNaN(maxUsage) && num > maxUsage) {
                        message = 'Không được lớn hơn tổng lượt dùng tối đa.';
                    }
                } else if (el.id.endsWith('_endDate') && val !== '') {
                    const start = document.getElementById(prefix + '_startDate').value;
                    if (start && val <= start)
                        message = 'Ngày kết thúc phải sau ngày bắt đầu.';
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

            document.querySelectorAll('#createVoucherForm [required], #editVoucherForm [required]').forEach(function (el) {
                el.addEventListener('blur', function () {
                    validateVoucherField(el);
                });
                el.addEventListener('input', function () {
                    if (el.classList.contains('invalid'))
                        validateVoucherField(el);
                });
            });

            // MỚI: check trùng mã voucher real-time (chỉ form Tạo — form Sửa mã đã readonly)
            document.getElementById('nv_code').addEventListener('blur', function () {
                const el = this;
                const val = el.value.trim().toUpperCase();
                const errorEl = document.getElementById('nv_code_error');
                if (!validateVoucherField(el))
                    return;
                if (val === '')
                    return;

                fetch('${pageContext.request.contextPath}/manager/promotions?checkVoucherCode=' + encodeURIComponent(val))
                        .then(res => res.text())
                        .then(result => {
                            if (result === 'EXISTS') {
                                el.classList.add('invalid');
                                errorEl.textContent = 'Mã voucher "' + val + '" đã tồn tại.';
                                errorEl.classList.add('show');
                            }
                        })
                        .catch(err => console.error('Lỗi khi check mã voucher trùng:', err));
            });

            function validateVoucherForm(prefix) {
                try {
                    const form = document.getElementById(prefix === 'nv' ? 'createVoucherForm' : 'editVoucherForm');
                    let allValid = true;
                    let firstInvalid = null;

                    form.querySelectorAll('[required]').forEach(function (el) {
                        if (el.offsetParent === null)
                            return;
                        if (!validateVoucherField(el)) {
                            allValid = false;
                            if (!firstInvalid)
                                firstInvalid = el;
                        }
                    });

                    if (!allValid && firstInvalid) {
                        firstInvalid.scrollIntoView({behavior: 'smooth', block: 'center'});
                        firstInvalid.focus();
                    }
                    return allValid;
                } catch (err) {
                    alert('Có lỗi JavaScript xảy ra: ' + err.message + '\nMở Console (F12) để xem chi tiết.');
                    console.error(err);
                    return false;
                }
            }

            function showResultToast(text, isError) {
                const toast = document.getElementById('resultToast');
                document.getElementById('resultToastText').textContent = text;
                toast.classList.toggle('error', !!isError);
                const iconUse = document.querySelector('#resultToastIcon use');
                iconUse.setAttribute('href', isError ? '#ic-alert-circle' : '#ic-check');
                toast.classList.add('show');
                setTimeout(() => toast.classList.remove('show'), 3500);
            }

            <c:if test="${not empty flashMessage}">
            window.addEventListener('DOMContentLoaded', function () {
                showResultToast('${flashMessage}', ${flashError});
            });
            </c:if>
        </script>
    </body>
</html>