package com.firzzle.main.test;

import com.firzzle.common.logging.dto.LogEvent;
import com.firzzle.common.logging.service.LoggingService;
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
        LoggingService.log(LogEvent.userAction("test_user_id","test_action"));
        return "테스트 호출 - 메인 v1 서버";
    }
}
