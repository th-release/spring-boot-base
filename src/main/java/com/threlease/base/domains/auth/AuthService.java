package com.threlease.base.domains.auth;

import com.threlease.base.entites.AuthEntity;
import org.jose4j.lang.JoseException;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
@Service
public interface AuthService {
    public Optional<AuthEntity> findOneByUUID(@Param("uuid") String uuid);
    Optional<AuthEntity> findOneByUsername(@Param("username") String username);
    List<AuthEntity> findAllLimitOrderByCreatedAtDesc(@Param("limit") int limit);
    ResponseEntity<Object> Login(
            @RequestParam(value = "username") String username,
            @RequestParam(value = "password") String password
    ) throws JoseException;
    ResponseEntity<Object> SignUp(
            @RequestParam(value = "username") String username,
            @RequestParam(value = "nickname") String nickname,
            @RequestParam(value = "password") String password,
            @RequestParam(value = "file") MultipartFile file
    );
    ResponseEntity<Object> getProfile(@RequestParam("uuid") String uuid) throws IOException;
    ResponseEntity<Object> Me(@RequestHeader(value = "Authorization") String token) throws JoseException;
}
