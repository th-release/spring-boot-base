package com.threlease.base.functions.auth.v1;

import com.threlease.base.common.annotation.ApiVersion;
import com.threlease.base.common.utils.responses.BasicResponse;
import com.threlease.base.entities.AuthEntity;
import com.threlease.base.functions.auth.AuthFlowService;
import com.threlease.base.functions.auth.dto.MfaRegisterDto;
import com.threlease.base.functions.auth.dto.MfaSetupResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@ApiVersion(1)
@RequestMapping("/auth/mfa")
@Tag(name = "Auth MFA API (v1)")
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.auth.mfa", name = "enabled", havingValue = "true")
public class AuthMfaController {
    private final AuthFlowService authFlowService;

    @GetMapping("/setup")
    @Operation(summary = "MFA 설정 정보 조회")
    public ResponseEntity<BasicResponse<MfaSetupResponseDto>> setupMfa(HttpServletRequest request) {
        return BasicResponse.ok(authFlowService.setupMfa((AuthEntity) request.getAttribute("user")));
    }

    @PostMapping("/register")
    @Operation(summary = "MFA 등록 완료")
    public ResponseEntity<BasicResponse<Void>> registerMfa(@RequestBody @Valid MfaRegisterDto dto, HttpServletRequest request) {
        authFlowService.registerMfa((AuthEntity) request.getAttribute("user"), dto, request);
        return BasicResponse.noContent();
    }
}
