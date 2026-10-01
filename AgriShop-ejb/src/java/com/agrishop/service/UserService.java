package com.agrishop.service;

import com.agrishop.dto.UserDTO;
import com.agrishop.entity.User;
import com.agrishop.entity.UserToken;
import com.agrishop.util.PasswordUtils;
import com.agrishop.util.TotpUtils;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Stateless
public class UserService implements UserServiceLocal {

    @PersistenceContext(unitName = "AgriShopPU")
    private EntityManager em;

    @EJB
    private EmailServiceLocal emailService;

    // Hỗ trợ inject EntityManager cho môi trường test
    public void setEntityManager(EntityManager em) {
        this.em = em;
    }

    public void setEmailService(EmailServiceLocal emailService) {
        this.emailService = emailService;
    }

    @Override
    public UserDTO login(String username, String password) {
        try {
            User user = em.createQuery("SELECT u FROM User u WHERE u.username = :uname OR u.email = :uname", User.class)
                    .setParameter("uname", username)
                    .getSingleResult();

            String hashedPassword = PasswordUtils.hashPassword(password);
            
            // So sánh mật khẩu (Hỗ trợ mật khẩu chưa mã hóa cũ để test Admin và mật khẩu mới mã hóa)
            if (password.equals(user.getPasswordHash()) || hashedPassword.equals(user.getPasswordHash())) {
                UserDTO dto = new UserDTO();
                dto.setId(user.getId());
                dto.setUserCode(user.getUserCode());
                dto.setUsername(user.getUsername());
                dto.setFullName(user.getFullName());
                dto.setRole(user.getRole());
                dto.setEmail(user.getEmail());
                dto.setPhone(user.getPhone());
                dto.setAddress(user.getAddress());
                dto.setAvatarUrl(user.getAvatarUrl());
                dto.setStatus(user.getStatus());
                dto.setIsEmailVerified(user.getIsEmailVerified());
                dto.setTotpEnabled(user.getTotpEnabled());
                return dto;
            }
            return null; // Mật khẩu không khớp
        } catch (NoResultException e) {
            return null; // Không tìm thấy username
        }
    }

    @Override
    public void registerCustomer(String username, String rawPassword, String fullName, String phone, String email) throws Exception {
        registerCustomer(username, rawPassword, fullName, phone, email, "http://localhost:8080/AgriShop-war");
    }

    @Override
    public void registerCustomer(String username, String rawPassword, String fullName, String phone, String email, String baseUrl) throws Exception {
        // Kiểm tra xem username hoặc email đã tồn tại chưa
        long countUser = em.createQuery("SELECT COUNT(u) FROM User u WHERE u.username = :username", Long.class)
                .setParameter("username", username)
                .getSingleResult();
        if (countUser > 0) {
            throw new Exception("Tên đăng nhập đã tồn tại.");
        }

        long countEmail = em.createQuery("SELECT COUNT(u) FROM User u WHERE u.email = :email", Long.class)
                .setParameter("email", email)
                .getSingleResult();
        if (countEmail > 0) {
            throw new Exception("Email đã được sử dụng.");
        }

        User user = new User();
        
        // Tạo User Code tự động
        SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmmss");
        user.setUserCode("CUS-" + sdf.format(new Date()));
        
        user.setUsername(username);
        user.setPasswordHash(PasswordUtils.hashPassword(rawPassword));
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPhone(phone);
        user.setRole("CUSTOMER");
        // Đặt trạng thái chờ kích hoạt email
        user.setStatus("PENDING_ACTIVATION");
        user.setIsEmailVerified(false);
        user.setTotpEnabled(false);
        user.setCreatedAt(new Date());
        user.setUpdatedAt(new Date());

        em.persist(user);
        em.flush();

        // Sinh token kích hoạt
        String token = UUID.randomUUID().toString();
        Date expiresAt = new Date(System.currentTimeMillis() + 24L * 60 * 60 * 1000); // 24 giờ
        UserToken userToken = new UserToken(user, token, "VERIFY_EMAIL", expiresAt);
        em.persist(userToken);

        // Gửi email kích hoạt
        String base = (baseUrl != null && !baseUrl.isEmpty()) ? baseUrl : "http://localhost:8080/AgriShop-war";
        String activationLink = base + "/activate-account.xhtml?token=" + token;
        if (emailService != null) {
            emailService.sendActivationEmail(email, fullName, activationLink);
        }
    }

