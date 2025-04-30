package com.firzzle.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1")
public class AuthController {
    @GetMapping("/test")
    public String test(){
        return "테스트 호출 - 인증 v1 서버";
    }
}
