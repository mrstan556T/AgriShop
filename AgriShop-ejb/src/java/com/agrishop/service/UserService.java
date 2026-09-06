package com.agrishop.service;

import com.agrishop.dto.UserDTO;
import com.agrishop.entity.User;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;

@Stateless
public class UserService implements UserServiceLocal {

    @PersistenceContext(unitName = "AgriShopPU")
    private EntityManager em;

    // Hỗ trợ inject EntityManager cho môi trường test Java SE
    public void setEntityManager(EntityManager em) {
        this.em = em;
    }

    @Override
    public UserDTO login(String username, String password) {
        try {
            User user = em.createQuery("SELECT u FROM User u WHERE u.username = :uname AND u.status = 'ACTIVE'", User.class)
                    .setParameter("uname", username)
                    .getSingleResult();

            // So sánh mật khẩu (Chưa mã hóa theo yêu cầu test Admin)
            if (password.equals(user.getPasswordHash())) {
                UserDTO dto = new UserDTO();
                dto.setId(user.getId());
                dto.setUserCode(user.getUserCode());
                dto.setUsername(user.getUsername());
                dto.setFullName(user.getFullName());
                dto.setRole(user.getRole());
                return dto;
            }
            return null; // Mật khẩu không khớp
        } catch (NoResultException e) {
            return null; // Không tìm thấy username
        }
    }
}