    @Override
    public boolean activateAccount(String token) {
        if (token == null || token.trim().isEmpty()) return false;
        try {
            UserToken ut = em.createQuery("SELECT t FROM UserToken t WHERE t.token = :token AND t.tokenType = 'VERIFY_EMAIL'", UserToken.class)
                    .setParameter("token", token.trim())
                    .getSingleResult();
            if (ut == null || !ut.isValid()) {
                return false;
            }

            ut.setIsUsed(true);
            User user = ut.getUser();
            user.setIsEmailVerified(true);
            if ("PENDING_ACTIVATION".equals(user.getStatus())) {
                user.setStatus("ACTIVE");
            }
            user.setUpdatedAt(new Date());
            em.merge(user);
            em.merge(ut);
            return true;
        } catch (NoResultException e) {
            return false;
        }
    }

    @Override
    public boolean requestPasswordReset(String emailOrUsername, String baseUrl) throws Exception {
        if (emailOrUsername == null || emailOrUsername.trim().isEmpty()) {
            throw new Exception("Vui lòng nhập tên đăng nhập hoặc email.");
        }
        String clean = emailOrUsername.trim();
        List<User> list = em.createQuery("SELECT u FROM User u WHERE u.username = :val OR u.email = :val", User.class)
                .setParameter("val", clean)
                .getResultList();
        if (list.isEmpty()) {
            throw new Exception("Không tìm thấy tài khoản với thông tin đã cung cấp.");
        }

        User user = list.get(0);
        String token = UUID.randomUUID().toString();
        Date expiresAt = new Date(System.currentTimeMillis() + 30L * 60 * 1000); // 30 phút
        UserToken ut = new UserToken(user, token, "RESET_PASSWORD", expiresAt);
        em.persist(ut);

        String base = (baseUrl != null && !baseUrl.isEmpty()) ? baseUrl : "http://localhost:8080/AgriShop-war";
        String resetLink = base + "/reset-password.xhtml?token=" + token;
        if (emailService != null) {
            emailService.sendPasswordResetEmail(user.getEmail(), user.getFullName(), resetLink);
        }
        return true;
    }

    @Override
    public boolean resetPasswordWithToken(String token, String newPassword) throws Exception {
        if (token == null || token.trim().isEmpty()) {
            throw new Exception("Mã xác thực (token) không hợp lệ.");
        }
        if (newPassword == null || newPassword.length() < 6) {
            throw new Exception("Mật khẩu mới phải có ít nhất 6 ký tự.");
        }

        try {
            UserToken ut = em.createQuery("SELECT t FROM UserToken t WHERE t.token = :token AND t.tokenType = 'RESET_PASSWORD'", UserToken.class)
                    .setParameter("token", token.trim())
                    .getSingleResult();
            if (ut == null || !ut.isValid()) {
                throw new Exception("Liên kết đặt lại mật khẩu đã hết hạn hoặc đã được sử dụng.");
            }

            User user = ut.getUser();
            user.setPasswordHash(PasswordUtils.hashPassword(newPassword));
            user.setUpdatedAt(new Date());
            ut.setIsUsed(true);
            em.merge(user);
            em.merge(ut);
            return true;
        } catch (NoResultException e) {
            throw new Exception("Liên kết đặt lại mật khẩu không hợp lệ.");
        }
    }

    @Override
    public String getOrCreateAdminTotpSecret(Long adminUserId) {
        User user = em.find(User.class, adminUserId);
        if (user == null) return null;
        if (user.getTotpSecret() == null || user.getTotpSecret().trim().isEmpty()) {
            String secret = TotpUtils.generateSecretKey();
            user.setTotpSecret(secret);
            user.setUpdatedAt(new Date());
            em.merge(user);
            return secret;
        }
        return user.getTotpSecret();
    }

