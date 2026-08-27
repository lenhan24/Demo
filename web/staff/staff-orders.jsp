<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="vi">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Xử lý đơn hàng — Staff | Booky Mart</title>
<link href="https://cdnjs.cloudflare.com/ajax/libs/twitter-bootstrap/5.3.3/css/bootstrap.min.css" rel="stylesheet">
<link href="https://fonts.googleapis.com/css2?family=Fraunces:opsz,wght@9..144,400;9..144,600;9..144,700&family=Inter:wght@400;500;600;700&family=JetBrains+Mono:wght@500&display=swap" rel="stylesheet">
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css">
</head>
<body>

<%@ include file="/WEB-INF/jspf/icons.jspf" %>
<div class="admin-shell">
  <div class="admin-sidebar">
    <a class="brand" href="${pageContext.request.contextPath}/home">Booky<span class="dot">.</span>Mart</a>
    <div class="group-label">Staff Panel</div>
      <a href="${pageContext.request.contextPath}/staff/orders" class="active"><svg class="icon"><use href="#ic-clipboard-check"/></svg> Xử lý đơn hàng</a>
      <a href="${pageContext.request.contextPath}/staff/returns" class=""><svg class="icon"><use href="#ic-refresh"/></svg> Tiếp nhận trả hàng</a>
    <div class="group-label">Khác</div>
      <a href="${pageContext.request.contextPath}/home"><svg class="icon"><use href="#ic-arrow-right"/></svg> Về trang khách hàng</a>
      <a href="${pageContext.request.contextPath}/logout" style="color:#E29B8A;"><svg class="icon"><use href="#ic-logout"/></svg> Đăng xuất</a>
  </div>

  <div class="admin-main">
    <div class="admin-topbar">
      <div style="font-weight:700;color:var(--ink);">Xử lý đơn hàng</div>
      <div class="d-flex align-items-center gap-3">
        <button class="icon-btn"><svg class="icon"><use href="#ic-bell"/></svg></button>
        <%@ include file="/WEB-INF/jspf/account-info.jspf" %>
      </div>
    </div>
    <div class="admin-content">
      <div class="page-title-row">
        <div>
          <h1>Xử lý đơn hàng</h1>
          <div class="sub">Xác nhận, đóng gói và chuyển giao đơn hàng cho vận chuyển</div>
        </div>
      </div>

      <%-- Flash message --%>
      <c:if test="${not empty flashMessage}">
        <div class="alert ${flashError ? 'alert-danger' : 'alert-success'} mb-3" role="alert">${flashMessage}</div>
      </c:if>

      <!-- Thông báo yêu cầu hoàn hàng đang chờ xử lý — khách bấm "Hoàn hàng"
           ở trang chi tiết đơn sẽ xuất hiện ở đây, kèm thông tin liên hệ để
           Staff chủ động liên hệ xử lý NGOÀI hệ thống (gọi điện/nhắn tin),
           xong việc thì bấm xác nhận hoàn tất bên dưới. -->
      <%-- SỬA: TRƯỚC ĐÂY có 2 nút hành động trực tiếp tại đây (confirmReturn/
           rejectReturn kiểu cũ, không phân biệt hàng tốt/lỗi) — bỏ hẳn, vì
           trang "/staff/returns" giờ đã có đủ 2 nút "Hàng tốt/Hàng lỗi" đúng
           quy trình mới. Ở đây chỉ còn vai trò THÔNG BÁO + dẫn đường, tránh
           2 nơi cùng xử lý 1 việc theo 2 cách khác nhau. --%>
      <c:if test="${not empty pendingReturns}">
        <div class="return-request-banner">
          <div class="panel-title">
            <svg class="icon" style="width:15px;vertical-align:-2px;"><use href="#ic-alert-circle"/></svg>
            Yêu cầu hoàn hàng đang chờ xử lý (${fn:length(pendingReturns)})
          </div>
          <c:forEach var="r" items="${pendingReturns}">
            <div class="return-request-row">
              <div class="rr-main">
                <strong>Đơn #${r.orderCode}</strong> — ${r.receiverName} · ${r.receiverPhone} · ${r.accountEmail}
                <div class="rr-sub">
                  Lý do: <strong>${not empty r.returnReason ? r.returnReason : '(không có)'}</strong><br>
                  Tổng đơn: <fmt:formatNumber value="${r.totalAmount}" pattern="#,###"/>đ
                </div>
              </div>
              <a href="${pageContext.request.contextPath}/staff/returns" class="btn-sm-shop btn-terracotta" style="text-decoration:none;">
                <svg class="icon" style="width:13px;vertical-align:-2px;"><use href="#ic-eye"/></svg> Xem chi tiết & xử lý
              </a>
            </div>
          </c:forEach>
        </div>
      </c:if>

      <%-- Thẻ thống kê nhanh --%>
      <div class="row g-3 mb-4">
        <div class="col-md"><div class="stat-card">
          <div class="label"><svg class="icon" style="width:14px;"><use href="#ic-card"/></svg> Chờ thanh toán</div>
          <div class="value">${statPendingPayment}</div>
        </div></div>
        <div class="col-md"><div class="stat-card">
          <div class="label"><svg class="icon" style="width:14px;"><use href="#ic-clipboard-check"/></svg> Chờ xử lý</div>
          <div class="value">${statPending}</div>
        </div></div>
        <div class="col-md"><div class="stat-card">
          <div class="label"><svg class="icon" style="width:14px;"><use href="#ic-package"/></svg> Đang xử lý</div>
          <div class="value">${statProcessing}</div>
        </div></div>
        <div class="col-md"><div class="stat-card">
          <div class="label"><svg class="icon" style="width:14px;"><use href="#ic-truck"/></svg> Đang giao</div>
          <div class="value">${statShipped}</div>
        </div></div>
        <div class="col-md"><div class="stat-card">
          <div class="label"><svg class="icon" style="width:14px;"><use href="#ic-alert-circle"/></svg> Tạm giữ</div>
          <div class="value">${statOnHold}</div>
        </div></div>
      </div>

      <%-- Toolbar: search + filter --%>
      <div class="admin-toolbar">
        <div class="admin-search-box">
          <form method="get" action="${pageContext.request.contextPath}/staff/orders" style="display:contents;">
          <svg class="icon"><use href="#ic-search"/></svg>
          <input type="text" name="keyword" value="${keyword}" placeholder="Tìm theo mã đơn, tên khách hàng...">
          </form>
        </div>
        <div class="d-flex gap-2">
          <select class="sort-select" id="orderStatusFilter"
                  onchange="location.href='${pageContext.request.contextPath}/staff/orders?status='+this.value">
            <option value="" ${empty statusFilter ? 'selected' : ''}>Tất cả trạng thái</option>
            <option value="PENDING_PAYMENT" ${statusFilter=='PENDING_PAYMENT'?'selected':''}>Chờ thanh toán (VietQR)</option>
            <option value="PENDING"    ${statusFilter=='PENDING'?'selected':''}>Chờ xử lý</option>
            <option value="PROCESSING" ${statusFilter=='PROCESSING'?'selected':''}>Đang xử lý</option>
            <option value="SHIPPED"    ${statusFilter=='SHIPPED'?'selected':''}>Đang giao</option>
            <option value="ON_HOLD"    ${statusFilter=='ON_HOLD'?'selected':''}>Tạm giữ (On Hold)</option>
          </select>
        </div>
      </div>

      <%-- Bảng đơn hàng --%>
      <table class="table-shop">
        <thead>
          <tr>
            <th>Mã đơn</th><th>Khách hàng</th><th>Sản phẩm</th><th>Tổng tiền</th>
            <th>Thanh toán</th><th>Trạng thái</th><th>Ngày đặt</th><th></th>
          </tr>
        </thead>
        <tbody id="ordersTableBody">
          <c:choose>
            <c:when test="${empty orders}">
              <tr><td colspan="8" style="text-align:center;color:var(--ink-soft);padding:28px 0;">
                Không có đơn hàng nào ở trạng thái này.
              </td></tr>
            </c:when>
            <c:otherwise>
              <c:forEach var="o" items="${orders}">
                <tr id="orderRow-${o.orderId}">
                  <td style="font-weight:700;">#${o.orderCode}</td>
                  <td>${o.receiverName}</td>
                  <td>${o.details.size()} sản phẩm</td>
                  <td class="mono"><fmt:formatNumber value="${o.totalAmount}" type="number" groupingUsed="true"/>đ</td>
                  <td>
                    <c:choose>
                      <c:when test="${o.paymentStatus == 'REFUND_PENDING'}"><span class="status-badge status-hold" style="font-size:.68rem;">Chờ hoàn tiền</span></c:when>
                      <c:when test="${o.paymentStatus == 'REFUNDED'}"><span class="status-badge status-delivered" style="font-size:.68rem;">Đã hoàn tiền</span></c:when>
                      <c:when test="${o.paymentStatus == 'PAID'}"><span class="status-badge status-delivered" style="font-size:.68rem;">Đã TT</span></c:when>
                      <c:when test="${o.paymentMethod == 'COD'}"><span class="status-badge status-hold" style="font-size:.68rem;">COD</span></c:when>
                      <c:otherwise><span class="status-badge status-cancelled" style="font-size:.68rem;">Chưa TT</span></c:otherwise>
                    </c:choose>
                  </td>
                  <td id="orderStatusCell-${o.orderId}">
                    <c:choose>
                      <c:when test="${o.status == 'PENDING_PAYMENT'}"><span class="status-badge status-hold"><span class="dot"></span>Chờ thanh toán</span></c:when>
                      <c:when test="${o.status == 'PENDING'}"><span class="status-badge status-pending"><span class="dot"></span>Chờ xử lý</span></c:when>
                      <c:when test="${o.status == 'PROCESSING'}"><span class="status-badge status-processing"><span class="dot"></span>Đang xử lý</span></c:when>
                      <c:when test="${o.status == 'SHIPPED'}"><span class="status-badge status-shipped"><span class="dot"></span>Đang giao</span></c:when>
                      <c:when test="${o.status == 'ON_HOLD'}"><span class="status-badge status-hold"><span class="dot"></span>Tạm giữ</span></c:when>
                      <c:when test="${o.status == 'DELIVERED'}"><span class="status-badge status-delivered">Đã giao</span></c:when>
                      <c:when test="${o.status == 'CANCELLED'}"><span class="status-badge status-cancelled">Đã hủy</span></c:when>
                      <c:otherwise><span class="status-badge">${o.status}</span></c:otherwise>
                    </c:choose>
                  </td>
                  <td><fmt:formatDate value="${o.orderDate}" pattern="dd/MM HH:mm"/></td>
                  <td id="orderActionCell-${o.orderId}">
                    <%-- Nút hành động tùy theo status --%>
                    <c:if test="${o.status == 'PENDING_PAYMENT'}">
                      <form method="post" action="${pageContext.request.contextPath}/staff/orders" style="display:inline"
                            onsubmit="return confirm('Xác nhận ĐÃ ĐỐI CHIẾU SAO KÊ và tiền của đơn #${o.orderCode} (${o.totalAmount}đ) đã về tài khoản ngân hàng của shop? Sau khi xác nhận, đơn sẽ chuyển sang Chờ xử lý và đánh dấu đã thanh toán.');">
                        <input type="hidden" name="action" value="updateStatus">
                        <input type="hidden" name="orderId" value="${o.orderId}">
                        <input type="hidden" name="newStatus" value="PENDING">
                        <input type="hidden" name="currentFilter" value="${statusFilter}">
                        <button type="submit" class="btn-sm-shop btn-terracotta" style="border:none;">Xác nhận đã nhận tiền</button>
                      </form>
                    </c:if>
                   <c:if test="${o.status == 'PENDING' || o.status == 'PROCESSING'}">
                      <form method="post" action="${pageContext.request.contextPath}/staff/orders" style="display:inline-flex;align-items:center;gap:4px;margin-right:4px;">
                        <input type="hidden" name="action" value="confirmPhone">
                        <input type="hidden" name="orderId" value="${o.orderId}">
                        <input type="hidden" name="currentFilter" value="${statusFilter}">
                        <%-- SỬA: bỏ auto-bind "selected" theo trạng thái hiện tại — trước
                             đây khiến onchange không kích hoạt khi Staff chọn LẶP LẠI đúng
                             option đang hiển thị (VD chọn "Không liên hệ được" 2 lần liên
                             tiếp), vì trình duyệt coi đó là "không đổi giá trị". Giờ dropdown
                             luôn bắt đầu ở placeholder trống, mọi lựa chọn (kể cả lặp lại ý
                             nghĩa) đều tính là 1 lần đổi giá trị thật -> submit được. --%>
                        <select name="phoneStatus" class="form-select-shop" style="width:auto;font-size:.72rem;padding:2px 6px;"
                                onchange="this.form.submit()" title="Ghi nhận kết quả gọi điện">
                          <option value="" selected disabled>-- Ghi nhận --</option>
                          <option value="CONFIRMED">Đã xác nhận</option>
                          <option value="NOT_CONFIRMED">Không liên hệ được</option>
                        </select>
                        <%-- Trạng thái hiện tại hiển thị riêng, không còn gắn vào dropdown --%>
                        <c:if test="${o.phoneConfirmStatus == 'CONFIRMED'}">
                          <span style="font-size:.68rem;color:#2E7D32;">✓ Đã xác nhận</span>
                        </c:if>
                        <c:if test="${o.phoneConfirmStatus == 'NOT_CONFIRMED'}">
                          <span style="font-size:.68rem;color:#C24545;">Không liên hệ được<c:if test="${o.phoneAttemptCount > 0}"> (${o.phoneAttemptCount}/3 lần)</c:if></span>
                        </c:if>
                      </form>
                    </c:if>
                    <c:if test="${o.status == 'PENDING'}">
                      <button type="button" class="row-action-btn" title="Xem chi tiết" onclick="openOrderDetailModal(${o.orderId})"><svg class="icon"><use href="#ic-eye"/></svg></button>
                      <button type="button" class="btn-sm-shop btn-terracotta" style="border:none;"
                              onclick="openConfirmOrderModal(${o.orderId})">Xác nhận</button>
                    </c:if>
                    <c:if test="${o.status == 'PROCESSING'}">
                      <button type="button" class="row-action-btn" title="Xem chi tiết" onclick="openOrderDetailModal(${o.orderId})"><svg class="icon"><use href="#ic-eye"/></svg></button>
                      <button type="button" class="btn-sm-shop btn-outline-ink" style="border:none;"
                              onclick="openPrintLabelModal(${o.orderId})">Giao hàng</button>
                    </c:if>
                    <c:if test="${o.status == 'PENDING' || o.status == 'PROCESSING'}">
                      <form method="post" action="${pageContext.request.contextPath}/staff/orders" style="display:inline"
                            onsubmit="return confirm('Tạm giữ đơn #${o.orderCode}?')">
                        <input type="hidden" name="action" value="updateStatus">
                        <input type="hidden" name="orderId" value="${o.orderId}">
                        <input type="hidden" name="newStatus" value="ON_HOLD">
                        <input type="hidden" name="currentFilter" value="${statusFilter}">
                        <button type="submit" class="row-action-btn" title="Tạm giữ"><svg class="icon"><use href="#ic-alert-circle"/></svg></button>
                      </form>
                    </c:if>
                    <%-- MỚI: TRƯỚC ĐÂY đơn vào ON_HOLD là kẹt vĩnh viễn — DB cho
                         phép chuyển ON_HOLD -> PROCESSING nhưng giao diện không
                         có nút nào để làm việc này, chỉ có nút hủy. Thêm nút
                         "Tiếp tục xử lý" để Staff đưa đơn quay lại xử lý bình
                         thường sau khi giải quyết xong vấn đề tạm giữ. --%>
                    <c:if test="${o.status == 'ON_HOLD'}">
                      <button type="button" class="row-action-btn" title="Xem chi tiết" onclick="openOrderDetailModal(${o.orderId})"><svg class="icon"><use href="#ic-eye"/></svg></button>
                      <form method="post" action="${pageContext.request.contextPath}/staff/orders" style="display:inline"
                            onsubmit="return confirm('Đưa đơn #${o.orderCode} quay lại \'Đang xử lý\'?')">
                        <input type="hidden" name="action" value="updateStatus">
                        <input type="hidden" name="orderId" value="${o.orderId}">
                        <input type="hidden" name="newStatus" value="PROCESSING">
                        <input type="hidden" name="currentFilter" value="${statusFilter}">
                        <button type="submit" class="btn-sm-shop btn-terracotta" style="border:none;">Tiếp tục xử lý</button>
                      </form>
                    </c:if>
                          <c:if test="${o.status == 'SHIPPED'}">
                          <button type="button" class="row-action-btn" title="Xem chi tiết" onclick="openOrderDetailModal(${o.orderId})"><svg class="icon"><use href="#ic-eye"/></svg></button>
                          <form method="post" action="${pageContext.request.contextPath}/staff/orders" style="display:inline"
                                onsubmit="return confirm('Đánh dấu đơn #${o.orderCode} GIAO THẤT BẠI (không liên hệ được khách / khách từ chối nhận)? Đơn sẽ chuyển sang Tạm giữ, hàng coi như đã về lại kho, và sẽ KHÔNG được hoàn tiền nếu sau đó bị hủy hẳn.')">
                              <input type="hidden" name="action" value="updateStatus">
                              <input type="hidden" name="orderId" value="${o.orderId}">
                              <input type="hidden" name="newStatus" value="ON_HOLD">
                              <input type="hidden" name="reason" value="DELIVERY_FAILED">
                              <input type="hidden" name="currentFilter" value="${statusFilter}">
                              <button type="submit" class="btn-sm-shop btn-outline-ink" style="border:none;">Giao thất bại</button>
                          </form>
                      </c:if>
                    <c:if test="${o.status == 'DELIVERED'}">
                      <button type="button" class="row-action-btn" title="Xem chi tiết" onclick="openOrderDetailModal(${o.orderId})"><svg class="icon"><use href="#ic-eye"/></svg></button>
                    </c:if>
                    <button type="button" class="row-action-btn" title="Thao tác khác" onclick="openQuickActions(${o.orderId})"><svg class="icon"><use href="#ic-dots"/></svg></button>
                  </td>
                </tr>
              </c:forEach>
            </c:otherwise>
          </c:choose>
        </tbody>
      </table>

      <p class="form-hint mt-3">
        <svg class="icon" style="width:13px;vertical-align:-2px;"><use href="#ic-alert-circle"/></svg>
        Phát hiện hàng lỗi lúc đóng gói chỉ trừ vào tồn kho (BR-STF07.1) — đơn vẫn tiếp tục xử lý bình thường
        nếu còn đủ hàng tốt để giao. Chỉ chuyển "Tạm giữ" khi không còn đủ hàng tốt cho đơn đó (E1, UC-STF-01).
      </p>

      <!-- Modal xác nhận xử lý đơn -->
      <div class="modal-confirm-backdrop" id="confirmOrderModal">
        <div class="modal-confirm-box">
          <h3 id="confirmOrderModalTitle">Xác nhận xử lý đơn?</h3>
          <p>Hệ thống sẽ kiểm tra tồn kho cho từng sản phẩm trong đơn trước khi chuyển sang trạng thái "Đang xử lý".</p>
          <div class="d-flex gap-2">
            <button class="btn-outline-ink btn-block-shop" onclick="document.getElementById('confirmOrderModal').classList.remove('open')">Hủy</button>
            <button class="btn-terracotta btn-block-shop" onclick="finalizeConfirmOrder()">Xác nhận</button>
          </div>
        </div>
      </div>

      <!-- Modal in nhãn vận chuyển -->
      <div class="modal-confirm-backdrop" id="printLabelModal">
        <div class="modal-confirm-box">
          <h3 id="printLabelModalTitle">In nhãn vận chuyển?</h3>
          <p>Đơn sẽ chuyển sang trạng thái <strong>"Đang giao"</strong> sau khi in nhãn và bàn giao cho đơn vị vận chuyển.</p>
          <div class="d-flex gap-2">
            <button class="btn-outline-ink btn-block-shop" onclick="document.getElementById('printLabelModal').classList.remove('open')">Hủy</button>
            <button class="btn-terracotta btn-block-shop" onclick="finalizePrintLabel()">In nhãn &amp; Chuyển giao</button>
          </div>
        </div>
      </div>

      <!-- Modal xem báo cáo hàng lỗi -->
      <div class="modal-confirm-backdrop" id="viewReportModal">
        <div class="modal-confirm-box" style="text-align:left;max-width:440px;">
          <h3 style="text-align:center;" id="viewReportModalTitle">Báo cáo hàng lỗi</h3>
          <div class="delivery-info-row"><svg class="icon"><use href="#ic-clipboard"/></svg><div><div class="dl">Mã báo cáo</div><div class="dv">#DF-0042</div></div></div>
          <div class="delivery-info-row"><svg class="icon"><use href="#ic-box-alert"/></svg><div><div class="dl">Sản phẩm lỗi</div><div class="dv">1 sản phẩm trong đơn (còn lại vẫn đủ hàng tốt để giao)</div></div></div>
          <div class="delivery-info-row"><svg class="icon"><use href="#ic-alert-circle"/></svg><div><div class="dl">Nguyên nhân</div><div class="dv">Hộp đóng gói bị ẩm trong quá trình lưu kho</div></div></div>
          <div class="delivery-info-row"><svg class="icon"><use href="#ic-user"/></svg><div><div class="dl">Người lập báo cáo</div><div class="dv">Bạn — 22/06/2026 09:12</div></div></div>
          <p class="form-hint mb-3">
            <svg class="icon" style="width:13px;vertical-align:-2px;"><use href="#ic-alert-circle"/></svg>
            Số lượng lỗi đã được trừ khỏi tồn kho (BR-STF07.1). Đơn hàng vẫn tiếp tục xử lý bình thường vì còn đủ hàng tốt để giao.
          </p>
          <button class="btn-outline-ink btn-block-shop" onclick="document.getElementById('viewReportModal').classList.remove('open')">Đóng</button>
        </div>
      </div>

      <!-- Modal thao tác nhanh -->
      <div class="modal-confirm-backdrop" id="quickActionsModal">
        <div class="modal-confirm-box" style="text-align:left;max-width:400px;">
          <h3 style="text-align:center;" id="quickActionsModalTitle">Thao tác — Đơn</h3>
          <p style="text-align:center;color:var(--ink-soft);font-size:.85rem;margin-bottom:18px;" id="quickActionsModalSubtitle"></p>
          <div class="d-flex flex-column gap-2">
            <button class="btn-outline-ink btn-block-shop" style="justify-content:flex-start;gap:10px;" onclick="document.getElementById('quickActionsModal').classList.remove('open');openOrderDetailModal(currentOrderId)">
              <svg class="icon"><use href="#ic-eye"/></svg> Xem chi tiết đơn
            </button>
            <button class="btn-outline-ink btn-block-shop" style="justify-content:flex-start;gap:10px;" onclick="contactCustomer(currentOrderId)">
              <svg class="icon"><use href="#ic-phone"/></svg> Liên hệ khách hàng
            </button>
            <button id="qaCancelBtn" class="btn-danger-outline btn-block-shop" style="justify-content:flex-start;gap:10px;" onclick="document.getElementById('quickActionsModal').classList.remove('open');openCancelOrderModal(currentOrderId)">
              <svg class="icon"><use href="#ic-x-circle"/></svg> Hủy đơn hàng
            </button>
            <p id="qaCancelHint" class="form-hint" style="display:none;margin:2px 0 0;">
              Đơn đang ở trạng thái không thể hủy (đã giao/đang giao/đã hủy).
            </p>
          </div>
          <button class="btn-outline-ink btn-block-shop mt-3" onclick="document.getElementById('quickActionsModal').classList.remove('open')">Đóng</button>
        </div>
      </div>

      <!-- Modal xác nhận hủy đơn -->
      <div class="modal-confirm-backdrop" id="cancelOrderModal">
        <div class="modal-confirm-box">
          <h3 id="cancelOrderModalTitle">Hủy đơn?</h3>
          <p>Đơn sẽ chuyển sang trạng thái "Đã hủy" theo lý do CUSTOMER_REQUEST hoặc OTHER. Hành động này không thể hoàn tác.</p>
          <p id="cancelOrderRefundNote" class="form-hint" style="display:none;color:#d9534f;">
            <svg class="icon" style="width:13px;vertical-align:-2px;"><use href="#ic-alert-circle"/></svg>
            Đơn này đã thanh toán online — sau khi huỷ, shop cần chủ động liên hệ khách để hoàn tiền.
          </p>
          <div class="d-flex gap-2">
            <button class="btn-outline-ink btn-block-shop" onclick="document.getElementById('cancelOrderModal').classList.remove('open')">Không hủy</button>
            <button class="btn-danger-outline btn-block-shop" onclick="finalizeCancelOrder()">Xác nhận hủy đơn</button>
          </div>
        </div>
      </div>

      <!-- Form thật để hủy đơn — TRƯỚC ĐÂY finalizeCancelOrder() chỉ đóng modal
           và hiện toast giả "Đã hủy thành công", KHÔNG hề gọi lên server, đơn
           hàng thật không hề bị huỷ. Giờ submit thật vào đúng servlet đã có
           sẵn state-machine + hoàn kho + đánh dấu cần hoàn tiền. -->
      <form id="realCancelForm" method="post" action="${pageContext.request.contextPath}/staff/orders" style="display:none;">
        <input type="hidden" name="action" value="updateStatus">
        <input type="hidden" name="orderId" id="realCancelOrderId" value="">
        <input type="hidden" name="newStatus" value="CANCELLED">
        <input type="hidden" name="currentFilter" value="${statusFilter}">
      </form>

      <!-- Form thật để chuyển đơn PENDING -> PROCESSING, submit khi bấm "Xác nhận" trong confirmOrderModal -->
      <form id="realConfirmOrderForm" method="post" action="${pageContext.request.contextPath}/staff/orders" style="display:none;">
        <input type="hidden" name="action" value="updateStatus">
        <input type="hidden" name="orderId" id="realConfirmOrderId" value="">
        <input type="hidden" name="newStatus" value="PROCESSING">
        <input type="hidden" name="currentFilter" value="${statusFilter}">
      </form>

      <!-- Form thật để chuyển đơn PROCESSING -> SHIPPED, submit khi bấm "In nhãn & Chuyển giao" trong printLabelModal -->
      <form id="realPrintLabelForm" method="post" action="${pageContext.request.contextPath}/staff/orders" style="display:none;">
        <input type="hidden" name="action" value="updateStatus">
        <input type="hidden" name="orderId" id="realPrintLabelOrderId" value="">
        <input type="hidden" name="newStatus" value="SHIPPED">
        <input type="hidden" name="currentFilter" value="${statusFilter}">
      </form>

      <!-- Modal chi tiết đơn -->
      <div class="modal-confirm-backdrop" id="orderDetailModal">
        <div class="modal-confirm-box" style="text-align:left;max-width:460px;">
          <h3 style="text-align:center;" id="orderDetailModalTitle">Chi tiết đơn</h3>
          <div class="delivery-info-row"><svg class="icon"><use href="#ic-user"/></svg><div><div class="dl">Khách hàng</div><div class="dv" id="orderDetailCustomer"></div></div></div>
          <div class="delivery-info-row"><svg class="icon"><use href="#ic-bag"/></svg><div><div class="dl">Sản phẩm</div><div class="dv" id="orderDetailProducts"></div></div></div>
          <div class="delivery-info-row"><svg class="icon"><use href="#ic-card"/></svg><div><div class="dl">Thanh toán</div><div class="dv" id="orderDetailPayment"></div></div></div>
         <div class="delivery-info-row" id="orderDetailShippingRow" style="display:none;"><svg class="icon"><use href="#ic-truck"/></svg><div><div class="dl">Trạng thái vận chuyển</div><div class="dv" id="orderDetailShipping"></div></div></div>
          <div class="delivery-info-row"><svg class="icon"><use href="#ic-clock"/></svg><div><div class="dl">Ngày đặt</div><div class="dv" id="orderDetailDate"></div></div></div>
          <%-- MỚI: hiện rõ lý do hủy khi đơn bị hủy — đặc biệt phân biệt "tự động
               hủy do không liên hệ được sau 3 lần" với hủy tay thông thường,
               tránh Staff/Manager tưởng nhầm là hủy thủ công. --%>
          <div class="delivery-info-row" id="orderDetailCancelRow" style="display:none;"><svg class="icon"><use href="#ic-alert-circle"/></svg><div><div class="dl">Lý do hủy</div><div class="dv" id="orderDetailCancelReason"></div></div></div>
          <button class="btn-outline-ink btn-block-shop mt-2" onclick="document.getElementById('orderDetailModal').classList.remove('open')">Đóng</button>
        </div>
      </div>

      <!-- Toast thông báo kết quả -->
      <div class="result-toast" id="resultToast">
        <div class="rt-icon"><svg class="icon" style="width:18px;"><use href="#ic-check"/></svg></div>
        <div class="rt-text" id="resultToastText">Đã xác nhận xử lý đơn thành công.</div>
      </div>
    </div><%-- admin-content --%>
  </div><%-- admin-main --%>
