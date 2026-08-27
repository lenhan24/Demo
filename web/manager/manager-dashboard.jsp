<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Tổng quan & Báo cáo — Manager | Booky Mart</title>
<link href="https://cdnjs.cloudflare.com/ajax/libs/twitter-bootstrap/5.3.3/css/bootstrap.min.css" rel="stylesheet">
<link href="https://fonts.googleapis.com/css2?family=Fraunces:opsz,wght@9..144,400;9..144,600;9..144,700&family=Inter:wght@400;500;600;700&family=JetBrains+Mono:wght@500&display=swap" rel="stylesheet">
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css">
<script src="https://cdn.jsdelivr.net/npm/chart.js@4"></script>
</head>
<body>

<%@ include file="/WEB-INF/jspf/icons.jspf" %>

<div class="admin-shell">
  <div class="admin-sidebar">
    <a class="brand" href="${pageContext.request.contextPath}/home">Booky<span class="dot">.</span>Mart</a>
    <div class="group-label">Manager Panel</div>
      <a href="${pageContext.request.contextPath}/manager/dashboard" class="active"><svg class="icon"><use href="#ic-dashboard"/></svg> Tổng quan & Báo cáo</a>
      <a href="${pageContext.request.contextPath}/manager/products" class=""><svg class="icon"><use href="#ic-package"/></svg> Sản phẩm</a>
    
      <a href="${pageContext.request.contextPath}/manager/promotions" class=""><svg class="icon"><use href="#ic-percent"/></svg> Khuyến mãi</a>
    <div class="group-label">Khác</div>
      <a href="${pageContext.request.contextPath}/home"><svg class="icon"><use href="#ic-arrow-right"/></svg> Về trang khách hàng</a>
      <a href="${pageContext.request.contextPath}/logout" style="color:#E29B8A;"><svg class="icon"><use href="#ic-logout"/></svg> Đăng xuất</a>
  </div>

  <div class="admin-main">
    <div class="admin-topbar">
      <div style="font-weight:700;color:var(--ink);">Tổng quan & Báo cáo</div>
      <div class="d-flex align-items-center gap-3">
        <button class="icon-btn"><svg class="icon"><use href="#ic-bell"/></svg></button>
        <%@ include file="/WEB-INF/jspf/account-info.jspf" %>
      </div>
    </div>

    <div class="admin-content">
      <div class="page-title-row">
        <div>
          <h1>Tổng quan & Báo cáo</h1>
          <div class="sub">${fromLabel} — ${toLabel} · Chỉ tính đơn hàng đã DELIVERED</div>
        </div>
        <div class="d-flex align-items-center gap-2">
          <div class="range-tabs">
            <a href="?range=7" class="${rangeDays == 7 ? 'active' : ''}">7 ngày</a>
            <a href="?range=30" class="${rangeDays == 30 ? 'active' : ''}">30 ngày</a>
            <a href="?range=90" class="${rangeDays == 90 ? 'active' : ''}">90 ngày</a>
          </div>
          <a href="${pageContext.request.contextPath}/manager/dashboard?export=excel&range=${rangeDays}" class="btn-outline-ink btn-sm-shop">
            <svg class="icon" style="width:14px;vertical-align:-2px;"><use href="#ic-package"/></svg> Xuất Excel
          </a>
        </div>
      </div>

      <%-- Flash message --%>
      <c:if test="${not empty flashMessage}">
        <div class="alert ${flashError ? 'alert-danger' : 'alert-success'} mb-3" role="alert">${flashMessage}</div>
      </c:if>

      <%-- MỚI: đơn đang chờ hoàn tiền — sinh ra từ đơn hủy đã thanh toán
           online, hoặc yêu cầu hoàn hàng Staff đã tiếp nhận xong. Manager là
           người xác nhận đã CHUYỂN KHOẢN HOÀN TIỀN THẬT cho khách (ngoài hệ
           thống), rồi bấm xác nhận ở đây để đóng hẳn vòng đời của đơn. --%>
      <c:if test="${not empty pendingRefunds}">
        <div class="return-request-banner">
          <div class="panel-title">
            <svg class="icon" style="width:15px;vertical-align:-2px;"><use href="#ic-alert-circle"/></svg>
            Đơn đang chờ hoàn tiền (${fn:length(pendingRefunds)})
          </div>
          <c:forEach var="r" items="${pendingRefunds}">
            <div class="return-request-row">
              <div class="rr-main">
                <strong>Đơn #${r.orderCode}</strong> — ${r.receiverName} · ${r.receiverPhone} · ${r.accountEmail}
                <div class="rr-sub">
                  Cần hoàn: <fmt:formatNumber value="${r.totalAmount}" pattern="#,###"/>đ
                  — ${r.status == 'CANCELLED' ? 'do đơn bị hủy sau khi đã thanh toán online' : 'do yêu cầu hoàn hàng đã tiếp nhận xong'}.
                  Vui lòng chuyển khoản hoàn tiền cho khách trước, rồi mới bấm xác nhận.
                </div>
              </div>
              <form action="${pageContext.request.contextPath}/manager/dashboard" method="post"
                    onsubmit="return confirm('Xác nhận ĐÃ chuyển khoản hoàn tiền xong cho đơn #${r.orderCode}? Hành động này không thể hoàn tác.');">
                <input type="hidden" name="action" value="confirmRefund">
                <input type="hidden" name="orderId" value="${r.orderId}">
                <button type="submit" class="btn-sm-shop btn-terracotta">Đã hoàn tiền xong</button>
              </form>
            </div>
          </c:forEach>
        </div>
      </c:if>

      <div class="d-flex gap-3 mb-4" style="flex-wrap:wrap;">
        <div class="dash-card tone-revenue" style="flex:1 1 0;min-width:180px;">
            <div class="label">Doanh thu <br>(đã trừ voucher)</div>
          <div class="value"><fmt:formatNumber value="${summary.totalRevenue}" pattern="#,##0"/>đ</div>
        </div>
        <div class="dash-card tone-profit" style="flex:1 1 0;min-width:180px;">
            <div class="label">Lợi nhuận <br>(totalRevenue - totalCost)</div>
          <div class="value"><fmt:formatNumber value="${summary.grossProfit}" pattern="#,##0"/>đ</div>
        </div>
        <div class="dash-card tone-cost" style="flex:1 1 0;min-width:180px;">
          <div class="label">Chi phí voucher</div>
          <div class="value"><fmt:formatNumber value="${summary.totalVoucherDiscount}" pattern="#,##0"/>đ</div>
        </div>
        <div class="dash-card tone-neutral" style="flex:1 1 0;min-width:180px;">
          <div class="label">Tổng đơn hàng</div>
          <div class="value">${summary.totalOrders}</div>
        </div>
        <div class="dash-card tone-neutral" style="flex:1 1 0;min-width:180px;">
          <div class="label">Giá trị TB/ 1 đơn hàng (AOV)</div>
          <div class="value"><fmt:formatNumber value="${summary.avgOrderValue}" pattern="#,##0"/>đ</div>
        </div>
      </div>

      <div class="row g-3 mb-3">
        <div class="col-md-8">
          <div class="dash-chart-box">
            <div class="dash-chart-title">Doanh thu theo ngày</div>
            <canvas id="revenueByDateChart" height="90"></canvas>
          </div>
        </div>
        <div class="col-md-4">
          <div class="dash-chart-box">
            <div class="dash-chart-title">Doanh thu theo danh mục</div>
            <canvas id="revenueByCategoryChart" height="90"></canvas>
          </div>
        </div>
      </div>

      <div class="row g-3 mb-4">
        <div class="col-md-12">
          <div class="dash-chart-box">
            <div class="dash-chart-title">Doanh thu theo tháng (12 tháng gần nhất)</div>
            <canvas id="revenueByMonthChart" height="70"></canvas>
          </div>
        </div>
      </div>

      <div class="row g-3">
        <div class="col-md-7">
          <div class="dash-chart-box">
            <div class="dash-chart-title">Top ${topProducts.size()} sản phẩm bán chạy</div>
            <table class="table-shop">
              <thead><tr><th>Sản phẩm</th><th>SKU</th><th>SL bán</th><th>Doanh thu</th></tr></thead>
              <tbody>
                <c:forEach var="t" items="${topProducts}">
                  <tr>
                    <td>${t.productName}</td>
                    <td class="mono">${t.sku}</td>
                    <td class="mono">${t.totalQuantitySold}</td>
                    <td class="mono"><fmt:formatNumber value="${t.totalRevenue}" pattern="#,##0"/>đ</td>
                  </tr>
                </c:forEach>
                <c:if test="${empty topProducts}">
                  <tr><td colspan="4" style="text-align:center;color:var(--ink-soft);">Chưa có dữ liệu bán hàng trong khoảng thời gian này.</td></tr>
                </c:if>
              </tbody>
            </table>
          </div>
        </div>

        <div class="col-md-5">
          <div class="dash-chart-box">
            <div class="dash-chart-title">
              <svg class="icon" style="width:14px;color:#C24545;"><use href="#ic-box-alert"/></svg>
              Cảnh báo tồn kho thấp (≤ 10)
            </div>
            <table class="table-shop">
              <thead><tr><th>Sản phẩm</th><th>SKU</th><th>Tồn kho</th></tr></thead>
              <tbody>
                <c:forEach var="p" items="${lowStockProducts}">
                  <tr>
                    <td>${p.name}</td>
                    <td class="mono">${p.sku}</td>
                    <td><span class="low-stock-badge">${p.stockQuantity}</span></td>
                  </tr>
                </c:forEach>
                <c:if test="${empty lowStockProducts}">
                  <tr><td colspan="3" style="text-align:center;color:var(--ink-soft);">Không có sản phẩm nào sắp hết hàng.</td></tr>
                </c:if>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  </div>
