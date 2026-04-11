package com.threlease.base.functions.auth.dto;

import com.threlease.base.entities.AuthEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface AuthMapper {
    AuthMapper INSTANCE = Mappers.getMapper(AuthMapper.class);

    // AuthEntity -> LoginDto
    @Mapping(target = "otpCode", ignore = true)
    LoginDto toLoginDto(AuthEntity entity);

    // AuthEntity -> SignUpDto
    SignUpDto toSignUpDto(AuthEntity entity);

    // SignUpDto -> AuthEntity (Password 등 필요한 필드는 수동 처리 필요할 수 있음)
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "salt", ignore = true)
    @Mapping(target = "type", ignore = true)
    AuthEntity toEntity(SignUpDto dto);
}