</div><%-- admin-shell --%>
<script>
let currentOrderId = null;

// Dữ liệu chi tiết đơn hàng, build sẵn từ JSTL để modal "Xem chi tiết" đổ dữ liệu vào — không cần gọi thêm API.
const orderDataMap = {};
<c:forEach var="o" items="${orders}">
orderDataMap[${o.orderId}] = {
  status: "${o.status}",
  customer: "${fn:escapeXml(o.receiverName)} — ${fn:escapeXml(o.receiverPhone)}",
  products: "<c:forEach var='d' items='${o.details}' varStatus='ds'>${fn:escapeXml(d.productNameSnapshot)} x${d.quantity}<c:if test='${!ds.last}'>, </c:if></c:forEach>",
  <%-- SỬA: công thức cũ chỉ phân biệt PAID/không-PAID, khiến REFUND_PENDING
       và REFUNDED đều bị gộp nhầm vào "Chưa thanh toán" — sai vì tiền THỰC
       SỰ đã được thanh toán, chỉ là đang/đã hoàn lại. --%>
  payment: "${o.paymentMethod == 'COD' ? 'COD' : 'Chuyển khoản / QR'} — <c:choose><c:when test='${o.paymentStatus == "PAID"}'>Đã thanh toán</c:when><c:when test='${o.paymentStatus == "REFUND_PENDING"}'>Đã thanh toán — chờ hoàn tiền</c:when><c:when test='${o.paymentStatus == "REFUNDED"}'>Đã hoàn tiền</c:when><c:otherwise>Chưa thanh toán</c:otherwise></c:choose>",
  shipping: "${o.status == 'SHIPPED' ? 'Đang giao hàng' : (o.status == 'DELIVERED' ? 'Đã giao thành công' : '')}",
  date: "<fmt:formatDate value='${o.orderDate}' pattern='dd/MM/yyyy HH:mm'/>",
  cancelReason: "${o.cancelReason}",
  needsRefundIfCancelled: ${o.paymentMethod == 'ONLINE_QR' && o.paymentStatus == 'PAID'}
};
</c:forEach>

