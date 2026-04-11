package com.threlease.base.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.threlease.base.common.annotation.ExcelColumn;
import com.threlease.base.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Builder
@Entity
@Getter
@Setter
@Table(name = "tb_auth_login_history")
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class AuthLoginHistoryEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @ExcelColumn(headerName = "ID", order = 0)
    private Long id;

    @Column(name = "user_uuid", length = 36)
    @ExcelColumn(headerName = "사용자 UUID", order = 1)
    private String userUuid;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_uuid", referencedColumnName = "uuid", insertable = false, updatable = false)
    private AuthEntity user;

    @Column(name = "username", length = 24)
    @ExcelColumn(headerName = "아이디", order = 2)
    private String username;

    @Column(name = "success", nullable = false)
    @ExcelColumn(headerName = "성공 여부", order = 3)
    private boolean success;

    @Column(name = "failure_reason", length = 120)
    @ExcelColumn(headerName = "실패 사유", order = 4)
    private String failureReason;

    @Column(name = "failed_login_count", nullable = false)
    @ExcelColumn(headerName = "누적 실패 횟수", order = 5)
    @Builder.Default
    private int failedLoginCount = 0;

    @Column(name = "locked_until")
    @ExcelColumn(headerName = "잠금 만료 시간", order = 6)
    private LocalDateTime lockedUntil;

    @Column(name = "client_ip", length = 64)
    @ExcelColumn(headerName = "클라이언트 IP", order = 7)
    private String clientIp;

    @Column(name = "user_agent", length = 512)
    @ExcelColumn(headerName = "User-Agent", order = 8)
    private String userAgent;
}
