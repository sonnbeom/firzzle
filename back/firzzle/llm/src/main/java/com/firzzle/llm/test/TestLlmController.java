package com.firzzle.llm.test;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1")
public class TestLlmController {
    @GetMapping("/test")
    public String test(){
        return "테스트 호출 - llm v1 서버";
    }

}