function openConfirmOrderModal(orderId){
  currentOrderId = orderId;
  document.getElementById('confirmOrderModalTitle').textContent = `Xác nhận xử lý đơn #\${orderId}?`;
  document.getElementById('confirmOrderModal').classList.add('open');
}
function finalizeConfirmOrder(){
  // TRƯỚC ĐÂY: chỉ đóng modal + hiện toast giả "Đã chuyển sang Đang xử lý",
  // KHÔNG hề gọi lên server — đơn hàng thật không hề đổi trạng thái dù giao
  // diện báo thành công. Giờ submit form thật vào servlet updateStatus.
  document.getElementById('confirmOrderModal').classList.remove('open');
  document.getElementById('realConfirmOrderId').value = currentOrderId;
  document.getElementById('realConfirmOrderForm').submit();
}

function openPrintLabelModal(orderId){
  currentOrderId = orderId;
  document.getElementById('printLabelModalTitle').textContent = `In nhãn vận chuyển — Đơn #\${orderId}?`;
  document.getElementById('printLabelModal').classList.add('open');
}
function finalizePrintLabel(){
  // TRƯỚC ĐÂY: chỉ đóng modal + hiện toast giả "Đã in nhãn...", KHÔNG hề gọi
  // lên server — đơn hàng thật không hề chuyển sang "Đang giao". Giờ submit
  // form thật vào servlet updateStatus.
  document.getElementById('printLabelModal').classList.remove('open');
  document.getElementById('realPrintLabelOrderId').value = currentOrderId;
  document.getElementById('realPrintLabelForm').submit();
}

