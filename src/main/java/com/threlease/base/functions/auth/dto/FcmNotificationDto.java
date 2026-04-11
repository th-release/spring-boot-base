package com.threlease.base.functions.auth.dto;

import com.threlease.base.common.annotation.ExcelColumn;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class FcmNotificationDto {
    @ExcelColumn(headerName = "알림 ID", order = 0)
    private Long id;

    @ExcelColumn(headerName = "FCM 메시지 ID", order = 1)
    private String messageId;

    @ExcelColumn(headerName = "제목", order = 2)
    private String title;

    @ExcelColumn(headerName = "본문", order = 3)
    private String body;

    @ExcelColumn(headerName = "데이터", order = 4)
    private String data;

    @ExcelColumn(headerName = "읽음 여부", order = 5)
    private boolean read;

    @ExcelColumn(headerName = "읽은 시간", order = 6)
    private LocalDateTime readAt;

    @ExcelColumn(headerName = "생성 시간", order = 7)
    private LocalDateTime createdAt;
}
