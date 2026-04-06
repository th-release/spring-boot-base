package com.threlease.base.common.utils.QR;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.client.j2se.MatrixToImageConfig;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.threlease.base.common.properties.app.qr.QrCodeProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.EnumMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class QRCode {
    private final QrCodeProperties props;

    // ──────────────────────────────────────────────────────────────
    // 1. QR 생성 — byte[]
    // ──────────────────────────────────────────────────────────────

    /**
     * 기본 설정으로 QR 코드를 생성해 byte[]로 반환합니다.
     *
     * @param content 인코딩할 문자열
     * @return PNG 이미지 바이트 배열
     */
    public byte[] generateQrCode(String content) throws WriterException, IOException {
        return generateQrCode(content, QRCodeOption.builder()
                .width(props.getWidth())
                .height(props.getHeight())
                .format(props.getFormat())
                .charset(props.getCharset())
                .margin(props.getMargin())
                .errorCorrectionLevel(ErrorCorrectionLevel.M)
                .foregroundColor(Color.BLACK)
                .backgroundColor(Color.WHITE)
                .build());
    }

    /**
     * 크기를 지정해 QR 코드를 byte[]로 반환합니다.
     */
    public byte[] generateQrCode(String content, int width, int height)
            throws WriterException, IOException {
        var options = QRCodeOption.builder().width(width).height(height).build();
        return generateQrCode(content, options);
    }

    /**
     * {@link QRCodeOption}로 세부 설정을 지정해 QR 코드를 byte[]로 반환합니다.
     */
    public byte[] generateQrCode(String content, QRCodeOption options)
            throws WriterException, IOException {

        log.debug("QR 생성 시작 - content length: {}, size: {}x{}",
                content.length(), options.getWidth(), options.getHeight());

        var bitMatrix = buildBitMatrix(content, options);
        var config = new MatrixToImageConfig(
                options.getForegroundColor().getRGB(),
                options.getBackgroundColor().getRGB()
        );

        try (var out = new ByteArrayOutputStream()) {
            MatrixToImageWriter.writeToStream(bitMatrix, options.getFormat(), out, config);
            var result = out.toByteArray();
            log.debug("QR 생성 완료 - 바이트 크기: {}", result.length);
            return result;
        }
    }

    // ──────────────────────────────────────────────────────────────
    // 2. QR 생성 — Base64 (data URI)
    // ──────────────────────────────────────────────────────────────

    /**
     * QR 코드를 {@code data:image/png;base64,...} 형식의 문자열로 반환합니다.
     */
    public String generateQrCodeBase64(String content) throws WriterException, IOException {
        return generateQrCodeBase64(content, QRCodeOption.builder()
                .width(props.getWidth())
                .height(props.getHeight())
                .format(props.getFormat())
                .charset(props.getCharset())
                .margin(props.getMargin())
                .errorCorrectionLevel(ErrorCorrectionLevel.M)
                .foregroundColor(Color.BLACK)
                .backgroundColor(Color.WHITE)
                .build());
    }

    /**
     * 옵션을 지정해 QR 코드를 Base64 data URI로 반환합니다.
     */
    public String generateQrCodeBase64(String content, QRCodeOption options)
            throws WriterException, IOException {
        var qrBytes = generateQrCode(content, options);
        return "data:image/%s;base64,%s".formatted(
                options.getFormat().toLowerCase(),
                Base64.getEncoder().encodeToString(qrBytes)
        );
    }

    // ──────────────────────────────────────────────────────────────
    // 3. QR 생성 — 파일 저장
    // ──────────────────────────────────────────────────────────────

    /**
     * QR 코드를 지정 경로의 파일로 저장합니다.
     *
     * @param content  인코딩할 문자열
     * @param filePath 저장 경로 (상위 디렉터리가 없으면 자동 생성)
     */
    public void generateQrCodeToFile(String content, Path filePath)
            throws WriterException, IOException {
        generateQrCodeToFile(content, QRCodeOption.builder()
                .width(props.getWidth())
                .height(props.getHeight())
                .format(props.getFormat())
                .charset(props.getCharset())
                .margin(props.getMargin())
                .errorCorrectionLevel(ErrorCorrectionLevel.M)
                .foregroundColor(Color.BLACK)
                .backgroundColor(Color.WHITE)
                .build(), filePath);
    }

    /**
     * 옵션을 지정해 QR 코드를 파일로 저장합니다.
     */
    public void generateQrCodeToFile(String content, QRCodeOption options, Path filePath)
            throws WriterException, IOException {

        if (filePath.getParent() != null) {
            Files.createDirectories(filePath.getParent());
        }
        var bitMatrix = buildBitMatrix(content, options);
        MatrixToImageWriter.writeToPath(bitMatrix, options.getFormat(), filePath);
        log.info("QR 파일 저장 완료: {}", filePath.toAbsolutePath());
    }

    // ──────────────────────────────────────────────────────────────
    // 4. QR 생성 — 로고 삽입
    // ──────────────────────────────────────────────────────────────

    /**
     * QR 코드 중앙에 로고 이미지를 삽입합니다.
     *
     * <p>로고 영역이 가려지므로 오류 정정 수준은 {@code H}로 자동 강제됩니다.
     *
     * @param content       인코딩할 문자열
     * @param options       QR 생성 옵션 (ECL은 내부에서 H로 재설정됨)
     * @param logoBytes     삽입할 로고 이미지 바이트 배열
     * @param logoSizeRatio QR 크기 대비 로고 비율 (권장: 0.2 ~ 0.3)
     * @return PNG 이미지 바이트 배열
     */
    public byte[] generateQrCodeWithLogo(String content, QRCodeOption options,
                                         byte[] logoBytes, float logoSizeRatio)
            throws WriterException, IOException {

        // 로고 삽입 시 ErrorCorrectionLevel.H 강제
        var hOptions = QRCodeOption.builder()
                .width(options.getWidth())
                .height(options.getHeight())
                .format(options.getFormat())
                .charset(options.getCharset())
                .margin(options.getMargin())
                .errorCorrectionLevel(ErrorCorrectionLevel.H)
                .foregroundColor(options.getForegroundColor())
                .backgroundColor(options.getBackgroundColor())
                .build();

        var bitMatrix = buildBitMatrix(content, hOptions);
        var qrImage   = MatrixToImageWriter.toBufferedImage(bitMatrix);

        // 로고 리사이즈
        int logoW = (int) (options.getWidth()  * logoSizeRatio);
        int logoH = (int) (options.getHeight() * logoSizeRatio);

        var rawLogo = ImageIO.read(new ByteArrayInputStream(logoBytes));
        var scaledLogo = new BufferedImage(logoW, logoH, BufferedImage.TYPE_INT_ARGB);

        var g2dLogo = scaledLogo.createGraphics();
        g2dLogo.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2dLogo.drawImage(rawLogo, 0, 0, logoW, logoH, null);
        g2dLogo.dispose();

        // QR에 로고 합성
        var g2d = qrImage.createGraphics();
        g2d.drawImage(scaledLogo,
                (options.getWidth()  - logoW) / 2,
                (options.getHeight() - logoH) / 2,
                null);
        g2d.dispose();

        try (var out = new ByteArrayOutputStream()) {
            ImageIO.write(qrImage, options.getFormat(), out);
            return out.toByteArray();
        }
    }

    // ──────────────────────────────────────────────────────────────
    // 5. QR 디코딩
    // ──────────────────────────────────────────────────────────────

    /**
     * QR 코드 이미지(byte[])에서 텍스트를 디코딩합니다.
     */
    public String decodeQrCode(byte[] imageBytes) throws IOException, NotFoundException {
        var image = ImageIO.read(new ByteArrayInputStream(imageBytes));
        return decodeQrCode(image);
    }

    /**
     * QR 코드 이미지 파일 경로에서 텍스트를 디코딩합니다.
     */
    public String decodeQrCode(Path filePath) throws IOException, NotFoundException {
        var image = ImageIO.read(filePath.toFile());
        return decodeQrCode(image);
    }

    /**
     * Base64 문자열(또는 data URI)에서 QR 코드를 디코딩합니다.
     *
     * <p>{@code data:image/png;base64,...} 형식과 순수 Base64 모두 지원합니다.
     */
    public String decodeQrCodeFromBase64(String base64Image) throws IOException, NotFoundException {
        var base64Data = base64Image.contains(",")
                ? base64Image.split(",", 2)[1]
                : base64Image;
        return decodeQrCode(Base64.getDecoder().decode(base64Data));
    }

    /**
     * {@link BufferedImage}에서 QR 코드를 디코딩합니다.
     */
    public String decodeQrCode(BufferedImage image) throws NotFoundException {
        var source = new BufferedImageLuminanceSource(image);
        var bitmap = new BinaryBitmap(new HybridBinarizer(source));

        Map<DecodeHintType, Object> hints = new EnumMap<>(DecodeHintType.class);
        hints.put(DecodeHintType.CHARACTER_SET, props.getCharset());
        hints.put(DecodeHintType.TRY_HARDER,    Boolean.TRUE);

        var result = new MultiFormatReader().decode(bitmap, hints);
        log.debug("QR 디코딩 완료 - content: {}", result.getText());
        return result.getText();
    }

    // ──────────────────────────────────────────────────────────────
    // 내부 헬퍼
    // ──────────────────────────────────────────────────────────────

    private BitMatrix buildBitMatrix(String content, QRCodeOption options)
        throws WriterException {
            Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
            hints.put(EncodeHintType.CHARACTER_SET, options.getCharset());
            hints.put(EncodeHintType.ERROR_CORRECTION, options.getErrorCorrectionLevel());
            hints.put(EncodeHintType.MARGIN, options.getMargin());

            return new QRCodeWriter().encode(
                    content, BarcodeFormat.QR_CODE,
                    options.getWidth(), options.getHeight(),
                    hints
            );
        }
}