function openQuickActions(orderId){
  currentOrderId = orderId;
  document.getElementById('quickActionsModalTitle').textContent = `Thao tác — Đơn #\${orderId}`;

  // MỚI: nút "Hủy đơn hàng" TRƯỚC ĐÂY luôn hiện bất kể trạng thái đơn — kể cả
  // đơn đã SHIPPED/DELIVERED/CANCELLED — bấm vào chỉ nhận về lỗi chung chung
  // "Cập nhật trạng thái thất bại" vì server đã chặn theo state machine
  // (ALLOWED_TRANSITIONS trong DAOOrder), nhưng giao diện không hề báo trước.
  // Giờ chỉ hiện nút hủy khi đơn đang ở trạng thái THẬT SỰ hủy được.
  const info = orderDataMap[orderId];
  const cancellableStatuses = ['PENDING_PAYMENT', 'PENDING', 'PROCESSING', 'ON_HOLD'];
  const canCancel = info && cancellableStatuses.includes(info.status);
  document.getElementById('qaCancelBtn').style.display  = canCancel ? 'flex' : 'none';
  document.getElementById('qaCancelHint').style.display = canCancel ? 'none' : 'block';

  document.getElementById('quickActionsModal').classList.add('open');
}

function contactCustomer(orderId){
  showResultToast(`Đã liên hệ khách hàng đơn #\${orderId}.`);
}

