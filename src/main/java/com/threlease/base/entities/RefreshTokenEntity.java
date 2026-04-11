package com.threlease.base.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.threlease.base.common.annotation.ExcelColumn;
import com.threlease.base.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Builder
@Entity
@Getter
@Setter
@Table(name = "tb_refresh_token")
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class RefreshTokenEntity extends BaseEntity {
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

    @Column(name = "token_id", nullable = false, length = 64)
    @ExcelColumn(headerName = "토큰 ID", order = 2)
    private String tokenId;

    @Column(name = "family_id", nullable = false, length = 64)
    @ExcelColumn(headerName = "토큰 패밀리 ID", order = 3)
    private String familyId;

    @Column(name = "token_hash", nullable = false, length = 1024)
    private String tokenHash;

    @Column(name = "token", nullable = false, length = 1024)
    private String token;

    @Column(name = "expiry_date", nullable = false)
    @ExcelColumn(headerName = "만료 시간", order = 4)
    private LocalDateTime expiryDate;

    @Column(name = "user_agent", length = 512)
    @ExcelColumn(headerName = "User-Agent", order = 5)
    private String userAgent;

    @Column(name = "device_label", length = 128)
    @ExcelColumn(headerName = "기기명", order = 6)
    private String deviceLabel;

    @Column(name = "ip_address", length = 64)
    @ExcelColumn(headerName = "IP", order = 7)
    private String ipAddress;

    @Column(name = "last_used_at")
    @ExcelColumn(headerName = "최근 사용 시간", order = 8)
    private LocalDateTime lastUsedAt;

    @Column(name = "revoked", nullable = false)
    @ExcelColumn(headerName = "폐기 여부", order = 9)
    @Builder.Default
    private boolean revoked = false;

    @Column(name = "replaced_by_token_id", length = 64)
    @ExcelColumn(headerName = "교체 토큰 ID", order = 10)
    private String replacedByTokenId;

    public boolean isExpired() {
        return expiryDate.isBefore(LocalDateTime.now());
    }
}
