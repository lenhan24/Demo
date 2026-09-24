<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Quản lý tài khoản — Admin | Booky Mart</title>
<link href="https://cdnjs.cloudflare.com/ajax/libs/twitter-bootstrap/5.3.3/css/bootstrap.min.css" rel="stylesheet">
<link href="https://fonts.googleapis.com/css2?family=Fraunces:opsz,wght@9..144,400;9..144,600;9..144,700&family=Inter:wght@400;500;600;700&family=JetBrains+Mono:wght@500&display=swap" rel="stylesheet">
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css">
</head>
<body>

<svg style="display:none" aria-hidden="true">
  <symbol id="ic-tag" viewBox="0 0 24 24"><path d="M20.59 13.41 11 3.83A2 2 0 0 0 9.59 3.24L3 3v6.59a2 2 0 0 0 .59 1.41l9.59 9.59a2 2 0 0 0 2.82 0l4.59-4.59a2 2 0 0 0 0-2.82Z"/><circle cx="7.5" cy="7.5" r="1.5"/></symbol>
  <symbol id="ic-truck" viewBox="0 0 24 24"><path d="M1 3h15v13H1zM16 8h4l3 3v5h-7zM5 21a2 2 0 1 0 0-4 2 2 0 0 0 0 4ZM19 21a2 2 0 1 0 0-4 2 2 0 0 0 0 4Z"/></symbol>
  <symbol id="ic-gift" viewBox="0 0 24 24"><path d="M20 12v9H4v-9M2 7h20v5H2zM12 22V7M12 7H7.5a2.5 2.5 0 1 1 0-5C11 2 12 7 12 7ZM12 7h4.5a2.5 2.5 0 1 0 0-5C13 2 12 7 12 7Z"/></symbol>
  <symbol id="ic-clock" viewBox="0 0 24 24"><circle cx="12" cy="12" r="10"/><path d="M12 6v6l4 2"/></symbol>
  <symbol id="ic-pin" viewBox="0 0 24 24"><path d="M21 10c0 6-9 12-9 12s-9-6-9-12a9 9 0 1 1 18 0Z"/><circle cx="12" cy="10" r="3"/></symbol>
  <symbol id="ic-phone" viewBox="0 0 24 24"><path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72c.127.96.362 1.903.7 2.81a2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45c.907.338 1.85.573 2.81.7A2 2 0 0 1 22 16.92Z"/></symbol>
  <symbol id="ic-search" viewBox="0 0 24 24"><circle cx="11" cy="11" r="8"/><path d="M21 21l-4.3-4.3"/></symbol>
  <symbol id="ic-heart" viewBox="0 0 24 24"><path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78Z"/></symbol>
  <symbol id="ic-user" viewBox="0 0 24 24"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></symbol>
  <symbol id="ic-bag" viewBox="0 0 24 24"><path d="M6 2 3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4Z"/><path d="M3 6h18"/><path d="M16 10a4 4 0 0 1-8 0"/></symbol>
  <symbol id="ic-book" viewBox="0 0 24 24"><path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"/><path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2Z"/></symbol>
  <symbol id="ic-book-open" viewBox="0 0 24 24"><path d="M2 3h6a4 4 0 0 1 4 4v14a3 3 0 0 0-3-3H2Z"/><path d="M22 3h-6a4 4 0 0 0-4 4v14a3 3 0 0 1 3-3h7Z"/></symbol>
  <symbol id="ic-grad" viewBox="0 0 24 24"><path d="M22 10 12 5 2 10l10 5 10-5Z"/><path d="M6 12v5c0 1.5 3 3 6 3s6-1.5 6-3v-5"/></symbol>
  <symbol id="ic-child" viewBox="0 0 24 24"><circle cx="12" cy="5" r="2"/><path d="M9 22 12 13l3 9"/><path d="M5 12l4-2 3 2 3-2 4 2"/></symbol>
  <symbol id="ic-pen" viewBox="0 0 24 24"><path d="M17 3a2.85 2.85 0 1 1 4 4L7.5 20.5 2 22l1.5-5.5Z"/></symbol>
  <symbol id="ic-note" viewBox="0 0 24 24"><path d="M15.5 3H6a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V9.5Z"/><path d="M15 3v6h6"/></symbol>
  <symbol id="ic-briefcase" viewBox="0 0 24 24"><rect x="2" y="7" width="20" height="14" rx="2"/><path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16"/></symbol>
  <symbol id="ic-arrow-right" viewBox="0 0 24 24"><path d="M5 12h14"/><path d="m12 5 7 7-7 7"/></symbol>
  <symbol id="ic-plus" viewBox="0 0 24 24"><path d="M12 5v14"/><path d="M5 12h14"/></symbol>
  <symbol id="ic-brain" viewBox="0 0 24 24"><path d="M9.5 2a3.5 3.5 0 0 0-3.5 3.5v.6A3 3 0 0 0 4 9v1a3 3 0 0 0 1 2.24V14a3 3 0 0 0 2 2.83V19a3 3 0 0 0 5 2.24"/><path d="M14.5 2A3.5 3.5 0 0 1 18 5.5v.6a3 3 0 0 1 2 2.9v1a3 3 0 0 1-1 2.24V14a3 3 0 0 1-2 2.83V19a3 3 0 0 1-5 2.24"/><path d="M12 4v18"/></symbol>
  <symbol id="ic-ruler" viewBox="0 0 24 24"><path d="M16 2 2 16l6 6L22 8Z"/><path d="m14.5 5.5 2 2"/><path d="m11.5 8.5 2 2"/><path d="m8.5 11.5 2 2"/></symbol>
  <symbol id="ic-highlighter" viewBox="0 0 24 24"><path d="m9 11-6 6v3h3l6-6"/><path d="m13 4 7 7-4 4-7-7Z"/></symbol>
  <symbol id="ic-archive" viewBox="0 0 24 24"><rect x="2" y="3" width="20" height="5" rx="1"/><path d="M4 8v11a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8"/><path d="M10 12h4"/></symbol>
  <symbol id="ic-layers" viewBox="0 0 24 24"><path d="m12 2 9 5-9 5-9-5Z"/><path d="m3 12 9 5 9-5"/><path d="m3 17 9 5 9-5"/></symbol>
  <symbol id="ic-mail" viewBox="0 0 24 24"><rect x="2" y="4" width="20" height="16" rx="2"/><path d="m22 6-10 7L2 6"/></symbol>
  <symbol id="ic-facebook" viewBox="0 0 24 24"><path d="M18 2h-3a5 5 0 0 0-5 5v3H7v4h3v8h4v-8h3l1-4h-4V7a1 1 0 0 1 1-1h3Z"/></symbol>
  <symbol id="ic-instagram" viewBox="0 0 24 24"><rect x="2" y="2" width="20" height="20" rx="5"/><circle cx="12" cy="12" r="4"/><circle cx="17.5" cy="6.5" r="1" fill="currentColor" stroke="none"/></symbol>
  <symbol id="ic-tiktok" viewBox="0 0 24 24"><path d="M9 12a4 4 0 1 0 4 4V4a5 5 0 0 0 5 5"/></symbol>
  <symbol id="ic-check" viewBox="0 0 24 24"><path d="M20 6 9 17l-5-5"/></symbol>
  <symbol id="ic-chevron-left" viewBox="0 0 24 24"><path d="m15 18-6-6 6-6"/></symbol>
  <symbol id="ic-chevron-right" viewBox="0 0 24 24"><path d="m9 18 6-6-6-6"/></symbol>
  <symbol id="ic-trending" viewBox="0 0 24 24"><path d="m22 7-8.5 8.5-5-5L1 18"/><path d="M16 7h6v6"/></symbol>
  <symbol id="ic-star" viewBox="0 0 24 24"><path d="M12 2 9.2 8.6 2 9.2l5.5 4.7L5.8 21 12 17.1 18.2 21l-1.7-7.1L22 9.2l-7.2-.6Z" fill="currentColor" stroke="none"/></symbol>
  <symbol id="ic-filter" viewBox="0 0 24 24"><path d="M22 3H2l8 9.46V19l4 2v-8.54L22 3Z"/></symbol>
  <symbol id="ic-chevron-down" viewBox="0 0 24 24"><path d="m6 9 6 6 6-6"/></symbol>
  <symbol id="ic-minus" viewBox="0 0 24 24"><path d="M5 12h14"/></symbol>
  <symbol id="ic-x-circle" viewBox="0 0 24 24"><circle cx="12" cy="12" r="10"/><path d="m15 9-6 6m0-6 6 6"/></symbol>
  <symbol id="ic-grid" viewBox="0 0 24 24"><rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/><rect x="3" y="14" width="7" height="7"/><rect x="14" y="14" width="7" height="7"/></symbol>
  <symbol id="ic-list" viewBox="0 0 24 24"><path d="M8 6h13M8 12h13M8 18h13"/><path d="M3 6h.01M3 12h.01M3 18h.01"/></symbol>
  <symbol id="ic-share" viewBox="0 0 24 24"><circle cx="18" cy="5" r="3"/><circle cx="6" cy="12" r="3"/><circle cx="18" cy="19" r="3"/><path d="m8.59 13.51 6.83 3.98M15.41 6.51 8.59 10.49"/></symbol>
  <symbol id="ic-shield" viewBox="0 0 24 24"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10Z"/></symbol>
  <symbol id="ic-x" viewBox="0 0 24 24"><path d="M18 6 6 18M6 6l12 12"/></symbol>
  <symbol id="ic-trash" viewBox="0 0 24 24"><path d="M3 6h18"/><path d="M8 6V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/><path d="M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6"/></symbol>
  <symbol id="ic-edit" viewBox="0 0 24 24"><path d="M17 3a2.85 2.85 0 1 1 4 4L7.5 20.5 2 22l1.5-5.5Z"/></symbol>
  <symbol id="ic-package" viewBox="0 0 24 24"><path d="m21 8-9-5-9 5 9 5 9-5Z"/><path d="M3 8v8l9 5 9-5V8"/><path d="M12 13v8"/></symbol>
  <symbol id="ic-settings" viewBox="0 0 24 24"><circle cx="12" cy="12" r="3"/><path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 1 1-2.83 2.83l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-4 0v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 1 1-2.83-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1 0-4h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 1 1 2.83-2.83l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 4 0v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 1 1 2.83 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 0 4h-.09a1.65 1.65 0 0 0-1.51 1Z"/></symbol>
  <symbol id="ic-key" viewBox="0 0 24 24"><circle cx="7.5" cy="15.5" r="5.5"/><path d="m21 2-9.6 9.6"/><path d="m15.5 7.5 3 3L22 7l-3-3"/></symbol>
  <symbol id="ic-bell" viewBox="0 0 24 24"><path d="M6 8a6 6 0 1 1 12 0c0 7 3 9 3 9H3s3-2 3-9"/><path d="M10.3 21a1.94 1.94 0 0 0 3.4 0"/></symbol>
  <symbol id="ic-dashboard" viewBox="0 0 24 24"><rect x="3" y="3" width="7" height="9"/><rect x="14" y="3" width="7" height="5"/><rect x="14" y="12" width="7" height="9"/><rect x="3" y="16" width="7" height="5"/></symbol>
  <symbol id="ic-dots" viewBox="0 0 24 24"><circle cx="12" cy="5" r="1.5" fill="currentColor" stroke="none"/><circle cx="12" cy="12" r="1.5" fill="currentColor" stroke="none"/><circle cx="12" cy="19" r="1.5" fill="currentColor" stroke="none"/></symbol>
  <symbol id="ic-download" viewBox="0 0 24 24"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><path d="M7 10l5 5 5-5"/><path d="M12 15V3"/></symbol>
  <symbol id="ic-users" viewBox="0 0 24 24"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></symbol>
  <symbol id="ic-wallet" viewBox="0 0 24 24"><path d="M21 12V7a2 2 0 0 0-2-2H5a2 2 0 0 0-2 2v10a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-5Z"/><path d="M16 12h2"/></symbol>
  <symbol id="ic-percent" viewBox="0 0 24 24"><path d="M19 5 5 19"/><circle cx="6.5" cy="6.5" r="2.5"/><circle cx="17.5" cy="17.5" r="2.5"/></symbol>
  <symbol id="ic-logout" viewBox="0 0 24 24"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><path d="M16 17l5-5-5-5"/><path d="M21 12H9"/></symbol>
  <symbol id="ic-history" viewBox="0 0 24 24"><path d="M3 3v5h5"/><path d="M3.05 13A9 9 0 1 0 6 5.3L3 8"/><path d="M12 7v5l4 2"/></symbol>
  <symbol id="ic-box-alert" viewBox="0 0 24 24"><path d="m21 8-9-5-9 5 9 5 9-5Z"/><path d="M3 8v8l9 5 9-5V8"/><path d="M12 11v3"/><path d="M12 17h.01"/></symbol>
  <symbol id="ic-clipboard" viewBox="0 0 24 24"><rect x="8" y="2" width="8" height="4" rx="1"/><path d="M16 4h2a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2h2"/></symbol>
  <symbol id="ic-clipboard-check" viewBox="0 0 24 24"><rect x="8" y="2" width="8" height="4" rx="1"/><path d="M16 4h2a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2h2"/><path d="m9 14 2 2 4-4"/></symbol>
  <symbol id="ic-camera" viewBox="0 0 24 24"><path d="m9 3 2 3h2l2-3h2a2 2 0 0 1 2 2v13a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2Z"/><circle cx="12" cy="13" r="4"/></symbol>
  <symbol id="ic-card" viewBox="0 0 24 24"><rect x="2" y="5" width="20" height="14" rx="2"/><path d="M2 10h20"/></symbol>
  <symbol id="ic-qr" viewBox="0 0 24 24"><rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/><rect x="3" y="14" width="7" height="7"/><path d="M14 14h3v3h-3zM20 14h1v1h-1zM14 20h1v1h-1zM20 20h1v1h-1z"/></symbol>
  <symbol id="ic-refresh" viewBox="0 0 24 24"><path d="M21 2v6h-6"/><path d="M3 12a9 9 0 0 1 15-6.7L21 8"/><path d="M3 22v-6h6"/><path d="M21 12a9 9 0 0 1-15 6.7L3 16"/></symbol>
  <symbol id="ic-smartphone" viewBox="0 0 24 24"><rect x="5" y="2" width="14" height="20" rx="2"/><path d="M12 18h.01"/></symbol>
  <symbol id="ic-lock" viewBox="0 0 24 24"><rect x="3" y="11" width="18" height="11" rx="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/></symbol>
  <symbol id="ic-eye" viewBox="0 0 24 24"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8Z"/><circle cx="12" cy="12" r="3"/></symbol>
  <symbol id="ic-eye-off" viewBox="0 0 24 24"><path d="M17.94 17.94A10.94 10.94 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 4.06-5.94M9.9 4.24A10.94 10.94 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19"/><path d="M14.12 14.12a3 3 0 1 1-4.24-4.24"/><path d="M1 1l22 22"/></symbol>
  <symbol id="ic-alert-circle" viewBox="0 0 24 24"><circle cx="12" cy="12" r="10"/><path d="M12 8v4M12 16h.01"/></symbol>
  <symbol id="ic-upload" viewBox="0 0 24 24"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><path d="M17 8l-5-5-5 5"/><path d="M12 3v12"/></symbol>
  <symbol id="ic-chat" viewBox="0 0 24 24"><path d="M21 11.5a8.38 8.38 0 0 1-9 8.37A8.38 8.38 0 0 1 3 11.5a8.5 8.5 0 0 1 17 0Z"/><path d="M8 11h.01M12 11h.01M16 11h.01"/></symbol>
