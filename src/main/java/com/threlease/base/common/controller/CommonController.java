package com.threlease.base.common.controller;

import com.threlease.base.common.enums.AuthStatuses;
import com.threlease.base.common.enums.UserTypes;
import com.threlease.base.common.utils.enumeration.EnumMapperValue;
import com.threlease.base.common.utils.responses.BasicResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 공통 코드(Enum) 조회를 위한 컨트롤러
 */
@RestController
@RequestMapping("/api/common")
@Tag(name = "Common API", description = "공통 코드 및 설정 정보 API")
public class CommonController {

    @GetMapping("/enums")
    @Operation(summary = "전체 Enum 코드 목록 조회", description = "시스템에서 사용하는 모든 Enum 코드와 명칭을 반환합니다.")
    public ResponseEntity<BasicResponse<Map<String, List<EnumMapperValue>>>> getEnums() {
        Map<String, List<EnumMapperValue>> enums = new LinkedHashMap<>();
        
        // 여기에 노출할 Enum 클래스들을 추가합니다.
        enums.put("userTypes", toEnumValues(UserTypes.class));
        enums.put("authStatuses", toEnumValues(AuthStatuses.class));
        
        return BasicResponse.ok(enums);
    }

    private List<EnumMapperValue> toEnumValues(Class<? extends com.threlease.base.common.enums.EnumValue> e) {
        return Arrays.stream(e.getEnumConstants())
                .map(EnumMapperValue::new)
                .collect(Collectors.toList());
    }
}
