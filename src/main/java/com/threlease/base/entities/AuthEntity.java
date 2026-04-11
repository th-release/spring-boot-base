package com.threlease.base.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.threlease.base.common.annotation.ExcelColumn;
import com.threlease.base.common.entity.BaseEntity;
import com.threlease.base.common.enums.UserTypes;
import jakarta.persistence.*;
import lombok.*;
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
    @ExcelColumn(headerName = "사용자 UUID", order = 0)
    private String uuid;

    @Column(name = "username", length = 24, nullable = false)
    @ExcelColumn(headerName = "아이디", order = 1)
    private String username;

    @Column(name = "nickname", length = 36, nullable = false)
    @ExcelColumn(headerName = "닉네임", order = 2)
    private String nickname;

    @Column(name = "email", length = 255)
    @ExcelColumn(headerName = "이메일", order = 3)
    private String email;

    @JsonIgnore
    @Column(name = "password", columnDefinition = "text", nullable = false)
    private String password;

    @JsonIgnore
    @Column(name = "salt", length = 32, nullable = false)
    private String salt;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 50)
    @ExcelColumn(headerName = "사용자 타입", order = 4)
    @Builder.Default
    private UserTypes type = UserTypes.USER;
}
