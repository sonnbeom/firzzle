package com.firzzle.stt.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.firzzle.stt.service.SttService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/stt")
@RequiredArgsConstructor
public class SttController {

    private final SttService sttService;
    
    @PostMapping("/transcribe")
    public ResponseEntity<?> transcribe(@RequestParam("file") MultipartFile file) throws Exception{
        return ResponseEntity.ok(sttService.transcribeFromFile(file));
    }

    @PostMapping("/transcribeByUrl")
    public ResponseEntity<?> transcribeByYoutubeUrl(@RequestParam("url") String youtubeUrl) throws Exception{
        return ResponseEntity.ok(sttService.transcribeFromYoutube(youtubeUrl));
    }
}
