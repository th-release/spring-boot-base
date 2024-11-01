package com.threlease.base.functions.auth;

import com.threlease.base.entites.AuthEntity;
import com.threlease.base.enums.Roles;
import com.threlease.base.functions.auth.dto.LoginDto;
import com.threlease.base.functions.auth.dto.SignUpDto;
import com.threlease.base.utils.Hash;
import com.threlease.base.utils.random.GetRandom;
import com.threlease.base.utils.random.RandomType;
import com.threlease.base.utils.responses.BasicResponse;
import com.threlease.base.utils.responses.Messages;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
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

        if (!Objects.equals(auth.get().getPassword(), new Hash().generateSHA512(dto.getPassword())))
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
                .nickname(dto.getNickname())
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
