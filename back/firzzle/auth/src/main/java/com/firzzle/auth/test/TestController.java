package com.firzzle.auth.test;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RestController
@RequiredArgsConstructor
public class TestController {
    @GetMapping("/test")
    public String test(){
        return "테스트 호출 - 인증 서버";
    }
}