function openCancelOrderModal(orderId){
  currentOrderId = orderId;
  document.getElementById('cancelOrderModalTitle').textContent = `Hủy đơn #\${orderId}?`;
  const info = orderDataMap[orderId];
  document.getElementById('cancelOrderRefundNote').style.display =
      (info && info.needsRefundIfCancelled) ? 'block' : 'none';
  document.getElementById('cancelOrderModal').classList.add('open');
}
function finalizeCancelOrder(){
  // TRƯỚC ĐÂY: chỉ đóng modal + hiện toast "Đã hủy đơn thành công" bằng JS,
  // KHÔNG hề gọi lên server — đơn hàng thật không hề bị huỷ dù giao diện báo
  // thành công. Giờ submit form thật, để server (đã có sẵn state-machine +
  // hoàn kho + đánh dấu REFUND_PENDING) xử lý, rồi mới điều hướng theo kết quả
  // thật (qua flashMessage), không tự bịa thông báo ở đây nữa.
  document.getElementById('realCancelOrderId').value = currentOrderId;
  document.getElementById('realCancelForm').submit();
}

function openOrderDetailModal(orderId){
  currentOrderId = orderId;
  document.getElementById('orderDetailModalTitle').textContent = `Chi tiết đơn #\${orderId}`;

  const d = orderDataMap[orderId];
  const shippingRow = document.getElementById('orderDetailShippingRow');
  const cancelRow = document.getElementById('orderDetailCancelRow');
  if (d) {
    document.getElementById('orderDetailCustomer').textContent = d.customer || '—';
    document.getElementById('orderDetailProducts').textContent = d.products || '—';
    document.getElementById('orderDetailPayment').textContent  = d.payment  || '—';
    document.getElementById('orderDetailDate').textContent     = d.date    || '—';
    if (d.shipping) {
      document.getElementById('orderDetailShipping').textContent = d.shipping;
      shippingRow.style.display = '';
    } else {
      shippingRow.style.display = 'none';
    }
    // Chỉ hiện dòng "Lý do hủy" khi đơn thực sự đã hủy và có lý do ghi nhận
    if (d.status === 'CANCELLED' && d.cancelReason) {
      const reasonLabel = d.cancelReason === 'AUTO_NO_CONTACT'
          ? 'Tự động hủy do không liên hệ được khách sau 3 lần gọi điện'
          : (d.cancelReason === 'CUSTOMER_REQUEST' ? 'Khách hàng yêu cầu hủy' : d.cancelReason);
      document.getElementById('orderDetailCancelReason').textContent = reasonLabel;
      cancelRow.style.display = '';
    } else {
      cancelRow.style.display = 'none';
    }
  } else {
    document.getElementById('orderDetailCustomer').textContent = 'Không tìm thấy dữ liệu đơn hàng.';
    document.getElementById('orderDetailProducts').textContent = '';
    document.getElementById('orderDetailPayment').textContent  = '';
    document.getElementById('orderDetailDate').textContent     = '';
    shippingRow.style.display = 'none';
  }

  document.getElementById('orderDetailModal').classList.add('open');
}

function openViewReportModal(orderId){
  document.getElementById('viewReportModalTitle').textContent = `Báo cáo hàng lỗi — Đơn #\${orderId}`;
  document.getElementById('viewReportModal').classList.add('open');
}

function showResultToast(text, isError){
  const toast = document.getElementById('resultToast');
  document.getElementById('resultToastText').textContent = text;
  toast.classList.toggle('error', !!isError);
  toast.classList.add('show');
  setTimeout(()=> toast.classList.remove('show'), 3500);
}
</script>
</body>
</html>