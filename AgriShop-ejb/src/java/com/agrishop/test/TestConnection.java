package com.agrishop.test;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class TestConnection {
    public static void main(String[] args) {
        EntityManagerFactory emf = null;
        EntityManager em = null;
        
        try {
            // Khởi tạo EntityManagerFactory dựa trên tên Persistence Unit
            emf = Persistence.createEntityManagerFactory("AgriShopPU");
            em = emf.createEntityManager();
            
            // Thực thi truy vấn cơ bản để xác thực kết nối
            Object result = em.createNativeQuery("SELECT 1").getSingleResult();
            
            if (result != null) {
                System.out.println("=========================================");
                System.out.println("KẾT NỐI CƠ SỞ DỮ LIỆU SQL SERVER THÀNH CÔNG!");
                System.out.println("=========================================");
            }
        } catch (Exception e) {
            System.err.println("=========================================");
            System.err.println("KẾT NỐI THẤT BẠI. KIỂM TRA LẠI CẤU HÌNH!");
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