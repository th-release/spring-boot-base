package com.threlease.base.functions.auth.v1;

import com.threlease.base.common.annotation.ApiVersion;
import com.threlease.base.common.utils.IpUtils;
import com.threlease.base.common.utils.responses.BasicResponse;
import com.threlease.base.entities.AuthEntity;
import com.threlease.base.functions.auth.AuthFcmService;
import com.threlease.base.functions.auth.dto.FcmDeviceTokenDto;
import com.threlease.base.functions.auth.dto.FcmDeviceTokenRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@ApiVersion(1)
@RequestMapping("/auth/fcm")
@Tag(name = "Auth FCM API (v1)")
@RequiredArgsConstructor
public class AuthFcmController {
    private final AuthFcmService authFcmService;

    @GetMapping("/tokens")
    @Operation(summary = "내 FCM 디바이스 토큰 목록")
    public ResponseEntity<BasicResponse<List<FcmDeviceTokenDto>>> myFcmTokens(HttpServletRequest request) {
        return BasicResponse.ok(authFcmService.getMyTokens((AuthEntity) request.getAttribute("user")));
    }

    @PostMapping("/tokens")
    @Operation(summary = "내 FCM 디바이스 토큰 등록")
    public ResponseEntity<BasicResponse<FcmDeviceTokenDto>> registerFcmToken(@RequestBody @Valid FcmDeviceTokenRequestDto dto,
                                                                             HttpServletRequest request) {
        return BasicResponse.created(authFcmService.registerToken(
                (AuthEntity) request.getAttribute("user"),
                dto,
                request.getHeader("User-Agent"),
                IpUtils.getClientIp(request)
        ));
    }

    @DeleteMapping("/tokens/{id}")
    @Operation(summary = "내 FCM 디바이스 토큰 비활성화")
    public ResponseEntity<BasicResponse<Void>> deleteMyFcmToken(@PathVariable Long id, HttpServletRequest request) {
        authFcmService.disableMyToken((AuthEntity) request.getAttribute("user"), id);
        return BasicResponse.noContent();
    }
}
