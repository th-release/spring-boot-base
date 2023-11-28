package com.threlease.base.functions.auth;

import com.threlease.base.entites.AuthEntity;
import com.threlease.base.enums.Roles;
import com.threlease.base.functions.auth.dto.LoginDto;
import com.threlease.base.functions.auth.dto.SignUpDto;
import com.threlease.base.utils.GetRandom;
import com.threlease.base.utils.Hash;
import com.threlease.base.utils.JpaConnect;
import com.threlease.base.utils.responses.BasicResponse;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.lang.JoseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
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
            @RequestBody LoginDto dto
    ) throws JoseException {
        Optional<AuthEntity> auth = authService.findOneByUsername(dto.getUsername());

        if (auth.isEmpty()) {
            BasicResponse response = BasicResponse.builder()
                    .success(false)
                    .message(Optional.of("유저를 찾을 수 없습니다."))
                    .build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } else {
            AuthEntity user = auth.get();
            if (user.getPassword().equals(new Hash().generateSHA512(dto.getPassword() + user.getSalt()))) {
                String accessToken = authService.tokenSign(user.getUuid());

                BasicResponse response = BasicResponse.builder()
                        .success(true)
                        .message(Optional.of("로그인 성공"))
                        .data(Optional.ofNullable(accessToken))
                        .build();
                return ResponseEntity.status(HttpStatus.CREATED).body(response);
            } else {
                BasicResponse response = BasicResponse.builder()
                        .success(false)
                        .message(Optional.of("아이디 혹은 비밀번호를 확인해주세요."))
                        .build();
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<Object> SignUp(
            @RequestBody SignUpDto dto,
            @RequestParam(value = "file") MultipartFile file
    ) {
        if (dto.getUsername() == null || dto.getUsername().trim().isEmpty()) {
            BasicResponse response = BasicResponse.builder()
                    .success(false)
                    .message(Optional.of("username을 입력해주세요."))
                    .build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        if (dto.getNickname() == null || dto.getNickname().trim().isEmpty()) {
            BasicResponse response = BasicResponse.builder()
                    .success(false)
                    .message(Optional.of("nickname을 입력해주세요."))
                    .build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        if (dto.getPassword() == null || dto.getPassword().trim().isEmpty()) {
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

        String fileExtension = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".") + 1);
        String salt = new GetRandom().run("all", 32);
        AuthEntity auth = AuthEntity.builder()
                .username(dto.getUsername())
                .nickname(dto.getNickname())
                .password(new Hash().generateSHA512(dto.getPassword() + salt))
                .salt(salt)
                .createdAt(LocalDateTime.now())
                .role(Roles.ROLE_USER)
                .build();

        Optional<AuthEntity> User = authService.findOneByUsername(dto.getUsername());
        try {
            String random = new GetRandom().run("all", 32);
            String currentDirectory = System.getProperty("user.dir");
            String MIME = Objects.requireNonNull(file.getContentType()).split("/")[0];
            if (User.isPresent()) throw new Exception("이미 다른 유저가 데이터베이스에 있습니다.");
            if (!MIME.equals("image")) throw new IOException("파일을 업로드 중 오류가 발생했습니다.");
            String filePath = currentDirectory + File.separator + "user/profile/" + random + "." + fileExtension;

            auth.setProfilePath("user/profile/" + random + "." + fileExtension);
            File dest = new File(filePath);

            file.transferTo(dest);
        } catch (IOException e) {
            BasicResponse response = BasicResponse.builder()
                    .success(false)
                    .data(Optional.of(e.getMessage()))
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        } catch (Exception e) {
            BasicResponse response = BasicResponse.builder()
                    .success(false)
                    .data(Optional.of(e.getMessage()))
                    .build();
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }

        JpaConnect jpaConnect = new JpaConnect(lambda -> {
            authService.authSave(auth);

            Map<String, Object> result = new HashMap<>();
            result.put("uuid", auth.getUuid());
            result.put("username", auth.getUsername());
            BasicResponse response = BasicResponse.builder()
                    .success(true)
                    .message(Optional.of("정상적으로 회원가입이 완료되었습니다"))
                    .data(Optional.of(result))
                    .build();

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        });

        return jpaConnect.run();
    }

    @GetMapping("/@me")
    public ResponseEntity<Object> Me(
            @RequestHeader(value = "Authorization") String token
    ) throws JoseException, InvalidJwtException, MalformedClaimException {
        return authService.Me(token);
    }
}
