package com.threlease.base.functions.auth;

import com.threlease.base.common.exception.BusinessException;
import com.threlease.base.common.exception.ErrorCode;
import com.threlease.base.common.properties.app.auth.AuthSecurityProperties;
import com.threlease.base.common.properties.app.redis.RedisProperties;
import com.threlease.base.common.properties.app.token.TokenProperties;
import com.threlease.base.common.provider.JwtProvider;
import com.threlease.base.common.provider.JwtProvider.RefreshTokenClaims;
import com.threlease.base.common.utils.DeviceUtils;
import com.threlease.base.common.utils.crypto.HashComponent;
import com.threlease.base.common.utils.random.RandomComponent;
import com.threlease.base.entities.AuthEntity;
import com.threlease.base.entities.RefreshTokenEntity;
import com.threlease.base.functions.auth.dto.AdminUserSummaryDto;
import com.threlease.base.functions.auth.dto.AuthProfileDto;
import com.threlease.base.functions.auth.dto.RefreshTokenSessionDto;
import com.threlease.base.functions.auth.dto.TokenResponseDto;
import com.threlease.base.repositories.auth.AuthRepository;
import com.threlease.base.repositories.auth.RefreshTokenRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class AuthService {
    private static final String REDIS_TOKEN_KEY_PREFIX = "refresh_token:";
    private static final String REDIS_FAMILY_KEY_PREFIX = "refresh_token_family:";
    private static final String REDIS_USER_KEY_PREFIX = "refresh_token_user:";

    private final AuthRepository authRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProvider jwtProvider;
    private final ObjectProvider<StringRedisTemplate> stringRedisTemplateProvider;
    private final HashComponent hashComponent;
    private final RandomComponent randomComponent;
    private final TokenProperties tokenProperties;
    private final AuthSecurityProperties authSecurityProperties;
    private final boolean redisEnabled;

    public AuthService(AuthRepository authRepository, 
                       RefreshTokenRepository refreshTokenRepository,
                       JwtProvider jwtProvider, 
                       ObjectProvider<StringRedisTemplate> stringRedisTemplateProvider,
                       HashComponent hashComponent,
                       RandomComponent randomComponent,
                       RedisProperties redisProperties,
                       TokenProperties tokenProperties,
                       AuthSecurityProperties authSecurityProperties) {
        this.authRepository = authRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtProvider = jwtProvider;
        this.stringRedisTemplateProvider = stringRedisTemplateProvider;
        this.hashComponent = hashComponent;
        this.randomComponent = randomComponent;
        this.tokenProperties = tokenProperties;
        this.authSecurityProperties = authSecurityProperties;
        this.redisEnabled = Boolean.TRUE.equals(redisProperties.getEnabled());

        if (!isRdbStorage() && !redisEnabled) {
            throw new IllegalStateException("Refresh token storage requires app.redis.enabled=true when app.token.storage is not 'rdb'");
        }
    }

    public Optional<AuthEntity> findOneByUUID(String uuid) {
        return Optional.ofNullable(findCachedUserByUUID(uuid));
    }

    public Optional<AuthEntity> findOneByUsername(String username) {
        return Optional.ofNullable(findCachedUserByUsername(username));
    }

    public Optional<AuthEntity> findOneByEmail(String email) {
        return authRepository.findOneByEmail(email);
    }

    public Optional<AuthEntity> findOneByIdentifier(String identifier) {
        return authRepository.findOneByUsernameOrEmail(identifier);
    }

    @Caching(evict = {
            @CacheEvict(value = "user", key = "#auth.uuid"),
            @CacheEvict(value = "user", key = "#auth.username")
    })
    public void authSave(AuthEntity auth) {
        authRepository.save(auth);
    }

    public void ensureLoginAllowed(AuthEntity auth) {
        if (auth.getLockedUntil() != null && auth.getLockedUntil().isAfter(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.ACCOUNT_LOCKED);
        }
    }

    public void recordFailedLogin(AuthEntity auth) {
        if (auth == null || !authSecurityProperties.getLoginFailure().isEnabled()) {
            return;
        }
        auth.setFailedLoginCount(auth.getFailedLoginCount() + 1);
        if (auth.getFailedLoginCount() >= authSecurityProperties.getLoginFailure().getMaxAttempts()) {
            auth.setLockedUntil(LocalDateTime.now().plusMinutes(authSecurityProperties.getLoginFailure().getLockMinutes()));
        }
        authSave(auth);
    }

    public void recordSuccessfulLogin(AuthEntity auth, String clientIp) {
        auth.setFailedLoginCount(0);
        auth.setLockedUntil(null);
        auth.setLastLoginAt(LocalDateTime.now());
        auth.setLastLoginIp(clientIp);
        authSave(auth);
    }

    public void changePassword(AuthEntity auth, String encodedPassword) {
        auth.setPassword(encodedPassword);
        auth.setPasswordResetCodeHash(null);
        auth.setPasswordResetCodeExpiry(null);
        authSave(auth);
    }

    @Cacheable(value = "user", key = "#uuid", unless = "#result == null")
    public AuthEntity findCachedUserByUUID(String uuid) {
        return authRepository.findOneByUUID(uuid).orElse(null);
    }

    @Cacheable(value = "user", key = "#username", unless = "#result == null")
    public AuthEntity findCachedUserByUsername(String username) {
        return authRepository.findOneByUsername(username).orElse(null);
    }

    /**
     * 로그인 - 토큰 세트 발급
     */
    public TokenResponseDto issueTokens(AuthEntity user) {
        return issueTokens(user, UUID.randomUUID().toString(), null, null);
    }

    public TokenResponseDto issueTokens(AuthEntity user, String familyId) {
        return issueTokens(user, familyId, null, null);
    }

    public TokenResponseDto issueTokens(AuthEntity user, String userAgent, String ipAddress) {
        return issueTokens(user, UUID.randomUUID().toString(), userAgent, ipAddress);
    }

    public TokenResponseDto issueTokens(AuthEntity user, String familyId, String userAgent, String ipAddress) {
        enforceSessionLimit(user.getUuid());
        String refreshTokenId = UUID.randomUUID().toString();
        String accessToken = jwtProvider.createAccessToken(user.getUuid());
        String refreshToken = jwtProvider.createRefreshToken(user.getUuid(), refreshTokenId, familyId);

        saveRefreshToken(user.getUuid(), refreshTokenId, familyId, refreshToken, userAgent, ipAddress);

        return TokenResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    /**
     * 토큰 갱신 (Token Rotation 적용)
     */
    public TokenResponseDto refresh(String refreshToken, String userAgent, String ipAddress) {
        RefreshTokenClaims claims = jwtProvider.getRefreshTokenClaims(refreshToken);
        if (claims == null) {
            throw new BusinessException(ErrorCode.TOKEN_INVALID);
        }

        RefreshTokenRecord storedToken = getStoredRefreshToken(claims.tokenId());
        if (storedToken == null || storedToken.isExpired() || storedToken.revoked()
                || !storedToken.userUuid().equals(claims.userUuid())
                || !storedToken.familyId().equals(claims.familyId())
                || !storedToken.tokenHash().equals(hashRefreshToken(refreshToken))) {
            // 토큰 탈취 가능성 (이전 토큰 재사용 시도) -> 저장된 토큰 삭제 후 에러
            revokeRefreshTokenFamily(claims.familyId());
            log.warn("Refresh token reuse detected for user: {}. Revoking token family: {}", claims.userUuid(), claims.familyId());
            throw new BusinessException(ErrorCode.TOKEN_INVALID);
        }

        AuthEntity user = findOneByUUID(claims.userUuid())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        rotateRefreshToken(storedToken);
        return issueTokens(user, claims.familyId(), userAgent, ipAddress);
    }

    public void logout(String refreshToken, String userUuid) {
        RefreshTokenClaims claims = jwtProvider.getRefreshTokenClaims(refreshToken);
        if (claims == null || !claims.userUuid().equals(userUuid)) {
            throw new BusinessException(ErrorCode.TOKEN_INVALID);
        }

        deleteRefreshToken(claims.tokenId(), claims.familyId());
    }

    public void logoutAll(String userUuid) {
        if (isRdbStorage()) {
            refreshTokenRepository.findAllByUserUuidAndRevokedFalse(userUuid)
                    .forEach(entity -> revokeRefreshToken(entity.getTokenId(), entity.getFamilyId(), "LOGOUT_ALL"));
            return;
        }

        getUserTokenIds(userUuid).stream()
                .map(this::getStoredRefreshToken)
                .filter(java.util.Objects::nonNull)
                .forEach(record -> revokeRefreshToken(record.tokenId(), record.familyId(), "LOGOUT_ALL"));
    }

    public void revokeSession(String userUuid, String tokenId) {
        if (isRdbStorage()) {
            RefreshTokenEntity entity = refreshTokenRepository.findByTokenIdAndUserUuid(tokenId, userUuid)
                    .orElseThrow(() -> new BusinessException(ErrorCode.TOKEN_INVALID));
            revokeRefreshToken(entity.getTokenId(), entity.getFamilyId(), "REVOKED");
            return;
        }

        RefreshTokenRecord record = getStoredRefreshToken(tokenId);
        if (record == null || !userUuid.equals(record.userUuid())) {
            throw new BusinessException(ErrorCode.TOKEN_INVALID);
        }
        revokeRefreshToken(record.tokenId(), record.familyId(), "REVOKED");
    }

    public List<RefreshTokenSessionDto> getSessions(String userUuid, String currentRefreshToken) {
        String currentTokenId = Optional.ofNullable(jwtProvider.getRefreshTokenClaims(currentRefreshToken))
                .map(RefreshTokenClaims::tokenId)
                .orElse(null);

        return getActiveRefreshTokenRecords(userUuid).stream()
                .sorted(Comparator.comparing(RefreshTokenRecord::expiryDate).reversed())
                .map(record -> RefreshTokenSessionDto.builder()
                        .tokenId(record.tokenId())
                        .familyId(record.familyId())
                        .issuedAt(record.issuedAt())
                        .lastUsedAt(record.lastUsedAt())
                        .expiryDate(record.expiryDate())
                        .userAgent(record.userAgent())
                        .deviceLabel(record.deviceLabel())
                        .ipAddress(record.ipAddress())
                        .current(record.tokenId().equals(currentTokenId))
                        .build())
                .toList();
    }

    public List<RefreshTokenSessionDto> getSessionsForUser(String userUuid) {
        return getSessions(userUuid, null);
    }

    public PageResult<AdminUserSummaryDto> getUsers(String query, int page, int size) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(Math.max(page, 0), Math.max(size, 1));
        org.springframework.data.domain.Page<AuthEntity> pageResult =
                (query == null || query.isBlank())
                        ? authRepository.findByPagination(pageable)
                        : authRepository.findByUsernameContainingIgnoreCaseOrNicknameContainingIgnoreCase(query, query, pageable);

        return new PageResult<>(
                pageResult.getContent().stream().map(this::toAdminUserSummary).toList(),
                pageResult.getNumber(),
                pageResult.getSize(),
                pageResult.getTotalElements(),
                pageResult.getTotalPages()
        );
    }

    public Optional<AuthEntity> findManagedUserByUuid(String uuid) {
        return authRepository.findOneByUUID(uuid);
    }

    public void forceLockUser(AuthEntity auth, long minutes) {
        auth.setLockedUntil(LocalDateTime.now().plusMinutes(Math.max(minutes, 1)));
        authSave(auth);
    }

    public String createPasswordResetCode(AuthEntity auth) {
        String code = randomComponent.generateOtp(6);
        auth.setPasswordResetCodeHash(hashComponent.generateSHA256(code));
        auth.setPasswordResetCodeExpiry(LocalDateTime.now().plusMinutes(Math.max(1, authSecurityProperties.getPasswordReset().getCodeExpireMinutes())));
        authSave(auth);
        return code;
    }

    public void validatePasswordResetCode(AuthEntity auth, String verificationCode) {
        if (verificationCode == null || verificationCode.isBlank()) {
            throw new BusinessException(ErrorCode.PASSWORD_RESET_CODE_INVALID);
        }
        if (auth.getPasswordResetCodeHash() == null || auth.getPasswordResetCodeExpiry() == null) {
            throw new BusinessException(ErrorCode.PASSWORD_RESET_CODE_INVALID);
        }
        if (auth.getPasswordResetCodeExpiry().isBefore(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.PASSWORD_RESET_CODE_EXPIRED);
        }
        String providedHash = hashComponent.generateSHA256(verificationCode);
        if (!providedHash.equals(auth.getPasswordResetCodeHash())) {
            throw new BusinessException(ErrorCode.PASSWORD_RESET_CODE_INVALID);
        }
    }

    public void clearPasswordResetCode(AuthEntity auth) {
        auth.setPasswordResetCodeHash(null);
        auth.setPasswordResetCodeExpiry(null);
        authSave(auth);
    }

    public int getPasswordResetExpireMinutes() {
        return Math.max(1, authSecurityProperties.getPasswordReset().getCodeExpireMinutes());
    }

    private void saveRefreshToken(String uuid, String tokenId, String familyId, String refreshToken, String userAgent, String ipAddress) {
        String hashedToken = hashRefreshToken(refreshToken);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiryDate = now.plusSeconds(jwtProvider.getRefreshTokenExpSeconds());
        String normalizedUserAgent = trim(userAgent, 512);
        String deviceLabel = trim(DeviceUtils.describe(userAgent), 128);
        String normalizedIp = trim(ipAddress, 64);

        if (isRdbStorage()) {
            refreshTokenRepository.save(RefreshTokenEntity.builder()
                    .userUuid(uuid)
                    .tokenId(tokenId)
                    .familyId(familyId)
                    .tokenHash(hashedToken)
                    .token(hashedToken)
                    .userAgent(normalizedUserAgent)
                    .deviceLabel(deviceLabel)
                    .ipAddress(normalizedIp)
                    .lastUsedAt(now)
                    .expiryDate(expiryDate)
                    .revoked(false)
                    .build());
            return;
        }

        StringRedisTemplate redisTemplate = getRedisTemplate();
        redisTemplate.opsForValue().set(buildRedisTokenKey(tokenId),
                serializeRefreshTokenRecord(new RefreshTokenRecord(tokenId, familyId, uuid, hashedToken, now, now, expiryDate, normalizedUserAgent, deviceLabel, normalizedIp, false)),
                jwtProvider.getRefreshTokenExpSeconds(),
                TimeUnit.SECONDS);
        redisTemplate.opsForSet().add(buildRedisFamilyKey(familyId), tokenId);
        redisTemplate.opsForSet().add(buildRedisUserKey(uuid), tokenId);
        redisTemplate.expire(buildRedisFamilyKey(familyId), jwtProvider.getRefreshTokenExpSeconds(), TimeUnit.SECONDS);
        redisTemplate.expire(buildRedisUserKey(uuid), jwtProvider.getRefreshTokenExpSeconds(), TimeUnit.SECONDS);
    }

    private RefreshTokenRecord getStoredRefreshToken(String tokenId) {
        if (isRdbStorage()) {
            return refreshTokenRepository.findByTokenId(tokenId)
                    .map(this::toRefreshTokenRecord)
                    .orElse(null);
        }

        StringRedisTemplate redisTemplate = getRedisTemplate();
        String storedValue = redisTemplate.opsForValue().get(buildRedisTokenKey(tokenId));
        return storedValue == null ? null : deserializeRefreshTokenRecord(storedValue);
    }

    private void deleteRefreshToken(String tokenId, String familyId) {
        if (isRdbStorage()) {
            refreshTokenRepository.deleteByTokenId(tokenId);
            return;
        }

        revokeRefreshToken(tokenId, familyId, "DELETED");
    }

    public Optional<AuthEntity> findOneByToken(String token) {
        return jwtProvider.findOneByToken(token);
    }

    private String hashRefreshToken(String refreshToken) {
        return hashComponent.generateSHA256(refreshToken);
    }

    private boolean isRdbStorage() {
        return "rdb".equalsIgnoreCase(tokenProperties.getStorage());
    }

    private StringRedisTemplate getRedisTemplate() {
        StringRedisTemplate redisTemplate = stringRedisTemplateProvider.getIfAvailable();
        if (redisTemplate == null) {
            throw new IllegalStateException("StringRedisTemplate is required when app.token.storage is not 'rdb'");
        }
        return redisTemplate;
    }

    private void rotateRefreshToken(RefreshTokenRecord currentToken) {
        revokeRefreshToken(currentToken.tokenId(), currentToken.familyId(), "ROTATED");
    }

    private void revokeRefreshTokenFamily(String familyId) {
        if (isRdbStorage()) {
            refreshTokenRepository.findAllByFamilyId(familyId).forEach(entity -> {
                entity.setRevoked(true);
                refreshTokenRepository.save(entity);
            });
            return;
        }

        StringRedisTemplate redisTemplate = getRedisTemplate();
        String familyKey = buildRedisFamilyKey(familyId);
        Optional.ofNullable(redisTemplate.opsForSet().members(familyKey))
                .ifPresent(tokenIds -> tokenIds.forEach(tokenId -> {
                    RefreshTokenRecord record = getStoredRefreshToken(tokenId);
                    redisTemplate.delete(buildRedisTokenKey(tokenId));
                    if (record != null) {
                        redisTemplate.opsForSet().remove(buildRedisUserKey(record.userUuid()), tokenId);
                    }
                }));
        redisTemplate.delete(familyKey);
    }

    private RefreshTokenRecord toRefreshTokenRecord(RefreshTokenEntity entity) {
        return new RefreshTokenRecord(
                entity.getTokenId(),
                entity.getFamilyId(),
                entity.getUserUuid(),
                entity.getTokenHash(),
                entity.getCreatedAt(),
                entity.getLastUsedAt(),
                entity.getExpiryDate(),
                entity.getUserAgent(),
                entity.getDeviceLabel(),
                entity.getIpAddress(),
                entity.isRevoked()
        );
    }

    private String buildRedisTokenKey(String tokenId) {
        return REDIS_TOKEN_KEY_PREFIX + tokenId;
    }

    private String buildRedisFamilyKey(String familyId) {
        return REDIS_FAMILY_KEY_PREFIX + familyId;
    }

    private String buildRedisUserKey(String userUuid) {
        return REDIS_USER_KEY_PREFIX + userUuid;
    }

    private String serializeRefreshTokenRecord(RefreshTokenRecord record) {
        return String.join("|",
                record.tokenId(),
                record.familyId(),
                record.userUuid(),
                record.tokenHash(),
                String.valueOf(toEpochSecond(record.issuedAt())),
                String.valueOf(toEpochSecond(record.lastUsedAt())),
                String.valueOf(record.expiryDate().toEpochSecond(ZoneOffset.UTC)),
                nullSafe(record.userAgent()),
                nullSafe(record.deviceLabel()),
                nullSafe(record.ipAddress()),
                String.valueOf(record.revoked()));
    }

    private RefreshTokenRecord deserializeRefreshTokenRecord(String value) {
        String[] parts = value.split("\\|", -1);
        return new RefreshTokenRecord(
                parts[0],
                parts[1],
                parts[2],
                parts[3],
                fromEpochSecond(parts[4]),
                fromEpochSecond(parts[5]),
                LocalDateTime.ofEpochSecond(Long.parseLong(parts[6]), 0, ZoneOffset.UTC),
                emptyToNull(parts[7]),
                emptyToNull(parts[8]),
                emptyToNull(parts[9]),
                Boolean.parseBoolean(parts[10])
        );
    }

    private record RefreshTokenRecord(
            String tokenId,
            String familyId,
            String userUuid,
            String tokenHash,
            LocalDateTime issuedAt,
            LocalDateTime lastUsedAt,
            LocalDateTime expiryDate,
            String userAgent,
            String deviceLabel,
            String ipAddress,
            boolean revoked
    ) {
        private boolean isExpired() {
            return expiryDate.isBefore(LocalDateTime.now());
        }
    }

    private void revokeRefreshToken(String tokenId, String familyId, String replacementState) {
        if (isRdbStorage()) {
            refreshTokenRepository.findByTokenId(tokenId).ifPresent(entity -> {
                entity.setRevoked(true);
                entity.setReplacedByTokenId(replacementState);
                refreshTokenRepository.save(entity);
            });
            return;
        }

        RefreshTokenRecord record = getStoredRefreshToken(tokenId);
        if (record == null) {
            return;
        }

        StringRedisTemplate redisTemplate = getRedisTemplate();
        redisTemplate.delete(buildRedisTokenKey(tokenId));
        redisTemplate.opsForSet().remove(buildRedisFamilyKey(familyId), tokenId);
        redisTemplate.opsForSet().remove(buildRedisUserKey(record.userUuid()), tokenId);
    }

    private List<RefreshTokenRecord> getActiveRefreshTokenRecords(String userUuid) {
        if (isRdbStorage()) {
            return refreshTokenRepository.findAllByUserUuidAndRevokedFalse(userUuid).stream()
                    .map(this::toRefreshTokenRecord)
                    .filter(record -> !record.isExpired())
                    .toList();
        }

        return getUserTokenIds(userUuid).stream()
                .map(this::getStoredRefreshToken)
                .filter(record -> record != null && !record.isExpired() && !record.revoked())
                .toList();
    }

    private Set<String> getUserTokenIds(String userUuid) {
        if (isRdbStorage()) {
            return refreshTokenRepository.findAllByUserUuidAndRevokedFalse(userUuid).stream()
                    .map(RefreshTokenEntity::getTokenId)
                    .collect(java.util.stream.Collectors.toCollection(TreeSet::new));
        }

        StringRedisTemplate redisTemplate = getRedisTemplate();
        Set<String> members = redisTemplate.opsForSet().members(buildRedisUserKey(userUuid));
        return members == null ? Set.of() : members;
    }

    private void enforceSessionLimit(String userUuid) {
        int maxSessions = Math.max(tokenProperties.getMaxSessionsPerUser(), 1);
        List<RefreshTokenRecord> activeSessions = getActiveRefreshTokenRecords(userUuid).stream()
                .sorted(Comparator.comparing(RefreshTokenRecord::expiryDate))
                .toList();

        int overflow = activeSessions.size() - maxSessions + 1;
        if (overflow <= 0) {
            return;
        }

        for (int i = 0; i < overflow; i++) {
            RefreshTokenRecord record = activeSessions.get(i);
            revokeRefreshToken(record.tokenId(), record.familyId(), "SESSION_LIMIT");
        }
    }

    private AdminUserSummaryDto toAdminUserSummary(AuthEntity auth) {
        return AdminUserSummaryDto.builder()
                .uuid(auth.getUuid())
                .username(auth.getUsername())
                .nickname(auth.getNickname())
                .role(auth.getRole())
                .failedLoginCount(auth.getFailedLoginCount())
                .lockedUntil(auth.getLockedUntil())
                .lastLoginAt(auth.getLastLoginAt())
                .lastLoginIp(auth.getLastLoginIp())
                .mfaEnabled(auth.isMfaEnabled())
                .build();
    }

    public AuthProfileDto toAuthProfile(AuthEntity auth) {
        return AuthProfileDto.builder()
                .uuid(auth.getUuid())
                .username(auth.getUsername())
                .nickname(auth.getNickname())
                .email(auth.getEmail())
                .role(auth.getRole())
                .mfaEnabled(auth.isMfaEnabled())
                .build();
    }

    public record PageResult<T>(List<T> content, int page, int size, long totalElements, int totalPages) {
    }

    private long toEpochSecond(LocalDateTime dateTime) {
        return (dateTime == null ? LocalDateTime.now() : dateTime).toEpochSecond(ZoneOffset.UTC);
    }

    private LocalDateTime fromEpochSecond(String epochSecond) {
        if (epochSecond == null || epochSecond.isBlank()) {
            return null;
        }
        return LocalDateTime.ofEpochSecond(Long.parseLong(epochSecond), 0, ZoneOffset.UTC);
    }

    private String nullSafe(String value) {
        return value == null ? "" : value;
    }

    private String emptyToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private String trim(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        return value.substring(0, Math.min(value.length(), maxLength));
    }
}
