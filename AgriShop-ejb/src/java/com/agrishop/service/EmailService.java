package com.agrishop.service;

import com.agrishop.entity.Orders;
import jakarta.ejb.Asynchronous;
import jakarta.ejb.Stateless;
import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import javax.naming.InitialContext;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

@Stateless
public class EmailService implements EmailServiceLocal {

    private static final Logger LOGGER = Logger.getLogger(EmailService.class.getName());
    private static final String DEFAULT_JNDI_MAIL_SESSION = "mail/AgriShopSession";

    private Session getMailSession() {
        try {
            InitialContext ctx = new InitialContext();
            return (Session) ctx.lookup(DEFAULT_JNDI_MAIL_SESSION);
        } catch (Exception e) {
            LOGGER.log(Level.FINE, "Không tìm thấy JNDI {0}, sử dụng Session cục bộ dự phòng.", DEFAULT_JNDI_MAIL_SESSION);
            Properties props = new Properties();
            props.put("mail.smtp.host", "localhost");
            props.put("mail.smtp.port", "25");
            props.put("mail.smtp.connectiontimeout", "3000");
            props.put("mail.smtp.timeout", "3000");
            return Session.getInstance(props);
        }
    }

    private String getStatusLabel(String status) {
        if (status == null) return "Chờ xử lý";
        switch (status) {
            case "CONFIRMED": return "Đã xác nhận đơn hàng";
            case "PROCESSING": return "Đang đóng gói & chuẩn bị nông sản";
            case "SHIPPED": return "Đang trên đường giao hàng";
            case "DELIVERED": return "Giao hàng thành công";
            case "CANCELLED": return "Đơn hàng đã hủy";
            case "RETURNED": return "Đã hoàn trả hàng";
            default: return status;
        }
    }

    @Override
    @Asynchronous
    public void sendOrderStatusEmail(Orders order, String newStatus) {
        if (order == null || order.getUser() == null || order.getUser().getEmail() == null) {
            return;
        }

        String recipient = order.getUser().getEmail().trim();
        String customerName = order.getUser().getFullName() != null ? order.getUser().getFullName() : "Quý khách";
        String orderCode = order.getOrderCode() != null ? order.getOrderCode() : "N/A";
        String statusLabel = getStatusLabel(newStatus);
        String formattedTime = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date());
        String formattedTotal = new DecimalFormat("#,##0").format(order.getTotalAmount() != null ? order.getTotalAmount() : 0);

        try {
            Session session = getMailSession();
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress("no-reply@agrishop.vn", "AgriShop - Nông Sản Hữu Cơ"));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
            message.setSubject("[AgriShop] Cập nhật đơn hàng #" + orderCode + ": " + statusLabel, "UTF-8");

            String htmlBody = "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; border: 1px solid #E5E7EB; border-radius: 8px; overflow: hidden;'>"
                + "<div style='background: #1B4332; color: #FFFFFF; padding: 20px; text-align: center;'>"
                + "  <h2 style='margin: 0; font-size: 22px;'>AgriShop - Nông Sản Sạch & Hữu Cơ</h2>"
                + "  <p style='margin: 5px 0 0 0; font-size: 13px; opacity: 0.9;'>Thông báo trạng thái đơn hàng</p>"
                + "</div>"
                + "<div style='padding: 24px; color: #1F2937; line-height: 1.6;'>"
                + "  <p style='margin-top: 0;'>Kính chào <strong>" + customerName + "</strong>,</p>"
                + "  <p>Đơn hàng <strong>#" + orderCode + "</strong> của quý khách vừa được cập nhật trạng thái mới:</p>"
                + "  <div style='background: #F0FDF4; border-left: 4px solid #16A34A; padding: 12px 16px; margin: 16px 0; border-radius: 4px;'>"
                + "    <span style='font-size: 16px; font-weight: bold; color: #15803D;'>" + statusLabel + "</span>"
                + "    <div style='font-size: 12px; color: #6B7280; margin-top: 4px;'>Thời gian cập nhật: " + formattedTime + "</div>"
                + "  </div>"
                + "  <table style='width: 100%; font-size: 14px; margin-top: 16px; border-collapse: collapse;'>"
                + "    <tr><td style='padding: 6px 0; color: #6B7280;'>Mã đơn hàng:</td><td style='padding: 6px 0; font-weight: bold; text-align: right;'>" + orderCode + "</td></tr>"
                + "    <tr><td style='padding: 6px 0; color: #6B7280;'>Hình thức thanh toán:</td><td style='padding: 6px 0; text-align: right;'>" + (order.getPaymentMethod() != null ? order.getPaymentMethod() : "COD") + "</td></tr>"
                + "    <tr><td style='padding: 6px 0; color: #6B7280;'>Tổng thanh toán:</td><td style='padding: 6px 0; font-weight: bold; color: #B91C1C; text-align: right;'>" + formattedTotal + " ₫</td></tr>"
                + "    <tr><td style='padding: 6px 0; color: #6B7280;'>Địa chỉ giao nhận:</td><td style='padding: 6px 0; text-align: right;'>" + (order.getShippingAddress() != null ? order.getShippingAddress() : "Theo thông tin tài khoản") + "</td></tr>"
                + "  </table>"
                + "  <div style='text-align: center; margin-top: 28px;'>"
                + "    <a href='http://localhost:8080/AgriShop-war/order-history.xhtml' style='background: #1B4332; color: #FFFFFF; text-decoration: none; padding: 10px 24px; border-radius: 6px; font-weight: bold; font-size: 14px; display: inline-block;'>Theo Dõi Đơn Hàng</a>"
                + "  </div>"
                + "</div>"
                + "<div style='background: #F9FAFB; padding: 14px; text-align: center; font-size: 12px; color: #9CA3AF; border-top: 1px solid #E5E7EB;'>"
                + "  Mọi thắc mắc xin vui lòng liên hệ hotline: 1900 1234 hoặc email support@agrishop.vn.<br/>Cảm ơn quý khách đã đồng hành cùng nông sản Việt!"
                + "</div>"
                + "</div>";

