package com.threlease.base.functions.auth;

import com.threlease.base.common.enums.AuthVerificationType;
import com.threlease.base.common.enums.AuthStatuses;
import com.threlease.base.common.enums.AuthTypes;
import com.threlease.base.common.exception.BusinessException;
import com.threlease.base.common.exception.ErrorCode;
import com.threlease.base.common.properties.app.email.EmailProperties;
import com.threlease.base.common.utils.email.EmailService;
import com.threlease.base.entities.AuthEntity;
import com.threlease.base.functions.auth.dto.AuthProfileDto;
import com.threlease.base.functions.auth.dto.ChangePasswordDto;
import com.threlease.base.functions.auth.dto.LoginDto;
import com.threlease.base.functions.auth.dto.MfaRegisterDto;
import com.threlease.base.functions.auth.dto.MfaSetupResponseDto;
import com.threlease.base.functions.auth.dto.PasswordResetConfirmDto;
import com.threlease.base.functions.auth.dto.PasswordResetRequestDto;
import com.threlease.base.functions.auth.dto.RefreshTokenSessionDto;
import com.threlease.base.functions.auth.dto.SignUpDto;
import com.threlease.base.functions.auth.dto.TokenResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthFlowService {
    private final AuthService authService;
    private final AuditLogService auditLogService;
    private final MfaService mfaService;
    private final EmailService emailService;
    private final EmailProperties emailProperties;
    private final AuthVerificationService authVerificationService;
    private final AuthAccountFactory authAccountFactory;
    private final AuthPasswordService authPasswordService;

    public TokenResponseDto login(LoginDto dto, String userAgent, String clientIp, HttpServletRequest request) {
        AuthEntity auth = authService.findOneByUsername(dto.getUsername())
                .orElseThrow(() -> {
                    auditLogService.log(null, "LOGIN", "AUTH", dto.getUsername(), false, request, "User not found");
                    return new BusinessException(ErrorCode.WRONG_PASSWORD);
                });

        try {
            authService.ensureLoginAllowed(auth);
        } catch (BusinessException e) {
            auditLogService.log(auth.getUuid(), "LOGIN", "AUTH", auth.getUuid(), false, request, "Account locked");
            throw e;
        }

        if (!authPasswordService.matches(dto.getPassword(), auth)) {
            authService.recordFailedLogin(auth, clientIp, userAgent, "WRONG_PASSWORD");
            auditLogService.log(auth.getUuid(), "LOGIN", "AUTH", auth.getUuid(), false, request, "Wrong password");
            throw new BusinessException(ErrorCode.WRONG_PASSWORD);
        }

        mfaService.verifyLogin(auth, dto.getOtpCode());
        authService.recordSuccessfulLogin(auth, clientIp, userAgent);
        auditLogService.log(auth.getUuid(), "LOGIN", "AUTH", auth.getUuid(), true, request, "User login succeeded");
        TokenResponseDto response = authService.issueTokens(auth, userAgent, clientIp);
        response.setMfaEnabled(mfaService.isEnabled(auth));
        response.setMfaEnrollmentRequired(mfaService.isEnrollmentRequired(auth));
        return response;
    }

    public TokenResponseDto refresh(String refreshToken, String userAgent, String clientIp) {
        return authService.refresh(refreshToken, userAgent, clientIp);
    }

    public void logout(AuthEntity user, String refreshToken, HttpServletRequest request) {
        authService.logout(refreshToken, user.getUuid());
        auditLogService.log(user.getUuid(), "LOGOUT", "AUTH", user.getUuid(), true, request, "Current session logout");
    }

    public void logoutAll(AuthEntity user, HttpServletRequest request) {
        authService.logoutAll(user.getUuid());
        auditLogService.log(user.getUuid(), "LOGOUT_ALL", "AUTH", user.getUuid(), true, request, "All sessions revoked");
    }

    public List<RefreshTokenSessionDto> getSessions(AuthEntity user, String currentRefreshToken) {
        return authService.getSessions(user.getUuid(), currentRefreshToken);
    }

    public void revokeSession(AuthEntity user, String tokenId, HttpServletRequest request) {
        authService.revokeSession(user.getUuid(), tokenId);
        auditLogService.log(user.getUuid(), "REVOKE_SESSION", "REFRESH_TOKEN", tokenId, true, request, "Single session revoked");
    }

    public AuthProfileDto signUp(SignUpDto dto) {
        if (authService.findOneByUsername(dto.getUsername()).isPresent()) {
            throw new BusinessException(ErrorCode.USER_DUPLICATE);
        }
        if (authService.findOneByEmail(dto.getEmail()).isPresent()) {
            throw new BusinessException(ErrorCode.USER_DUPLICATE);
        }

        AuthEntity user = authAccountFactory.create(
                dto.getUsername(),
                dto.getNickname(),
                dto.getEmail(),
                dto.getPassword(),
                AuthTypes.GENERAL,
                AuthStatuses.ACTIVE
        );

        authService.authSave(user);
        auditLogService.log(user.getUuid(), "SIGNUP", "AUTH", user.getUuid(), true, null, "User signup completed");
        return authService.toAuthProfile(user);
    }

    public void changePassword(AuthEntity user, ChangePasswordDto dto, HttpServletRequest request) {
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new BusinessException(ErrorCode.PASSWORD_CHANGE_MISMATCH);
        }
        if (!authPasswordService.matches(dto.getCurrentPassword(), user)) {
            throw new BusinessException(ErrorCode.WRONG_PASSWORD);
        }

        AuthPasswordService.EncodedPassword encodedPassword = authPasswordService.encode(dto.getNewPassword());
        authService.changePassword(user, encodedPassword.passwordHash(), encodedPassword.salt());
        authService.logoutAll(user.getUuid());
        auditLogService.log(user.getUuid(), "CHANGE_PASSWORD", "AUTH", user.getUuid(), true, request, "Password changed");
    }

    public void requestPasswordReset(PasswordResetRequestDto dto, HttpServletRequest request) {
        if (!emailProperties.isEnabled()) {
            throw new BusinessException(ErrorCode.EMAIL_DISABLED);
        }

        AuthEntity user = authService.findOneByIdentifier(dto.getIdentifier())
                .orElse(null);
        if (user == null) {
            auditLogService.log(null, "REQUEST_PASSWORD_RESET", "AUTH", dto.getIdentifier(), false, request, "Password reset requested for unknown identifier");
            return;
        }
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            auditLogService.log(user.getUuid(), "REQUEST_PASSWORD_RESET", "AUTH", user.getUuid(), false, request, "Password reset requested without registered email");
            return;
        }

        String code = authVerificationService.issueCode(
                user,
                AuthVerificationType.PASSWORD_RESET,
                user.getEmail(),
                authService.getPasswordResetExpireMinutes()
        );
        emailService.sendPasswordResetCode(user.getEmail(), code, authService.getPasswordResetExpireMinutes());
        auditLogService.log(user.getUuid(), "REQUEST_PASSWORD_RESET", "AUTH", user.getUuid(), true, request, "Password reset code issued");
    }

    public void confirmPasswordReset(PasswordResetConfirmDto dto, HttpServletRequest request) {
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new BusinessException(ErrorCode.PASSWORD_CHANGE_MISMATCH);
        }

        AuthEntity user = authService.findOneByIdentifier(dto.getIdentifier())
                .orElse(null);
        if (user == null) {
            throw new BusinessException(emailProperties.isEnabled() ? ErrorCode.PASSWORD_RESET_CODE_INVALID : ErrorCode.WRONG_PASSWORD);
        }

        if (emailProperties.isEnabled()) {
            authVerificationService.verifyCode(user, AuthVerificationType.PASSWORD_RESET, dto.getVerificationCode());
            AuthPasswordService.EncodedPassword encodedPassword = authPasswordService.encode(dto.getNewPassword());
            authService.changePassword(user, encodedPassword.passwordHash(), encodedPassword.salt());
            authService.logoutAll(user.getUuid());
            authVerificationService.clear(user, AuthVerificationType.PASSWORD_RESET);
            auditLogService.log(user.getUuid(), "CONFIRM_PASSWORD_RESET", "AUTH", user.getUuid(), true, request, "Password reset by email verification");
            return;
        }

        if (dto.getCurrentPassword() == null || dto.getCurrentPassword().isBlank()) {
            throw new BusinessException(ErrorCode.WRONG_PASSWORD);
        }
        if (!authPasswordService.matches(dto.getCurrentPassword(), user)) {
            auditLogService.log(user.getUuid(), "CONFIRM_PASSWORD_RESET", "AUTH", user.getUuid(), false, request, "Wrong current password");
            throw new BusinessException(ErrorCode.WRONG_PASSWORD);
        }

        AuthPasswordService.EncodedPassword encodedPassword = authPasswordService.encode(dto.getNewPassword());
        authService.changePassword(user, encodedPassword.passwordHash(), encodedPassword.salt());
        authService.logoutAll(user.getUuid());
        auditLogService.log(user.getUuid(), "CONFIRM_PASSWORD_RESET", "AUTH", user.getUuid(), true, request, "Password reset by current password");
    }

    public MfaSetupResponseDto setupMfa(AuthEntity user) {
        return mfaService.setup(user);
    }

    public void registerMfa(AuthEntity user, MfaRegisterDto dto, HttpServletRequest request) {
        mfaService.completeEnrollment(user, dto.getOtpCode());
        auditLogService.log(user.getUuid(), "REGISTER_MFA", "AUTH", user.getUuid(), true, request, "MFA enrollment completed");
    }

    public AuthProfileDto getMyProfile(String token) {
        AuthEntity user = authService.findOneByToken(token)
                .orElseThrow(() -> new BusinessException(ErrorCode.TOKEN_INVALID));
        return authService.toAuthProfile(user);
    }
}
