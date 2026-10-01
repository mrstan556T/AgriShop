package com.agrishop.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "LoginAttempts")
public class LoginAttempt implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String identifier; // username hoặc IP address

    @Column(name = "attempt_count")
    private Integer attemptCount = 0;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "last_attempt_at", nullable = false)
    private Date lastAttemptAt;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "locked_until")
    private Date lockedUntil;

    public LoginAttempt() {}

    public LoginAttempt(String identifier) {
        this.identifier = identifier;
        this.attemptCount = 1;
        this.lastAttemptAt = new Date();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getIdentifier() { return identifier; }
    public void setIdentifier(String identifier) { this.identifier = identifier; }

    public Integer getAttemptCount() { return attemptCount != null ? attemptCount : 0; }
    public void setAttemptCount(Integer attemptCount) { this.attemptCount = attemptCount; }

    public Date getLastAttemptAt() { return lastAttemptAt; }
    public void setLastAttemptAt(Date lastAttemptAt) { this.lastAttemptAt = lastAttemptAt; }

    public Date getLockedUntil() { return lockedUntil; }
    public void setLockedUntil(Date lockedUntil) { this.lockedUntil = lockedUntil; }

    public boolean isCurrentlyLocked() {
        return lockedUntil != null && lockedUntil.after(new Date());
    }

    public long getRemainingLockMinutes() {
        if (!isCurrentlyLocked()) return 0;
        long diff = lockedUntil.getTime() - System.currentTimeMillis();
        return Math.max(1, (diff + 59999) / 60000);
    }
}
