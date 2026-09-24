<%-- 
    Document   : SignIn
    Created on : Jun 27, 2026, 5:41:06 PM
    Author     : GiGaByte
--%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="vi">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Đăng nhập — Booky Mart</title>
<link href="https://cdnjs.cloudflare.com/ajax/libs/twitter-bootstrap/5.3.3/css/bootstrap.min.css" rel="stylesheet">
<link href="https://fonts.googleapis.com/css2?family=Fraunces:opsz,wght@9..144,400;9..144,600;9..144,700&family=Inter:wght@400;500;600;700&family=JetBrains+Mono:wght@500&display=swap" rel="stylesheet">
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css">

</head>
<body>


<jsp:include page="assets/header.jsp" />


<div class="container">
  <div class="auth-shell">
    <div class="row g-4 align-items-center w-100">

      <!-- Left: branding panel chỗ này có lẽ sẽ phải chỉnh content nhưng k phải vấn đề chính nghiệp vụ gì cả chỉ là thông báo thôi-->
      <div class="col-lg-5 d-none d-lg-block">
          <div class="auth-side">
              <svg class="icon quote-icon"><use href="#ic-book-open"/></svg>
              <h3>Tài khoản của bạn tại BookyMart.</h3>
              <p>Đăng nhập để quản lý đơn hàng, theo dõi danh sách sản phẩm yêu thích và nhận thông báo về các ưu đãi mới nhất.</p>

              <div class="stat-row">
                  <div><div class="num">Lưu trữ</div><div class="lbl">Yêu thích</div></div>
                  <div><div class="num">Quản lý</div><div class="lbl">Đơn hàng</div></div>
                  <div><div class="num">Ưu đãi</div><div class="lbl">Hội viên</div></div>
              </div>
          </div>
      </div>

      <!-- Right: login form có áp dụng cơ chế lock tài khoản đọc cại srs nhá-->
      <div class="col-lg-7">
        <div class="auth-card-shop">
  <h1>Đăng nhập</h1>
  <p class="sub">Chào mừng bạn quay lại Booky Mart</p>


  <c:if test="${not empty errorMessage}">
    <div class="alert-banner error" id="wrongCredAlert" style="display:flex;">
      <svg class="icon"><use href="#ic-alert-circle"/></svg>
      <div>${errorMessage}</div>
    </div>
  </c:if>

  <%-- MỚI: hiện khi AuthFilter chặn tài khoản nội bộ (Staff/Manager/Admin)
       cố mua hàng/tự sửa thông tin — đưa về đây thay vì trang lỗi 403 trắng. --%>
  <c:if test="${param.blocked == 'internal'}">
    <div class="alert-banner error" style="display:flex;">
      <svg class="icon"><use href="#ic-alert-circle"/></svg>
      <div>Tài khoản nội bộ (Staff/Manager/Admin) không thể mua hàng. Vui lòng đăng xuất và đăng nhập lại bằng tài khoản khách hàng nếu muốn tiếp tục.</div>
    </div>
  </c:if>

  <form id="loginForm" action="login" method="POST" novalidate>
    <div class="form-group-shop">
      <label class="form-label-shop">Email</label>
      <div class="input-icon-wrap">
        <svg class="icon"><use href="#ic-mail"/></svg>
        <input type="email" class="form-control-shop" name="email" id="loginEmail" 
               value="${oldEmail}" placeholder="ten@email.com" required>
      </div>
    </div>

    <div class="form-group-shop mb-0">
      <label class="form-label-shop">Mật khẩu</label>
      <div class="input-icon-wrap">
        <svg class="icon"><use href="#ic-lock"/></svg>
        <input type="password" class="form-control-shop" name="password" id="loginPassword" placeholder="Nhập mật khẩu" required>
        <button type="button" class="toggle-pw" onclick="togglePw('loginPassword',this)"><svg class="icon"><use href="#ic-eye"/></svg></button>
      </div>
    </div>

    <div class="remember-row mt-3">
      <label><input type="checkbox" name="remember"> Ghi nhớ đăng nhập</label>
      <a href="forgotpassword">Quên mật khẩu?</a>
    </div>

    <button type="submit" class="btn-terracotta btn-block-shop mt-3" id="loginSubmitBtn">Đăng nhập</button>
  </form>

 

  <div class="auth-footer-link">Chưa có tài khoản? <a href="SignUp.jsp">Đăng ký ngay</a></div>
