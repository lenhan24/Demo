<%-- 
    Document   : SignUp
    Created on : Jun 27, 2026, 3:54:13 PM
    Author     : GiGaByte
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Đăng ký tài khoản — Booky Mart</title>
<link href="https://cdnjs.cloudflare.com/ajax/libs/twitter-bootstrap/5.3.3/css/bootstrap.min.css" rel="stylesheet">
<link href="https://fonts.googleapis.com/css2?family=Fraunces:opsz,wght@9..144,400;9..144,600;9..144,700&family=Inter:wght@400;500;600;700&family=JetBrains+Mono:wght@500&display=swap" rel="stylesheet">
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css">

</head>
<body>


<jsp:include page="assets/header.jsp" />


<div class="container">
  <div class="auth-shell">
    <div class="row g-4 align-items-center w-100">

      <!-- Left: branding panel -->
      <div class="col-lg-5 d-none d-lg-block">
          <div class="auth-side">
              <svg class="icon quote-icon"><use href="#ic-book-open"/></svg>
              <h3>Cùng bạn kiến tạo không gian sống và học tập.</h3>
              <p>Dù là đắm mình vào những trang sách hay tỉ mỉ cùng những món đồ văn phòng phẩm, chúng mình ở đây để đồng hành cùng bạn trong từng khoảnh khắc sáng tạo mỗi ngày.</p>

              <div class="stat-row">
                  <div><div class="num">Chọn lọc</div><div class="lbl">Tri thức</div></div>
                  <div><div class="num">Cảm hứng</div><div class="lbl">Sáng tạo</div></div>
                  <div><div class="num">Tận tâm</div><div class="lbl">Đồng hành</div></div>
              </div>
          </div>
      </div>

      <!-- Right: register form -->
      <div class="col-lg-7">
        <div class="auth-card-shop">
  <h1>Tạo tài khoản mới</h1>
  <p class="sub">Đăng ký để bắt đầu mua sắm tại Booky Mart</p>

  <c:if test="${not empty errorMessage}">
    <div class="alert-banner error" style="display:flex;">
      <svg class="icon"><use href="#ic-alert-circle"/></svg>
      <div>${errorMessage}</div>
    </div>
  </c:if>

  <form id="registerForm" action="register" method="POST" novalidate>

    <div class="form-group-shop">
      <label class="form-label-shop">Họ và tên <span class="required-mark">*</span></label>
      <div class="input-icon-wrap">
        <svg class="icon"><use href="#ic-user"/></svg>
        <input type="text" class="form-control-shop" name="fullName" id="fullName" 
               value="${oldFullName}" 
               placeholder="Nguyễn Văn An" minlength="2" maxlength="100" required>
      </div>
      <div class="form-error" id="fullNameError" style="display:none;">Họ tên phải có từ 2 đến 100 ký tự.</div>
    </div>

    <div class="form-group-shop">
      <label class="form-label-shop">Địa chỉ email <span class="required-mark">*</span></label>
      <div class="input-icon-wrap">
        <svg class="icon"><use href="#ic-mail"/></svg>
        <input type="email" class="form-control-shop" name="email" id="email" 
               value="${oldEmail}" 
               placeholder="ten@email.com" required oninput="document.getElementById('emailExistsAlert').style.display='none'">
      </div>
      <div class="form-error" id="emailError" style="display:none;">Vui lòng nhập một địa chỉ email hợp lệ.</div>
    </div>

    <div class="form-group-shop">
      <label class="form-label-shop">Mật khẩu <span class="required-mark">*</span></label>
      <div class="input-icon-wrap">
        <svg class="icon"><use href="#ic-lock"/></svg>
        <input type="password" class="form-control-shop" name="password" id="password" placeholder="Tối thiểu 8 ký tự" minlength="8" maxlength="64" required oninput="checkPasswordStrength()">
        <button type="button" class="toggle-pw" onclick="togglePw('password',this)"><svg class="icon"><use href="#ic-eye"/></svg></button>
      </div>
      <div class="pw-strength-track" id="pwStrengthTrack">
        <div class="seg"></div><div class="seg"></div><div class="seg"></div>
      </div>
      <div class="pw-rule-list">
        <div class="rule" id="ruleLen"><svg class="icon"><use href="#ic-check"/></svg> 8–64 ký tự</div>
        <div class="rule" id="ruleLetter"><svg class="icon"><use href="#ic-check"/></svg> Có ít nhất 1 chữ cái</div>
        <div class="rule" id="ruleDigit"><svg class="icon"><use href="#ic-check"/></svg> Có ít nhất 1 chữ số</div>
      </div>
    </div>

    <div class="form-group-shop">
      <label class="form-label-shop">Xác nhận mật khẩu <span class="required-mark">*</span></label>
      <div class="input-icon-wrap">
        <svg class="icon"><use href="#ic-lock"/></svg>
        <input type="password" class="form-control-shop" id="confirmPassword" placeholder="Nhập lại mật khẩu" required oninput="checkConfirmPassword()">
        <button type="button" class="toggle-pw" onclick="togglePw('confirmPassword',this)"><svg class="icon"><use href="#ic-eye"/></svg></button>
      </div>
      <div class="form-error" id="confirmError" style="display:none;">Mật khẩu xác nhận không khớp.</div>
    </div>

  

    <div class="check-shop mb-1">
      <input type="checkbox" id="agreeTerms" onchange="document.getElementById('termsError').style.display='none'" required>
      <label for="agreeTerms">Tôi đồng ý với <a href="#" style="color:var(--terracotta);">Điều khoản dịch vụ</a> và <a href="#" style="color:var(--terracotta);">Chính sách bảo mật</a> của Booky Mart.</label>
    </div>
    <div class="form-error mb-3" id="termsError" style="display:none;">Bạn cần đồng ý với Điều khoản &amp; Chính sách bảo mật để tiếp tục.</div>

    <button type="submit" class="btn-terracotta btn-block-shop">Tạo tài khoản</button>
  </form>

  <div class="verify-pending-box" id="verifyBox" style="display:none;">
    <svg class="icon"><use href="#ic-mail"/></svg>
    <div>
      <div class="vt">Xác minh email để hoàn tất đăng ký</div>
      <div class="vd">
        Booky Mart đã gửi một email xác minh đến địa chỉ bạn vừa đăng ký (MSG 17).
        Bạn có thể đăng nhập ngay, nhưng <strong>cần xác minh email trước khi đặt hàng</strong> (BR-GU06.3).
      </div>
    </div>
  </div>
  <div class="auth-footer-link">Đã có tài khoản? <a href="Login.jsp">Đăng nhập ngay</a></div>
