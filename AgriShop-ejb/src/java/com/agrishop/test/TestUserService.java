package com.agrishop.test;

import com.agrishop.dto.UserDTO;
import com.agrishop.service.UserService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class TestUserService {
    public static void main(String[] args) {
        EntityManagerFactory emf = null;
        EntityManager em = null;
        
        try {
            emf = Persistence.createEntityManagerFactory("AgriShopPU");
            em = emf.createEntityManager();
            
            // Khởi tạo Service thủ công và Inject EntityManager
            UserService userService = new UserService();
            userService.setEntityManager(em);
            
            // Thực thi phương thức login
            UserDTO user = userService.login("admin", "123456");
            
            System.out.println("=========================================");
            if (user != null) {
                System.out.println("ĐĂNG NHẬP THÀNH CÔNG");
                System.out.println("User: " + user.getFullName() + " | Role: " + user.getRole());
            } else {
                System.out.println("ĐĂNG NHẬP THẤT BẠI: Sai tài khoản hoặc mật khẩu.");
            }
            System.out.println("=========================================");
            
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
            if (emf != null && emf.isOpen()) {
                emf.close();
            }
        }
    }
}