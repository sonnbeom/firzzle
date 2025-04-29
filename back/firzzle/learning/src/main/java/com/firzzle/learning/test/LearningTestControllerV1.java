package com.firzzle.learning.test;

import com.firzzle.learning.kafka.producer.LearningProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1")
public class LearningTestControllerV1 {
    @GetMapping("/test")
    public String test(){
        return "테스트 호출 - 학습 v1 서버";
    }

    private final LearningProducer learningProducer;

    // Learning → STT 서버로 메시지 전송
    @GetMapping("/stt")
    public String sendToStt(@RequestParam String message) {
        learningProducer.sendToStt(message);
        return "Message sent to STT: " + message;
    }

    // Learning → LLM 서버로 메시지 전송
    @GetMapping("/llm")
    public String sendToLlm(@RequestParam String message) {
        learningProducer.sendToLlm(message);
        return "Message sent to LLM: " + message;
    }
}
