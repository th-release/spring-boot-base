package com.threlease.base.functions.auth.v1;

import com.threlease.base.common.HttpConstants;
import com.threlease.base.common.annotation.ApiVersion;
import com.threlease.base.common.annotation.RateLimit;
import com.threlease.base.common.exception.BusinessException;
import com.threlease.base.common.exception.ErrorCode;
import com.threlease.base.common.utils.crypto.HashComponent;
import com.threlease.base.common.utils.random.RandomComponent;
import com.threlease.base.common.utils.responses.BasicResponse;
import com.threlease.base.entities.AuthEntity;
import com.threlease.base.functions.auth.AuthService;
import com.threlease.base.functions.auth.dto.LoginDto;
import com.threlease.base.functions.auth.dto.RefreshTokenSessionDto;
import com.threlease.base.functions.auth.dto.SignUpDto;
import com.threlease.base.functions.auth.dto.TokenResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
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
    private final HashComponent hashComponent;
    private final RandomComponent randomComponent;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    @RateLimit(limit = 10, window = 60)
    @Operation(summary = "로그인")
    public ResponseEntity<BasicResponse<TokenResponseDto>> login(@RequestBody @Valid LoginDto dto) {
        AuthEntity auth = authService.findOneByUsername(dto.getUsername())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (!isPasswordValid(dto.getPassword(), auth)) {
            throw new BusinessException(ErrorCode.WRONG_PASSWORD);
        }

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
        return BasicResponse.noContent();
    }

    @PostMapping("/logout-all")
    @Operation(summary = "전체 로그아웃")
    public ResponseEntity<BasicResponse<Void>> logoutAll(HttpServletRequest request) {
        AuthEntity user = (AuthEntity) request.getAttribute("user");
        authService.logoutAll(user.getUuid());
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
        return BasicResponse.noContent();
    }

    @PostMapping("/signup")
    @RateLimit(limit = 5, window = 60)
    @Operation(summary = "회원가입")
    public ResponseEntity<BasicResponse<AuthEntity>> signUp(@RequestBody @Valid SignUpDto dto) {
        if (authService.findOneByUsername(dto.getUsername()).isPresent()) {
            throw new BusinessException(ErrorCode.USER_DUPLICATE);
        }

        String salt = randomComponent.generateAlphanumeric(32);

        AuthEntity user = AuthEntity.builder()
                .username(dto.getUsername())
                .password(passwordEncoder.encode(dto.getPassword()))
                .salt(salt)
                .role(com.threlease.base.common.enums.Roles.ROLE_USER)
                .build();

        authService.authSave(user);

        return BasicResponse.created(user);
    }

    @GetMapping("/@me")
    @Operation(summary = "내 정보 조회")
    public ResponseEntity<BasicResponse<AuthEntity>> me(@RequestHeader(HttpConstants.HEADER_AUTHORIZATION) String token) {
        AuthEntity user = authService.findOneByToken(token)
                .orElseThrow(() -> new BusinessException(ErrorCode.TOKEN_INVALID));

        return BasicResponse.ok(user);
    }

    private boolean isPasswordValid(String rawPassword, AuthEntity auth) {
        if (passwordEncoder.matches(rawPassword, auth.getPassword())) {
            return true;
        }

        String legacyHash = hashComponent.generateSHA512(rawPassword + auth.getSalt());
        if (!legacyHash.equals(auth.getPassword())) {
            return false;
        }

        auth.setPassword(passwordEncoder.encode(rawPassword));
        auth.setSalt(randomComponent.generateAlphanumeric(32));
        authService.authSave(auth);
        return true;
    }
}