            message.setContent(htmlBody, "text/html; charset=UTF-8");
            message.setSentDate(new Date());

            Transport.send(message);
            LOGGER.log(Level.INFO, "Đã gửi email cập nhật trạng thái đơn {0} ({1}) tới {2}", new Object[]{orderCode, statusLabel, recipient});

        } catch (Exception e) {
            // Không làm gián đoạn transaction nếu mail server chưa chạy hoặc lỗi kết nối
            LOGGER.log(Level.INFO, "[Mô phỏng gửi Email] Đơn {0} -> Trạng thái {1} tới {2}. (SMTP Offline/Fallback: {3})", 
                       new Object[]{orderCode, statusLabel, recipient, e.getMessage()});
        }
    }

    @Override
    @Asynchronous
    public void sendActivationEmail(String toEmail, String fullName, String activationLink) {
        if (toEmail == null || toEmail.trim().isEmpty()) return;
        String recipient = toEmail.trim();
        String name = (fullName != null && !fullName.trim().isEmpty()) ? fullName : "Quý khách";

        LOGGER.info(String.format("===============================================================\n"
                + ">>> [AGRISHOP EMAIL SERVICE] GỬI LINK KÍCH HOẠT TÀI KHOẢN <<<\n"
                + "Người nhận: %s (%s)\n"
                + "Đường dẫn kích hoạt: %s\n"
                + "===============================================================", name, recipient, activationLink));

        try {
            Session session = getMailSession();
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress("no-reply@agrishop.vn", "AgriShop - Nông Sản Hữu Cơ"));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
            message.setSubject("[AgriShop] Kích hoạt tài khoản thành viên của bạn", "UTF-8");

            String htmlBody = "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; border: 1px solid #E5E7EB; border-radius: 8px; overflow: hidden;'>"
                + "<div style='background: #1B4332; color: #FFFFFF; padding: 24px; text-align: center;'>"
                + "  <h2 style='margin: 0; font-size: 24px;'>Chào mừng bạn đến với AgriShop!</h2>"
                + "  <p style='margin: 6px 0 0 0; font-size: 14px; opacity: 0.9;'>Nông Sản Sạch & Thực Phẩm Hữu Cơ Chuẩn VietGAP</p>"
                + "</div>"
                + "<div style='padding: 28px; color: #1F2937; line-height: 1.6;'>"
                + "  <p style='margin-top: 0;'>Xin chào <strong>" + name + "</strong>,</p>"
                + "  <p>Cảm ơn bạn đã đăng ký tài khoản tại <strong>AgriShop</strong>. Để bảo vệ an toàn cho tài khoản và kích hoạt các quyền lợi thành viên, vui lòng nhấn vào nút bên dưới để hoàn tất xác thực email:</p>"
                + "  <div style='text-align: center; margin: 30px 0;'>"
                + "    <a href='" + activationLink + "' style='background: #1B4332; color: #FFFFFF; text-decoration: none; padding: 12px 30px; border-radius: 6px; font-weight: bold; font-size: 15px; display: inline-block; box-shadow: 0 2px 4px rgba(0,0,0,0.1);'>Kích Hoạt Tài Khoản Ngay</a>"
                + "  </div>"
                + "  <p style='font-size: 13px; color: #6B7280;'>Nếu nút bấm trên không hoạt động, bạn có thể sao chép liên kết sau dán vào trình duyệt:</p>"
                + "  <p style='font-size: 12px; word-break: break-all; color: #2563EB; background: #F3F4F6; padding: 10px; border-radius: 4px;'>" + activationLink + "</p>"
                + "  <p style='font-size: 13px; color: #9CA3AF;'>Liên kết này có hiệu lực trong vòng 24 giờ. Nếu bạn không yêu cầu đăng ký tài khoản này, vui lòng bỏ qua email này.</p>"
                + "</div>"
                + "<div style='background: #F9FAFB; padding: 14px; text-align: center; font-size: 12px; color: #9CA3AF; border-top: 1px solid #E5E7EB;'>"
                + "  Hệ thống AgriShop Vietnam &bull; Hotline: 1900 6868 &bull; support@agrishop.vn"
                + "</div>"
                + "</div>";

            message.setContent(htmlBody, "text/html; charset=UTF-8");
            message.setSentDate(new Date());
            Transport.send(message);
        } catch (Exception e) {
            LOGGER.log(Level.INFO, "[Mô phỏng gửi Activation Email] Tới: {0} ({1})", new Object[]{recipient, e.getMessage()});
        }
    }

    @Override
    @Asynchronous
    public void sendPasswordResetEmail(String toEmail, String fullName, String resetLink) {
        if (toEmail == null || toEmail.trim().isEmpty()) return;
        String recipient = toEmail.trim();
        String name = (fullName != null && !fullName.trim().isEmpty()) ? fullName : "Quý khách";

        LOGGER.info(String.format("===============================================================\n"
                + ">>> [AGRISHOP EMAIL SERVICE] GỬI LINK ĐẶT LẠI MẬT KHẨU <<<\n"
                + "Người nhận: %s (%s)\n"
                + "Đường dẫn đặt lại: %s\n"
                + "===============================================================", name, recipient, resetLink));

        try {
            Session session = getMailSession();
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress("no-reply@agrishop.vn", "AgriShop - Nông Sản Hữu Cơ"));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
            message.setSubject("[AgriShop] Yêu cầu đặt lại mật khẩu tài khoản", "UTF-8");

            String htmlBody = "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; border: 1px solid #E5E7EB; border-radius: 8px; overflow: hidden;'>"
                + "<div style='background: #1B4332; color: #FFFFFF; padding: 24px; text-align: center;'>"
                + "  <h2 style='margin: 0; font-size: 24px;'>Yêu Cầu Đặt Lại Mật Khẩu</h2>"
                + "  <p style='margin: 6px 0 0 0; font-size: 14px; opacity: 0.9;'>AgriShop An Ninh & Bảo Mật</p>"
                + "</div>"
                + "<div style='padding: 28px; color: #1F2937; line-height: 1.6;'>"
                + "  <p style='margin-top: 0;'>Kính chào <strong>" + name + "</strong>,</p>"
                + "  <p>Chúng tôi nhận được yêu cầu đặt lại mật khẩu cho tài khoản AgriShop gắn với địa chỉ email này. Vui lòng nhấn vào nút bên dưới để thiết lập mật khẩu mới:</p>"
                + "  <div style='text-align: center; margin: 30px 0;'>"
                + "    <a href='" + resetLink + "' style='background: #B91C1C; color: #FFFFFF; text-decoration: none; padding: 12px 30px; border-radius: 6px; font-weight: bold; font-size: 15px; display: inline-block; box-shadow: 0 2px 4px rgba(0,0,0,0.1);'>Đặt Lại Mật Khẩu Ngay</a>"
                + "  </div>"
                + "  <p style='font-size: 13px; color: #6B7280;'>Nếu nút bấm trên không mở được, bạn hãy sao chép liên kết sau dán vào trình duyệt:</p>"
                + "  <p style='font-size: 12px; word-break: break-all; color: #B91C1C; background: #FEF2F2; padding: 10px; border-radius: 4px;'>" + resetLink + "</p>"
                + "  <div style='background: #FFFBEB; border-left: 4px solid #F59E0B; padding: 10px 14px; font-size: 13px; color: #92400E; margin-top: 20px; border-radius: 4px;'>"
                + "    <strong>Lưu ý quan trọng:</strong> Liên kết này chỉ có giá trị trong vòng <strong>30 phút</strong>. Nếu quý khách không thực hiện yêu cầu này, hãy yên tâm bỏ qua email này, mật khẩu hiện tại của quý khách vẫn an toàn."
                + "  </div>"
                + "</div>"
                + "<div style='background: #F9FAFB; padding: 14px; text-align: center; font-size: 12px; color: #9CA3AF; border-top: 1px solid #E5E7EB;'>"
                + "  Hệ thống AgriShop Vietnam &bull; Hotline: 1900 6868 &bull; support@agrishop.vn"
                + "</div>"
                + "</div>";

            message.setContent(htmlBody, "text/html; charset=UTF-8");
            message.setSentDate(new Date());
            Transport.send(message);
        } catch (Exception e) {
            LOGGER.log(Level.INFO, "[Mô phỏng gửi Password Reset Email] Tới: {0} ({1})", new Object[]{recipient, e.getMessage()});
        }
    }
}
