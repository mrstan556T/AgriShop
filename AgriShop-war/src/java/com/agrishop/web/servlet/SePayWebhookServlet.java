package com.agrishop.web.servlet;

import com.agrishop.service.OrderManagementServiceLocal;
import jakarta.ejb.EJB;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import jakarta.servlet.ServletException;
import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * SePayWebhookServlet — nhận callback tự động từ SePay khi MB Bank nhận được tiền.
 *
 * Cấu hình tại SePay Dashboard (https://sepay.vn):
 *   Webhook URL: https://<your-domain>/AgriShop-war/sepay-webhook
 *   Phương thức:  POST / JSON
 *   Secret Token: Cấu hình trong web.xml → context-param "SEPAY_SECRET_TOKEN"
 *
 * JSON body SePay gửi về (tham khảo: https://docs.sepay.vn):
 * {
 *   "id":              1234,
 *   "gateway":         "MBBank",
 *   "transactionDate": "2024-01-01 10:00:00",
 *   "accountNumber":   "0123456789",
 *   "subAccount":      null,
 *   "code":            "AGRI-ORD-20260922001",   ← nội dung CK chứa mã đơn
 *   "content":         "chuyen khoan AGRI-ORD-20260922001",
 *   "transferType":    "in",
 *   "transferAmount":  330000,
 *   "accumulated":     330000,
 *   "referenceCode":   "FT24001A12B3C4",
 *   "description":     ""
 * }
 *
 * Quy tắc đặt nội dung chuyển khoản cho khách:
 *   "AGRI <order_code>" — ví dụ: "AGRI ORD-20260922090000-001"
 *   Hệ thống tìm order_code trong content và cập nhật payment_status = PAID.
 */
@WebServlet(name = "SePayWebhookServlet", urlPatterns = {"/sepay-webhook"})
public class SePayWebhookServlet extends HttpServlet {

    @EJB
    private OrderManagementServiceLocal orderManagementService;

    // Secret token để xác thực request từ SePay (đặt trong web.xml context-param)
    private String sePaySecretToken;

    @Override
    public void init() throws ServletException {
        sePaySecretToken = getServletContext().getInitParameter("SEPAY_SECRET_TOKEN");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // 1. Xác thực token nếu đã cấu hình
        if (sePaySecretToken != null && !sePaySecretToken.isBlank()) {
            String apiKey = req.getHeader("Authorization");
            if (apiKey == null || !apiKey.equals("Apikey " + sePaySecretToken)) {
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                resp.getWriter().write("{\"success\":false,\"message\":\"Unauthorized\"}");
                return;
            }
        }

        // 2. Đọc JSON body
        String body = readBody(req);
        getServletContext().log("[SePay] Webhook received: " + body);

        // 3. Chỉ xử lý giao dịch vào (transferType = "in")
        if (!body.contains("\"transferType\":\"in\"")) {
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write("{\"success\":true,\"message\":\"ignored - not inbound\"}");
            return;
        }

        // 4. Parse mã đơn hàng từ "content" field
        //    Khách phải ghi nội dung CK: "AGRI <order_code>"
        String orderCode = extractOrderCode(body);
        if (orderCode == null || orderCode.isBlank()) {
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write("{\"success\":false,\"message\":\"order code not found in content\"}");
            return;
        }

        // 5. Cập nhật payment_status = PAID
        try {
            boolean updated = orderManagementService.confirmPaymentByOrderCode(orderCode);
            if (updated) {
                getServletContext().log("[SePay] Payment confirmed for order: " + orderCode);
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.setContentType("application/json; charset=UTF-8");
                resp.getWriter().write("{\"success\":true,\"message\":\"Payment confirmed for " + orderCode + "\"}");
            } else {
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.setContentType("application/json; charset=UTF-8");
                resp.getWriter().write("{\"success\":false,\"message\":\"Order not found or already paid: " + orderCode + "\"}");
            }
        } catch (Exception e) {
            getServletContext().log("[SePay] Error confirming payment: " + e.getMessage());
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"success\":false,\"message\":\"" + e.getMessage() + "\"}");
        }
    }

    /**
     * Tìm mã đơn hàng AgriShop trong nội dung chuyển khoản.
     * Pattern: "AGRI ORD-xxxxxxxx-xxx" hoặc chỉ "ORD-xxxxxxxx-xxx"
     */
    private String extractOrderCode(String json) {
        // Trích "content" field từ JSON thô
        String content = extractJsonString(json, "content");
        if (content == null) content = "";
        String code = extractJsonString(json, "code");
        if (code == null) code = "";

        // Tìm pattern ORD- trong nội dung
        String target = content.isEmpty() ? code : content;
        target = target.toUpperCase();

        int idx = target.indexOf("ORD-");
        if (idx < 0) return null;
        // Đọc đến khoảng trắng hoặc hết chuỗi
        int end = target.indexOf(' ', idx);
        return end < 0 ? target.substring(idx) : target.substring(idx, end);
    }

    /** Trích value của một key từ JSON string đơn giản (không dùng thư viện) */
    private String extractJsonString(String json, String key) {
        String search = "\"" + key + "\":\"";
        int start = json.indexOf(search);
        if (start < 0) return null;
        start += search.length();
        int end = json.indexOf("\"", start);
        if (end < 0) return null;
        return json.substring(start, end);
    }

    private String readBody(HttpServletRequest req) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(req.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
        }
        return sb.toString();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("text/plain; charset=UTF-8");
        resp.getWriter().write("SePay Webhook endpoint is active. POST only.");
    }
}
