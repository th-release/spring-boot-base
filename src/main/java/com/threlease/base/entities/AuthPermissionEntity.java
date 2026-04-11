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
@Table(name = "tb_auth_permission")
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class AuthPermissionEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @ExcelColumn(headerName = "ID", order = 0)
    private Long id;

    @Column(name = "code", nullable = false, length = 120)
    @ExcelColumn(headerName = "권한 코드", order = 1)
    private String code;

    @Column(name = "name", nullable = false, length = 120)
    @ExcelColumn(headerName = "권한명", order = 2)
    private String name;

    @Column(name = "depth", nullable = false)
    @ExcelColumn(headerName = "뎁스", order = 3)
    private int depth;

    @Column(name = "parent_id")
    @ExcelColumn(headerName = "상위 권한 ID", order = 4)
    private Long parentId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", referencedColumnName = "id", insertable = false, updatable = false)
    private AuthPermissionEntity parent;

    @Column(name = "sort_order", nullable = false)
    @ExcelColumn(headerName = "정렬 순서", order = 5)
    @Builder.Default
    private int sortOrder = 0;

    @Column(name = "description", length = 255)
    @ExcelColumn(headerName = "설명", order = 6)
    private String description;
}
