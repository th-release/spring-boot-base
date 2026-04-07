package com.threlease.base.common.utils.storage.batch;

import com.threlease.base.common.properties.storage.StorageProperties;
import com.threlease.base.common.utils.storage.entity.FileEntity;
import com.threlease.base.common.utils.storage.repository.FileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * soft delete 된 파일을 실제 스토리지에서 제거하고 DB 레코드를 완전 삭제하는 배치 잡
 *
 * 실행 주기 설정 (application.yml):
 * <pre>
 * storage:
 *   local:
 *     path: ./uploads
 *   cleanup:
 *     cron: "0 0 3 * * *"   # 매일 새벽 3시 (기본값). 비활성화하려면 "-" 입력
 *     fixed-rate: 0          # 고정 주기(ms). 0이면 비활성화, 양수면 해당 ms마다 실행
 *     chunk-size: 100        # 한 번에 처리할 파일 수 (기본값 100)
 * </pre>
 *
 * cron 과 fixed-rate 를 동시에 설정하면 둘 다 독립적으로 실행됩니다.
 * 하나만 사용하려면 나머지를 비활성화 값으로 설정하세요.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrphanFileCleanupJob {

    private final FileRepository fileRepository;
    private final OrphanFileDeleter orphanFileDeleter;
    private final StorageProperties storageProperties;

    // =========================================================
    // cron 기반 실행 (기본: 매일 새벽 3시)
    // =========================================================

    @Scheduled(cron = "${storage.cleanup.cron:0 0 3 * * *}")
    public void cleanupByCron() {
        log.info("[OrphanFileCleanup] cron 배치 시작");
        cleanup();
    }

    // =========================================================
    // fixed-rate 기반 실행 (비활성 시 주석 처리 권장)
    // =========================================================

    // storage.cleanup.fixed-rate가 설정되지 않았을 때 매우 큰 값(100년)을 주어 사실상 비활성화합니다.
    @Scheduled(fixedRateString = "${storage.cleanup.fixed-rate:3153600000000}")
    public void cleanupByFixedRate() {
        Long fixedRate = storageProperties.getCleanup().getFixedRate();
        if (fixedRate == null || fixedRate <= 0) return;
        log.info("[OrphanFileCleanup] fixed-rate 배치 시작 ({}ms 주기)", fixedRate);
        cleanup();
    }

    // =========================================================
    // 핵심 정리 로직
    // =========================================================

    @Transactional
    public void cleanup() {
        int chunkSize = storageProperties.getCleanup().getChunkSize();
        long totalTarget = fileRepository.countByDeletedTrue();
        if (totalTarget == 0) {
            log.info("[OrphanFileCleanup] 정리할 파일 없음, 종료");
            return;
        }

        log.info("[OrphanFileCleanup] 정리 대상: {}건", totalTarget);

        int successCount = 0;
        int failCount = 0;
        List<FileEntity> chunk;

        // 청크 단위 반복 처리 (전체 로드 시 OOM 방지)
        do {
            chunk = fileRepository.findAllByDeletedTrue(PageRequest.of(0, chunkSize));

            for (FileEntity file : chunk) {
                boolean deleted = orphanFileDeleter.deletePhysical(file);
                if (deleted) {
                    fileRepository.delete(file);
                    successCount++;
                } else {
                    failCount++;
                }
            }

        } while (chunk.size() == chunkSize);

        log.info("[OrphanFileCleanup] 완료 — 성공: {}건, 실패: {}건", successCount, failCount);
    }
}
