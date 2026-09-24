<%-- 
    Document   : Cart.jsp
    Created on : Jun 30, 2026, 4:50:20 PM
    Author     : GiGaByte
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Giỏ hàng — Booky Mart</title>
        <link href="https://cdnjs.cloudflare.com/ajax/libs/twitter-bootstrap/5.3.3/css/bootstrap.min.css" rel="stylesheet">
        <link href="https://fonts.googleapis.com/css2?family=Fraunces:opsz,wght@9..144,400;9..144,600;9..144,700&family=Inter:wght@400;500;600;700&family=JetBrains+Mono:wght@500&display=swap" rel="stylesheet">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css">

    </head>
    <body>
    <jsp:include page="assets/header.jsp" />

        <!-- Breadcrumb -->
        <div class="container">
            <c:if test="${not empty flashMessage}">
                <div class="alert ${flashError ? 'alert-danger' : 'alert-success'} mb-3" role="alert">${flashMessage}</div>
            </c:if>
            <div class="breadcrumb-shop">
                <a href="home">Trang chủ</a><span class="sep">/</span>
                <span class="current">Giỏ hàng</span>
            </div>
        </div>


        <div class="container pb-5">
            <h1 style="font-size:1.7rem;margin-bottom:24px;">
                Giỏ hàng của bạn 
                <span style="color:var(--ink-soft);font-size:1rem;font-weight:500;">(${cartItems.size()} sản phẩm)</span>
            </h1>
            <form id="checkoutForm" action="checkout" method="get">
                <div class="row g-4">
                    <div class="col-lg-8">

                        <c:forEach var="item" items="${cartItems}">

                            <div class="cart-line ${not item.isSelected ? 'dimmed' : ''}">

                               <%-- MỚI: sản phẩm CÒN hàng lúc thêm vào giỏ, nhưng SAU ĐÓ hết hàng
                                     (người khác mua hết / Manager sửa tồn kho) — trước đây vẫn tick
                                     chọn được bình thường, dễ mua nhầm sản phẩm không còn hàng. Giờ
                                     disable hẳn checkbox nếu hết hàng, không cho chọn được nữa. --%>
                                <input type="checkbox" name="selectedItems" value="${item.cartItemId}" class="cart-check"
                                       ${item.isSelected && item.inStock ? 'checked' : ''}
                                       ${item.inStock ? '' : 'disabled'}
                                       onchange="recalculateCart()">
                                
                                <div class="cart-thumb thumb-fiction">
                                    <c:choose>
                                        <c:when test="${not empty item.productImage}">
                                            <img src="${empty item.productImage ? 'assets/img/products/no-image.svg' : item.productImage}" alt="${item.productName}" style="width:100%; height:100%; object-fit:cover; border-radius:8px;">
                                        </c:when>
                                        <c:otherwise>
                                            <svg class="icon" style="width:1.8rem;height:1.8rem;"><use href="#ic-book-open"/></svg>
                                        </c:otherwise>
                                    </c:choose>
                                </div>

                               <div class="cart-line-info">
                                    <div class="cart-line-name">${item.productName}</div>
                                    <%-- MỚI: TRƯỚC ĐÂY nếu sản phẩm hết hàng nhưng CHƯA TỪNG được tick
                                         chọn, code chỉ hiện "Chưa chọn — vẫn được giữ lại", không hề
                                         cảnh báo hết hàng gì cả — khách không biết sản phẩm đó đã hết
                                         hàng cho tới khi thử tick chọn. Giờ kiểm tra tồn kho TRƯỚC,
                                         không phụ thuộc trạng thái tick chọn nữa. --%>
                                    <c:choose>
                                        <c:when test="${!item.inStock}">
                                            <div class="cart-line-meta">
                                                ${item.productMeta} &middot; <span style="color:red;font-weight:700;">Hết hàng</span>
                                            </div>
                                            <div class="cart-line-stay-note" style="color:#C24545;">
                                                <svg class="icon" style="width:12px;"><use href="#ic-alert-circle"/></svg> Sản phẩm đã hết hàng — không thể chọn mua, vui lòng xóa khỏi giỏ hoặc chờ hàng về.
                                            </div>
                                        </c:when>
                                        <c:when test="${item.isSelected}">
                                            <div class="cart-line-meta">${item.productMeta} &middot; Còn hàng</div>
                                        </c:when>
                                        <c:otherwise>
                                            <div class="cart-line-meta">${item.productMeta}</div>
                                            <div class="cart-line-stay-note">
                                                <svg class="icon" style="width:12px;"><use href="#ic-check"/></svg> Chưa chọn — vẫn được giữ lại trong giỏ
                                            </div>
                                        </c:otherwise>
                                    </c:choose>
                                </div>

                                <div class="qty-stepper-sm">
                                    <button type="button" onclick="changeQty('${item.cartItemId}', -1)">
                                        <svg class="icon" style="width:14px;"><use href="#ic-minus"/></svg>
                                    </button>
                                    <input id="q_${item.cartItemId}" type="text" inputmode="numeric" pattern="[0-9]*" value="${item.quantity}"
                                        name="qty_${item.cartItemId}"
                                        data-min-stock="1"
                                        data-unit-price="${item.price}"
                                        data-max-stock="${item.activeStock}"
                                        onchange="changeQty('${item.cartItemId}', 0)">
                                    <button type="button" onclick="changeQty('${item.cartItemId}', 1)">
                                        <svg class="icon" style="width:14px;"><use href="#ic-plus"/></svg>
                                    </button>
                                </div>
                                <%-- MỚI: trước đây bấm "+" liên tục sẽ bị ghim ở số tồn kho tối đa mà không
                                     báo gì cả, cảm giác như lỗi. Dòng cảnh báo này ẩn mặc định, JS hiện ra
                                     trong chốc lát đúng lúc bị ghim, rồi tự ẩn lại. --%>
                                <div id="stockLimitNote_${item.cartItemId}" class="stock-limit-note" style="display:none;color:#C24545;font-size:.78rem;margin-top:4px;">
                                    Chỉ còn ${item.activeStock} sản phẩm trong kho.
                                </div>

                                <div class="cart-line-price" id="price_${item.cartItemId}">
                                    <fmt:formatNumber value="${item.price * item.quantity}" pattern="#,###"/>đ
                                </div>

                                <a href="deletecart?cartItemId=${item.cartItemId}" class="remove-line-btn"
                                   onclick="return confirm('Xóa sản phẩm này khỏi giỏ hàng?');"><svg class="icon"><use href="#ic-trash"/></svg></a>
                            </div>
                        </c:forEach>
                        <a href="home" class="nav-link-shop d-inline-flex align-items-center gap-2 mt-2" style="border:1px solid var(--line);border-radius:8px;">
                            <svg class="icon"><use href="#ic-chevron-left"/></svg> Tiếp tục mua sắm
                        </a>
                    </div>
                    <div class="col-lg-4">
                        <div class="panel">
                            <div class="panel-title"><span><svg class="icon"><use href="#ic-bag"/></svg> Tóm tắt đơn hàng</span></div>

                            <div class="invoice-summary-block">
                                <div class="invoice-summary-row">
                                    <span class="isr-label"><svg class="icon"><use href="#ic-package"/></svg> Tạm tính (3 món đã chọn)</span>
                                    <span class="isr-value" id="subtotal-value">00000</span>
                                </div>
                                <div class="invoice-summary-row discount">
                                    <span class="isr-label"><svg class="icon"><use href="#ic-tag"/></svg> Giảm giá</span>
                                    <span class="isr-value">−0đ</span>
                                </div>
                                <div class="invoice-summary-row muted">
                                    <span class="isr-label"><svg class="icon"><use href="#ic-truck"/></svg> Phí vận chuyển</span>
                                    <span class="isr-value">Tính ở bước sau</span>
                                </div>

                                <div class="invoice-grand-total">
                                    <span class="igt-label">Tổng cộng</span>
                                    <span class="igt-value" id="grandtotal-value">00000</span>
                                </div>
                            </div>

                            <!--        <button class="btn-terracotta btn-block-shop mt-3" onclick="location.href='checkout'">Tiến hành thanh toán</button>-->
                            <button type="submit" class="btn-terracotta btn-block-shop mt-3">
                                Tiến hành thanh toán
                            </button>       
                            <p style="font-size:.76rem;color:var(--ink-soft);text-align:center;margin-top:10px;margin-bottom:0;">
                                Cần chọn ít nhất 1 sản phẩm để tiếp tục!
                            </p>
                        </div>
                    </div>
                </div>
                </form>
    </div>
                <!-- Footer -->
                <jsp:include page="assets/footer.jsp"></jsp:include>
                    <script>
                        function recalculateCart() {
                            let subtotal = 0;
                            let selectedCount = 0;

                          document.querySelectorAll('.cart-line').forEach(line => {
                            const checkbox = line.querySelector('.cart-check');
                            const qtyInput = line.querySelector('input[data-unit-price]');
                            const unitPrice = parseFloat(qtyInput.dataset.unitPrice);
                                const quantity = parseInt(qtyInput.value) || 1;
                                const lineTotal = unitPrice * quantity;

                                // Cập nhật giá hiển thị của dòng này
                                const priceEl = line.querySelector('.cart-line-price');
                                priceEl.textContent = formatVND(lineTotal) + 'đ';

                                // Cập nhật class dimmed theo checkbox
                                line.classList.toggle('dimmed', !checkbox.checked);

                                // Chỉ cộng vào tổng nếu được chọn
                                if (checkbox.checked) {
                                    subtotal += lineTotal;
                                    selectedCount++;
                                }
                            });

                            document.getElementById('subtotal-value').textContent = formatVND(subtotal) + 'đ';
                            document.getElementById('grandtotal-value').textContent = formatVND(subtotal) + 'đ';

                            // Cập nhật label "(x món đã chọn)" nếu cần
                            const subtotalLabel = document.querySelector('.invoice-summary-row .isr-label');
                            if (subtotalLabel) {
                                subtotalLabel.innerHTML = subtotalLabel.innerHTML.replace(/\(\d+ món đã chọn\)/, `(${selectedCount} món đã chọn)`);
                            }
                        }

                        function formatVND(number) {
                            return number.toLocaleString('vi-VN');
                        }
                        function changeQty(cartItemId, delta) {
                            const input = document.getElementById('q_' + cartItemId);
                            if (!input)
                                return;

                            let current = parseInt(input.value, 10);
                            if (isNaN(current))
                                current = 1;

                            const min = parseInt(input.dataset.minStock, 10) || 1;
                            // Lấy đúng tồn kho thật (data-max-stock, do server tính sẵn) làm giới hạn trên.
                            const max = parseInt(input.dataset.maxStock, 10) || min;
                            let newValue = current + delta;

                            // MỚI: trước đây ghim newValue về min/max một cách âm thầm, không có cách
                            // nào để khách biết TẠI SAO số lượng không tăng/giảm nữa nữa — cảm giác
                            // giống lỗi. Giờ phát hiện đúng lúc bị ghim để hiện cảnh báo rõ ràng.
                            let hitLimit = false;
                            if (newValue < min) {
                                newValue = min;
                                hitLimit = true;
                            }
                            if (newValue > max) {
                                newValue = max;
                                hitLimit = true;
                            }

                            input.value = newValue; // ← gán trực tiếp, không qua stepUp/stepDown

                            const note = document.getElementById('stockLimitNote_' + cartItemId);
                            if (note) {
                                if (hitLimit && newValue >= max) {
                                    note.style.display = 'block';
                                    clearTimeout(note._hideTimer);
                                    note._hideTimer = setTimeout(function () {
                                        note.style.display = 'none';
                                    }, 2500);
                                } else {
                                    note.style.display = 'none';
                                }
                            }

                            recalculateCart();
                        }

                        // Tính lần đầu khi trang load (để khớp với checkbox mặc định)
                        document.addEventListener('DOMContentLoaded', recalculateCart);
                </script>
                </body>
                </html>
