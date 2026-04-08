package com.threlease.base.functions.auth.v1;

import com.threlease.base.common.annotation.ApiVersion;
import com.threlease.base.common.utils.responses.BasicResponse;
import com.threlease.base.entities.AuthEntity;
import com.threlease.base.functions.auth.AuthAdminService;
import com.threlease.base.functions.auth.AuthService;
import com.threlease.base.functions.auth.dto.AdminUserSummaryDto;
import com.threlease.base.functions.auth.dto.AuditLogDto;
import com.threlease.base.functions.auth.dto.RefreshTokenSessionDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@ApiVersion(1)
@RequestMapping("/auth/admin")
@Tag(name = "Auth Admin API (v1)")
@RequiredArgsConstructor
public class AuthAdminController {
    private final AuthAdminService authAdminService;

    @GetMapping("/users")
    @Operation(summary = "관리자용 사용자 목록")
    public ResponseEntity<BasicResponse<AuthService.PageResult<AdminUserSummaryDto>>> users(
            @RequestParam(defaultValue = "") String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest request) {
        return BasicResponse.ok(authAdminService.getUsers((AuthEntity) request.getAttribute("user"), query, page, size));
    }

    @GetMapping("/users/{uuid}/sessions")
    @Operation(summary = "관리자용 사용자 세션 조회")
    public ResponseEntity<BasicResponse<List<RefreshTokenSessionDto>>> userSessions(@PathVariable String uuid, HttpServletRequest request) {
        return BasicResponse.ok(authAdminService.getUserSessions((AuthEntity) request.getAttribute("user"), uuid));
    }

    @PostMapping("/users/{uuid}/logout-all")
    @Operation(summary = "관리자용 전체 세션 종료")
    public ResponseEntity<BasicResponse<Void>> logoutAll(@PathVariable String uuid, HttpServletRequest request) {
        authAdminService.logoutAll((AuthEntity) request.getAttribute("user"), uuid, request);
        return BasicResponse.noContent();
    }

    @PostMapping("/users/{uuid}/lock")
    @Operation(summary = "관리자용 사용자 잠금")
    public ResponseEntity<BasicResponse<Void>> lockUser(@PathVariable String uuid,
                                                        @RequestParam(defaultValue = "30") long minutes,
                                                        HttpServletRequest request) {
        authAdminService.lockUser((AuthEntity) request.getAttribute("user"), uuid, minutes, request);
        return BasicResponse.noContent();
    }

    @PostMapping("/users/{uuid}/unlock")
    @Operation(summary = "관리자용 사용자 잠금 해제")
    public ResponseEntity<BasicResponse<Void>> unlockUser(@PathVariable String uuid, HttpServletRequest request) {
        authAdminService.unlockUser((AuthEntity) request.getAttribute("user"), uuid, request);
        return BasicResponse.noContent();
    }

    @PostMapping("/users/{uuid}/mfa/reset")
    @Operation(summary = "관리자용 사용자 MFA 초기화")
    public ResponseEntity<BasicResponse<Void>> resetUserMfa(@PathVariable String uuid, HttpServletRequest request) {
        authAdminService.resetUserMfa((AuthEntity) request.getAttribute("user"), uuid, request);
        return BasicResponse.noContent();
    }

    @GetMapping("/audit-logs")
    @Operation(summary = "관리자용 감사 로그 조회")
    public ResponseEntity<BasicResponse<Page<AuditLogDto>>> auditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest request) {
        return BasicResponse.ok(authAdminService.getAuditLogs((AuthEntity) request.getAttribute("user"), page, size));
    }
}
