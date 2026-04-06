package com.threlease.base.common.utils.QR;

import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.awt.*;

@Getter
@Setter
@Builder
public class QRCodeOption {
    private int width;
    private int height;
    private String format;
    private String charset;
    private int margin;
    private ErrorCorrectionLevel errorCorrectionLevel;
    private Color foregroundColor;
    private Color backgroundColor;
}