</div>
      </div>
    </div>
  </div>
</div>

<script>
function togglePw(id, btn){
  const inp = document.getElementById(id);
  const isPw = inp.type === 'password';
  inp.type = isPw ? 'text' : 'password';
  btn.innerHTML = isPw
    ? '<svg class="icon"><use href="#ic-eye-off"/></svg>'
    : '<svg class="icon"><use href="#ic-eye"/></svg>';
}

function checkPasswordStrength(){
  const pw = document.getElementById('password').value;
  const lenOk = pw.length >= 8 && pw.length <= 64;
  const letterOk = /[A-Za-z]/.test(pw);
  const digitOk = /[0-9]/.test(pw);

  setRule('ruleLen', lenOk);
  setRule('ruleLetter', letterOk);
  setRule('ruleDigit', digitOk);

  const metCount = [lenOk, letterOk, digitOk].filter(Boolean).length;
  const segs = document.querySelectorAll('#pwStrengthTrack .seg');
  segs.forEach((s,i)=>{
    s.className = 'seg';
    if (i < metCount){
      s.classList.add(metCount === 1 ? 'weak' : metCount === 2 ? 'medium' : 'strong');
    }
  });
  checkConfirmPassword();
}

function setRule(id, met){
  const el = document.getElementById(id);
  el.classList.toggle('met', met);
  el.querySelector('svg use').setAttribute('href', met ? '#ic-check' : '#ic-check');
}

function checkConfirmPassword(){
  const pw = document.getElementById('password').value;
  const cpw = document.getElementById('confirmPassword').value;
  const err = document.getElementById('confirmError');
  if (cpw.length > 0 && cpw !== pw){
    err.style.display = 'block';
  } else {
    err.style.display = 'none';
  }
}

function handleRegister(e){
  e.preventDefault();
  let valid = true;

  const fullName = document.getElementById('fullName').value.trim();
  if (fullName.length < 2 || fullName.length > 100){
    document.getElementById('fullNameError').style.display = 'block';
    valid = false;
  } else {
    document.getElementById('fullNameError').style.display = 'none';
  }

  const email = document.getElementById('email').value.trim();
  const emailRe = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  if (!emailRe.test(email)){
    document.getElementById('emailError').style.display = 'block';
    valid = false;
  } else {
    document.getElementById('emailError').style.display = 'none';
  }

  const pw = document.getElementById('password').value;
  const pwOk = pw.length >= 8 && pw.length <= 64 && /[A-Za-z]/.test(pw) && /[0-9]/.test(pw);
  if (!pwOk) valid = false;

  const cpw = document.getElementById('confirmPassword').value;
  if (cpw !== pw){
    document.getElementById('confirmError').style.display = 'block';
    valid = false;
  }

  // E2: Terms checkbox not checked -> block submission
  if (!document.getElementById('agreeTerms').checked){
    document.getElementById('termsError').style.display = 'block';
    valid = false;
  }

  // E1 demo: email "taken@bookymart.vn" giả lập email đã tồn tại
  if (email.toLowerCase() === 'taken@bookymart.vn'){
    document.getElementById('emailExistsAlert').style.display = 'flex';
    valid = false;
  }

  if (!valid) return false;

  // Success: BR-GU06.4 -> role = Customer; gửi verification email (MSG 17 / ET2)
  document.getElementById('registerForm').style.display = 'none';
  document.getElementById('verifyBox').style.display = 'flex';
  return false;
}
</script>

<!-- Footer -->
<jsp:include page="assets/footer.jsp"></jsp:include>
 

</body>
</html>
