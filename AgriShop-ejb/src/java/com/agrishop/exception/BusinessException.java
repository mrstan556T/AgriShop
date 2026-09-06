package com.agrishop.exception;

import jakarta.ejb.ApplicationException;

/**
 * Custom Exception để vứt ra lỗi Logic Nghiệp vụ.
 * Transaction sẽ bị Rollback khi Exception này xảy ra.
 */
@ApplicationException(rollback = true)
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}