</svg>

<div class="admin-shell">
  <div class="admin-sidebar">
    <a class="brand" href="${pageContext.request.contextPath}/index.html">Booky<span class="dot">.</span>Mart</a>
    <div class="group-label">Admin Panel</div>
      <a href="${pageContext.request.contextPath}/admin/roles" class=""><svg class="icon"><use href="#ic-shield"/></svg> Phân quyền tài khoản</a>
      <a href="${pageContext.request.contextPath}/admin/accounts" class="active"><svg class="icon"><use href="#ic-users"/></svg> Quản lý Staff/Manager</a>
      <a href="${pageContext.request.contextPath}/admin/logs" class=""><svg class="icon"><use href="#ic-history"/></svg> Lịch sử hoạt động</a>
    <div class="group-label">Khác</div>
     <a href="${pageContext.request.contextPath}/home"><svg class="icon"><use href="#ic-arrow-right"/></svg> Về trang khách hàng</a>
            <a href="${pageContext.request.contextPath}/logout" style="color:#E29B8A;"><svg class="icon"><use href="#ic-logout"/></svg> Đăng xuất</a>
  </div>

  <div class="admin-main">
    <div class="admin-topbar">
      <div style="font-weight:700;color:var(--ink);">Quản lý Staff/Manager</div>
      <div class="d-flex align-items-center gap-3">
        <button class="icon-btn"><svg class="icon"><use href="#ic-bell"/></svg></button>
       
      </div>
    </div>
    <div class="admin-content">
      <div class="page-title-row">
        <div>
          <h1>Quản lý tài khoản</h1>
          <div class="sub">Tạo, sửa, vô hiệu hóa tài khoản Staff và Manager</div>
        </div>
      </div>

      <c:if test="${not empty deleteAccountError}">
        <div class="alert-banner error mb-3">
          <svg class="icon"><use href="#ic-alert-circle"/></svg>
          <span>${deleteAccountError}</span>
        </div>
      </c:if>

      <div class="admin-toolbar">
        <form class="admin-search-box" method="get" action="${pageContext.request.contextPath}/admin/accounts">
          <svg class="icon"><use href="#ic-search"/></svg>
          <input type="text" name="q" value="${param.q}" placeholder="Tìm theo tên, email...">
          <c:if test="${not empty param.role}"><input type="hidden" name="role" value="${param.role}"></c:if>
        </form>
        <div class="d-flex gap-2">
          <form method="get" action="${pageContext.request.contextPath}/admin/accounts">
            <c:if test="${not empty param.q}"><input type="hidden" name="q" value="${param.q}"></c:if>
            <select class="sort-select" name="role" onchange="this.form.submit()">
              <option value="">Tất cả vai trò</option>
              <option value="CUSTOMER" ${param.role == 'CUSTOMER' ? 'selected' : ''}>Customer</option>
              <option value="STAFF" ${param.role == 'STAFF' ? 'selected' : ''}>Staff</option>
              <option value="MANAGER" ${param.role == 'MANAGER' ? 'selected' : ''}>Manager</option>
              <option value="ADMIN" ${param.role == 'ADMIN' ? 'selected' : ''}>Admin</option>
            </select>
          </form>
          <button class="btn-terracotta btn-sm-shop" onclick="document.getElementById('newAccountModal').classList.add('open')">
            <svg class="icon" style="width:14px;vertical-align:-2px;"><use href="#ic-plus"/></svg> Thêm tài khoản
          </button>
        </div>
      </div>

      <table class="table-shop">
        <thead><tr><th>Họ tên</th><th>Email</th><th>SĐT</th><th>Vai trò</th><th>Trạng thái</th><th></th></tr></thead>
        <tbody>
          <c:forEach var="acc" items="${accounts}">
            <tr ${acc.status != 'ACTIVE' ? "style='opacity:.6;'" : ''}>
              <td style="font-weight:600;">${acc.fullName}</td>
              <td>${acc.email}</td>
              <td class="mono">${acc.phone}</td>
              <td>
                <span class="role-badge ${acc.role == 'ADMIN' ? 'role-admin' : (acc.role == 'MANAGER' ? 'role-manager' : (acc.role == 'STAFF' ? 'role-staff' : 'role-customer'))}">${acc.role}</span>
              </td>
              <td>
                <span class="status-badge ${acc.status == 'ACTIVE' ? 'status-delivered' : (acc.status == 'LOCKED' ? 'status-locked' : 'status-cancelled')}">${acc.status}</span>
              </td>
              <td>
                <button type="button" class="row-action-btn" title="Sửa"
                  data-id="${acc.accountId}" data-fullname="${acc.fullName}"
                  data-email="${acc.email}" data-phone="${acc.phone}" data-role="${acc.role}"
                  data-status="${acc.status}"
                  data-lockeduntil="<c:if test="${acc.lockedUntil != null}"><fmt:formatDate value="${acc.lockedUntil}" pattern="yyyy-MM-dd'T'HH:mm"/></c:if>"
                  onclick="openEditModal(this)">
                  <svg class="icon"><use href="#ic-edit"/></svg>
                </button>
                <button type="button" class="row-action-btn" title="Xóa"
                  onclick="confirmDeleteAccount(${acc.accountId}, '${acc.fullName}')">
                  <svg class="icon"><use href="#ic-trash"/></svg>
                </button>
              </td>
            </tr>
          </c:forEach>
          <c:if test="${empty accounts}">
            <tr><td colspan="6" class="text-center" style="color:var(--ink-soft);padding:24px 0;">Chưa có tài khoản nào.</td></tr>
          </c:if>
        </tbody>
      </table>

      <c:set var="baseUrl" value="${pageContext.request.contextPath}/admin/accounts" scope="page"/>
      <%@ include file="/WEB-INF/pagination.jspf" %>

      <!-- Modal thêm tài khoản nội bộ -->
      <div class="modal-confirm-backdrop ${not empty createAccountError ? 'open' : ''}" id="newAccountModal">
        <div class="modal-confirm-box" style="text-align:left;max-width:460px;">
          <h3 style="text-align:center;">Thêm tài khoản nội bộ</h3>
          <c:if test="${not empty createAccountError}">
            <div class="alert-banner error">
              <svg class="icon"><use href="#ic-alert-circle"/></svg>
              <span>${createAccountError}</span>
            </div>
          </c:if>
          <form method="post" action="${pageContext.request.contextPath}/admin/accounts">
            <input type="hidden" name="page" value="${currentPage}">
            <c:if test="${not empty param.q}"><input type="hidden" name="q" value="${param.q}"></c:if>
            <c:if test="${not empty param.role}"><input type="hidden" name="role" value="${param.role}"></c:if>
            <div class="form-group-shop">
              <label class="form-label-shop">Họ và tên</label>
              <input class="form-control-shop" name="fullName" placeholder="2–100 ký tự" required minlength="2" maxlength="100">
            </div>
            <div class="form-group-shop">
              <label class="form-label-shop">Email</label>
              <input class="form-control-shop" type="email" name="email" placeholder="ten@bookymart.vn" required>
            </div>
            <div class="form-group-shop">
              <label class="form-label-shop">Số điện thoại</label>
              <input class="form-control-shop" name="phone" placeholder="VD: 0912345678 (chỉ chữ số)" pattern="[0-9]*">
            </div>
            <div class="form-group-shop">
              <label class="form-label-shop">Vai trò</label>
              <select class="form-select-shop" name="role">
                <option value="STAFF">Staff</option>
                <option value="MANAGER">Manager</option>
              </select>
            </div>
            <p class="form-hint mb-3">
              <svg class="icon" style="width:13px;vertical-align:-2px;"><use href="#ic-shield"/></svg>
              Chỉ Admin được tạo tài khoản Staff/Manager. Không thể tạo vai trò Admin hoặc Customer tại đây.
            </p>
            <div class="d-flex gap-2">
              <button type="button" class="btn-outline-ink btn-block-shop" onclick="document.getElementById('newAccountModal').classList.remove('open')">Hủy</button>
              <button type="submit" class="btn-terracotta btn-block-shop">Tạo tài khoản</button>
            </div>
          </form>
        </div>
      </div>

      <!-- Modal hiển thị mật khẩu tạm thời sau khi tạo tài khoản thành công -->
      <c:if test="${not empty createAccountSuccessPassword}">
        <div class="modal-confirm-backdrop open" id="newAccountSuccessModal">
          <div class="modal-confirm-box" style="text-align:left;max-width:420px;">
            <h3 style="text-align:center;">Đã tạo tài khoản thành công</h3>
            <p style="text-align:center;">
              <strong>${createAccountSuccessName}</strong><br>
              <span class="mono">${createAccountSuccessEmail}</span>
            </p>
            <p class="form-label-shop" style="margin-bottom:6px;">Mật khẩu tạm thời</p>
            <div class="form-control-shop mono mb-3" style="text-align:center;font-weight:700;letter-spacing:.05em;">
              ${createAccountSuccessPassword}
            </div>
            <p class="form-hint mb-3">
              <svg class="icon" style="width:13px;vertical-align:-2px;"><use href="#ic-shield"/></svg>
              Hãy gửi mật khẩu này cho nhân viên qua kênh an toàn (không phải email này) và yêu cầu đổi mật khẩu ngay sau lần đăng nhập đầu tiên. Mật khẩu này sẽ không hiển thị lại.
            </p>
            <button class="btn-terracotta btn-block-shop" onclick="document.getElementById('newAccountSuccessModal').classList.remove('open')">Đã hiểu</button>
          </div>
        </div>
      </c:if>
      <!-- Modal sửa tài khoản -->
      <div class="modal-confirm-backdrop ${not empty editAccountError ? 'open' : ''}" id="editAccountModal">
        <div class="modal-confirm-box" style="text-align:left;max-width:460px;">
          <h3 style="text-align:center;">Sửa thông tin tài khoản</h3>
          <c:if test="${not empty editAccountError}">
            <div class="alert-banner error">
              <svg class="icon"><use href="#ic-alert-circle"/></svg>
              <span>${editAccountError}</span>
            </div>
          </c:if>
          <form method="post" action="${pageContext.request.contextPath}/admin/accounts">
            <input type="hidden" name="action" value="update">
            <input type="hidden" name="page" value="${currentPage}">
            <c:if test="${not empty param.q}"><input type="hidden" name="q" value="${param.q}"></c:if>
            <c:if test="${not empty param.role}"><input type="hidden" name="role" value="${param.role}"></c:if>
            <input type="hidden" name="accountId" id="editAccountId" value="${editAccountId}">
            <div class="form-group-shop">
              <label class="form-label-shop">Họ và tên</label>
              <input class="form-control-shop" name="fullName" id="editFullName" value="${editFullName}" placeholder="2–100 ký tự" required minlength="2" maxlength="100">
            </div>
            <div class="form-group-shop">
              <label class="form-label-shop">Email</label>
              <input class="form-control-shop" type="email" name="email" id="editEmail" value="${editEmail}" placeholder="ten@bookymart.vn" required>
            </div>
            <div class="form-group-shop">
              <label class="form-label-shop">Số điện thoại</label>
              <input class="form-control-shop" name="phone" id="editPhone" value="${editPhone}" placeholder="VD: 0912345678 (chỉ chữ số)" pattern="[0-9]*">
            </div>
            <div class="form-group-shop">
              <label class="form-label-shop">Vai trò</label>
              <select class="form-select-shop" name="role" id="editRole">
                <option value="CUSTOMER">Customer</option>
                <option value="STAFF">Staff</option>
                <option value="MANAGER">Manager</option>
                <option value="ADMIN">Admin</option>
              </select>
            </div>
            <div class="form-group-shop">
              <label class="form-label-shop">Trạng thái</label>
              <select class="form-select-shop" name="status" id="editStatus" onchange="toggleLockedUntilField()">
                <option value="ACTIVE">Active</option>
                <option value="INACTIVE">Inactive (vô hiệu hóa vĩnh viễn)</option>
                <option value="LOCKED">Locked (khóa tạm thời)</option>
              </select>
            </div>
            <div class="form-group-shop" id="lockedUntilGroup" style="display:none;">
              <label class="form-label-shop">Khóa đến</label>
              <input class="form-control-shop" type="datetime-local" name="lockedUntil" id="editLockedUntil">
              <p class="form-hint" style="margin-top:5px;">Tài khoản sẽ tự động có thể hoạt động lại sau thời điểm này (theo logic đăng nhập ở phía khách hàng).</p>
            </div>
            <div class="d-flex gap-2">
              <button type="button" class="btn-outline-ink btn-block-shop" onclick="document.getElementById('editAccountModal').classList.remove('open')">Hủy</button>
              <button type="submit" class="btn-terracotta btn-block-shop">Lưu thay đổi</button>
            </div>
          </form>
        </div>
      </div>

      <!-- Modal xác nhận xóa tài khoản -->
      <div class="modal-confirm-backdrop" id="deleteAccountModal">
        <div class="modal-confirm-box">
          <div class="micon"><svg class="icon"><use href="#ic-trash"/></svg></div>
          <h3>Xóa tài khoản?</h3>
          <p id="deleteAccountText"></p>
          <div class="d-flex gap-2">
            <button type="button" class="btn-outline-ink btn-block-shop" onclick="document.getElementById('deleteAccountModal').classList.remove('open')">Hủy</button>
            <button type="button" class="btn-terracotta btn-block-shop" onclick="document.getElementById('deleteAccountForm').submit()">Xóa</button>
          </div>
        </div>
      </div>

      <!-- Form ẩn thực hiện xóa, submit khi bấm "Xóa" trong modal xác nhận -->
      <form id="deleteAccountForm" method="post" action="${pageContext.request.contextPath}/admin/accounts">
        <input type="hidden" name="action" value="delete">
        <input type="hidden" name="accountId" id="deleteAccountId">
        <input type="hidden" name="page" value="${currentPage}">
            <c:if test="${not empty param.q}"><input type="hidden" name="q" value="${param.q}"></c:if>
            <c:if test="${not empty param.role}"><input type="hidden" name="role" value="${param.role}"></c:if>
      </form>
    </div>
  </div>