</div>

<script>
const revenueDates = [<c:forEach var="r" items="${revenueByDate}" varStatus="loop">'${r.dateLabel}'<c:if test="${!loop.last}">,</c:if></c:forEach>];
const revenueValues = [<c:forEach var="r" items="${revenueByDate}" varStatus="loop">${r.revenue}<c:if test="${!loop.last}">,</c:if></c:forEach>];

new Chart(document.getElementById('revenueByDateChart'), {
  type: 'line',
  data: {
    labels: revenueDates,
    datasets: [{
      label: 'Doanh thu',
      data: revenueValues,
      borderColor: '#C2542D',
      backgroundColor: 'rgba(194,84,45,.1)',
      fill: true,
      tension: 0.3
    }]
  },
  options: {
    plugins: { legend: { display: false } },
    scales: { y: { ticks: { callback: v => v.toLocaleString('vi-VN') + 'đ' } } }
  }
});

const categoryLabels = [<c:forEach var="c" items="${revenueByCategory}" varStatus="loop">'${c.categoryName}'<c:if test="${!loop.last}">,</c:if></c:forEach>];
const categoryValues = [<c:forEach var="c" items="${revenueByCategory}" varStatus="loop">${c.totalRevenue}<c:if test="${!loop.last}">,</c:if></c:forEach>];

