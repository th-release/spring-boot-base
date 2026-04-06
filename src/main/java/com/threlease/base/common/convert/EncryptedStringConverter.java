package com.threlease.base.common.convert;

import com.threlease.base.common.utils.crypto.AesComponent;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.RequiredArgsConstructor;

/**
 * JPA Entity 필드에 @Convert(converter = EncryptedStringConverter.class) 를 붙이면
 * DB 저장 시 자동 암호화, 조회 시 자동 복호화됩니다.
 *
 * 사용 예시:
 * <pre>
 *   {@literal @}Convert(converter = EncryptedStringConverter.class)
 *   private String phoneNumber;
 * </pre>
 */
@Converter
@RequiredArgsConstructor
public class EncryptedStringConverter implements AttributeConverter<String, String> {

    private final AesComponent aesComponentEncryptionUtils;

    @Override
    public String convertToDatabaseColumn(String attribute) {
        return aesComponentEncryptionUtils.encrypt(attribute);
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        return aesComponentEncryptionUtils.decrypt(dbData);
    }
}
