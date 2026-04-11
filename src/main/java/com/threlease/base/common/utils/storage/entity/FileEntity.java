package com.threlease.base.common.utils.storage.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.threlease.base.common.annotation.ExcelColumn;
import com.threlease.base.entities.AuthEntity;
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
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "uuid", length = 36, nullable = false)
    @ExcelColumn(headerName = "ID", order = 0)
    private String uuid;

    /** 저장된 파일 경로 또는 S3 Key */
    @Column(name = "file_path", nullable = false)
    @ExcelColumn(headerName = "파일 경로", order = 1)
    private String filePath;

    /** 원본 파일명 */
    @Column(name = "original_file_name", nullable = false)
    @ExcelColumn(headerName = "원본 파일명", order = 2)
    private String originalFileName;

    /** MIME 타입 (예: image/jpeg) */
    @Column(name = "content_type")
    @ExcelColumn(headerName = "콘텐츠 타입", order = 3)
    private String contentType;

    /** 파일 크기 (bytes) */
    @Column(name = "file_size")
    @ExcelColumn(headerName = "파일 크기", order = 4)
    private Long fileSize;

    /** 저장 디렉토리 구분 (예: "profile", "post") */
    @Column(name = "dir_name", nullable = false)
    @ExcelColumn(headerName = "디렉토리", order = 5)
    private String dirName;

    /** 파일 소유자 UUID */
    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "owner_uuid", referencedColumnName = "uuid")
    @ExcelColumn(headerName = "소유자")
    private AuthEntity owner;

    /** 스토리지 종류 */
    @Enumerated(EnumType.STRING)
    @Column(name = "storage_type", nullable = false, length = 20)
    @ExcelColumn(headerName = "스토리지 타입", order = 7)
    private StorageType storageType;

    /** 접근 가능한 URL */
    @Column(name = "url", nullable = false)
    @ExcelColumn(headerName = "URL", order = 8)
    private String url;

    /** soft delete 여부 */
    @Column(name = "deleted", nullable = false)
    @ExcelColumn(headerName = "삭제 여부", order = 9)
    @Builder.Default
    private boolean deleted = false;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    @ExcelColumn(headerName = "생성 시간", order = 10)
    private LocalDateTime createdAt;

    // -------------------------------------------------------

    public void markDeleted() {
        this.deleted = true;
    }

    public enum StorageType {
        LOCAL, S3
    }
}