new Chart(document.getElementById('revenueByCategoryChart'), {
  type: 'doughnut',
  data: {
    labels: categoryLabels,
    datasets: [{
      data: categoryValues,
      backgroundColor: ['#C2542D', '#5B6B4F', '#D9A441', '#42506A', '#A4421F', '#7A2E14']
    }]
  },
  options: {
    plugins: { legend: { position: 'bottom', labels: { boxWidth: 12, font: { size: 11 } } } }
  }
});

const monthLabels = [<c:forEach var="r" items="${revenueByMonth}" varStatus="loop">'${r.dateLabel}'<c:if test="${!loop.last}">,</c:if></c:forEach>];
const monthValues = [<c:forEach var="r" items="${revenueByMonth}" varStatus="loop">${r.revenue}<c:if test="${!loop.last}">,</c:if></c:forEach>];

new Chart(document.getElementById('revenueByMonthChart'), {
  type: 'bar',
  data: {
    labels: monthLabels,
    datasets: [{
      label: 'Doanh thu',
      data: monthValues,
      backgroundColor: '#C2542D'
    }]
  },
  options: {
    plugins: { legend: { display: false } },
    scales: { y: { ticks: { callback: v => v.toLocaleString('vi-VN') + 'đ' } } }
  }
});
</script>
</body>
</html>