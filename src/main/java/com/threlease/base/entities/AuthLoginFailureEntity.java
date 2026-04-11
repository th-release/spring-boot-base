package com.threlease.base.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.threlease.base.common.annotation.ExcelColumn;
import com.threlease.base.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
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
@Table(name = "tb_auth_login_failure")
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class AuthLoginFailureEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @ExcelColumn(headerName = "ID")
    private Long id;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "user_uuid", referencedColumnName = "uuid")
    @ExcelColumn(headerName = "사용자")
    private AuthEntity user;

    @Column(name = "failed_login_count", nullable = false)
    @ExcelColumn(headerName = "누적 실패 횟수")
    @Builder.Default
    private int failedLoginCount = 0;

    @Column(name = "locked_until")
    @ExcelColumn(headerName = "잠금 만료 시간")
    private LocalDateTime lockedUntil;
}
