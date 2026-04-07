package com.threlease.base.common.utils;

import com.threlease.base.common.annotation.ExcelColumn;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 엑셀 다운로드 및 업로드(Import/Export) 유틸리티
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ExcelUtils {

    /**
     * 리스트 데이터를 엑셀 워크북으로 변환 (다운로드용)
     */
    public static <T> Workbook export(List<T> dataList, Class<T> clazz) {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Sheet1");

        List<Field> fields = getSortedExcelFields(clazz);
        
        // 1. 헤더 생성
        Row headerRow = sheet.createRow(0);
        CellStyle headerStyle = createHeaderStyle(workbook);
        
        for (int i = 0; i < fields.size(); i++) {
            Cell cell = headerRow.createCell(i);
            ExcelColumn ann = fields.get(i).getAnnotation(ExcelColumn.class);
            cell.setCellValue(ann.headerName());
            cell.setCellStyle(headerStyle);
        }

        // 2. 데이터 생성
        int rowIdx = 1;
        for (T data : dataList) {
            Row row = sheet.createRow(rowIdx++);
            for (int i = 0; i < fields.size(); i++) {
                Cell cell = row.createCell(i);
                try {
                    Field field = fields.get(i);
                    field.setAccessible(true);
                    Object value = field.get(data);
                    if (value != null) {
                        cell.setCellValue(value.toString());
                    }
                } catch (IllegalAccessException e) {
                    log.error("엑셀 추출 중 필드 접근 오류: {}", e.getMessage());
                }
            }
        }

        // 3. 열 너비 자동 조정
        for (int i = 0; i < fields.size(); i++) {
            sheet.autoSizeColumn(i);
        }

        return workbook;
    }

    /**
     * 업로드된 엑셀 파일을 객체 리스트로 변환 (업로드용)
     */
    public static <T> List<T> importExcel(InputStream is, Class<T> clazz) throws IOException {
        List<T> resultList = new ArrayList<>();
        Workbook workbook = WorkbookFactory.create(is);
        Sheet sheet = workbook.getSheetAt(0);
        
        Row headerRow = sheet.getRow(0);
        if (headerRow == null) return Collections.emptyList();

        Map<String, Integer> headerMap = new HashMap<>();
        for (Cell cell : headerRow) {
            headerMap.put(cell.getStringCellValue(), cell.getColumnIndex());
        }

        List<Field> excelFields = getSortedExcelFields(clazz);

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            try {
                T instance = clazz.getDeclaredConstructor().newInstance();
                boolean hasData = false;

                for (Field field : excelFields) {
                    ExcelColumn ann = field.getAnnotation(ExcelColumn.class);
                    Integer colIdx = headerMap.get(ann.headerName());
                    
                    if (colIdx != null) {
                        Cell cell = row.getCell(colIdx);
                        if (cell != null) {
                            String cellValue = getCellValueAsString(cell);
                            if (cellValue != null && !cellValue.isEmpty()) {
                                setFieldValue(instance, field, cellValue);
                                hasData = true;
                            }
                        }
                    }
                }
                if (hasData) resultList.add(instance);
            } catch (Exception e) {
                log.error("엑셀 행 파싱 중 오류 (rowIdx: {}): {}", i, e.getMessage());
            }
        }
        return resultList;
    }

    private static List<Field> getSortedExcelFields(Class<?> clazz) {
        return Arrays.stream(clazz.getDeclaredFields())
                .filter(f -> f.isAnnotationPresent(ExcelColumn.class))
                .sorted(Comparator.comparingInt(f -> f.getAnnotation(ExcelColumn.class).order()))
                .collect(Collectors.toList());
    }

    private static CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        return style;
    }

    private static String getCellValueAsString(Cell cell) {
        switch (cell.getCellType()) {
            case STRING: return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) return cell.getDateCellValue().toString();
                return String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN: return String.valueOf(cell.getBooleanCellValue());
            case FORMULA: return cell.getCellFormula();
            default: return "";
        }
    }

    private static void setFieldValue(Object instance, Field field, String value) throws IllegalAccessException {
        field.setAccessible(true);
        Class<?> type = field.getType();
        
        if (type == String.class) field.set(instance, value);
        else if (type == Integer.class || type == int.class) field.set(instance, Integer.parseInt(value));
        else if (type == Long.class || type == long.class) field.set(instance, Long.parseLong(value));
        else if (type == Double.class || type == double.class) field.set(instance, Double.parseDouble(value));
        else if (type == Boolean.class || type == boolean.class) field.set(instance, Boolean.parseBoolean(value));
        // 필요한 다른 타입들도 여기에 추가 가능
    }
}