</div>
      </div>
    </div>
  </div>
</div>

<script>
let failedAttempts = 0;
let lockInterval = null;
const MAX_ATTEMPTS = 5;
const LOCK_SECONDS = 15 * 60;

function togglePw(id, btn){
  const inp = document.getElementById(id);
  const isPw = inp.type === 'password';
  inp.type = isPw ? 'text' : 'password';
  btn.innerHTML = isPw
    ? '<svg class="icon"><use href="#ic-eye-off"/></svg>'
    : '<svg class="icon"><use href="#ic-eye"/></svg>';
}

function handleLogin(e){
  e.preventDefault();

  // E2: nếu đang bị khóa, từ chối xác thực dù nhập đúng (BR-GU07.1)
  if (document.getElementById('lockoutBox').style.display === 'flex'){
    return false;
  }

  document.getElementById('wrongCredAlert').style.display = 'none';
  document.getElementById('cartMergeAlert').style.display = 'none';

  const email = document.getElementById('loginEmail').value.trim();
  const pw = document.getElementById('loginPassword').value;

  // Demo: coi mật khẩu "demo123" là đúng để minh hoạ luồng thành công
  const isCorrect = (pw === 'demo123');

  if (isCorrect){
    // BR-GU07.2: reset bộ đếm sau khi đăng nhập thành công
    failedAttempts = 0;
    document.getElementById('attemptWarning').style.display = 'none';

    // A1: merge giỏ hàng Guest vào Customer (UC-CUS-12 / MSG 14)
    document.getElementById('cartMergeAlert').style.display = 'flex';
    document.getElementById('loginSubmitBtn').textContent = 'Đang chuyển hướng...';
    document.getElementById('loginSubmitBtn').disabled = true;
    setTimeout(()=>{ location.href = 'index.html'; }, 1600);
    return false;
  }

  // E1: sai thông tin -> tăng bộ đếm, KHÔNG tiết lộ sai email hay sai password (MSG 1)
  failedAttempts++;
  document.getElementById('wrongCredAlert').style.display = 'flex';

  const remaining = MAX_ATTEMPTS - failedAttempts;
  if (remaining > 0){
    const warn = document.getElementById('attemptWarning');
    warn.style.display = 'flex';
    warn.innerHTML = '<svg class="icon" style="width:13px;color:#A4392F;"><use href="#ic-alert-circle"/></svg> Còn ' + remaining + ' lần thử trước khi tài khoản bị tạm khóa.';
  }

  if (failedAttempts >= MAX_ATTEMPTS){
    triggerLockout();
  }

  return false;
}

function triggerLockout(){
  document.getElementById('wrongCredAlert').style.display = 'none';
  document.getElementById('attemptWarning').style.display = 'none';
  document.getElementById('loginForm').style.opacity = '.45';
  document.getElementById('loginForm').style.pointerEvents = 'none';

  const box = document.getElementById('lockoutBox');
  box.style.display = 'flex';

  let seconds = LOCK_SECONDS;
  const el = document.getElementById('lockCountdown');
  if (lockInterval) clearInterval(lockInterval);
  lockInterval = setInterval(()=>{
    seconds--;
    const m = String(Math.floor(seconds/60)).padStart(2,'0');
    const s = String(seconds%60).padStart(2,'0');
    el.textContent = m + ':' + s;
    if (seconds <= 0){
      clearInterval(lockInterval);
      box.style.display = 'none';
      document.getElementById('loginForm').style.opacity = '1';
      document.getElementById('loginForm').style.pointerEvents = 'auto';
      failedAttempts = 0; // mở khóa, cho thử lại từ đầu
    }
  }, 1000);

  // Demo: rút ngắn xuống 5 giây để bạn xem được hết luồng nhanh, không cần chờ 15 phút thật
  clearInterval(lockInterval);
  let demoSeconds = 5;
  el.textContent = '00:0' + demoSeconds;
  lockInterval = setInterval(()=>{
    demoSeconds--;
    el.textContent = '00:0' + Math.max(demoSeconds,0);
    if (demoSeconds <= 0){
      clearInterval(lockInterval);
      box.style.display = 'none';
      document.getElementById('loginForm').style.opacity = '1';
      document.getElementById('loginForm').style.pointerEvents = 'auto';
      failedAttempts = 0;
    }
  }, 1000);
}
</script>

<!-- Footer -->
<jsp:include page="assets/footer.jsp"></jsp:include>

</body>
</html>
