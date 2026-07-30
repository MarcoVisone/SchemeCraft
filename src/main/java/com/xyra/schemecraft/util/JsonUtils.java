package com.xyra.schemecraft.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.xyra.schemecraft.dto.*;
import com.xyra.schemecraft.model.*;
import org.json.JSONArray;
import org.json.JSONObject;

public final class JsonUtils {

    private JsonUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    // =========================================================================
    // HTTP RESPONSE HELPERS
    // =========================================================================

    public static void sendJson(HttpServletResponse resp, JSONObject json, int statusCode) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.setStatus(statusCode);
        resp.getWriter().print(json.toString());
    }

    public static void sendSuccess(HttpServletResponse resp, String message) throws IOException {
        JSONObject json = new JSONObject();
        json.put("success", true);
        if (message != null) {
            json.put("message", message);
        }
        sendJson(resp, json, HttpServletResponse.SC_OK);
    }

    public static void sendSuccess(HttpServletResponse resp, String message, Object... keyValues) throws IOException {
        JSONObject json = new JSONObject();
        json.put("success", true);
        if (message != null) {
            json.put("message", message);
        }
        for (int i = 0; i < keyValues.length; i += 2) {
            if (i + 1 < keyValues.length) {
                json.put(keyValues[i].toString(), keyValues[i + 1]);
            }
        }
        sendJson(resp, json, HttpServletResponse.SC_OK);
    }

    public static void sendSuccess(HttpServletResponse resp, String dataKey, Object data) throws IOException {
        JSONObject json = new JSONObject();
        json.put("success", true);

        if (data instanceof List<?> list) {
            JSONArray array = serializeList(list);
            json.put(dataKey, array);
        } else {
            json.put(dataKey, data);
        }

        sendJson(resp, json, HttpServletResponse.SC_OK);
    }

    public static void sendSuccessWithData(HttpServletResponse resp, String message, String dataKey, Object data) throws IOException {
        JSONObject json = new JSONObject();
        json.put("success", true);
        if (message != null) {
            json.put("message", message);
        }

        if (data instanceof List<?> list) {
            json.put(dataKey, serializeList(list));
        } else {
            json.put(dataKey, serializeSingle(data));
        }

        sendJson(resp, json, HttpServletResponse.SC_OK);
    }

    public static void sendError(HttpServletResponse resp, String errorMessage, int statusCode) throws IOException {
        JSONObject json = new JSONObject();
        json.put("success", false);
        json.put("error", errorMessage);
        sendJson(resp, json, statusCode);
    }

    // =========================================================================
    // HTTP REQUEST HELPERS
    // =========================================================================

    public static JSONObject readJsonBody(HttpServletRequest req) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = req.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        return new JSONObject(sb.toString());
    }

    // =========================================================================
    // SERIALIZERS (Model -> JSONObject)
    // =========================================================================

    public static JSONObject serializeProduct(ProductBean product) {
        if (product == null) {
            return new JSONObject();
        }

        JSONObject obj = new JSONObject();
        obj.put("productId", product.getProductId());
        obj.put("accountId", product.getAccountId());
        obj.put("currencyId", product.getCurrencyId());
        obj.put("productName", product.getProductName());
        obj.put("description", product.getDescription());
        obj.put("price", product.getPrice());
        obj.put("discount", product.getDiscount());

        obj.put("averageRating", product.getAverageRating());
        obj.put("totalReviews", product.getTotalReviews());
        obj.put("totalDownloads", product.getTotalDownloads());
        obj.put(
                "createdAt",
                product.getCreatedAt() != null
                        ? product.getCreatedAt().toString()
                        : null
        );
        obj.put("stockQuantity", product.getStockQuantity());
        obj.put("isActive", product.isActive());

        return obj;
    }

    public static JSONObject serializeProductSuggestion(ProductSuggestionDTO suggestion) {
        if (suggestion == null) {
            return new JSONObject();
        }
        JSONObject obj = new JSONObject();
        obj.put("productId", suggestion.productId());
        obj.put("productName", suggestion.productName());
        obj.put("description", suggestion.description());
        obj.put("coverImagePath", suggestion.coverImagePath());
        return obj;
    }

    public static JSONObject serializeOrderAdminView(OrderAdminView view) {
        if (view == null) {
            return new JSONObject();
        }
        JSONObject obj = new JSONObject();
        obj.put("orderId", view.orderId());
        obj.put("accountId", view.accountId());
        obj.put("customerUsername", view.username());
        obj.put("customerEmail", view.email());
        obj.put("orderDate", view.createdAt() != null ? view.createdAt().toString() : null);
        obj.put("totalAmount", view.totalAmount());
        obj.put("status", view.status());
        return obj;
    }

    public static JSONObject serializeProductFull(ProductFullDTO bean) {
        if (bean == null) {
            return new JSONObject();
        }

        JSONObject obj = new JSONObject();

        // Serialize product (base data)
        if (bean.product() != null) {
            obj.put("product", serializeProduct(bean.product()));
        } else {
            obj.put("product", JSONObject.NULL);
        }

        // Serialize categories
        JSONArray categoriesArray = new JSONArray();
        if (bean.categories() != null) {
            for (CategoryBean cat : bean.categories()) {
                categoriesArray.put(serializeCategory(cat));
            }
        }
        obj.put("categories", categoriesArray);

        // Serialize image paths (as plain strings)
        JSONArray imagePathsArray = new JSONArray();
        if (bean.imagePaths() != null) {
            for (String path : bean.imagePaths()) {
                imagePathsArray.put(path);
            }
        }
        obj.put("imagePaths", imagePathsArray);

        // Serialize versions
        JSONArray versionsArray = new JSONArray();
        if (bean.versions() != null) {
            for (ProductVersionBean version : bean.versions()) {
                versionsArray.put(serializeVersion(version));
            }
        }
        obj.put("versions", versionsArray);

        return obj;
    }

    public static JSONObject serializeOrderStatus(OrderStatusBean status) {
        if (status == null) {
            return new JSONObject();
        }
        JSONObject obj = new JSONObject();
        obj.put("statusId", status.getStatusId());
        obj.put("statusName", status.getStatusName());
        return obj;
    }

    public static JSONObject serializeImage(ProductImageBean image) {
        if (image == null) {
            return new JSONObject();
        }

        JSONObject imgObj = new JSONObject();
        imgObj.put("imageId", image.getImageId());
        imgObj.put("productId", image.getProductId());
        imgObj.put("imagePath", image.getImagePath());
        imgObj.put("displayOrder", image.getDisplayOrder());
        return imgObj;
    }

    public static JSONObject serializeVersion(ProductVersionBean version) {
        if (version == null) {
            return new JSONObject();
        }

        JSONObject vObj = new JSONObject();
        vObj.put("versionId", version.getVersionId());
        vObj.put("productId", version.getProductId());
        vObj.put("version", version.getVersion());
        vObj.put("minecraftVersion", version.getMinecraftVersion());
        vObj.put("filePath", version.getFilePath());
        vObj.put("changelog", version.getChangelog());
        return vObj;
    }

    public static JSONObject serializeOwnedProductItem(OwnedProductItem item) {
        if (item == null) {
            return new JSONObject();
        }
        JSONObject obj = new JSONObject();
        obj.put("product", serializeProduct(item.getProduct()));
        obj.put("accountId", item.getOwnerAccountId());
        obj.put("isPurchased", item.isPurchased());
        obj.put("isOwned", item.isOwned());
        return obj;
    }

    public static JSONObject serializeAddress(AddressBean addr) {
        if (addr == null) {
            return new JSONObject();
        }
        JSONObject obj = new JSONObject();
        obj.put("addressId", addr.getAddressId());
        obj.put("countryId", addr.getCountryId());
        obj.put("streetAddress", addr.getStreetAddress());
        obj.put("city", addr.getCity());
        obj.put("stateProvince", addr.getStateProvince());
        obj.put("postalCode", addr.getPostalCode());
        obj.put("isDefault", addr.isDefault());
        obj.put("isActive", addr.isActive());
        return obj;
    }

    public static JSONObject serializePaymentMethod(PaymentMethodBean pm) {
        if (pm == null) {
            return new JSONObject();
        }
        JSONObject obj = new JSONObject();
        obj.put("paymentMethodId", pm.getPaymentMethodId());
        obj.put("methodType", pm.getMethodType());
        obj.put("cardBrand", pm.getCardBrand());
        obj.put("cardLastFour", pm.getCardLastFour());
        obj.put("cardExpiration", pm.getCardExpiration());
        obj.put("paymentEmail", pm.getPaymentEmail());
        obj.put("isDefault", pm.isDefault());
        return obj;
    }

    public static JSONObject serializeOrder(OrderBean order) {
        if (order == null) {
            return new JSONObject();
        }
        JSONObject obj = new JSONObject();
        obj.put("orderId", order.getOrderId());
        obj.put("accountId", order.getAccountId());
        obj.put("addressId", order.getAddressId());
        obj.put("currencyId", order.getCurrencyId());
        obj.put("methodType", order.getMethodType());
        obj.put("status", order.getStatus());
        obj.put("createdAt", order.getCreatedAt() != null ? order.getCreatedAt().toString() : null);
        obj.put("totalAmount", order.getTotalAmount());
        obj.put("transactionId", order.getTransactionId());
        return obj;
    }

    public static JSONObject serializeCategory(CategoryBean category) {
        if (category == null) {
            return new JSONObject();
        }
        JSONObject obj = new JSONObject();
        obj.put("categoryId", category.getCategoryId());
        obj.put("categoryName", category.getCategoryName());
        obj.put("parentCategoryId", category.getParentCategoryId());
        obj.put("description", category.getDescription());
        return obj;
    }

    public static JSONObject serializeCurrency(CurrencyBean currency) {
        if (currency == null) {
            return new JSONObject();
        }
        JSONObject obj = new JSONObject();
        obj.put("currencyId", currency.getCurrencyId());
        obj.put("currencyName", currency.getCurrencyName());
        obj.put("symbol", currency.getSymbol());
        obj.put("isActive", currency.isActive());
        return obj;
    }

    public static JSONObject serializeProductWithCategories(ProductBean product, List<CategoryBean> categories) {
        JSONObject obj = serializeProduct(product);

        JSONArray categoriesArray = new JSONArray();
        if (categories != null) {
            for (CategoryBean category : categories) {
                categoriesArray.put(serializeCategory(category));
            }
        }
        obj.put("categories", categoriesArray);

        return obj;
    }

    // Dispatches a single (non-list) model object to its dedicated serializer, so
    // sendSuccessWithData never relies on org.json's uncontrolled reflection fallback.
    private static Object serializeSingle(Object item) {
        if (item instanceof AddressBean addr) {
            return serializeAddress(addr);
        } else if (item instanceof PaymentMethodBean pm) {
            return serializePaymentMethod(pm);
        } else if (item instanceof OrderBean ord) {
            return serializeOrder(ord);
        } else if (item instanceof ProductBean prod) {
            return serializeProduct(prod);
        } else if (item instanceof CategoryBean cat) {
            return serializeCategory(cat);
        } else if (item instanceof ProductImageBean img) {
            return serializeImage(img);
        } else if (item instanceof ProductVersionBean ver) {
            return serializeVersion(ver);
        } else if (item instanceof OwnedProductItem opi) {
            return serializeOwnedProductItem(opi);
        } else if (item instanceof CurrencyBean cur) {
            return serializeCurrency(cur);
        } else if (item instanceof OrderStatusBean osb) {
            return serializeOrderStatus(osb);
        } else if (item instanceof OrderDetailDTO odd) {
            return serializeOrderDetail(odd);
        } else if (item instanceof ProductFullDTO prf) {
            return serializeProductFull(prf);
        } else if(item instanceof OrderAdminView view) {
            return serializeOrderAdminView(view);
        } else {
            return item;
        }
    }

    private static JSONArray serializeList(List<?> list) {
        JSONArray array = new JSONArray();
        for (Object item : list) {
            array.put(serializeSingle(item));
        }
        return array;
    }

    public static JSONObject serializeOrderItemDetail(OrderItemDetailDTO detail) {
        if (detail == null) {
            return new JSONObject();
        }

        JSONObject obj = new JSONObject();

        if (detail.getItem() != null) {
            OrderItemBean item = detail.getItem();
            obj.put("productId", item.getProductId());
            obj.put("pricePaid", item.getPrice());
            obj.put("discountApplied", item.getDiscount());
            obj.put("taxPaid", item.getTax());
            obj.put("lineTotal", detail.getLineTotal());
        }

        if (detail.getProduct() != null) {
            obj.put("product", serializeProduct(detail.getProduct()));
        }

        return obj;
    }

    public static JSONObject serializeOrderDetail(OrderDetailDTO detail) {
        if (detail == null) {
            return new JSONObject();
        }

        JSONObject obj = serializeOrder(detail.getOrder());

        if (detail.getStatusInfo() != null) {
            obj.put("statusInfo", serializeOrderStatus(detail.getStatusInfo()));
        }

        if (detail.getAddress() != null) {
            obj.put("address", serializeAddress(detail.getAddress()));
        }

        if (detail.getCurrency() != null) {
            obj.put("currency", serializeCurrency(detail.getCurrency()));
        }

        JSONArray itemsArray = new JSONArray();
        if (detail.getItems() != null) {
            for (OrderItemDetailDTO itemDetail : detail.getItems()) {
                itemsArray.put(serializeOrderItemDetail(itemDetail));
            }
        }
        obj.put("items", itemsArray);

        return obj;
    }
}
