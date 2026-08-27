<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <title>${pageTitle} - BookyMart</title>
        <link href="https://fonts.googleapis.com/css2?family=Fraunces:opsz,wght@9..144,400;9..144,600;9..144,700&family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
        <link href="https://cdnjs.cloudflare.com/ajax/libs/bootstrap/5.3.3/css/bootstrap.min.css" rel="stylesheet">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css">
        <!-- Toàn bộ CSS riêng của trang này đã gộp vào main.css (mục "sidebar lọc giá"). -->
    </head>
    <body>
        <jsp:include page="assets/header.jsp"></jsp:include>

        <div class="container" style="padding: 40px 15px 80px;">
            <h2 class="display" style="font-family: 'Fraunces', serif; font-weight: 700; color: #1F2B3D;">
                ${pageTitle}
            </h2>

            <div class="catalog-layout" style="margin-top: 20px;">
                <%-- MỚI: sidebar lọc theo danh mục — TRƯỚC ĐÂY không có cách nào
                     thu hẹp kết quả theo danh mục, chỉ lọc được theo type (Sách/VPP)
                     hoặc từ khóa riêng lẻ. --%>
                <aside class="filter-sidebar">
                    <h5>Danh mục</h5>
                    <c:choose>
                        <c:when test="${isSearchPage}">
                            <a href="search?keyword=${currentKeywordEncoded}" class="${empty currentCategoryId ? 'active' : ''}">Tất cả</a>
                        </c:when>
                       <c:otherwise>
                        <%-- SỬA: trước đây href="home" cố định -> bấm "Tất cả" bị nhảy về Trang
                        chủ, mất luôn context đang xem (Sách/VPP). Giờ quay về đúng "toàn bộ
                        loại đang xem", chỉ bỏ lọc theo danh mục con. --%>
                        <a href="${not empty currentType ? 'products?type='.concat(currentType) : 'home'}"
                        class="${empty currentCid ? 'active' : ''}">Tất cả</a>
</c:otherwise>
                    </c:choose>
                    <c:forEach var="cat" items="${categories}">
                        <c:choose>
                            <c:when test="${isSearchPage}">
                                <a href="search?keyword=${currentKeywordEncoded}&categoryId=${cat.categoryId}"
                                   class="${currentCategoryId == cat.categoryId ? 'active' : ''}">
                                    ${cat.name} <span class="count">${cat.productCount}</span>
                                </a>
                            </c:when>
                            <c:otherwise>
                                <a href="category?cid=${cat.categoryId}"
                                   class="${currentCid == cat.categoryId ? 'active' : ''}">
                                    ${cat.name} <span class="count">${cat.productCount}</span>
                                </a>
                            </c:otherwise>
                        </c:choose>
                    </c:forEach>

                    <h5>Khoảng giá</h5>
                    <c:choose>
                        <c:when test="${isSearchPage}">
                            <c:set var="filterAction" value="search" />
                        </c:when>
                        <c:when test="${not empty currentCid}">
                            <c:set var="filterAction" value="category" />
                        </c:when>
                        <c:otherwise>
                            <c:set var="filterAction" value="products" />
                        </c:otherwise>
                    </c:choose>
                    <form class="price-filter-form" method="get" action="${filterAction}">
                        <c:if test="${isSearchPage}"><input type="hidden" name="keyword" value="${currentKeyword}"></c:if>
                        <c:if test="${isSearchPage && not empty currentCategoryId}"><input type="hidden" name="categoryId" value="${currentCategoryId}"></c:if>
                        <c:if test="${not empty currentCid}"><input type="hidden" name="cid" value="${currentCid}"></c:if>
                        <c:if test="${not empty currentType}"><input type="hidden" name="type" value="${currentType}"></c:if>
                        <input type="number" name="minPrice" placeholder="Từ (đ)" min="0" value="${currentMinPrice}">
                        <input type="number" name="maxPrice" placeholder="Đến (đ)" min="0" value="${currentMaxPrice}">
                        <button type="submit">Áp dụng</button>
                    </form>
                </aside>

                <div class="catalog-main">
                    <c:if test="${empty listProducts}">
                        <div class="alert alert-warning text-center mt-2" style="border-radius: 12px; padding: 20px; border: 1px solid #ffeeba; background: #fff3cd;">
                            <p style="margin: 10px 0; font-size: 1.1rem; color: #856404;">Hiện chưa có sản phẩm khớp với bộ lọc hiện tại.</p>
                            <a href="home" style="color: #C2542D; font-weight: 600; text-decoration: underline;">Quay lại Trang Chủ</a>
                        </div>
                    </c:if>

                    <c:if test="${not empty listProducts}">
                        <div class="product-grid">
                            <c:forEach items="${listProducts}" var="p">
                                <div class="product-card">
                                    <div class="badge-new">Mới</div>
                                    <div class="fav-btn">♥</div>

                                    <div class="product-img-box">
                                        <c:choose>
                                            <c:when test="${not empty p.thumbnailUrl}">
                                                <img src="${empty p.thumbnailUrl ? 'assets/img/products/no-image.svg' : p.thumbnailUrl}" alt="${p.name}">
                                            </c:when>
                                            <c:otherwise>
                                                <img src="assets/img/products/no-image.svg" alt="No Image">
                                            </c:otherwise>
                                        </c:choose>
                                    </div>

                                    <p class="category">${p.categoryName}</p>
                                    <h3 class="name">${p.name}</h3>
                                    <p class="author">${p.authorOrBrand != null ? p.authorOrBrand : 'Đang cập nhật'}</p>
                                    <div class="stars">★★★★★</div>

                                    <div class="price-row">
                                        <p class="price">
                                            <fmt:formatNumber value="${p.price}" type="currency" pattern="###,###" />đ
                                        </p>
                                        <a href="productdt?id=${p.productId}" class="add-btn">+</a>
                                    </div>
                                </div>
                            </c:forEach>
                        </div>

                        <%-- MỚI: phân trang rõ ràng hơn — hiện "Trang X / Y" + nút Trước/Sau.
                             TRƯỚC ĐÂY dùng 1 biểu thức EL lồng 2-3 tầng ternary để tự suy luận
                             URL gốc ngay trong JSP -> rất dễ sai và khó dò lỗi. Giờ mỗi
                             controller (Product/Category/Search) tự tính sẵn 1 chuỗi
                             `paginationBaseUrl` gửi qua, JSP chỉ cần nối thêm "&page=N". --%>
                        <c:if test="${not empty endPage && endPage > 1}">
                            <div class="pagination-container">
                                <a href="${paginationBaseUrl}&page=${currentPage - 1}" class="page-link ${currentPage <= 1 ? 'disabled' : ''}">‹</a>
                                <c:forEach begin="1" end="${endPage}" var="i">
                                    <a href="${paginationBaseUrl}&page=${i}" class="page-link ${i == currentPage ? 'active' : ''}">${i}</a>
                                </c:forEach>
                                <a href="${paginationBaseUrl}&page=${currentPage + 1}" class="page-link ${currentPage >= endPage ? 'disabled' : ''}">›</a>
                            </div>
                            <div class="pagination-status">Trang ${currentPage} / ${endPage}<c:if test="${not empty totalProducts}"> · ${totalProducts} sản phẩm</c:if></div>
                        </c:if>
                    </c:if>
                </div>
            </div>
        </div>

        <jsp:include page="assets/footer.jsp"></jsp:include>

    </body>
</html>