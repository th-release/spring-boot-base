package com.threlease.base.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.threlease.base.common.annotation.ExcelColumn;
import com.threlease.base.common.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
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
public class AuthPermissionGrantEntity extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "uuid", length = 36, nullable = false)
    @ExcelColumn(headerName = "ID", order = 0)
    private String uuid;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "user_uuid", referencedColumnName = "uuid")
    @ExcelColumn(headerName = "사용자")
    private AuthEntity user;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "permission_uuid", referencedColumnName = "uuid")
    @ExcelColumn(headerName = "권한")
    private AuthPermissionEntity permission;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "granted_by_uuid", referencedColumnName = "uuid")
    @ExcelColumn(headerName = "부여자")
    private AuthEntity grantedBy;
}
