package com.agrishop.test;

import com.agrishop.entity.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class TestEntityMapping {
    public static void main(String[] args) {
        EntityManagerFactory emf = null;
        EntityManager em = null;
        
        try {
            emf = Persistence.createEntityManagerFactory("AgriShopPU");
            em = emf.createEntityManager();
            
            // Truy vấn kiểm tra Entity User (tài khoản admin đã insert ở Phiên 1)
            User admin = em.createQuery("SELECT u FROM User u WHERE u.username = :uname", User.class)
                           .setParameter("uname", "admin")
                           .getSingleResult();
            
            if (admin != null) {
                System.out.println("=========================================");
                System.out.println("MAPPING ENTITY THÀNH CÔNG!");
                System.out.println("User Code: " + admin.getUserCode());
                System.out.println("Username: " + admin.getUsername());
                System.out.println("Full Name: " + admin.getFullName());
                System.out.println("Role: " + admin.getRole());
                System.out.println("=========================================");
            }
        } catch (Exception e) {
            System.err.println("=========================================");
            System.err.println("MAPPING ENTITY THẤT BẠI. KIỂM TRA LẠI CODE!");
            System.err.println("=========================================");
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