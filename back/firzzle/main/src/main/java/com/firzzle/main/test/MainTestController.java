package com.firzzle.main.test;

import com.firzzle.common.logging.dto.LogEvent;
import com.firzzle.common.logging.service.LoggingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.firzzle.common.logging.dto.LogEvent.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class MainTestController {

    @GetMapping("/test")
    public String test(){
        long start = System.currentTimeMillis();
        long duration = System.currentTimeMillis() - start;
        LoggingService.log(apiPerformance("test_user_id", "test_path", duration));
        return "테스트 호출 - 메인 v1 서버";
    }
}
