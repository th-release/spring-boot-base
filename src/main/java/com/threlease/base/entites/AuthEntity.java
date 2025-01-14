package com.threlease.base.entites;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.threlease.base.common.enums.Roles;
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
    @Column(length = 36, nullable = false)
    private String uuid;

    @Column(length = 24, nullable = false, unique = true)
    private String username;

    @Column(length = 36, nullable = false)
    private String nickname;

    @JsonIgnore
    @Column(columnDefinition = "text", nullable = false)
    private String password;

    @JsonIgnore
    @Column(length = 32, nullable = false)
    private String salt;

    @CreatedDate
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    private Roles role;
}
