package com.threlease.base.functions.auth.v1;

import com.threlease.base.common.HttpConstants;
import com.threlease.base.common.annotation.ApiVersion;
import com.threlease.base.common.utils.responses.BasicResponse;
import com.threlease.base.entities.AuthEntity;
import com.threlease.base.functions.auth.AuthFlowService;
import com.threlease.base.functions.auth.dto.AuthProfileDto;
import com.threlease.base.functions.auth.dto.ChangePasswordDto;
import com.threlease.base.functions.auth.dto.RefreshTokenSessionDto;
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
@RequestMapping("/auth")
@Tag(name = "Auth Account API (v1)")
@RequiredArgsConstructor
public class AuthAccountController {
    private final AuthFlowService authFlowService;

    @PostMapping("/logout")
    @Operation(summary = "로그아웃")
    public ResponseEntity<BasicResponse<Void>> logout(@RequestHeader(HttpConstants.HEADER_REFRESH_TOKEN) String refreshToken,
                                                      HttpServletRequest request) {
        authFlowService.logout((AuthEntity) request.getAttribute("user"), refreshToken, request);
        return BasicResponse.noContent();
    }

    @PostMapping("/logout-all")
    @Operation(summary = "전체 로그아웃")
    public ResponseEntity<BasicResponse<Void>> logoutAll(HttpServletRequest request) {
        authFlowService.logoutAll((AuthEntity) request.getAttribute("user"), request);
        return BasicResponse.noContent();
    }

    @GetMapping("/sessions")
    @Operation(summary = "활성 세션 목록 조회")
    public ResponseEntity<BasicResponse<List<RefreshTokenSessionDto>>> sessions(
            HttpServletRequest request,
            @RequestHeader(value = HttpConstants.HEADER_REFRESH_TOKEN, required = false) String refreshToken) {
        return BasicResponse.ok(authFlowService.getSessions((AuthEntity) request.getAttribute("user"), refreshToken));
    }

    @DeleteMapping("/sessions/{tokenId}")
    @Operation(summary = "특정 세션 종료")
    public ResponseEntity<BasicResponse<Void>> revokeSession(@PathVariable String tokenId, HttpServletRequest request) {
        authFlowService.revokeSession((AuthEntity) request.getAttribute("user"), tokenId, request);
        return BasicResponse.noContent();
    }

    @PostMapping("/password/change")
    @Operation(summary = "비밀번호 변경")
    public ResponseEntity<BasicResponse<Void>> changePassword(@RequestBody @Valid ChangePasswordDto dto,
                                                              HttpServletRequest request) {
        authFlowService.changePassword((AuthEntity) request.getAttribute("user"), dto, request);
        return BasicResponse.noContent();
    }

    @GetMapping("/@me")
    @Operation(summary = "내 정보 조회")
    public ResponseEntity<BasicResponse<AuthProfileDto>> me(@RequestHeader(HttpConstants.HEADER_AUTHORIZATION) String token) {
        return BasicResponse.ok(authFlowService.getMyProfile(token));
    }
}
