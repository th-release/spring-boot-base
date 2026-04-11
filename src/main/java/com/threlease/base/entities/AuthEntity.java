package com.threlease.base.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.threlease.base.common.annotation.ExcelColumn;
import com.threlease.base.common.entity.BaseEntity;
import com.threlease.base.common.enums.AuthStatuses;
import com.threlease.base.common.enums.AuthTypes;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Table(name = "tb_auth")
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class AuthEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "uuid", length = 36, nullable = false)
    @ExcelColumn(headerName = "사용자 UUID")
    private String uuid;

    @Column(name = "username", length = 24, nullable = false)
    @ExcelColumn(headerName = "아이디")
    private String username;

    @Column(name = "nickname", length = 36, nullable = false)
    @ExcelColumn(headerName = "닉네임")
    private String nickname;

    @Column(name = "email", length = 255)
    @ExcelColumn(headerName = "이메일")
    private String email;

    @JsonIgnore
    @Column(name = "password", columnDefinition = "text", nullable = false)
    private String password;

    @JsonIgnore
    @Column(name = "salt", length = 64, nullable = false)
    private String salt;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 50)
    @ExcelColumn(headerName = "인증 타입")
    @Builder.Default
    private AuthTypes type = AuthTypes.GENERAL;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    @ExcelColumn(headerName = "계정 상태")
    @Builder.Default
    private AuthStatuses status = AuthStatuses.ACTIVE;
}
