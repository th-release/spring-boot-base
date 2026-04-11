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
@Table(name = "tb_fcm_device_token")
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class FcmDeviceTokenEntity extends BaseEntity {
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

    @Column(name = "device_token", nullable = false, length = 512)
    private String deviceToken;

    @Column(name = "device_label", length = 120)
    @ExcelColumn(headerName = "기기명", order = 2)
    private String deviceLabel;

    @Column(name = "user_agent", length = 512)
    @ExcelColumn(headerName = "User-Agent", order = 3)
    private String userAgent;

    @Column(name = "last_ip_address", length = 64)
    @ExcelColumn(headerName = "최근 IP", order = 4)
    private String lastIpAddress;

    @Column(name = "last_used_at")
    @ExcelColumn(headerName = "최근 사용 시간", order = 5)
    private LocalDateTime lastUsedAt;

    @Column(name = "enabled", nullable = false)
    @ExcelColumn(headerName = "활성화", order = 6)
    @Builder.Default
    private boolean enabled = true;
}
