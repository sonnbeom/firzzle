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
        System.out.println("▶️ [초기 실행] 쿠키 재발급 작업 시작");
        log.info("▶️ [초기 실행] 쿠키 재발급 작업 시작");
        renewCookies();
    }

    @Scheduled(cron = "0 0 2 * * *")
    public void renewCookies() {
        System.out.println("▶️ YouTube 쿠키 자동 재발급 시작");
        log.info("▶️ YouTube 쿠키 자동 재발급 시작");

        if (ytId == null || ytPw == null) {
            System.out.println("❌ 환경 변수 누락: ytId=" + ytId + ", ytPw=" + ytPw);
            log.error("❌ 환경 변수 누락: ytId={}, ytPw={}", ytId, ytPw);
            return;
        }

        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                .setHeadless(true)
                .setArgs(List.of(
                    "--disable-blink-features=AutomationControlled",
                    "--no-sandbox",
                    "--disable-setuid-sandbox",
                    "--disable-dev-shm-usage",
                    "--disable-gpu",
                    "--no-zygote"
                ))
            );
            BrowserContext context = browser.newContext(new Browser.NewContextOptions()
                .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                              "AppleWebKit/537.36 (KHTML, like Gecko) " +
                              "Chrome/120.0.0.0 Safari/537.36")
            );
            Page page = context.newPage();
            page.setDefaultTimeout(60_000);
            page.setDefaultNavigationTimeout(60_000);

            // 로그인 단계
            System.out.println("➡️ 로그인 페이지 접근 중...");
            page.navigate("https://accounts.google.com/signin/v2/identifier?service=youtube");
            page.waitForLoadState(LoadState.NETWORKIDLE);

            page.waitForSelector("input[type=\"email\"]", new Page.WaitForSelectorOptions().setState(WaitForSelectorState.VISIBLE));
            page.fill("input[type=\"email\"]", ytId);
            page.click("#identifierNext");

            page.waitForSelector("input[type=\"password\"]", new Page.WaitForSelectorOptions().setState(WaitForSelectorState.VISIBLE));
            page.fill("input[type=\"password\"]", ytPw);
            page.click("#passwordNext");

            // 🔍 2단계 인증 감지
            String currentUrl = page.url();
            String title = page.title().toLowerCase();

            System.out.println("🔍 로그인 이후 현재 URL: " + currentUrl);
            System.out.println("🔍 로그인 이후 페이지 제목: " + title);

            boolean is2FA = false;

            if (currentUrl.contains("/challenge")) {
                System.out.println("⚠️ 2단계 인증 URL 감지됨: " + currentUrl);
                log.warn("⚠️ 2FA URL 감지: {}", currentUrl);
                is2FA = true;
            }

            if (title.contains("인증") || title.contains("코드") || title.contains("확인") || title.contains("보안")) {
                System.out.println("⚠️ 2단계 인증 Title 감지됨: " + title);
                log.warn("⚠️ 2FA Title 감지: {}", title);
                is2FA = true;
            }

            if (page.isVisible("input[name=\"idvAnyPhonePin\"]") || page.isVisible("input[name=\"idvPin\"]")) {
                System.out.println("⚠️ 2단계 인증 입력창 감지됨");
                log.warn("⚠️ 2FA 입력 필드 감지됨");
                is2FA = true;
            }

            if (is2FA) {
                System.out.println("❌ 자동 로그인 실패: 2단계 인증이 활성화되어 있습니다.");
                log.error("❌ 쿠키 재발급 중단 - 2FA 감지됨");
                return;
            }

            // 유튜브 진입 감지
            page.waitForURL("https://www.youtube.com/*", new Page.WaitForURLOptions().setTimeout(60_000));
            System.out.println("✅ YouTube 페이지 접근 성공");

            List<Cookie> cookies = context.cookies();
            System.out.println("✅ 쿠키 추출 완료. 총 " + cookies.size() + "개");

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
            System.out.println("✅ 쿠키 저장 완료: " + COOKIE_PATH);

            try {
                Process chmodProcess = new ProcessBuilder("chmod", "600", COOKIE_PATH.toString()).start();
                int chmodExit = chmodProcess.waitFor();
                System.out.println("🔒 파일 권한 설정 완료 (chmod 600), 결과 코드: " + chmodExit);
            } catch (IOException e) {
                System.out.println("⚠️ chmod 실패: " + e.getMessage());
            }

            browser.close();
        } catch (IOException | InterruptedException | PlaywrightException e) {
            System.out.println("❌ 쿠키 재발급 실패: " + e.getMessage());
            log.error("❌ 쿠키 재발급 실패", e);
        }
    }
}
