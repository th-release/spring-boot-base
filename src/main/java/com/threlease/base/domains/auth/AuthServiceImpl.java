package com.threlease.base.domains.auth;

import com.threlease.base.enums.Roles;
import com.threlease.base.repositories.AuthRepository;
import com.threlease.base.entites.AuthEntity;
import com.threlease.base.utils.GetRandom;
import com.threlease.base.utils.Hash;
import com.threlease.base.utils.JpaConnect;
import com.threlease.base.utils.jsonwebtoken.JwtProvider;
import com.threlease.base.utils.responses.BasicResponse;
import org.jose4j.lang.JoseException;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class AuthServiceImpl implements AuthService {
    private final AuthRepository authRepository;
    private final JwtProvider jwtProvider;

    public AuthServiceImpl(AuthRepository authRepository, JwtProvider jwtProvider) {
        this.authRepository = authRepository;
        this.jwtProvider = jwtProvider;
    }

    @Override
    public Optional<AuthEntity> findOneByUUID(@Param("uuid") String uuid) {
        return authRepository.findOneByUUID(uuid);
    }

    @Override
    public Optional<AuthEntity> findOneByUsername(@Param("username") String username) {
        return authRepository.findOneByUsername(username);
    }

    @Override
    public List<AuthEntity> findAllLimitOrderByCreatedAtDesc(@Param("limit") int limit) {
        return authRepository.findAllLimitOrderByCreatedAtDesc(limit);
    }

    @Override
    public ResponseEntity<Object> getProfile(@RequestParam("uuid") String uuid) {
        try {
            Optional<AuthEntity> findUser = authRepository.findOneByUUID(uuid);
            if (findUser.isEmpty()) {
                BasicResponse response = BasicResponse.builder()
                        .success(true)
                        .message(Optional.of("유저를 찾을 수 없습니다."))
                        .build();
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            } else {
                String currentDirectory = System.getProperty("user.dir");
                String filePath = currentDirectory + File.separator + findUser.get().getProfilePath();

                FileSystemResource fileResource = new FileSystemResource(filePath);
                if (fileResource.exists()) {
                    return ResponseEntity.status(200)
                            .contentType(MediaType.IMAGE_JPEG)
                            .contentLength(fileResource.contentLength())
                            .body(fileResource);
                } else throw new IOException("INTERNAL_SERVER_ERROR");
            }
        } catch (IOException err) {
            BasicResponse response = BasicResponse.builder()
                    .success(true)
                    .data(Optional.of("INTERNAL_SERVER_ERROR"))
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @Override
    public ResponseEntity<Object> Login(
            @RequestParam(value = "username") String username,
            @RequestParam(value = "password") String password
    ) throws JoseException {
        Optional<AuthEntity> auth = authRepository.findOneByUsername(username);

        if (auth.isEmpty()) {
            BasicResponse response = BasicResponse.builder()
                    .success(false)
                    .message(Optional.of("유저를 찾을 수 없습니다."))
                    .build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } else {
            AuthEntity user = auth.get();
            if (user.getPassword().equals(new Hash().generateSHA512(password + user.getSalt()))) {
                String accessToken = jwtProvider.sign(user.getUuid());

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

    @Override
    public ResponseEntity<Object> SignUp(
            @RequestParam(value = "username") String username,
            @RequestParam(value = "nickname") String nickname,
            @RequestParam(value = "password") String password,
            @RequestParam(value = "file") MultipartFile file
    ) {
        String fileExtension = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".") + 1);
        String salt = new GetRandom().run("all", 32);
        AuthEntity auth = AuthEntity.builder()
                .username(username)
                .nickname(nickname)
                .password(new Hash().generateSHA512(password + salt))
                .salt(salt)
                .createdAt(LocalDateTime.now())
                .role(Roles.USER)
                .build();

        Optional<AuthEntity> User = authRepository.findOneByUsername(username);
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
            authRepository.save(auth);

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

    public ResponseEntity<Object> Me(
            @RequestHeader(value = "Authorization") String token
    ) throws JoseException {
        Optional<AuthEntity> data = jwtProvider.findByToken(token);

        if (data.isPresent()) {
            AuthEntity _data = data.get();
            _data
                .setPassword("unknown");
            _data
                .setSalt("unknown");
            data = Optional.of(_data);
        }

        BasicResponse response = BasicResponse.builder()
                .success(true)
                .message(Optional.empty())
                .data(Optional.of(data))
                .build();

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
