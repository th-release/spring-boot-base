package com.threlease.base.functions.auth.v1;

import com.threlease.base.common.annotation.ApiVersion;
import com.threlease.base.common.annotation.RateLimit;
import com.threlease.base.common.utils.responses.BasicResponse;
import com.threlease.base.functions.auth.AuthFlowService;
import com.threlease.base.functions.auth.dto.PasswordResetRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@ApiVersion(1)
@RequestMapping("/auth/password/reset")
@Tag(name = "Auth Email API (v1)")
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.email", name = "enabled", havingValue = "true")
public class AuthEmailController {
    private final AuthFlowService authFlowService;

    @PostMapping("/request")
    @RateLimit(limit = 5, window = 300)
    @Operation(summary = "비밀번호 재설정 요청")
    public ResponseEntity<BasicResponse<Void>> requestPasswordReset(@RequestBody @Valid PasswordResetRequestDto dto,
                                                                    HttpServletRequest request) {
        authFlowService.requestPasswordReset(dto, request);
        return BasicResponse.noContent();
    }
}
