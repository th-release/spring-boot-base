package com.threlease.base.functions.auth.v1;

import com.threlease.base.common.HttpConstants;
import com.threlease.base.common.annotation.ApiVersion;
import com.threlease.base.common.annotation.RateLimit;
import com.threlease.base.common.exception.BusinessException;
import com.threlease.base.common.exception.ErrorCode;
import com.threlease.base.common.enums.Roles;
import com.threlease.base.common.utils.IpUtils;
import com.threlease.base.common.utils.responses.BasicResponse;
import com.threlease.base.entities.AuthEntity;
import com.threlease.base.functions.auth.AuthService;
import com.threlease.base.functions.auth.AuditLogService;
import com.threlease.base.functions.auth.MfaService;
import com.threlease.base.functions.auth.dto.AdminUserSummaryDto;
import com.threlease.base.functions.auth.dto.AuditLogDto;
import com.threlease.base.functions.auth.dto.ChangePasswordDto;
import com.threlease.base.functions.auth.dto.LoginDto;
import com.threlease.base.functions.auth.dto.MfaDisableDto;
import com.threlease.base.functions.auth.dto.MfaEnableDto;
import com.threlease.base.functions.auth.dto.MfaSetupResponseDto;
import com.threlease.base.functions.auth.dto.RefreshTokenSessionDto;
import com.threlease.base.functions.auth.dto.SignUpDto;
import com.threlease.base.functions.auth.dto.TokenResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * v1 인증 컨트롤러
 * @ApiVersion(1)에 의해 자동으로 /api/v1/auth 경로가 생성됩니다.
 */
