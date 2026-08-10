package com.threlease.base.functions.auth;

import com.threlease.base.common.exception.BusinessException;
import com.threlease.base.common.exception.ErrorCode;
import com.threlease.base.common.properties.app.redis.RedisProperties;
import com.threlease.base.common.properties.app.token.TokenProperties;
import com.threlease.base.common.provider.JwtProvider;
import com.threlease.base.common.provider.JwtProvider.RefreshTokenClaims;
import com.threlease.base.common.utils.DeviceUtils;
import com.threlease.base.common.utils.crypto.HashComponent;
import com.threlease.base.entities.AuthEntity;
import com.threlease.base.entities.RefreshTokenEntity;
import com.threlease.base.functions.auth.dto.RefreshTokenSessionDto;
import com.threlease.base.functions.auth.dto.TokenResponseDto;
import com.threlease.base.repositories.auth.RefreshTokenRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class AuthSessionService {
    private static final String REDIS_TOKEN_KEY_PREFIX = "refresh_token:";
    private static final String REDIS_FAMILY_KEY_PREFIX = "refresh_token_family:";
    private static final String REDIS_USER_KEY_PREFIX = "refresh_token_user:";

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProvider jwtProvider;
    private final ObjectProvider<StringRedisTemplate> stringRedisTemplateProvider;
    private final HashComponent hashComponent;
    private final TokenProperties tokenProperties;
    private final AuthUserService authUserService;
    private final boolean redisEnabled;

    public AuthSessionService(RefreshTokenRepository refreshTokenRepository,
                              JwtProvider jwtProvider,
                              ObjectProvider<StringRedisTemplate> stringRedisTemplateProvider,
                              HashComponent hashComponent,
                              RedisProperties redisProperties,
                              TokenProperties tokenProperties,
                              AuthUserService authUserService) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtProvider = jwtProvider;
        this.stringRedisTemplateProvider = stringRedisTemplateProvider;
        this.hashComponent = hashComponent;
        this.tokenProperties = tokenProperties;
        this.authUserService = authUserService;
        this.redisEnabled = Boolean.TRUE.equals(redisProperties.getEnabled());

        if (!isRdbStorage() && !redisEnabled) {
            throw new IllegalStateException("Refresh token storage requires app.redis.enabled=true when app.token.storage is not 'rdb'");
        }
    }

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
        String accessToken = jwtProvider.createAccessToken(user.getUuid(), familyId);
        String refreshToken = jwtProvider.createRefreshToken(user.getUuid(), refreshTokenId, familyId);

        saveRefreshToken(user.getUuid(), refreshTokenId, familyId, refreshToken, userAgent, ipAddress);

        return TokenResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

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
            revokeRefreshTokenFamily(claims.familyId());
            log.warn("Refresh token reuse detected for user: {}. Revoking token family: {}", claims.userUuid(), claims.familyId());
            throw new BusinessException(ErrorCode.TOKEN_INVALID);
        }

        AuthEntity user = authUserService.findOneByUUID(claims.userUuid())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        authUserService.assertTokenUsable(user);

        rotateRefreshToken(storedToken);
        return issueTokens(user, claims.familyId(), userAgent, ipAddress);
    }

    public void logout(String refreshToken, String userUuid, String accessToken) {
        RefreshTokenClaims claims = jwtProvider.getRefreshTokenClaims(refreshToken);
        if (claims == null || !claims.userUuid().equals(userUuid)) {
            throw new BusinessException(ErrorCode.TOKEN_INVALID);
        }

        JwtProvider.AccessTokenClaims accessClaims = jwtProvider.getAccessTokenClaims(accessToken);
        if (accessClaims == null || accessClaims.familyId() == null || accessClaims.familyId().isBlank()) {
            AuthEntity user = authUserService.findOneByUUID(userUuid)
                    .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
            authUserService.invalidateAccessTokens(user);
        }

        revokeRefreshTokenFamily(claims.familyId());
    }

    public void logoutAll(String userUuid) {
        AuthEntity user = authUserService.findOneByUUID(userUuid)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        authUserService.invalidateAccessTokens(user);
        if (isRdbStorage()) {
            refreshTokenRepository.findAllByUserAndRevokedFalse(user)
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
            RefreshTokenEntity entity = refreshTokenRepository.findByTokenIdAndUser(tokenId, authRef(userUuid))
                    .orElseThrow(() -> new BusinessException(ErrorCode.TOKEN_INVALID));
            revokeRefreshTokenFamily(entity.getFamilyId());
            return;
        }

        RefreshTokenRecord record = getStoredRefreshToken(tokenId);
        if (record == null || !userUuid.equals(record.userUuid())) {
            throw new BusinessException(ErrorCode.TOKEN_INVALID);
        }
        revokeRefreshTokenFamily(record.familyId());
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

    public Optional<AuthEntity> findOneByToken(String token) {
        JwtProvider.AccessTokenClaims claims = jwtProvider.getAccessTokenClaims(token);
        if (claims == null) {
            return Optional.empty();
        }

        Optional<AuthEntity> user = authUserService.findOneByUUID(claims.userUuid());
        if (user.isEmpty()) {
            return Optional.empty();
        }

        authUserService.assertTokenUsable(user.get());

        if (claims.familyId() == null || claims.familyId().isBlank()) {
            if (claims.issuedAtMillis() <= 0) {
                return Optional.empty();
            }
            LocalDateTime invalidBefore = user.get().getAccessTokenInvalidBefore();
            if (invalidBefore != null) {
                long invalidBeforeMillis = invalidBefore.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
                if (claims.issuedAtMillis() <= invalidBeforeMillis) {
                    return Optional.empty();
                }
            }
            return user;
        }

        return isAccessTokenFamilyActive(claims.familyId()) ? user : Optional.empty();
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
                    .user(authRef(uuid))
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

    private boolean isAccessTokenFamilyActive(String familyId) {
        if (familyId == null || familyId.isBlank()) {
            return false;
        }

        if (isRdbStorage()) {
            return refreshTokenRepository.findAllByFamilyId(familyId).stream()
                    .anyMatch(entity -> !entity.isRevoked() && !entity.isExpired());
        }

        StringRedisTemplate redisTemplate = getRedisTemplate();
        Set<String> tokenIds = Optional.ofNullable(redisTemplate.opsForSet().members(buildRedisFamilyKey(familyId)))
                .orElse(Set.of());
        return tokenIds.stream()
                .map(this::getStoredRefreshToken)
                .anyMatch(record -> record != null && !record.revoked() && !record.isExpired());
    }

    private RefreshTokenRecord toRefreshTokenRecord(RefreshTokenEntity entity) {
        return new RefreshTokenRecord(
                entity.getTokenId(),
                entity.getFamilyId(),
                entity.getUser().getUuid(),
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
            return refreshTokenRepository.findAllByUserAndRevokedFalse(authRef(userUuid)).stream()
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
            return refreshTokenRepository.findAllByUserAndRevokedFalse(authRef(userUuid)).stream()
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

    private AuthEntity authRef(String uuid) {
        return AuthEntity.builder().uuid(uuid).build();
    }

    private String hashRefreshToken(String refreshToken) {
        return hashComponent.generateSHA256(refreshToken);
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
