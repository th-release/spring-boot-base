package com.threlease.base.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.threlease.base.common.annotation.ExcelColumn;
import com.threlease.base.common.entity.BaseEntity;
import com.threlease.base.common.enums.Roles;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Builder
@Entity
@Getter
@Setter
@Table(name = "tb_auth")
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class AuthEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "uuid", length = 36, nullable = false)
    @ExcelColumn(headerName = "사용자 UUID", order = 0)
    private String uuid;

    @Column(name = "username", length = 24, nullable = false)
    @ExcelColumn(headerName = "아이디", order = 1)
    private String username;

    @Column(name = "nickname", length = 36, nullable = false)
    @ExcelColumn(headerName = "닉네임", order = 2)
    private String nickname;

    @Column(name = "email", length = 255)
    @ExcelColumn(headerName = "이메일", order = 3)
    private String email;

    @JsonIgnore
    @Column(name = "password", columnDefinition = "text", nullable = false)
    private String password;

    @JsonIgnore
    @Column(name = "salt", length = 32, nullable = false)
    private String salt;

    @JsonIgnore
    @Column(name = "password_reset_code_hash", length = 255)
    private String passwordResetCodeHash;

    @JsonIgnore
    @Column(name = "password_reset_code_expiry")
    private LocalDateTime passwordResetCodeExpiry;

    @Column(name = "failed_login_count", nullable = false)
    @ExcelColumn(headerName = "로그인 실패 횟수", order = 4)
    @Builder.Default
    private int failedLoginCount = 0;

    @Column(name = "locked_until")
    @ExcelColumn(headerName = "잠금 만료 시간", order = 5)
    private LocalDateTime lockedUntil;

    @Column(name = "last_login_at")
    @ExcelColumn(headerName = "최근 로그인 시간", order = 6)
    private LocalDateTime lastLoginAt;

    @Column(name = "last_login_ip", length = 64)
    @ExcelColumn(headerName = "최근 로그인 IP", order = 7)
    private String lastLoginIp;

    @JsonIgnore
    @Column(name = "mfa_secret", columnDefinition = "text")
    private String mfaSecret;

    @Column(name = "mfa_enabled", nullable = false)
    @ExcelColumn(headerName = "MFA 활성화", order = 8)
    @Builder.Default
    private boolean mfaEnabled = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", length = 50)
    @ExcelColumn(headerName = "권한", order = 9)
    private Roles role;
}
