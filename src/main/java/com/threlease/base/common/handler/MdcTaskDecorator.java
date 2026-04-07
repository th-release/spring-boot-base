package com.threlease.base.common.handler;

import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;

import java.util.Map;

/**
 * 비동기 스레드 실행 시 MDC 정보를 전파하는 데코레이터
 * 메인 스레드의 correlationId 등을 비동기 스레드에서도 사용할 수 있게 합니다.
 */
public class MdcTaskDecorator implements TaskDecorator {

    @Override
    public Runnable decorate(Runnable runnable) {
        // 메인 스레드의 MDC 컨텍스트 맵을 가져옴
        Map<String, String> contextMap = MDC.getCopyOfContextMap();

        return () -> {
            try {
                // 비동기 스레드에 컨텍스트 맵 설정
                if (contextMap != null) {
                    MDC.setContextMap(contextMap);
                }
                runnable.run();
            } finally {
                // 비동기 작업 완료 후 MDC 정리
                MDC.clear();
            }
        };
    }
}
