package com.firzzle.main.test;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1")
public class MainTestController {

    @GetMapping("/test")
    public String test(){
        return "테스트 호출 - 메인 v1 서버";
    }
}
