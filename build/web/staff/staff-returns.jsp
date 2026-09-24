<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Tiếp nhận trả hàng — Staff | Booky Mart</title>
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
      <a href="${pageContext.request.contextPath}/staff/orders" class=""><svg class="icon"><use href="#ic-clipboard-check"/></svg> Xử lý đơn hàng</a>
      <a href="${pageContext.request.contextPath}/staff/returns" class="active"><svg class="icon"><use href="#ic-refresh"/></svg> Tiếp nhận trả hàng</a>
    <div class="group-label">Khác</div>
      <a href="${pageContext.request.contextPath}/home"><svg class="icon"><use href="#ic-arrow-right"/></svg> Về trang khách hàng</a>
      <a href="${pageContext.request.contextPath}/logout" style="color:#E29B8A;"><svg class="icon"><use href="#ic-logout"/></svg> Đăng xuất</a>
  </div>

  <div class="admin-main">
    <div class="admin-topbar">
      <div style="font-weight:700;color:var(--ink);">Tiếp nhận trả hàng</div>
      <div class="d-flex align-items-center gap-3">
        <button class="icon-btn"><svg class="icon"><use href="#ic-bell"/></svg></button>
        <%@ include file="/WEB-INF/jspf/account-info.jspf" %>
      </div>
    </div>
    <div class="admin-content">
      <div class="page-title-row">
        <div>
          <h1>Tiếp nhận trả hàng / hoàn tiền</h1>
          <div class="sub">Khách bấm "Hoàn hàng" ở trang chi tiết đơn sẽ xuất hiện ở đây — xác nhận khi đã nhận lại hàng thật để tự động cộng lại tồn kho</div>
        </div>
      </div>

      <%-- Flash message --%>
      <c:if test="${not empty flashMessage}">
        <div class="alert ${flashError ? 'alert-danger' : 'alert-success'} mb-3" role="alert">${flashMessage}</div>
      </c:if>

      <%-- Thẻ thống kê --%>
      <div class="row g-3 mb-4">
        <div class="col-md-4"><div class="stat-card">
          <div class="label"><svg class="icon" style="width:14px;"><use href="#ic-clock"/></svg> Đang chờ tiếp nhận</div>
          <div class="value">${fn:length(pendingReturns)}</div>
        </div></div>
      </div>

      <h2 class="section-title mb-3" style="font-size:1.1rem;">Đang chờ tiếp nhận</h2>
      <c:choose>
        <c:when test="${empty pendingReturns}">
          <div class="panel" style="text-align:center;color:var(--admin-text-soft);padding:32px 0;">
            Không có yêu cầu hoàn hàng nào đang chờ xử lý.
          </div>
        </c:when>
        <c:otherwise>
          <div class="row g-3 mb-4">
            <c:forEach var="r" items="${pendingReturns}">
              <div class="col-md-6">
                <div class="panel">
                  <div class="panel-title">
                    <span><svg class="icon"><use href="#ic-refresh"/></svg> Đơn #${r.orderCode}</span>
                    <span class="status-badge status-pending">Chờ tiếp nhận</span>
                  </div>
                  <div class="delivery-info-row"><svg class="icon"><use href="#ic-user"/></svg>
                    <div><div class="dl">Khách hàng</div><div class="dv">${r.receiverName} · ${r.receiverPhone} · ${r.accountEmail}</div></div>
                  </div>
                  <div class="delivery-info-row"><svg class="icon"><use href="#ic-alert-circle"/></svg>
                    <div><div class="dl">Lý do</div><div class="dv">${not empty r.returnReason ? r.returnReason : '(không có)'}</div></div>
                  </div>
                  <div class="delivery-info-row"><svg class="icon"><use href="#ic-package"/></svg>
                    <div><div class="dl">Tổng đơn</div><div class="dv"><fmt:formatNumber value="${r.totalAmount}" pattern="#,###"/>đ</div></div>
                  </div>
                  <a href="${pageContext.request.contextPath}/staff/orders?keyword=${r.orderCode}" target="_blank"
                     style="display:inline-flex;align-items:center;gap:5px;font-size:.78rem;color:var(--terracotta);font-weight:600;margin:8px 0;text-decoration:none;">
                    <svg class="icon" style="width:13px;"><use href="#ic-eye"/></svg> Xem chi tiết đơn hàng gốc
                  </a>
                 <%-- MỚI: 2 nút thay 1 — phân biệt hàng còn tốt (cộng lại
                       kho) và hàng lỗi/hư hỏng (KHÔNG cộng kho, tránh bán
                       nhầm hàng lỗi cho khách khác), vẫn hoàn tiền bình
                       thường ở cả 2 trường hợp. --%>
                  <div class="d-flex gap-2 mt-2">
                    <form action="${pageContext.request.contextPath}/staff/returns" method="post" style="flex:1;"
                          onsubmit="return confirm('Xác nhận hàng trả về CÒN TỐT cho đơn #${r.orderCode}? Hệ thống sẽ cộng lại số lượng vào kho.');">
                      <input type="hidden" name="action" value="confirmReturn">
                      <input type="hidden" name="orderId" value="${r.orderId}">
                      <input type="hidden" name="quality" value="good">
                      <button type="submit" class="btn-terracotta btn-block-shop">Hàng tốt — cộng lại kho</button>
                    </form>
                    <form action="${pageContext.request.contextPath}/staff/returns" method="post" style="flex:1;"
                          onsubmit="return confirm('Xác nhận hàng trả về BỊ LỖI/HƯ HỎNG cho đơn #${r.orderCode}? Hệ thống sẽ KHÔNG cộng lại kho, chỉ xử lý hoàn tiền.');">
                      <input type="hidden" name="action" value="confirmReturn">
                      <input type="hidden" name="orderId" value="${r.orderId}">
                      <input type="hidden" name="quality" value="bad">
                      <button type="submit" class="btn-outline-ink btn-block-shop">Hàng lỗi — không cộng kho</button>
                    </form>
                  </div>
                  <form action="${pageContext.request.contextPath}/staff/orders" method="post"
                        onsubmit="return confirm('Từ chối yêu cầu hoàn hàng cho đơn #${r.orderCode}?');">
                      <input type="hidden" name="action" value="rejectReturn">
                      <input type="hidden" name="orderId" value="${r.orderId}">
                      <input type="hidden" name="currentFilter" value="${statusFilter}">
                      <button type="submit" class="btn-sm-shop btn-danger-outline" style="margin-top: 10px">Từ chối</button>
                  </form>
                </div>
              </div>
            </c:forEach>
          </div>
        </c:otherwise>
      </c:choose>

      <h2 class="section-title mt-4 mb-3" style="font-size:1.1rem;">Lịch sử đã tiếp nhận</h2>
      <table class="table-shop">
        <thead><tr>
          <th>Đơn hàng</th><th>Khách hàng</th><th>Tổng đơn</th><th>Thanh toán</th>
        </tr></thead>
        <tbody>
          <c:choose>
            <c:when test="${empty completedReturns}">
              <tr><td colspan="4" style="text-align:center;color:var(--admin-text-soft);padding:24px;">Chưa có yêu cầu nào đã tiếp nhận xong.</td></tr>
            </c:when>
            <c:otherwise>
              <c:forEach var="r" items="${completedReturns}">
                <tr>
                  <td class="mono">
                    <a href="${pageContext.request.contextPath}/staff/orders?keyword=${r.orderCode}" target="_blank" style="color:var(--terracotta);text-decoration:none;">#${r.orderCode}</a>
                  </td>
                  <td>${r.receiverName} · ${r.receiverPhone}</td>
                  <td class="mono"><fmt:formatNumber value="${r.totalAmount}" pattern="#,###"/>đ</td>
                  <td>
                    <c:choose>
                      <c:when test="${r.paymentStatus == 'REFUND_PENDING'}"><span class="status-badge status-hold">Chờ hoàn tiền</span></c:when>
                      <c:when test="${r.paymentStatus == 'REFUNDED'}"><span class="status-badge status-delivered">Đã hoàn tiền</span></c:when>
                      <c:when test="${r.paymentStatus == 'PAID'}"><span class="status-badge status-delivered">Đã thanh toán</span></c:when>
                      <c:otherwise><span class="status-badge status-cancelled">Chưa thanh toán</span></c:otherwise>
                    </c:choose>
                  </td>
                </tr>
              </c:forEach>
            </c:otherwise>
          </c:choose>
        </tbody>
      </table>

    </div>
  </div>
</div>
</body>
</html>