@RestController
@ApiVersion(1)
@RequestMapping("/auth")
@Tag(name = "Auth API (v1)")
@AllArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;
    private final MfaService mfaService;

    @PostMapping("/login")
    @RateLimit(limit = 10, window = 60)
    @Operation(summary = "로그인")
    public ResponseEntity<BasicResponse<TokenResponseDto>> login(@RequestBody @Valid LoginDto dto, HttpServletRequest request) {
        AuthEntity auth = authService.findOneByUsername(dto.getUsername())
                .orElseThrow(() -> {
                    auditLogService.log(null, "LOGIN", "AUTH", dto.getUsername(), false, request, "User not found");
                    return new BusinessException(ErrorCode.USER_NOT_FOUND);
                });

        try {
            authService.ensureLoginAllowed(auth);
        } catch (BusinessException e) {
            auditLogService.log(auth.getUuid(), "LOGIN", "AUTH", auth.getUuid(), false, request, "Account locked");
            throw e;
        }

        if (!isPasswordValid(dto.getPassword(), auth)) {
            authService.recordFailedLogin(auth);
            auditLogService.log(auth.getUuid(), "LOGIN", "AUTH", auth.getUuid(), false, request, "Wrong password");
            throw new BusinessException(ErrorCode.WRONG_PASSWORD);
        }

        mfaService.verifyLogin(auth, dto.getOtpCode());
        authService.recordSuccessfulLogin(auth, IpUtils.getClientIp(request));
        auditLogService.log(auth.getUuid(), "LOGIN", "AUTH", auth.getUuid(), true, request, "User login succeeded");

        return BasicResponse.created(authService.issueTokens(auth));
    }

    @PostMapping("/refresh")
    @Operation(summary = "토큰 재발급")
    public ResponseEntity<BasicResponse<TokenResponseDto>> refresh(@RequestHeader(HttpConstants.HEADER_REFRESH_TOKEN) String refreshToken) {
        return BasicResponse.ok(authService.refresh(refreshToken));
    }

    @PostMapping("/logout")
    @Operation(summary = "로그아웃")
    public ResponseEntity<BasicResponse<Void>> logout(@RequestHeader(HttpConstants.HEADER_REFRESH_TOKEN) String refreshToken,
                                                      HttpServletRequest request) {
        AuthEntity user = (AuthEntity) request.getAttribute("user");
        authService.logout(refreshToken, user.getUuid());
        auditLogService.log(user.getUuid(), "LOGOUT", "AUTH", user.getUuid(), true, request, "Current session logout");
        return BasicResponse.noContent();
    }

    @PostMapping("/logout-all")
    @Operation(summary = "전체 로그아웃")
    public ResponseEntity<BasicResponse<Void>> logoutAll(HttpServletRequest request) {
        AuthEntity user = (AuthEntity) request.getAttribute("user");
        authService.logoutAll(user.getUuid());
        auditLogService.log(user.getUuid(), "LOGOUT_ALL", "AUTH", user.getUuid(), true, request, "All sessions revoked");
        return BasicResponse.noContent();
    }

    @GetMapping("/sessions")
    @Operation(summary = "활성 세션 목록 조회")
    public ResponseEntity<BasicResponse<List<RefreshTokenSessionDto>>> sessions(
            HttpServletRequest request,
            @RequestHeader(value = HttpConstants.HEADER_REFRESH_TOKEN, required = false) String refreshToken) {
        AuthEntity user = (AuthEntity) request.getAttribute("user");
        return BasicResponse.ok(authService.getSessions(user.getUuid(), refreshToken));
    }

    @DeleteMapping("/sessions/{tokenId}")
    @Operation(summary = "특정 세션 종료")
    public ResponseEntity<BasicResponse<Void>> revokeSession(@PathVariable String tokenId,
                                                             HttpServletRequest request) {
        AuthEntity user = (AuthEntity) request.getAttribute("user");
        authService.revokeSession(user.getUuid(), tokenId);
        auditLogService.log(user.getUuid(), "REVOKE_SESSION", "REFRESH_TOKEN", tokenId, true, request, "Single session revoked");
        return BasicResponse.noContent();
    }

    @PostMapping("/signup")
    @RateLimit(limit = 5, window = 60)
    @Operation(summary = "회원가입")
    public ResponseEntity<BasicResponse<AuthEntity>> signUp(@RequestBody @Valid SignUpDto dto) {
        if (authService.findOneByUsername(dto.getUsername()).isPresent()) {
            throw new BusinessException(ErrorCode.USER_DUPLICATE);
        }

        AuthEntity user = AuthEntity.builder()
                .username(dto.getUsername())
                .nickname(dto.getNickname())
                .password(passwordEncoder.encode(dto.getPassword()))
                .salt("")
                .role(com.threlease.base.common.enums.Roles.ROLE_USER)
                .build();

        authService.authSave(user);
        auditLogService.log(user.getUuid(), "SIGNUP", "AUTH", user.getUuid(), true, null, "User signup completed");

        return BasicResponse.created(user);
    }

    @PostMapping("/password/change")
    @Operation(summary = "비밀번호 변경")
    public ResponseEntity<BasicResponse<Void>> changePassword(@RequestBody @Valid ChangePasswordDto dto,
                                                              HttpServletRequest request) {
        AuthEntity user = (AuthEntity) request.getAttribute("user");
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new BusinessException(ErrorCode.PASSWORD_CHANGE_MISMATCH);
        }
        if (!isPasswordValid(dto.getCurrentPassword(), user)) {
            throw new BusinessException(ErrorCode.WRONG_PASSWORD);
        }
        authService.changePassword(user, passwordEncoder.encode(dto.getNewPassword()));
        auditLogService.log(user.getUuid(), "CHANGE_PASSWORD", "AUTH", user.getUuid(), true, request, "Password changed");
        return BasicResponse.noContent();
    }

    @GetMapping("/mfa/setup")
    @Operation(summary = "MFA 설정 정보 조회")
    public ResponseEntity<BasicResponse<MfaSetupResponseDto>> setupMfa(HttpServletRequest request) {
        AuthEntity user = (AuthEntity) request.getAttribute("user");
        return BasicResponse.ok(mfaService.setup(user));
    }

    @PostMapping("/mfa/enable")
    @Operation(summary = "MFA 활성화")
    public ResponseEntity<BasicResponse<Void>> enableMfa(@RequestBody @Valid MfaEnableDto dto, HttpServletRequest request) {
        AuthEntity user = (AuthEntity) request.getAttribute("user");
        mfaService.enable(user, dto.getOtpCode());
        auditLogService.log(user.getUuid(), "ENABLE_MFA", "AUTH", user.getUuid(), true, request, "MFA enabled");
        return BasicResponse.noContent();
    }

    @PostMapping("/mfa/disable")
    @Operation(summary = "MFA 비활성화")
    public ResponseEntity<BasicResponse<Void>> disableMfa(@RequestBody @Valid MfaDisableDto dto, HttpServletRequest request) {
        AuthEntity user = (AuthEntity) request.getAttribute("user");
        if (!isPasswordValid(dto.getPassword(), user)) {
            throw new BusinessException(ErrorCode.WRONG_PASSWORD);
        }
        if (user.isMfaEnabled()) {
            mfaService.verifyLogin(user, dto.getOtpCode());
        }
        mfaService.disable(user);
        auditLogService.log(user.getUuid(), "DISABLE_MFA", "AUTH", user.getUuid(), true, request, "MFA disabled");
        return BasicResponse.noContent();
    }

    @GetMapping("/@me")
    @Operation(summary = "내 정보 조회")
    public ResponseEntity<BasicResponse<AuthEntity>> me(@RequestHeader(HttpConstants.HEADER_AUTHORIZATION) String token) {
        AuthEntity user = authService.findOneByToken(token)
                .orElseThrow(() -> new BusinessException(ErrorCode.TOKEN_INVALID));

        return BasicResponse.ok(user);
    }

    @GetMapping("/admin/users")
    @Operation(summary = "관리자용 사용자 목록")
    public ResponseEntity<BasicResponse<AuthService.PageResult<AdminUserSummaryDto>>> adminUsers(
            @RequestParam(defaultValue = "") String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest request) {
        assertAdmin(request);
        return BasicResponse.ok(authService.getUsers(query, page, size));
    }

    @GetMapping("/admin/users/{uuid}/sessions")
    @Operation(summary = "관리자용 사용자 세션 조회")
    public ResponseEntity<BasicResponse<List<RefreshTokenSessionDto>>> adminUserSessions(@PathVariable String uuid,
                                                                                         HttpServletRequest request) {
        assertAdmin(request);
        return BasicResponse.ok(authService.getSessionsForUser(uuid));
    }

    @PostMapping("/admin/users/{uuid}/logout-all")
    @Operation(summary = "관리자용 전체 세션 종료")
    public ResponseEntity<BasicResponse<Void>> adminLogoutAll(@PathVariable String uuid, HttpServletRequest request) {
        AuthEntity admin = assertAdmin(request);
        authService.logoutAll(uuid);
        auditLogService.log(admin.getUuid(), "ADMIN_LOGOUT_ALL", "AUTH", uuid, true, request, "Admin revoked all sessions");
        return BasicResponse.noContent();
    }

    @PostMapping("/admin/users/{uuid}/lock")
    @Operation(summary = "관리자용 사용자 잠금")
    public ResponseEntity<BasicResponse<Void>> adminLockUser(@PathVariable String uuid,
                                                             @RequestParam(defaultValue = "30") long minutes,
                                                             HttpServletRequest request) {
        AuthEntity admin = assertAdmin(request);
        AuthEntity target = authService.findManagedUserByUuid(uuid)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        authService.forceLockUser(target, minutes);
        auditLogService.log(admin.getUuid(), "ADMIN_LOCK_USER", "AUTH", uuid, true, request, "User locked for " + minutes + " minutes");
        return BasicResponse.noContent();
    }

    @GetMapping("/admin/audit-logs")
    @Operation(summary = "관리자용 감사 로그 조회")
    public ResponseEntity<BasicResponse<org.springframework.data.domain.Page<AuditLogDto>>> adminAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest request) {
        assertAdmin(request);
        return BasicResponse.ok(auditLogService.getAuditLogs(PageRequest.of(page, size)));
    }

    private boolean isPasswordValid(String rawPassword, AuthEntity auth) {
        return passwordEncoder.matches(rawPassword, auth.getPassword());
    }

    private AuthEntity assertAdmin(HttpServletRequest request) {
        AuthEntity user = (AuthEntity) request.getAttribute("user");
        if (user == null || user.getRole() != Roles.ROLE_ADMIN) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        return user;
    }
}
