package com.threlease.base.repositories.auth;

import com.threlease.base.entities.AuditLogEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLogEntity, Long> {
    @Query("SELECT a FROM AuditLogEntity a ORDER BY a.createdAt DESC")
    Page<AuditLogEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Query("SELECT a FROM AuditLogEntity a WHERE a.createdAt < :cutoff")
    java.util.List<AuditLogEntity> findAllByCreatedAtBefore(@Param("cutoff") LocalDateTime cutoff);
}
