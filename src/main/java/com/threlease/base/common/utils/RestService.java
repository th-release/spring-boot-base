package com.threlease.base.common.utils;

import com.threlease.base.common.exception.BusinessException;
import com.threlease.base.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

/**
 * axios와 유사한 사용성을 제공하는 HTTP 요청 유틸리티 서비스 (Spring Retry 지원)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RestService {

    private final RestTemplate restTemplate;

    /**
     * GET 요청 (Query Params 지원, 3회 재시도)
     */
    @Retryable(value = Exception.class, maxAttempts = 3, backoff = @Backoff(delay = 2000))
    public <T> T get(String url, Map<String, Object> params, Class<T> responseType) {
        String finalUrl = buildUrl(url, params);
        log.debug("RestService GET: {}", finalUrl);
        return restTemplate.getForObject(finalUrl, responseType);
    }

    /**
     * POST 요청 (Body 지원, 3회 재시도)
     */
    @Retryable(value = Exception.class, maxAttempts = 3, backoff = @Backoff(delay = 2000))
    public <T> T post(String url, Object body, Class<T> responseType) {
        log.debug("RestService POST: {}", url);
        return restTemplate.postForObject(url, body, responseType);
    }

    /**
     * PUT 요청
     */
    @Retryable(value = Exception.class, maxAttempts = 2, backoff = @Backoff(delay = 1000))
    public void put(String url, Object body) {
        restTemplate.put(url, body);
    }

    /**
     * DELETE 요청
     */
    @Retryable(value = Exception.class, maxAttempts = 2, backoff = @Backoff(delay = 1000))
    public void delete(String url) {
        restTemplate.delete(url);
    }

    /**
     * 상세 설정이 필요한 경우 (Header 등)
     */
    @Retryable(value = Exception.class, maxAttempts = 3, backoff = @Backoff(delay = 2000))
    public <T> ResponseEntity<T> exchange(String url, HttpMethod method, Object body, Map<String, String> headers, Class<T> responseType) {
        HttpHeaders httpHeaders = new HttpHeaders();
        if (headers != null) {
            headers.forEach(httpHeaders::add);
        }

        HttpEntity<Object> requestEntity = new HttpEntity<>(body, httpHeaders);
        return restTemplate.exchange(url, method, requestEntity, responseType);
    }

    /**
     * List 형태의 응답을 받을 때 사용 (제네릭 타입 보존)
     */
    @Retryable(value = Exception.class, maxAttempts = 3, backoff = @Backoff(delay = 2000))
    public <T> T getList(String url, ParameterizedTypeReference<T> responseType) {
        return restTemplate.exchange(url, HttpMethod.GET, null, responseType).getBody();
    }

    /**
     * 모든 재시도가 실패했을 때 호출되는 최종 복구 로직
     */
    @Recover
    public Object recover(Exception e, String url) {
        log.error("RestService 최종 호출 실패 (URL: {}): {}", url, e.getMessage());
        throw new BusinessException(ErrorCode.INTERNAL_ERROR, "외부 API 서비스 호출 중 일시적인 오류가 발생했습니다.");
    }

    private String buildUrl(String url, Map<String, Object> params) {
        if (params == null || params.isEmpty()) return url;
        
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);
        params.forEach(builder::queryParam);
        return builder.toUriString();
    }
}
