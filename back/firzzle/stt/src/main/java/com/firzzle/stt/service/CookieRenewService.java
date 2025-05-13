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


// 클라우드 상에서 요청할 경우 youtube 보안에 막히는 문제를 해결하기 위한 서비스 
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

    @Scheduled(cron = "0 0 2 * * *")
    public void renewCookies() {
        log.info("▶️ YouTube 쿠키 자동 재발급 시작");

        if (ytId == null || ytPw == null) {
            log.error("❌ 환경 변수 누락: ytId={}, ytPw={}", ytId, ytPw);
            return;
        }

        try (Playwright playwright = Playwright.create()) {
            log.debug("✅ Playwright 인스턴스 생성 완료");

            BrowserType.LaunchOptions launchOpts = new BrowserType.LaunchOptions()
                .setHeadless(true)
                .setArgs(List.of(
                    "--disable-blink-features=AutomationControlled",
                    "--no-sandbox",
                    "--disable-setuid-sandbox",
                    "--disable-dev-shm-usage",
                    "--disable-gpu",
                    "--no-zygote"
                ));
            log.debug("✅ 브라우저 런치 옵션 구성 완료");

            Browser browser = playwright.chromium().launch(launchOpts);
            log.debug("✅ 브라우저 런치 성공");

            BrowserContext context = browser.newContext(new Browser.NewContextOptions()
                .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                              "AppleWebKit/537.36 (KHTML, like Gecko) " +
                              "Chrome/120.0.0.0 Safari/537.36")
            );
            Page page = context.newPage();
            page.setDefaultTimeout(60_000);
            page.setDefaultNavigationTimeout(60_000);

            log.debug("➡️ 구글 로그인 페이지 접근 중...");
            page.navigate("https://accounts.google.com/signin/v2/identifier?service=youtube");
            page.waitForLoadState(LoadState.NETWORKIDLE);
            log.debug("✅ 로그인 페이지 로딩 완료");

            log.debug("➡️ 이메일 입력 중...");
            page.waitForSelector("input[type=\"email\"]", new Page.WaitForSelectorOptions().setState(WaitForSelectorState.VISIBLE));
            page.fill("input[type=\"email\"]", ytId);
            log.debug("✅ 이메일 입력 완료");

            page.waitForSelector("#identifierNext", new Page.WaitForSelectorOptions().setState(WaitForSelectorState.VISIBLE));
            page.click("#identifierNext");
            log.debug("✅ 다음 버튼 클릭 완료 (이메일)");

            log.debug("➡️ 비밀번호 입력 대기 중...");
            page.waitForSelector("input[type=\"password\"]", new Page.WaitForSelectorOptions().setState(WaitForSelectorState.VISIBLE));
            page.fill("input[type=\"password\"]", ytPw);
            log.debug("✅ 비밀번호 입력 완료");

            page.waitForSelector("#passwordNext", new Page.WaitForSelectorOptions().setState(WaitForSelectorState.VISIBLE));
            page.click("#passwordNext");
            log.debug("✅ 다음 버튼 클릭 완료 (비밀번호)");

            log.debug("➡️ YouTube 메인 페이지 로딩 대기 중...");
            page.waitForURL("https://www.youtube.com/*", new Page.WaitForURLOptions().setTimeout(60_000));
            log.debug("✅ YouTube 페이지 접근 성공");

            List<Cookie> cookies = context.cookies();
            log.debug("✅ 쿠키 추출 완료. 총 {}개", cookies.size());

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

            log.debug("✅ 쿠키 변환 완료, 저장 경로: {}", COOKIE_PATH);

            Files.createDirectories(COOKIE_PATH.getParent());
            Files.write(COOKIE_PATH, lines, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            log.debug("✅ 쿠키 파일 저장 완료");

            try {
                log.debug("🔒 파일 권한 설정 (chmod 600)");
                Process chmodProcess = new ProcessBuilder("chmod", "600", COOKIE_PATH.toString()).start();
                int chmodExit = chmodProcess.waitFor();
                log.debug("✅ chmod 결과 코드: {}", chmodExit);
            } catch (IOException e) {
                log.warn("⚠️ chmod 명령 실행 실패: {}", e.getMessage());
            }

            log.info("✅ 쿠키 저장 완료: {}", COOKIE_PATH);
            browser.close();
        } catch (IOException | InterruptedException | PlaywrightException e) {
            log.error("❌ 쿠키 재발급 실패", e);
        }
    }
}