</div>

<script>
  function toggleLockedUntilField() {
    var isLocked = document.getElementById('editStatus').value === 'LOCKED';
    document.getElementById('lockedUntilGroup').style.display = isLocked ? 'block' : 'none';
  }

  function openEditModal(btn) {
    document.getElementById('editAccountId').value = btn.dataset.id;
    document.getElementById('editFullName').value = btn.dataset.fullname;
    document.getElementById('editEmail').value = btn.dataset.email;
    document.getElementById('editPhone').value = btn.dataset.phone || '';
    document.getElementById('editRole').value = btn.dataset.role;
    document.getElementById('editStatus').value = btn.dataset.status;
    document.getElementById('editLockedUntil').value = btn.dataset.lockeduntil || '';
    toggleLockedUntilField();
    document.getElementById('editAccountModal').classList.add('open');
  }

  <c:if test="${not empty editAccountError}">
  // Re-open the edit modal with the values the admin just submitted, since validation failed server-side.
  document.getElementById('editStatus').value = '${editStatus}';
  document.getElementById('editLockedUntil').value = '${editLockedUntil}';
  toggleLockedUntilField();
  </c:if>

  function confirmDeleteAccount(accountId, fullName) {
    document.getElementById('deleteAccountId').value = accountId;
    document.getElementById('deleteAccountText').innerHTML =
      'Tài khoản <strong>' + fullName + '</strong> sẽ bị xóa vĩnh viễn khỏi hệ thống. Hành động này không thể hoàn tác.';
    document.getElementById('deleteAccountModal').classList.add('open');
  }
</script>

</body>
</html>
