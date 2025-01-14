package com.threlease.base.common.convert;

import com.threlease.base.common.utils.Failable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * yyyy-MM-dd 형식으로 문자열이 오면 해당 문자열을 LocalDateTime 데이터로 바꿔준다.
 */
public class DateConverter {
    public static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     *
     * @param dateString yyyy-MM-dd 형식의 문자열
     * @return LocalDateTime 데이터
     */
    public static Failable<LocalDateTime, String> run(String dateString) {
        try {
            // 입력된 문자열이 지정된 형식에 맞는지 검증하며 LocalDate로 변환
            LocalDate date = LocalDate.parse(dateString, formatter);
            // 기본 시간(00:00:00)을 추가하여 LocalDateTime 반환
            return Failable.success(LocalDateTime.of(date, LocalTime.MIDNIGHT));
        } catch (DateTimeParseException e) {
            // 형식이 잘못되었을 경우 사용자 정의 예외 또는 메시지 반환
            return Failable.error("잘못된 날짜 형식입니다. 형식은 'yyyy-MM-dd'여야 합니다.");
        }
    }
}

