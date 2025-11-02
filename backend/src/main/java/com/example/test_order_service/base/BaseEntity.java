package com.example.test_order_service.base;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Data;

import java.time.LocalDateTime;

@MappedSuperclass
@Data
public abstract class BaseEntity {
    @Column(name = "created_by", updatable = false)
    protected String createdBy;

//    @CreatedDate -> Not needed as we are using @PrePersist
    @Column(name = "created_at", nullable = false, updatable = false)
    protected LocalDateTime createdAt;

//    @LastModifiedDate -> Not needed as we are using @PreUpdate
    @Column(name = "updated_at")
    protected LocalDateTime updatedAt;

    @Column(name = "deleted", nullable = false)
    protected boolean deleted = false;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.deleted = false;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
