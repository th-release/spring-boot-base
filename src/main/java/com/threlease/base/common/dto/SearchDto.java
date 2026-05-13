package com.threlease.base.common.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter(onMethod_ = @JsonIgnore)
@Builder
public class SearchDto {
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
    private String searchCnd1 = "";

    @Hidden
    @Builder.Default
    private String searchCnd2 = "";

    @Hidden
    @Builder.Default
    private String searchCnd3 = "";

    @Hidden
    @Builder.Default
    private String searchCnd4 = "";

    @Hidden
    @Builder.Default
    private String searchCnd5 = "";

    @Hidden
    @Builder.Default
    private String searchCnd6 = "";

    @Hidden
    @Builder.Default
    private String searchCnd7 = "";

    @Hidden
    @Builder.Default
    private String searchWrd = "";

    @Hidden
    @Builder.Default
    private String searchWrd2 = "";

    @Hidden
    @Builder.Default
    private String searchStartDate = "";

    @Hidden
    @Builder.Default
    private String searchEndDate = "";

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
    public Map<String, String> searchConditions() {
        Map<String, String> values = new LinkedHashMap<>();
        values.put("searchCnd1", normalize(searchCnd1));
        values.put("searchCnd2", normalize(searchCnd2));
        values.put("searchCnd3", normalize(searchCnd3));
        values.put("searchCnd4", normalize(searchCnd4));
        values.put("searchCnd5", normalize(searchCnd5));
        values.put("searchCnd6", normalize(searchCnd6));
        values.put("searchCnd7", normalize(searchCnd7));
        return values;
    }

    @JsonIgnore
    public String normalizedSearchWrd() {
        return normalize(searchWrd);
    }

    @JsonIgnore
    public String normalizedSearchWrd2() {
        return normalize(searchWrd2);
    }

    @JsonIgnore
    public String normalizedSearchStartDate() {
        return normalize(searchStartDate);
    }

    @JsonIgnore
    public String normalizedSearchEndDate() {
        return normalize(searchEndDate);
    }

    @JsonIgnore
    public String normalizedSearchDate() {
        return normalize(searchDate);
    }

    @JsonIgnore
    public String normalizedOrder() {
        return normalize(order);
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}
