package com.firzzle.stt.service;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.Cookie;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CookieRenewService {
    private static final Logger log = LoggerFactory.getLogger(CookieRenewService.class);
    private static final Path COOKIE_PATH = Paths.get("/data/firzzle/uploads/cookies.txt");

    @Value("${app.youtube.credentials.id}")
    private String ytId;

    @Value("${app.youtube.credentials.pw}")
    private String ytPw;

    @PostConstruct
    public void runOnceOnStartup() {
        log.info("▶️ [초기 실행] 쿠키 재발급 작업 시작");
        renewCookies();
    }

    @Scheduled(cron = "0 0 2 * * *") // 매일 새벽 02시 (KST) 실행
    public void renewCookies() {
        log.info("▶️ YouTube 쿠키 자동 재발급 시작");

        if (ytId == null || ytPw == null) {
            log.error("❌ 환경 변수 누락: ytId={}, ytPw={}", ytId, ytPw);
            return;
        }

        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium()
                .launch(new BrowserType.LaunchOptions().setHeadless(true));
            BrowserContext context = browser.newContext();
            Page page = context.newPage();

            // 1) 로그인 페이지 접근 및 계정 입력.
            page.navigate("https://accounts.google.com/signin/v2/identifier?service=youtube");
            page.waitForSelector("input[type=\"email\"]").fill(ytId);
            page.click("button:has-text(\"다음\")");

            // 2) 비밀번호 입력
            page.waitForSelector("input[type=\"password\"]", new Page.WaitForSelectorOptions().setTimeout(15000));
            page.fill("input[type=\"password\"]", ytPw);
            page.click("button:has-text(\"다음\")");

            // 3) YouTube 메인 페이지까지 이동 완료 대기
            page.waitForURL("https://www.youtube.com/*", new Page.WaitForURLOptions().setTimeout(60_000));

            // 4) 쿠키 가져오기
            List<Cookie> cookies = context.cookies();

            // 5) yt-dlp 호환 쿠키 포맷으로 저장.
            List<String> lines = cookies.stream()
                .map(c -> String.format("%s\t%s\t%s\t%s\t%d\t%s\t%s",
                    c.domain.startsWith(".") ? "TRUE" : "FALSE",
                    c.domain,
                    "/",
                    c.secure != null && c.secure ? "TRUE" : "FALSE",
                    c.expires != null ? c.expires.longValue() : 0,
                    c.name,
                    c.value
                ))
                .collect(Collectors.toList());

            // 디렉토리 생성 및 저장
            Files.createDirectories(COOKIE_PATH.getParent());
            Files.write(COOKIE_PATH, lines, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            // 파일 권한 제한
            Process chmod = new ProcessBuilder("chmod", "600", COOKIE_PATH.toString()).start();
            chmod.waitFor();

            log.info("✅ 쿠키 저장 완료: {}", COOKIE_PATH);
            browser.close();
        } catch (IOException | InterruptedException | PlaywrightException e) {
            log.error("❌ 쿠키 재발급 실패", e);
        }
    }
}
