<%--
    Document   : ForgotPassword
    MỚI: trước đây "Quên mật khẩu?" ở Login.jsp trỏ href="#" — link chết.
    Vì project chưa có hạ tầng gửi email (không JavaMail/SMTP), đây là bản
    tự khôi phục: khách nhập đúng Email + Số điện thoại khớp với DB thì được
    đặt mật khẩu mới ngay, không cần xác nhận qua email.
--%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="vi">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Quên mật khẩu — Booky Mart</title>
<link href="https://cdnjs.cloudflare.com/ajax/libs/twitter-bootstrap/5.3.3/css/bootstrap.min.css" rel="stylesheet">
<link href="https://fonts.googleapis.com/css2?family=Fraunces:opsz,wght@9..144,400;9..144,600;9..144,700&family=Inter:wght@400;500;600;700&family=JetBrains+Mono:wght@500&display=swap" rel="stylesheet">
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css">
</head>
<body>

<jsp:include page="assets/header.jsp" />

<div class="container">
  <div class="auth-shell">
    <div class="row g-4 align-items-center w-100">

        <div class="col-lg-5 d-none d-lg-block">
            <div class="auth-side">
                <svg class="icon quote-icon"><use href="#ic-lock"/></svg>
                <h3>Khôi phục truy cập tài khoản.</h3>
                <p>Vui lòng cung cấp Email và Số điện thoại đã đăng ký. Hệ thống sẽ hướng dẫn bạn các bước đặt lại mật khẩu nhanh chóng và an toàn.</p>
            </div>
        </div>

      <div class="col-lg-7">
        <div class="auth-card-shop">
          <h1>Quên mật khẩu</h1>
          <p class="sub">Nhập thông tin tài khoản để đặt lại mật khẩu mới</p>

          <c:if test="${not empty errorMessage}">
            <div class="alert-banner error" style="display:flex;">
              <svg class="icon"><use href="#ic-alert-circle"/></svg>
              <div>${errorMessage}</div>
            </div>
          </c:if>

          <c:if test="${not empty successMessage}">
            <div class="alert-banner success" style="display:flex;">
              <svg class="icon"><use href="#ic-check"/></svg>
              <div>${successMessage}</div>
            </div>
          </c:if>

          <form id="forgotForm" action="forgotpassword" method="POST" novalidate>
            <div class="form-group-shop">
              <label class="form-label-shop">Email</label>
              <div class="input-icon-wrap">
                <svg class="icon"><use href="#ic-mail"/></svg>
                <input type="email" class="form-control-shop" name="email" value="${oldEmail}" placeholder="ten@email.com" required>
              </div>
            </div>

            <div class="form-group-shop">
              <label class="form-label-shop">Số điện thoại đã đăng ký</label>
              <div class="input-icon-wrap">
                <svg class="icon"><use href="#ic-phone"/></svg>
                <input type="tel" class="form-control-shop" name="phone" value="${oldPhone}" placeholder="0xxxxxxxx" required>
              </div>
            </div>

            <div class="form-group-shop">
              <label class="form-label-shop">Mật khẩu mới</label>
              <div class="input-icon-wrap">
                <svg class="icon"><use href="#ic-lock"/></svg>
                <input type="password" class="form-control-shop" name="newPassword" id="newPassword" placeholder="Tối thiểu 8 ký tự" required minlength="8">
                <button type="button" class="toggle-pw" onclick="togglePw('newPassword',this)"><svg class="icon"><use href="#ic-eye"/></svg></button>
              </div>
            </div>

            <div class="form-group-shop mb-0">
              <label class="form-label-shop">Xác nhận mật khẩu mới</label>
              <div class="input-icon-wrap">
                <svg class="icon"><use href="#ic-lock"/></svg>
                <input type="password" class="form-control-shop" name="confirmPassword" id="confirmPassword" placeholder="Nhập lại mật khẩu mới" required minlength="8">
                <button type="button" class="toggle-pw" onclick="togglePw('confirmPassword',this)"><svg class="icon"><use href="#ic-eye"/></svg></button>
              </div>
            </div>

            <button type="submit" class="btn-terracotta btn-block-shop mt-4">Đặt lại mật khẩu</button>
          </form>

          <p class="switch-auth">Đã nhớ mật khẩu? <a href="Login.jsp">Đăng nhập</a></p>
        </div>
      </div>
    </div>
  </div>
</div>

<script>
function togglePw(id, btn){
  const el = document.getElementById(id);
  el.type = el.type === 'password' ? 'text' : 'password';
}
</script>

<jsp:include page="assets/footer.jsp" />

</body>
</html>
