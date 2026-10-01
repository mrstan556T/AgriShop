package com.agrishop.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PasswordUtils {

    /**
     * Băm mật khẩu sử dụng thuật toán SHA-256.
     * Trong thực tế, nên kết hợp với Salt (muối) hoặc dùng BCrypt để tăng cường bảo mật.
     * Ở đây sử dụng SHA-256 cơ bản để minh họa.
     */
    public static String hashPassword(String password) {
        if (password == null) return null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Lỗi băm mật khẩu: không tìm thấy thuật toán SHA-256", e);
        }
    }
}
