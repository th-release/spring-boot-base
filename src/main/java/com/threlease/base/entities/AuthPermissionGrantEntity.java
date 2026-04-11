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
@Table(name = "tb_auth_permission_grant")
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class AuthPermissionGrantEntity extends BaseEntity {
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

    @Column(name = "permission_id", nullable = false)
    @ExcelColumn(headerName = "권한 ID", order = 2)
    private Long permissionId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "permission_id", referencedColumnName = "id", insertable = false, updatable = false)
    private AuthPermissionEntity permission;

    @Column(name = "granted_by_uuid", length = 36)
    @ExcelColumn(headerName = "부여자 UUID", order = 3)
    private String grantedByUuid;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "granted_by_uuid", referencedColumnName = "uuid", insertable = false, updatable = false)
    private AuthEntity grantedBy;
}
