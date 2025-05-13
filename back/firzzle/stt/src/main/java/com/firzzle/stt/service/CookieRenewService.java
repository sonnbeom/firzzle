package com.firzzle.stt.service;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.Cookie;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;

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
            BrowserType.LaunchOptions launchOpts = new BrowserType.LaunchOptions()
                .setHeadless(false)
                .setArgs(List.of(
                    "--disable-blink-features=AutomationControlled",
                    "--no-sandbox",
                    "--disable-setuid-sandbox",
                    "--disable-dev-shm-usage",
                    "--disable-gpu",
                    "--no-zygote"
                ));

            Browser browser = playwright.chromium().launch(launchOpts);
            BrowserContext context = browser.newContext(new Browser.NewContextOptions()
                .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                              "AppleWebKit/537.36 (KHTML, like Gecko) " +
                              "Chrome/120.0.0.0 Safari/537.36")
            );
            Page page = context.newPage();

            // 기본 타임아웃 연장 (60초)
            page.setDefaultTimeout(60_000);
            page.setDefaultNavigationTimeout(60_000);

            // 1) 로그인 페이지 접근
            page.navigate("https://accounts.google.com/signin/v2/identifier?service=youtube");
            page.waitForLoadState(LoadState.NETWORKIDLE);

            // 2) 이메일 입력 및 다음 클릭
            page.waitForSelector("input[type=\"email\"]", new Page.WaitForSelectorOptions().setState(WaitForSelectorState.VISIBLE));
            page.fill("input[type=\"email\"]", ytId);
            page.waitForSelector("#identifierNext", new Page.WaitForSelectorOptions().setState(WaitForSelectorState.VISIBLE));
            page.click("#identifierNext");

            // 3) 비밀번호 입력 및 다음 클릭
            page.waitForSelector("input[type=\"password\"]", new Page.WaitForSelectorOptions().setState(WaitForSelectorState.VISIBLE));
            page.fill("input[type=\"password\"]", ytPw);
            page.waitForSelector("#passwordNext", new Page.WaitForSelectorOptions().setState(WaitForSelectorState.VISIBLE));
            page.click("#passwordNext");

            // 4) YouTube 메인 페이지 로딩 대기/
            page.waitForURL("https://www.youtube.com/*", new Page.WaitForURLOptions().setTimeout(60_000));

            // 5) 쿠키 가져오기
            List<Cookie> cookies = context.cookies();

            // 6) yt-dlp 호환 Netscape 쿠키 포맷으로 저장
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

            Files.createDirectories(COOKIE_PATH.getParent());
            Files.write(COOKIE_PATH, lines, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            // 7) 파일 권한 제한 (chmod 600)
            new ProcessBuilder("chmod", "600", COOKIE_PATH.toString()).start().waitFor();

            log.info("✅ 쿠키 저장 완료: {}", COOKIE_PATH);
            browser.close();
        } catch (IOException | InterruptedException | PlaywrightException e) {
            log.error("❌ 쿠키 재발급 실패", e);
        }
    }
}
