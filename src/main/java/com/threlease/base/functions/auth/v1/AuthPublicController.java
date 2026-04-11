package com.threlease.base.functions.auth.v1;

import com.threlease.base.common.HttpConstants;
import com.threlease.base.common.annotation.ApiVersion;
import com.threlease.base.common.annotation.RateLimit;
import com.threlease.base.common.utils.ClientIpResolver;
import com.threlease.base.common.utils.responses.BasicResponse;
import com.threlease.base.functions.auth.AuthFlowService;
import com.threlease.base.functions.auth.dto.AuthProfileDto;
import com.threlease.base.functions.auth.dto.LoginDto;
import com.threlease.base.functions.auth.dto.PasswordResetConfirmDto;
import com.threlease.base.functions.auth.dto.SignUpDto;
import com.threlease.base.functions.auth.dto.TokenResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@ApiVersion(1)
@RequestMapping("/auth")
@Tag(name = "Auth Public API (v1)")
@RequiredArgsConstructor
public class AuthPublicController {
    private final AuthFlowService authFlowService;
    private final ClientIpResolver clientIpResolver;

    @PostMapping("/login")
    @RateLimit(limit = 10, window = 60)
    @Operation(summary = "로그인")
    public ResponseEntity<BasicResponse<TokenResponseDto>> login(@RequestBody @Valid LoginDto dto, HttpServletRequest request) {
        return BasicResponse.created(authFlowService.login(dto, request.getHeader("User-Agent"), clientIpResolver.resolve(request), request));
    }

    @PostMapping("/refresh")
    @Operation(summary = "토큰 재발급")
    public ResponseEntity<BasicResponse<TokenResponseDto>> refresh(
            @RequestHeader(HttpConstants.HEADER_REFRESH_TOKEN) String refreshToken,
            HttpServletRequest request) {
        return BasicResponse.ok(authFlowService.refresh(refreshToken, request.getHeader("User-Agent"), clientIpResolver.resolve(request)));
    }

    @PostMapping("/signup")
    @RateLimit(limit = 5, window = 60)
    @Operation(summary = "회원가입")
    public ResponseEntity<BasicResponse<AuthProfileDto>> signUp(@RequestBody @Valid SignUpDto dto) {
        return BasicResponse.created(authFlowService.signUp(dto));
    }

    @PostMapping("/password/reset/confirm")
    @RateLimit(limit = 10, window = 300)
    @Operation(summary = "비밀번호 재설정 완료")
    public ResponseEntity<BasicResponse<Void>> confirmPasswordReset(@RequestBody @Valid PasswordResetConfirmDto dto,
                                                                    HttpServletRequest request) {
        authFlowService.confirmPasswordReset(dto, request);
        return BasicResponse.noContent();
    }
}
