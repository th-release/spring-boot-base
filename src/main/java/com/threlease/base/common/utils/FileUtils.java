package com.threlease.base.common.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.stream.Stream;

/**
 * 파일 시스템 관련 유틸리티 메서드를 제공하는 정적 클래스입니다.
 * <p>
 * 이 클래스는 java.nio.file 패키지를 활용하여 파일을 처리합니다.
 * </p>
 */
public class FileUtils {

    private FileUtils() {
        // 정적 유틸리티 클래스는 인스턴스화할 수 없습니다.
    }

    /**
     * 파일이 존재하는지 확인합니다.
     *
     * @param filePath 확인할 파일 경로 (절대 또는 상대)
     * @return 파일이 존재하면 true, 그렇지 않으면 false
     */
    public static boolean exists(String filePath) {
        return Files.exists(Paths.get(filePath));
    }

    /**
     * 디렉토리가 존재하는지 확인합니다.
     *
     * @param dirPath 확인할 디렉토리 경로 (절대 또는 상대)
     * @return 디렉토리가 존재하면 true, 그렇지 않으면 false
     */
    public static boolean isDirectory(String dirPath) {
        return Files.isDirectory(Paths.get(dirPath));
    }

    /**
     * 파일을 생성합니다. 이미 파일이 존재하면 아무것도 하지 않습니다.
     *
     * @param filePath 생성할 파일 경로
     * @throws IOException 파일 생성 중 오류 발생 시
     */
    public static void createFile(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            Files.createFile(path);
        }
    }

    /**
     * 디렉토리를 생성합니다. 상위 디렉토리가 없으면 함께 생성합니다.
     *
     * @param dirPath 생성할 디렉토리 경로
     * @throws IOException 디렉토리 생성 중 오류 발생 시
     */
    public static void createDirectory(String dirPath) throws IOException {
        Files.createDirectories(Paths.get(dirPath));
    }

    /**
     * 파일을 삭제합니다.
     *
     * @param filePath 삭제할 파일 경로
     * @return 파일 삭제 성공 시 true, 파일이 없거나 삭제 실패 시 false
     * @throws IOException 삭제 중 오류 발생 시
     */
    public static boolean deleteFile(String filePath) throws IOException {
        return Files.deleteIfExists(Paths.get(filePath));
    }

    /**
     * 디렉토리를 재귀적으로 삭제합니다. (내부에 파일이나 서브 디렉토리가 있어도 삭제)
     *
     * @param dirPath 삭제할 디렉토리 경로
     * @throws IOException 삭제 중 오류 발생 시
     */
    public static void deleteDirectoryRecursive(String dirPath) throws IOException {
        Path path = Paths.get(dirPath);
        if (Files.exists(path) && Files.isDirectory(path)) {
            try (Stream<Path> walk = Files.walk(path)) {
                walk.sorted(java.util.Comparator.reverseOrder())
                    .forEach(p -> {
                        try {
                            Files.delete(p);
                        } catch (IOException e) {
                            // 로깅 또는 예외 처리
                            System.err.println("Failed to delete " + p + ": " + e.getMessage());
                        }
                    });
            }
        }
    }

    /**
     * 파일 내용을 문자열로 읽어옵니다.
     *
     * @param filePath 읽어올 파일 경로
     * @return 파일 내용 문자열
     * @throws IOException 파일 읽기 중 오류 발생 시
     */
    public static String readFileToString(String filePath) throws IOException {
        return Files.readString(Paths.get(filePath));
    }

    /**
     * 파일에 문자열 내용을 씁니다. 파일이 존재하지 않으면 생성합니다.
     *
     * @param filePath 쓸 파일 경로
     * @param content 파일에 쓸 내용
     * @throws IOException 파일 쓰기 중 오류 발생 시
     */
    public static void writeStringToFile(String filePath, String content) throws IOException {
        Files.writeString(Paths.get(filePath), content);
    }

    /**
     * 파일을 다른 위치로 복사합니다.
     *
     * @param sourcePath 원본 파일 경로
     * @param targetPath 대상 파일 경로
     * @param replaceExisting 대상 파일이 존재할 경우 덮어쓸지 여부
     * @throws IOException 파일 복사 중 오류 발생 시
     */
    public static void copyFile(String sourcePath, String targetPath, boolean replaceExisting) throws IOException {
        Path source = Paths.get(sourcePath);
        Path target = Paths.get(targetPath);
        if (replaceExisting) {
            Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
        } else {
            Files.copy(source, target);
        }
    }

    /**
     * 파일의 확장자를 반환합니다.
     *
     * @param fileName 파일 이름 (또는 경로)
     * @return 파일 확장자 (예: "txt", "jpg"). 확장자가 없으면 빈 문자열 반환.
     */
    public static String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
            return fileName.substring(lastDotIndex + 1);
        }
        return "";
    }

    /**
     * 파일 이름에서 확장자를 제외한 부분을 반환합니다.
     *
     * @param fileName 파일 이름 (또는 경로)
     * @return 확장자 없는 파일 이름. 확장자가 없으면 전체 파일 이름 반환.
     */
    public static String getFileNameWithoutExtension(String fileName) {
        Path path = Paths.get(fileName);
        String name = path.getFileName().toString();
        int lastDotIndex = name.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return name.substring(0, lastDotIndex);
        }
        return name;
    }
}
