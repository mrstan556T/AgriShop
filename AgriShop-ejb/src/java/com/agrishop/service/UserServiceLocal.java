package com.agrishop.service;

import com.agrishop.dto.UserDTO;
import jakarta.ejb.Local;
import java.util.List;
import java.util.Map;

@Local
public interface UserServiceLocal {
    UserDTO login(String username, String password);
    void registerCustomer(String username, String rawPassword, String fullName, String phone, String email) throws Exception;
    void registerCustomer(String username, String rawPassword, String fullName, String phone, String email, String baseUrl) throws Exception;
    
    List<UserDTO> getUsersLazy(int first, int pageSize, String sortField, String sortOrder, Map<String, Object> filters);
    int countUsers(Map<String, Object> filters);
    void updateUserRole(Long userId, String role) throws Exception;
    void toggleUserStatus(Long userId) throws Exception;
    boolean updateProfile(Long userId, String fullName, String newPassword, String phone, String address, String avatarUrl);
    boolean changePassword(Long userId, String currentPassword, String newPassword) throws Exception;

    // --- Prompt 4: Xác thực email & Quên mật khẩu ---
    boolean activateAccount(String token);
    boolean requestPasswordReset(String emailOrUsername, String baseUrl) throws Exception;
    boolean resetPasswordWithToken(String token, String newPassword) throws Exception;

    // --- Prompt 4: 2FA TOTP cho Admin ---
    String getOrCreateAdminTotpSecret(Long adminUserId);
    boolean verifyAndEnableAdminTotp(Long adminUserId, String code);
    boolean verifyAdminTotpLogin(Long adminUserId, String code);
    
    UserDTO getUserById(Long userId);
    UserDTO getUserByUsernameOrEmail(String identifier);
}