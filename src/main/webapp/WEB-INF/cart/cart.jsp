<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Your Cart - SchemeCraft</title>
  <link rel="preconnect" href="https://fonts.googleapis.com">
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
  <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/header.css">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/footer.css">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/cart.css">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/confirm-modal.css">
  <script src="${pageContext.request.contextPath}/js/confirm-modal.js" defer></script>
</head>
<body>
<%@ include file="/WEB-INF/fragments/header.jsp" %>

<main class="cart-page">
  <h1 class="cart-page__title">Your Cart</h1>

  <c:choose>

    <%-- Empty cart --%>
    <c:when test="${empty cartItems}">
      <div class="cart-empty">
        <p class="cart-empty__message">Your cart is empty.</p>
        <a href="${pageContext.request.contextPath}/product" class="btn btn-primary">Browse Catalog</a>
      </div>
    </c:when>

    <c:otherwise>
      <div class="cart-layout">

        <!-- Cart items list -->
        <div class="cart-list" id="cartList">
          <c:forEach var="item" items="${cartItems}">
            <c:set var="p" value="${item.product}"/>
            <div class="cart-row" data-product-id="${p.productId}">

              <div class="cart-row__icon">
                <c:choose>
                  <c:when test="${not empty item.coverImagePath}">
                    <img src="${pageContext.request.contextPath}/${item.coverImagePath}" alt="${p.productName}">
                  </c:when>
                  <c:otherwise>
                    <img src="${pageContext.request.contextPath}/icons/chest.png" alt="No image available">
                  </c:otherwise>
                </c:choose>
              </div>

              <div class="cart-row__info">
                <span class="cart-row__name">${p.productName}</span>
                <c:choose>
                  <c:when test="${p.discount > 0}">
                                        <span class="cart-row__price cart-row__price--original">
                                            <fmt:formatNumber value="${p.price}" type="currency" currencySymbol="${currencySymbol}"/>
                                        </span>
                    <span class="cart-row__price cart-row__price--discounted">
                                            <fmt:formatNumber value="${p.price * (1 - p.discount / 100)}" type="currency" currencySymbol="${currencySymbol}"/>
                                        </span>
                  </c:when>
                  <c:otherwise>
                                        <span class="cart-row__price">
                                            <fmt:formatNumber value="${p.price}" type="currency" currencySymbol="${currencySymbol}"/>
                                        </span>
                  </c:otherwise>
                </c:choose>
              </div>

              <button class="cart-row__remove" onclick="removeFromCart('${p.productId}')" title="Remove from cart" aria-label="Remove from cart">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <line x1="18" y1="6" x2="6" y2="18"></line>
                  <line x1="6" y1="6" x2="18" y2="18"></line>
                </svg>
              </button>
            </div>
          </c:forEach>
        </div>

        <!-- Summary panel -->
        <aside class="cart-summary">
          <div class="cart-summary__title">Order Summary</div>

          <div class="cart-summary__row cart-summary__row--total">
            <span>Estimated total</span>
            <span id="cartEstimatedTotal">
                            <c:set var="total" value="0"/>
                            <c:forEach var="item" items="${cartItems}">
                              <c:set var="p" value="${item.product}"/>
                              <c:set var="total" value="${total + (p.price * (1 - p.discount / 100))}"/>
                            </c:forEach>
                            <fmt:formatNumber value="${total}" type="currency" currencySymbol="${currencySymbol}"/>
                        </span>
          </div>
          <p class="cart-summary__note">Taxes are calculated at checkout.</p>

          <c:choose>
            <c:when test="${not empty sessionScope.userSession && sessionScope.userSession.loggedIn}">
              <button class="btn btn-primary" onclick="checkout()">Checkout</button>
            </c:when>
            <c:otherwise>
              <a href="${pageContext.request.contextPath}/auth/login" class="btn btn-primary">
                Log in to Checkout
              </a>
            </c:otherwise>
          </c:choose>

          <button class="cart-summary__clear" onclick="clearCart()">Clear cart</button>
        </aside>

      </div>
    </c:otherwise>

  </c:choose>

  <div class="cart-status-toast" id="cartStatusToast"></div>
</main>

<%@ include file="/WEB-INF/fragments/footer.jsp" %>

<script>
  // Exposes the app's context path to plain JS files, so API calls
  // built in cart.js resolve correctly regardless of deploy path.
  const CONTEXT_PATH = "${pageContext.request.contextPath}";
</script>
<script src="${pageContext.request.contextPath}/js/cart.js"></script>
</body>
</html>
