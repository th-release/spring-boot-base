package com.threlease.base.common.handler;

import com.threlease.base.common.annotation.ApiVersion;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import org.springframework.web.servlet.mvc.condition.RequestCondition;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 요청 URL에서 버전 정보를 추출하고 매칭되는 버전을 찾는 조건 클래스
 */
public class ApiVersionRequestCondition implements RequestCondition<ApiVersionRequestCondition> {

    // URL에서 버전을 추출하기 위한 패턴 (예: /v1, /v2)
    private static final Pattern VERSION_PREFIX_PATTERN = Pattern.compile("/v(\\d+)/");

    @Getter
    private final int[] versions;

    public ApiVersionRequestCondition(int... versions) {
        this.versions = versions;
        if (this.versions != null) {
            Arrays.sort(this.versions);
        }
    }

    @Override
    public ApiVersionRequestCondition combine(ApiVersionRequestCondition other) {
        // 메서드 레벨의 어노테이션이 클래스 레벨보다 우선순위를 가짐
        return new ApiVersionRequestCondition(other.versions);
    }

    @Override
    public ApiVersionRequestCondition getMatchingCondition(HttpServletRequest request) {
        Matcher m = VERSION_PREFIX_PATTERN.matcher(request.getRequestURI());
        if (m.find()) {
            int version = Integer.parseInt(m.group(1));
            for (int v : versions) {
                if (v == version) {
                    return this;
                }
            }
        }
        return null;
    }

    @Override
    public int compareTo(ApiVersionRequestCondition other, HttpServletRequest request) {
        // 더 높은 버전이 우선순위를 가짐
        return other.versions[other.versions.length - 1] - this.versions[this.versions.length - 1];
    }
}
