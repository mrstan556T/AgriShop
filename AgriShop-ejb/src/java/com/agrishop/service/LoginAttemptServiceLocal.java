package com.agrishop.service;

import jakarta.ejb.Local;

@Local
public interface LoginAttemptServiceLocal {

    /**
     * Kiểm tra xem identifier (username hoặc IP) có đang bị khóa hay không.
     * @return số phút khóa còn lại (> 0 nếu đang bị khóa, 0 nếu không bị khóa).
     */
    long getRemainingLockMinutes(String identifier);

    /**
     * Ghi nhận một lần đăng nhập thất bại.
     * Nếu số lần thất bại liên tiếp đạt ngưỡng (5 lần), khóa tạm thời 15 phút.
     * @return số lần thất bại hiện tại.
     */
    int recordFailedAttempt(String identifier);

    /**
     * Xóa bỏ lịch sử thất bại sau khi đăng nhập thành công.
     */
    void resetAttempts(String identifier);
}
