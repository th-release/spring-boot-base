package com.threlease.base.common.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

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
    public String forwardToIndex() {
        return "forward:/index.html";
    }
}
