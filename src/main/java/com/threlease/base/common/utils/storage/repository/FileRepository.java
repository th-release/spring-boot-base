package com.threlease.base.common.utils.storage.repository;

import com.threlease.base.common.utils.storage.entity.FileEntity;
import com.threlease.base.entities.AuthEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FileRepository extends JpaRepository<FileEntity, Long> {
    @Query("SELECT f FROM FileEntity f WHERE f.id = :id AND f.deleted = false")
    Optional<FileEntity> findActiveById(@Param("id") Long id);

    @Query("SELECT f FROM FileEntity f WHERE f.id = :id AND f.deleted = false AND f.owner = :owner")
    Optional<FileEntity> findActiveByIdAndOwner(@Param("id") Long id, @Param("owner") AuthEntity owner);

    default Optional<FileEntity> findActiveByIdAndOwnerUuid(Long id, String ownerUuid) {
        return findActiveByIdAndOwner(id, AuthEntity.builder().uuid(ownerUuid).build());
    }

    /** 경로로 활성 파일 조회 */
    @Query("SELECT f FROM FileEntity f WHERE f.filePath = :filePath AND f.deleted = false")
    Optional<FileEntity> findByFilePathAndDeletedFalse(@Param("filePath") String filePath);

    /** 특정 디렉토리의 활성 파일 전체 조회 */
    @Query("SELECT f FROM FileEntity f WHERE f.dirName = :dirName AND f.deleted = false")
    List<FileEntity> findAllByDirNameAndDeletedFalse(@Param("dirName") String dirName);

    /** soft delete 된 파일 목록 조회 (배치용, 청크 단위 처리) */
    @Query("SELECT f FROM FileEntity f WHERE f.deleted = true")
    List<FileEntity> findAllByDeletedTrue(Pageable pageable);

    /** soft delete 된 파일 수 조회 */
    @Query("SELECT COUNT(f) FROM FileEntity f WHERE f.deleted = true")
    long countByDeletedTrue();
}
