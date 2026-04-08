package com.threlease.base.functions.auth.v1;

import com.threlease.base.common.annotation.ApiVersion;
import com.threlease.base.common.utils.responses.BasicResponse;
import com.threlease.base.entities.AuthEntity;
import com.threlease.base.functions.auth.AuthFcmService;
import com.threlease.base.functions.auth.dto.FcmDeviceTokenDto;
import com.threlease.base.functions.auth.dto.FcmPushRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@ApiVersion(1)
@RequestMapping("/auth/admin")
@Tag(name = "Auth Admin FCM API (v1)")
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.firebase", name = "enabled", havingValue = "true")
public class AuthAdminFcmController {
    private final AuthFcmService authFcmService;

    @GetMapping("/users/{uuid}/fcm/tokens")
    @Operation(summary = "관리자용 사용자 FCM 토큰 조회")
    public ResponseEntity<BasicResponse<List<FcmDeviceTokenDto>>> userFcmTokens(@PathVariable String uuid, HttpServletRequest request) {
        return BasicResponse.ok(authFcmService.getUserTokens((AuthEntity) request.getAttribute("user"), uuid));
    }

    @PostMapping("/users/{uuid}/fcm/push")
    @Operation(summary = "관리자용 사용자 FCM 푸시 발송")
    public ResponseEntity<BasicResponse<List<String>>> pushToUser(@PathVariable String uuid,
                                                                  @RequestBody @Valid FcmPushRequestDto dto,
                                                                  HttpServletRequest request) throws Exception {
        return BasicResponse.ok(authFcmService.pushToUser((AuthEntity) request.getAttribute("user"), uuid, dto, request));
    }
}
