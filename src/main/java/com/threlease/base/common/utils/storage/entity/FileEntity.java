package com.threlease.base.common.utils.storage.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_files")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class FileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 저장된 파일 경로 또는 S3 Key */
    @Column(nullable = false)
    private String filePath;

    /** 원본 파일명 */
    @Column(nullable = false)
    private String originalFileName;

    /** MIME 타입 (예: image/jpeg) */
    private String contentType;

    /** 파일 크기 (bytes) */
    private Long fileSize;

    /** 저장 디렉토리 구분 (예: "profile", "post") */
    @Column(nullable = false)
    private String dirName;

    /** 파일 소유자 UUID */
    @Column(length = 36)
    private String ownerUuid;

    /** 스토리지 종류 */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StorageType storageType;

    /** 접근 가능한 URL */
    @Column(nullable = false)
    private String url;

    /** soft delete 여부 */
    @Column(nullable = false)
    @Builder.Default
    private boolean deleted = false;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    // -------------------------------------------------------

    public void markDeleted() {
        this.deleted = true;
    }

    public enum StorageType {
        LOCAL, S3
    }
}
