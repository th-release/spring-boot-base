package com.threlease.base.common.utils;

import com.threlease.base.common.utils.Failable;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * 날짜 및 시간 관련 유틸리티 메서드를 제공합니다.
 */
@Component
public class DateTimeUtils {

    private final DateTimeFormatter DATE_FORMATTER;
    private final DateTimeFormatter DATETIME_FORMATTER;

    public DateTimeUtils() {
        this.DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        this.DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    }

    /**
     * yyyy-MM-dd 형식의 문자열을 LocalDateTime으로 변환합니다.
     * 시간 정보는 00:00:00으로 설정됩니다.
     *
     * @param dateString yyyy-MM-dd 형식의 문자열
     * @return 성공 시 LocalDateTime, 실패 시 오류 메시지를 포함하는 Failable 객체
     */
    public Failable<LocalDateTime, String> parseDateStringToLocalDateTime(String dateString) {
        try {
            LocalDate date = LocalDate.parse(dateString, DATE_FORMATTER);
            return Failable.success(LocalDateTime.of(date, LocalTime.MIDNIGHT));
        } catch (DateTimeParseException e) {
            return Failable.error("잘못된 날짜 형식입니다. 형식은 'yyyy-MM-dd'여야 합니다: " + dateString);
        }
    }

    /**
     * LocalDateTime을 yyyy-MM-dd 형식의 문자열로 포맷팅합니다.
     *
     * @param dateTime 포맷팅할 LocalDateTime 객체
     * @return yyyy-MM-dd 형식의 문자열
     */
    public String formatLocalDateTimeToDateString(LocalDateTime dateTime) {
        return dateTime.format(DATE_FORMATTER);
    }

    /**
     * LocalDateTime을 yyyy-MM-dd HH:mm:ss 형식의 문자열로 포맷팅합니다.
     *
     * @param dateTime 포맷팅할 LocalDateTime 객체
     * @return yyyy-MM-dd HH:mm:ss 형식의 문자열
     */
    public String formatLocalDateTimeToDateTimeString(LocalDateTime dateTime) {
        return dateTime.format(DATETIME_FORMATTER);
    }

    /**
     * 현재 시각을 LocalDateTime 객체로 반환합니다.
     *
     * @return 현재 LocalDateTime 객체
     */
    public LocalDateTime now() {
        return LocalDateTime.now();
    }
}
