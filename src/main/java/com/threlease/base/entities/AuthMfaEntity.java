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

@Builder
@Entity
@Getter
@Setter
@Table(name = "tb_auth_mfa")
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class AuthMfaEntity extends BaseEntity {
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

    @JsonIgnore
    @Column(name = "secret", columnDefinition = "text")
    private String secret;

    @Column(name = "enabled", nullable = false)
    @ExcelColumn(headerName = "활성화", order = 2)
    @Builder.Default
    private boolean enabled = false;
}
