package com.threlease.base.common.controller;

import com.threlease.base.common.annotation.ApiVersion;
import com.threlease.base.common.utils.storage.FileService;
import com.threlease.base.common.utils.storage.dto.FileDownloadUrlDto;
import com.threlease.base.common.utils.storage.dto.FileUploadResponseDto;
import com.threlease.base.common.utils.responses.BasicResponse;
import com.threlease.base.entities.AuthEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.util.AntPathMatcher;

import java.io.IOException;

@Validated
@RestController
@ApiVersion(1)
@RequestMapping("/files")
@RequiredArgsConstructor
@Tag(name = "File API", description = "파일 업로드, 삭제, 정적 서빙 API")
public class FileController {
    private final FileService fileService;
    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    @GetMapping("/content/**")
    @Operation(summary = "파일 정적 서빙", description = "DB에 저장된 파일 메타데이터를 기준으로 로컬 파일을 서빙하거나 S3 URL로 리다이렉트합니다.")
    public ResponseEntity<?> serve(HttpServletRequest request) {
        return fileService.serve(extractFilePath(request));
    }

    @GetMapping("/{id}/download")
    @Operation(summary = "대용량 파일 다운로드 URL 발급", description = "파일 다운로드용 URL을 발급합니다. S3는 presigned URL, 로컬은 정적 서빙 URL을 반환합니다.")
    public ResponseEntity<BasicResponse<FileDownloadUrlDto>> downloadUrl(
            @PathVariable Long id,
            @RequestAttribute("user") AuthEntity user
    ) {
        return BasicResponse.ok(fileService.createDownloadUrl(id, user));
    }

    @PostMapping
    @Operation(summary = "파일 업로드", description = "파일을 업로드하고 DB에 메타데이터를 저장합니다.")
    public ResponseEntity<BasicResponse<FileUploadResponseDto>> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("dirName") @NotBlank String dirName,
            @RequestAttribute("user") AuthEntity user
    ) throws IOException {
        return BasicResponse.created(fileService.upload(file, dirName, user));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "파일 삭제", description = "파일 메타데이터를 기준으로 실제 파일을 삭제하고 DB에는 soft delete 처리합니다.")
    public ResponseEntity<BasicResponse<Void>> delete(
            @PathVariable Long id,
            @RequestAttribute("user") AuthEntity user
    ) {
        fileService.delete(id, user);
        return BasicResponse.noContent();
    }

    private String extractFilePath(HttpServletRequest request) {
        String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        String bestMatchPattern = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        return antPathMatcher.extractPathWithinPattern(bestMatchPattern, path);
    }
}
