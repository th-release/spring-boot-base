package com.threlease.base.entites;

import com.threlease.base.enums.Roles;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Builder
@Entity
@Getter
@Setter
@Table(name = "AuthEntity")
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class AuthEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "uuid", length = 36, columnDefinition = "varchar(36)", nullable = false)
    private String uuid;

    @Column(name = "username", columnDefinition = "varchar(24)", nullable = false, unique = true)
    private String username;

    @Column(name = "nickname", columnDefinition = "varchar(22)", nullable = false)
    private String nickname;

    @Column(name = "password", columnDefinition = "text", nullable = false)
    private String password;

    @Column(name = "salt", columnDefinition = "varchar(32)", length = 32, nullable = false)
    private String salt;

    @Column(name = "profilePath", columnDefinition = "text", nullable = true)
    private String profilePath;

    @CreatedDate
    @Column(updatable = false, nullable = false, name = "createdAt")
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    private Roles role;
}
