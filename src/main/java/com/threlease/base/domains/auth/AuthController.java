package com.threlease.base.domains.auth;

import com.threlease.base.utils.responses.BasicResponse;
import org.jose4j.lang.JoseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/get/profile")
    public ResponseEntity<Object> getProfile(@RequestParam("uuid") String uuid) throws IOException {
        return authService.getProfile(uuid);
    }

    @PostMapping("/login")
    public ResponseEntity<Object> Login(
            @RequestParam(value = "username") String username,
            @RequestParam(value = "password") String password
    ) throws JoseException {
        return authService.Login(username, password);
    }

    @PostMapping("/signup")
    public ResponseEntity<Object> SignUp(
            @RequestParam(value = "username") String username,
            @RequestParam(value = "nickname") String nickname,
            @RequestParam(value = "password") String password,
            @RequestParam(value = "file") MultipartFile file
    ) {
        if (username == null || username.trim().isEmpty()) {
            BasicResponse response = BasicResponse.builder()
                    .success(false)
                    .message(Optional.of("username을 입력해주세요."))
                    .build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        if (nickname == null || nickname.trim().isEmpty()) {
            BasicResponse response = BasicResponse.builder()
                    .success(false)
                    .message(Optional.of("nickname을 입력해주세요."))
                    .build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        if (password == null || password.trim().isEmpty()) {
            BasicResponse response = BasicResponse.builder()
                    .success(false)
                    .message(Optional.of("password를 입력해주세요."))
                    .build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        if (file.isEmpty()) {
            BasicResponse response = BasicResponse.builder()
                    .success(false)
                    .message(Optional.of("file을 업로드 해주세요"))
                    .build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        return authService.SignUp(username, nickname, password, file);
    }

    @GetMapping("/@me")
    public ResponseEntity<Object> Me(
            @RequestHeader(value = "Authorization") String token
    ) throws JoseException {
        return authService.Me(token);
    }
}
