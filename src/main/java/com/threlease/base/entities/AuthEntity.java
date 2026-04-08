package com.threlease.base.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.threlease.base.common.entity.BaseEntity;
import com.threlease.base.common.enums.Roles;
import jakarta.persistence.Convert;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Builder
@Entity
@Getter
@Setter
@Table(name = "AuthEntity")
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class AuthEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(length = 36, nullable = false)
    private String uuid;

    @Column(length = 24, nullable = false, unique = true)
    private String username;

    @Column(length = 36, nullable = false)
    private String nickname;

    @Column(length = 255, unique = true)
    private String email;

    @JsonIgnore
    @Column(columnDefinition = "text", nullable = false)
    private String password;

    @JsonIgnore
    @Column(length = 32, nullable = false)
    private String salt;

    @JsonIgnore
    @Column(length = 255)
    private String passwordResetCodeHash;

    private LocalDateTime passwordResetCodeExpiry;

    @Column(nullable = false)
    @Builder.Default
    private int failedLoginCount = 0;

    private LocalDateTime lockedUntil;

    private LocalDateTime lastLoginAt;

    @Column(length = 64)
    private String lastLoginIp;

    @JsonIgnore
    @Column(columnDefinition = "text")
    private String mfaSecret;

    @Column(nullable = false)
    @Builder.Default
    private boolean mfaEnabled = false;

    @Enumerated(EnumType.STRING)
    private Roles role;
}
