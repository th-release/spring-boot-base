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
import com.threlease.base.functions.auth.dto.SignUpDto;
import com.threlease.base.functions.auth.dto.TokenResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/login")
    @RateLimit(limit = 10, window = 60)
    @Operation(summary = "로그인")
    public ResponseEntity<BasicResponse<TokenResponseDto>> login(@RequestBody @Valid LoginDto dto) {
        AuthEntity auth = authService.findOneByUsername(dto.getUsername())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (!hashComponent.generateSHA512(dto.getPassword() + auth.getSalt()).equals(auth.getPassword())) {
            throw new BusinessException(ErrorCode.WRONG_PASSWORD);
        }

        return BasicResponse.created(authService.issueTokens(auth));
    }

    @PostMapping("/refresh")
    @Operation(summary = "토큰 재발급")
    public ResponseEntity<BasicResponse<TokenResponseDto>> refresh(@RequestHeader(HttpConstants.HEADER_REFRESH_TOKEN) String refreshToken) {
        return BasicResponse.ok(authService.refresh(refreshToken));
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
                .password(hashComponent.generateSHA512(dto.getPassword() + salt))
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
}
