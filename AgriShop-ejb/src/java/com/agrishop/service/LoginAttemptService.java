package com.agrishop.service;

import com.agrishop.entity.LoginAttempt;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

@Stateless
public class LoginAttemptService implements LoginAttemptServiceLocal {

    private static final Logger LOGGER = Logger.getLogger(LoginAttemptService.class.getName());
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCK_DURATION_MINUTES = 15;

    @PersistenceContext(unitName = "AgriShopPU")
    private EntityManager em;

    private LoginAttempt findByIdentifier(String identifier) {
        if (identifier == null || identifier.trim().isEmpty()) return null;
        try {
            TypedQuery<LoginAttempt> q = em.createQuery(
                "SELECT la FROM LoginAttempt la WHERE la.identifier = :id", LoginAttempt.class);
            q.setParameter("id", identifier.trim().toLowerCase());
            List<LoginAttempt> list = q.getResultList();
            return list.isEmpty() ? null : list.get(0);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public long getRemainingLockMinutes(String identifier) {
        if (identifier == null || identifier.trim().isEmpty()) return 0;
        LoginAttempt attempt = findByIdentifier(identifier);
        if (attempt != null && attempt.isCurrentlyLocked()) {
            return attempt.getRemainingLockMinutes();
        }
        return 0;
    }

    @Override
    public int recordFailedAttempt(String identifier) {
        if (identifier == null || identifier.trim().isEmpty()) return 0;
        String cleanId = identifier.trim().toLowerCase();
        LoginAttempt attempt = findByIdentifier(cleanId);
        Date now = new Date();

        if (attempt == null) {
            attempt = new LoginAttempt(cleanId);
            attempt.setAttemptCount(1);
            attempt.setLastAttemptAt(now);
            em.persist(attempt);
            return 1;
        }

        // Nếu đã hết hạn khóa trước đó, reset đếm bắt đầu chu kỳ mới
        if (attempt.getLockedUntil() != null && attempt.getLockedUntil().before(now)) {
            attempt.setAttemptCount(1);
            attempt.setLockedUntil(null);
        } else {
            attempt.setAttemptCount(attempt.getAttemptCount() + 1);
        }

        attempt.setLastAttemptAt(now);

        // Khóa nếu đạt ngưỡng thất bại
        if (attempt.getAttemptCount() >= MAX_FAILED_ATTEMPTS) {
            Date lockUntil = new Date(now.getTime() + (long) LOCK_DURATION_MINUTES * 60 * 1000);
            attempt.setLockedUntil(lockUntil);
            LOGGER.warning(String.format(">>> BRUTE-FORCE DETECTED: Identifier '%s' bi khoa tam thoi %d phut den %s", 
                    cleanId, LOCK_DURATION_MINUTES, lockUntil.toString()));
        }

        em.merge(attempt);
        return attempt.getAttemptCount();
    }

    @Override
    public void resetAttempts(String identifier) {
        if (identifier == null || identifier.trim().isEmpty()) return;
        String cleanId = identifier.trim().toLowerCase();
        LoginAttempt attempt = findByIdentifier(cleanId);
        if (attempt != null) {
            attempt.setAttemptCount(0);
            attempt.setLockedUntil(null);
            attempt.setLastAttemptAt(new Date());
            em.merge(attempt);
        }
    }
}
