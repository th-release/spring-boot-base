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

import java.time.LocalDateTime;

@Builder
@Entity
@Getter
@Setter
@Table(name = "tb_fcm_notification")
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class FcmNotificationEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @ExcelColumn(headerName = "ID", order = 0)
    private Long id;

    @Column(name = "user_uuid", nullable = false, length = 36)
    @ExcelColumn(headerName = "수신자 UUID", order = 1)
    private String userUuid;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_uuid", referencedColumnName = "uuid", insertable = false, updatable = false)
    private AuthEntity user;

    @Column(name = "message_id", length = 255)
    @ExcelColumn(headerName = "FCM 메시지 ID", order = 2)
    private String messageId;

    @Column(name = "title", nullable = false, length = 255)
    @ExcelColumn(headerName = "제목", order = 3)
    private String title;

    @Column(name = "body", nullable = false, columnDefinition = "text")
    @ExcelColumn(headerName = "본문", order = 4)
    private String body;

    @Column(name = "data", columnDefinition = "text")
    @ExcelColumn(headerName = "데이터", order = 5)
    private String data;

    @Column(name = "read", nullable = false)
    @ExcelColumn(headerName = "읽음 여부", order = 6)
    @Builder.Default
    private boolean read = false;

    @Column(name = "read_at")
    @ExcelColumn(headerName = "읽은 시간", order = 7)
    private LocalDateTime readAt;

    public void markRead() {
        if (!read) {
            read = true;
            readAt = LocalDateTime.now();
        }
    }
}
