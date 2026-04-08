package com.threlease.base.entities;

import com.threlease.base.common.entity.BaseEntity;
import com.threlease.base.common.enums.AuthVerificationType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Builder
@Entity
@Getter
@Setter
@Table(name = "auth_verification")
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class AuthVerificationEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 36)
    private String userUuid;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AuthVerificationType type;

    @Column(nullable = false, length = 255)
    private String target;

    @Column(nullable = false, length = 255)
    private String verificationHash;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    @Builder.Default
    private boolean verified = false;

    @Column(length = 120)
    private String metadata;
}
