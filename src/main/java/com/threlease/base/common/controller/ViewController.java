package com.threlease.base.common.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.server.ResponseStatusException;

/**
 * SPA (React Router) 지원을 위한 뷰 컨트롤러
 * API(/api/**)로 시작하지 않고 확장자가 없는 모든 요청을 index.html로 포워딩합니다.
 */
@Controller
public class ViewController {

    @GetMapping({
            "/",
            "/{path:[^\\.]*}",
            "/**/{path:[^\\.]*}"
    })
    public String forwardToIndex(HttpServletRequest request) {
        if (request.getRequestURI().startsWith("/api/")) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return "forward:/index.html";
    }
}
