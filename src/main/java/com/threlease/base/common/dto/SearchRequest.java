package com.threlease.base.common.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchRequest {
    @Hidden
    @Builder.Default
    private int pageUnit = 10;

    @Hidden
    @Builder.Default
    private int pageSize = 10;

    @Hidden
    @Builder.Default
    private int pageIndex = 1;

    @Hidden
    @Builder.Default
    private int firstIndex = 1;

    @Hidden
    @Builder.Default
    private int lastIndex = 1;

    @Hidden
    @Builder.Default
    private int recordCountPerPage = 10;

    @Hidden
    @Builder.Default
    private String searchType1 = "";

    @Hidden
    @Builder.Default
    private String searchType2 = "";

    @Hidden
    @Builder.Default
    private String searchType3 = "";

    @Hidden
    @Builder.Default
    private String searchType4 = "";

    @Hidden
    @Builder.Default
    private String searchType5 = "";

    @Hidden
    @Builder.Default
    private String searchType6 = "";

    @Hidden
    @Builder.Default
    private String searchType7 = "";

    @Hidden
    @Builder.Default
    private String keyword = "";

    @Hidden
    @Builder.Default
    private String keyword2 = "";

    @Hidden
    @Builder.Default
    private String startDate = "";

    @Hidden
    @Builder.Default
    private String endDate = "";

    @Hidden
    @Builder.Default
    private String searchDate = "";

    @Hidden
    @Builder.Default
    private String order = "1";

    @JsonIgnore
    public int zeroBasedPage() {
        return Math.max(pageIndex - 1, 0);
    }

    @JsonIgnore
    public int pageSizeOrDefault() {
        return pageSize > 0 ? pageSize : recordCountPerPage;
    }

    @JsonIgnore
    public Map<String, String> searchTypes() {
        Map<String, String> values = new LinkedHashMap<>();
        values.put("searchType1", normalize(searchType1));
        values.put("searchType2", normalize(searchType2));
        values.put("searchType3", normalize(searchType3));
        values.put("searchType4", normalize(searchType4));
        values.put("searchType5", normalize(searchType5));
        values.put("searchType6", normalize(searchType6));
        values.put("searchType7", normalize(searchType7));
        return values;
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}