    @Override
    public boolean verifyAndEnableAdminTotp(Long adminUserId, String code) {
        User user = em.find(User.class, adminUserId);
        if (user == null || user.getTotpSecret() == null) return false;
        boolean valid = TotpUtils.verifyCode(user.getTotpSecret(), code);
        if (valid) {
            user.setTotpEnabled(true);
            user.setUpdatedAt(new Date());
            em.merge(user);
        }
        return valid;
    }

    @Override
    public boolean verifyAdminTotpLogin(Long adminUserId, String code) {
        User user = em.find(User.class, adminUserId);
        if (user == null || user.getTotpSecret() == null) return false;
        if (user.getTotpEnabled() == null || !user.getTotpEnabled()) {
            return verifyAndEnableAdminTotp(adminUserId, code);
        }
        return TotpUtils.verifyCode(user.getTotpSecret(), code);
    }

    @Override
    public UserDTO getUserById(Long userId) {
        User user = em.find(User.class, userId);
        if (user == null) return null;
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUserCode(user.getUserCode());
        dto.setUsername(user.getUsername());
        dto.setFullName(user.getFullName());
        dto.setRole(user.getRole());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setAddress(user.getAddress());
        dto.setAvatarUrl(user.getAvatarUrl());
        dto.setStatus(user.getStatus());
        dto.setIsEmailVerified(user.getIsEmailVerified());
        dto.setTotpEnabled(user.getTotpEnabled());
        return dto;
    }

