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
@Table(name = "tb_fcm_device_token")
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class FcmDeviceTokenEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 36)
    private String userUuid;

    @Column(nullable = false, unique = true, length = 512)
    private String deviceToken;

    @Column(length = 120)
    private String deviceLabel;

    @Column(length = 512)
    private String userAgent;

    @Column(length = 64)
    private String lastIpAddress;

    private LocalDateTime lastUsedAt;

    @Column(nullable = false)
    @Builder.Default
    private boolean enabled = true;
}
