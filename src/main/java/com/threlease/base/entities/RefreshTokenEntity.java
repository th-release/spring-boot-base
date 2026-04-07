package com.threlease.base.entities;

import com.threlease.base.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Builder
@Entity
@Getter
@Setter
@Table(name = "RefreshTokenEntity")
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class RefreshTokenEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String userUuid;

    @Column(nullable = false, unique = true, length = 64)
    private String tokenId;

    @Column(nullable = false, length = 64)
    private String familyId;

    @Column(nullable = false, length = 1024)
    private String tokenHash;

    @Column(nullable = false, length = 1024)
    private String token;

    @Column(nullable = false)
    private LocalDateTime expiryDate;

    @Column(nullable = false)
    @Builder.Default
    private boolean revoked = false;

    @Column(length = 64)
    private String replacedByTokenId;

    public boolean isExpired() {
        return expiryDate.isBefore(LocalDateTime.now());
    }
}
