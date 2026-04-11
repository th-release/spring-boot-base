package com.threlease.base.functions.auth.v1;

import com.threlease.base.common.annotation.ApiVersion;
import com.threlease.base.common.utils.ClientIpResolver;
import com.threlease.base.common.utils.responses.BasicResponse;
import com.threlease.base.entities.AuthEntity;
import com.threlease.base.functions.auth.AuthFcmService;
import com.threlease.base.functions.auth.dto.FcmDeviceTokenDto;
import com.threlease.base.functions.auth.dto.FcmDeviceTokenRequestDto;
import com.threlease.base.functions.auth.dto.FcmNotificationDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@ApiVersion(1)
@RequestMapping("/auth/fcm")
@Tag(name = "Auth FCM API (v1)")
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.firebase", name = "enabled", havingValue = "true")
public class AuthFcmController {
    private final AuthFcmService authFcmService;
    private final ClientIpResolver clientIpResolver;

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
                clientIpResolver.resolve(request)
        ));
    }

    @DeleteMapping("/tokens/{uuid}")
    @Operation(summary = "내 FCM 디바이스 토큰 비활성화")
    public ResponseEntity<BasicResponse<Void>> deleteMyFcmToken(@PathVariable String uuid, HttpServletRequest request) {
        authFcmService.disableMyToken((AuthEntity) request.getAttribute("user"), uuid);
        return BasicResponse.noContent();
    }

    @GetMapping("/notifications")
    @Operation(summary = "내 FCM 알림 목록 조회")
    public ResponseEntity<BasicResponse<List<FcmNotificationDto>>> myNotifications(@RequestParam(defaultValue = "0") int page,
                                                                                   @RequestParam(defaultValue = "20") int size,
                                                                                   HttpServletRequest request) {
        return BasicResponse.ok(authFcmService.getMyNotifications((AuthEntity) request.getAttribute("user"), page, size));
    }

    @PostMapping("/notifications/{uuid}/read")
    @Operation(summary = "내 FCM 알림 읽음 처리")
    public ResponseEntity<BasicResponse<FcmNotificationDto>> markNotificationRead(@PathVariable String uuid,
                                                                                  HttpServletRequest request) {
        return BasicResponse.ok(authFcmService.markMyNotificationRead((AuthEntity) request.getAttribute("user"), uuid));
    }
}