    @Override
    public UserDTO getUserByUsernameOrEmail(String identifier) {
        if (identifier == null || identifier.trim().isEmpty()) return null;
        try {
            User user = em.createQuery("SELECT u FROM User u WHERE u.username = :val OR u.email = :val", User.class)
                    .setParameter("val", identifier.trim())
                    .getSingleResult();
            return getUserById(user.getId());
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<UserDTO> getUsersLazy(int first, int pageSize, String sortField, String sortOrder, Map<String, Object> filters) {
        StringBuilder queryStr = new StringBuilder("SELECT u FROM User u WHERE 1=1 ");
        
        if (filters != null) {
            for (Map.Entry<String, Object> filter : filters.entrySet()) {
                if (filter.getValue() != null && !filter.getValue().toString().isEmpty()) {
                    if (filter.getKey().equals("role")) {
                        queryStr.append(" AND u.role = :").append(filter.getKey());
                    } else if (filter.getKey().equals("status")) {
                        queryStr.append(" AND u.status = :").append(filter.getKey());
                    } else if (filter.getKey().equals("username")) {
                        queryStr.append(" AND LOWER(u.username) LIKE LOWER(:").append(filter.getKey()).append(")");
                    }
                }
            }
        }
        
        if (sortField != null && !sortField.isEmpty()) {
            queryStr.append(" ORDER BY u.").append(sortField);
            if ("DESCENDING".equalsIgnoreCase(sortOrder)) {
                queryStr.append(" DESC");
            } else {
                queryStr.append(" ASC");
            }
        } else {
            queryStr.append(" ORDER BY u.id DESC");
        }

        var query = em.createQuery(queryStr.toString(), User.class);
        
        if (filters != null) {
            for (Map.Entry<String, Object> filter : filters.entrySet()) {
                if (filter.getValue() != null && !filter.getValue().toString().isEmpty()) {
                    if (filter.getKey().equals("username")) {
                        query.setParameter(filter.getKey(), "%" + filter.getValue() + "%");
                    } else {
                        query.setParameter(filter.getKey(), filter.getValue());
                    }
                }
            }
        }
        
        query.setFirstResult(first);
        query.setMaxResults(pageSize);

        List<User> users = query.getResultList();
        List<UserDTO> dtoList = new java.util.ArrayList<>();
        for (User u : users) {
            UserDTO dto = new UserDTO();
            dto.setId(u.getId());
            dto.setUserCode(u.getUserCode());
            dto.setUsername(u.getUsername());
            dto.setFullName(u.getFullName());
            dto.setRole(u.getRole());
            dto.setStatus(u.getStatus());
            dto.setEmail(u.getEmail());
            dto.setPhone(u.getPhone());
            dto.setAddress(u.getAddress());
            dto.setAvatarUrl(u.getAvatarUrl());
            dto.setCreatedAt(u.getCreatedAt());
            dto.setIsEmailVerified(u.getIsEmailVerified());
            dto.setTotpEnabled(u.getTotpEnabled());
            dtoList.add(dto);
        }
        return dtoList;
    }

    @Override
    public int countUsers(Map<String, Object> filters) {
        StringBuilder queryStr = new StringBuilder("SELECT COUNT(u) FROM User u WHERE 1=1 ");
        
        if (filters != null) {
            for (Map.Entry<String, Object> filter : filters.entrySet()) {
                if (filter.getValue() != null && !filter.getValue().toString().isEmpty()) {
                    if (filter.getKey().equals("role")) {
                        queryStr.append(" AND u.role = :").append(filter.getKey());
                    } else if (filter.getKey().equals("status")) {
                        queryStr.append(" AND u.status = :").append(filter.getKey());
                    } else if (filter.getKey().equals("username")) {
                        queryStr.append(" AND LOWER(u.username) LIKE LOWER(:").append(filter.getKey()).append(")");
                    }
                }
            }
        }

        var query = em.createQuery(queryStr.toString(), Long.class);
        
        if (filters != null) {
            for (Map.Entry<String, Object> filter : filters.entrySet()) {
                if (filter.getValue() != null && !filter.getValue().toString().isEmpty()) {
                    if (filter.getKey().equals("username")) {
                        query.setParameter(filter.getKey(), "%" + filter.getValue() + "%");
                    } else {
                        query.setParameter(filter.getKey(), filter.getValue());
                    }
                }
            }
        }

        return query.getSingleResult().intValue();
    }

    @Override
    public void updateUserRole(Long userId, String role) throws Exception {
        User user = em.find(User.class, userId);
        if (user == null) throw new Exception("Không tìm thấy người dùng.");
        user.setRole(role);
        user.setUpdatedAt(new Date());
        em.merge(user);
    }

    @Override
    public void toggleUserStatus(Long userId) throws Exception {
        User user = em.find(User.class, userId);
        if (user == null) throw new Exception("Không tìm thấy người dùng.");
        if ("ACTIVE".equals(user.getStatus())) {
            user.setStatus("INACTIVE");
        } else {
            user.setStatus("ACTIVE");
        }
        user.setUpdatedAt(new Date());
        em.merge(user);
    }

    @Override
    public boolean updateProfile(Long userId, String fullName, String newPassword, String phone, String address, String avatarUrl) {
        User user = em.find(User.class, userId);
        if (user != null) {
            if (fullName != null && !fullName.trim().isEmpty()) {
                user.setFullName(fullName);
            }
            if (newPassword != null && !newPassword.trim().isEmpty()) {
                user.setPasswordHash(PasswordUtils.hashPassword(newPassword));
            }
            if (phone != null) user.setPhone(phone);
            if (address != null) user.setAddress(address);
            if (avatarUrl != null) {
                user.setAvatarUrl(avatarUrl.trim().isEmpty() ? null : avatarUrl.trim());
            }
            
            user.setUpdatedAt(new Date());
            em.merge(user);
            return true;
        }
        return false;
    }

    @Override
    public boolean changePassword(Long userId, String currentPassword, String newPassword) throws Exception {
        User user = em.find(User.class, userId);
        if (user == null) {
            throw new Exception("Không tìm thấy thông tin tài khoản.");
        }

        String hashedCurrent = PasswordUtils.hashPassword(currentPassword);
        boolean matches = currentPassword.equals(user.getPasswordHash()) || hashedCurrent.equals(user.getPasswordHash());
        if (!matches) {
            throw new Exception("Mật khẩu hiện tại không chính xác.");
        }

        user.setPasswordHash(PasswordUtils.hashPassword(newPassword));
        user.setUpdatedAt(new Date());
        em.merge(user);
        return true;
    }
}