package com.threlease.base.functions.auth;

import com.threlease.base.entites.AuthEntity;
import com.threlease.base.enums.Roles;
import com.threlease.base.functions.auth.dto.LoginDto;
import com.threlease.base.utils.Hash;
import com.threlease.base.utils.responses.BasicResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    private ResponseEntity<?> login(
        @RequestAttribute LoginDto dto
    ) {
        Optional<AuthEntity> auth = authService.findOneByUsername(dto.getUsername());
        if (auth.isPresent()) {
            if (Objects.equals(auth.get().getPassword(), new Hash().generateSHA512(dto.getPassword()))) {
                BasicResponse response = BasicResponse.builder()
                        .success(true)
                        .message(Optional.empty())
                        .data(Optional.ofNullable(authService.sign(auth.get())))
                        .build();

                return ResponseEntity.status(201).body(response);
            } else {
                BasicResponse response = BasicResponse.builder()
                        .success(false)
                        .message(Optional.of("아이디 혹은 비밀번호를 확인해주세요."))
                        .data(Optional.empty())
                        .build();

                return ResponseEntity.status(403).body(response);
            }
        } else {
            BasicResponse response = BasicResponse.builder()
                    .success(false)
                    .message(Optional.of("유저를 찾을 수 없습니다."))
                    .data(Optional.empty())
                    .build();

            return ResponseEntity.status(401).body(response);
        }
    }

    @PostMapping("/signup")
    private ResponseEntity<?> signUp(
            @RequestAttribute LoginDto dto
    ) {
        Optional<AuthEntity> auth = authService.findOneByUsername(dto.getUsername());

        if (auth.isEmpty()) {
            AuthEntity user = AuthEntity.builder()
                    .username(dto.getUsername())
                    .password(dto.getPassword())
                    .role(Roles.ROLE_USER)
                    .build();

            authService.authSave(user);
            BasicResponse response = BasicResponse.builder()
                    .success(true)
                    .message(Optional.empty())
                    .data(Optional.empty())
                    .build();

            return ResponseEntity.status(201).body(response);
        } else {
            BasicResponse response = BasicResponse.builder()
                    .success(false)
                    .message(Optional.of("이미 해당 아이디를 사용하는 유저가 있습니다."))
                    .data(Optional.empty())
                    .build();

            return ResponseEntity.status(403).body(response);
        }
    }

    @GetMapping("/@me")
    private ResponseEntity<?> verify(
            @RequestHeader("Authorization") String token
    ) {
        Optional<AuthEntity> user = authService.findOneByToken(token);

        if (user.isPresent()) {
            user.get().setSalt("unknown");
            user.get().setPassword("unknown");

            BasicResponse response = BasicResponse.builder()
                    .success(true)
                    .message(Optional.empty())
                    .data(Optional.of(user.get()))
                    .build();

            return ResponseEntity.status(200).body(response);
        } else {
            BasicResponse response = BasicResponse.builder()
                    .success(false)
                    .message(Optional.of("세션이 만료 되었거나 인증에 문제가 생겼습니다."))
                    .data(Optional.empty())
                    .build();

            return ResponseEntity.status(403).body(response);
        }
    }
}
