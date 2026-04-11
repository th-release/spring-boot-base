package com.threlease.base.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.threlease.base.common.annotation.ExcelColumn;
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
@Table(name = "tb_auth_verification")
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class AuthVerificationEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @ExcelColumn(headerName = "ID", order = 0)
    private Long id;

    @Column(name = "user_uuid", nullable = false, length = 36)
    @ExcelColumn(headerName = "사용자 UUID", order = 1)
    private String userUuid;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_uuid", referencedColumnName = "uuid", insertable = false, updatable = false)
    private AuthEntity user;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 50)
    @ExcelColumn(headerName = "인증 타입", order = 2)
    private AuthVerificationType type;

    @Column(name = "target", nullable = false, length = 255)
    @ExcelColumn(headerName = "인증 대상", order = 3)
    private String target;

    @Column(name = "verification_hash", nullable = false, length = 255)
    private String verificationHash;

    @Column(name = "expires_at", nullable = false)
    @ExcelColumn(headerName = "만료 시간", order = 4)
    private LocalDateTime expiresAt;

    @Column(name = "verified", nullable = false)
    @ExcelColumn(headerName = "인증 완료", order = 5)
    @Builder.Default
    private boolean verified = false;

    @Column(name = "metadata", length = 120)
    @ExcelColumn(headerName = "메타데이터", order = 6)
    private String metadata;
}
