package com.threlease.base.common.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

/**
 * axios와 유사한 사용성을 제공하는 HTTP 요청 유틸리티 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RestService {

    private final RestTemplate restTemplate;

    /**
     * GET 요청 (Query Params 지원)
     */
    public <T> T get(String url, Map<String, Object> params, Class<T> responseType) {
        String finalUrl = buildUrl(url, params);
        return restTemplate.getForObject(finalUrl, responseType);
    }

    /**
     * POST 요청 (Body 지원)
     */
    public <T> T post(String url, Object body, Class<T> responseType) {
        return restTemplate.postForObject(url, body, responseType);
    }

    /**
     * PUT 요청
     */
    public <T> void put(String url, Object body) {
        restTemplate.put(url, body);
    }

    /**
     * DELETE 요청
     */
    public void delete(String url) {
        restTemplate.delete(url);
    }

    /**
     * 상세 설정이 필요한 경우 (Header 등)
     */
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
     * 예: List<User> users = restService.getList(url, new ParameterizedTypeReference<List<User>>() {});
     */
    public <T> T getList(String url, ParameterizedTypeReference<T> responseType) {
        return restTemplate.exchange(url, HttpMethod.GET, null, responseType).getBody();
    }

    /**
     * URL에 쿼리 파라미터를 결합해주는 헬퍼 메서드
     */
    private String buildUrl(String url, Map<String, Object> params) {
        if (params == null || params.isEmpty()) return url;
        
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);
        params.forEach(builder::queryParam);
        return builder.toUriString();
    }
}
