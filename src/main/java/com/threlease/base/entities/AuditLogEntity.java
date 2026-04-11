package com.threlease.base.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.threlease.base.common.annotation.ExcelColumn;
import com.threlease.base.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.EntityListeners;

@Entity
@Table(name = "tb_audit_log")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class AuditLogEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @ExcelColumn(headerName = "ID", order = 0)
    private Long id;

    @Column(name = "actor_uuid", length = 36)
    @ExcelColumn(headerName = "수행자 UUID", order = 1)
    private String actorUuid;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_uuid", referencedColumnName = "uuid", insertable = false, updatable = false)
    private AuthEntity actor;

    @Column(name = "action", nullable = false, length = 120)
    @ExcelColumn(headerName = "액션", order = 2)
    private String action;

    @Column(name = "resource_type", nullable = false, length = 120)
    @ExcelColumn(headerName = "리소스 타입", order = 3)
    private String resourceType;

    @Column(name = "resource_id", length = 120)
    @ExcelColumn(headerName = "리소스 ID", order = 4)
    private String resourceId;

    @Column(name = "success", nullable = false)
    @ExcelColumn(headerName = "성공 여부", order = 5)
    private boolean success;

    @Column(name = "client_ip", length = 64)
    @ExcelColumn(headerName = "클라이언트 IP", order = 6)
    private String clientIp;

    @Column(name = "user_agent", length = 512)
    @ExcelColumn(headerName = "User-Agent", order = 7)
    private String userAgent;

    @Column(name = "detail", columnDefinition = "text")
    @ExcelColumn(headerName = "상세", order = 8)
    private String detail;
}
