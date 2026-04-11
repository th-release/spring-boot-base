package com.threlease.base.functions.auth.dto;

import com.threlease.base.common.annotation.ExcelColumn;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthPermissionDto {
    @ExcelColumn(headerName = "권한 ID", order = 0)
    private String uuid;

    @ExcelColumn(headerName = "권한 코드", order = 1)
    private String code;

    @ExcelColumn(headerName = "권한명", order = 2)
    private String name;

    @ExcelColumn(headerName = "뎁스", order = 3)
    private int depth;

    @ExcelColumn(headerName = "상위 권한 ID", order = 4)
    private String parentUuid;

    @ExcelColumn(headerName = "정렬 순서", order = 5)
    private int sortOrder;

    @ExcelColumn(headerName = "설명", order = 6)
    private String description;
}
