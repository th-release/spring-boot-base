package com.threlease.base.functions.auth;

import com.threlease.base.entites.AuthEntity;
import com.threlease.base.common.enums.Roles;
import com.threlease.base.functions.auth.dto.LoginDto;
import com.threlease.base.functions.auth.dto.SignUpDto;
import com.threlease.base.common.crypto.Hash;
import com.threlease.base.common.utils.random.GetRandom;
import com.threlease.base.common.utils.random.RandomType;
import com.threlease.base.common.utils.responses.BasicResponse;
import com.threlease.base.common.utils.responses.Messages;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
@Tag(name = "Auth API")
@AllArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "로그인")
    private ResponseEntity<BasicResponse<String>> login(
        @RequestBody @Valid LoginDto dto
    ) {
        Optional<AuthEntity> auth = authService.findOneByUsername(dto.getUsername());

        if (auth.isEmpty())
            return ResponseEntity.status(404).body(
                    BasicResponse.<String>builder()
                            .success(false)
                            .message(Optional.of(Messages.NOT_FOUND_USER))
                            .build()
            );

        if (!Objects.equals(auth.get().getPassword(), Hash.generateSHA512(dto.getPassword() + auth.get().getSalt())))
            return ResponseEntity.status(403).body(
                    BasicResponse.<String>builder()
                            .success(false)
                            .message(Optional.of(Messages.WRONG_AUTH))
                            .build()
            );

        return ResponseEntity.status(201).body(
                BasicResponse.<String>builder()
                        .success(true)
                        .message(Optional.empty())
                        .data(Optional.ofNullable(authService.sign(auth.get())))
                        .build()
        );
    }

    @PostMapping("/signup")
    @Operation(summary = "회원가입")
    private ResponseEntity<BasicResponse<AuthEntity>> signUp(
            @RequestBody @Valid SignUpDto dto
    ) {
        Optional<AuthEntity> auth = authService.findOneByUsername(dto.getUsername());

        if (auth.isPresent())
            return ResponseEntity.status(403).body(
                    BasicResponse.<AuthEntity>builder()
                            .success(false)
                            .message(Optional.of(Messages.DUPLICATE_USER))
                            .build()
            );

        String salt = GetRandom.run(RandomType.ALL, 32);

        AuthEntity user = AuthEntity.builder()
                .username(dto.getUsername())
                .password(Hash.generateSHA512(dto.getPassword() + salt))
                .salt(salt)
                .role(Roles.ROLE_USER)
                .createdAt(LocalDateTime.now())
                .build();

        authService.authSave(user);

        return ResponseEntity.status(201).body(
                BasicResponse.<AuthEntity>builder()
                        .success(true)
                        .data(Optional.of(user))
                        .build()
        );
    }

    @GetMapping("/@me")
    @Operation(summary = "인증")
    private ResponseEntity<BasicResponse<AuthEntity>> verify(
            @RequestHeader("Authorization") String token
    ) {
        Optional<AuthEntity> user = authService.findOneByToken(token);

        return user.map(authEntity -> ResponseEntity.status(200).body(
                BasicResponse.<AuthEntity>builder()
                        .success(true)
                        .message(Optional.empty())
                        .data(Optional.of(authEntity))
                        .build()
        )).orElseGet(() -> ResponseEntity.status(403).body(
                BasicResponse.<AuthEntity>builder()
                        .success(false)
                        .message(Optional.of(Messages.SESSION_ERROR))
                        .build()
        ));
    }
}
