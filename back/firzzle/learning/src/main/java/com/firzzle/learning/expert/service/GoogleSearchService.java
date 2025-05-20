package com.firzzle.learning.expert.service;

import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Class Name : GoogleSearchService.java
 * @Description : Google 검색 서비스
 * @author Firzzle
 * @since 2025. 5. 18.
 */
@Service
public class GoogleSearchService {

    private final Logger logger = LoggerFactory.getLogger(GoogleSearchService.class);

    private static final String GOOGLE_SEARCH_URL = "https://www.google.com/search?q=";
    // 더 포괄적인 LinkedIn URL 패턴으로 변경
    private static final Pattern LINKEDIN_URL_PATTERN = Pattern.compile("https://(www\\.|kr\\.)?linkedin\\.com/in/[\\w\\-%.]+(/)?");

    @Value("${linkedin.crawler.user-agent:Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/113.0.0.0 Safari/537.36}")
    private String userAgent;

    private final RestTemplate restTemplate = new RestTemplate();

    // 문제가 있는 URL을 기록하기 위한 리스트
    private final List<String> problematicUrls = new ArrayList<>();

    /**
     * Google 검색을 통해 LinkedIn 프로필 URL 목록을 수집합니다.
     * 이중 인코딩된 URL을 완전히 디코딩하고, 디코딩이 완료되지 않은 URL은 결과에 포함하지 않습니다.
     *
     * @param keyword 검색 키워드
     * @param limit 수집할 URL 수
     * @return LinkedIn 프로필 URL 목록 (완전히 디코딩된 URL만 포함)
     */
    public List<String> searchLinkedInProfilesDynamic(String keyword, int limit) {
        List<String> linkedInUrls = new ArrayList<>();
        Set<String> processedUrls = new HashSet<>(); // 중복 처리 방지용 세트
        WebDriver driver = null;
        limit = 20;
        try {
            // WebDriver 설정
            WebDriverManager.chromedriver().setup();
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--headless");
            options.addArguments("--disable-gpu");
            options.addArguments("--window-size=1920,1080");
            options.addArguments("--user-agent=" + userAgent);

            // 자동화 감지 우회를 위한 설정
            options.addArguments("--disable-blink-features=AutomationControlled");
            options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});
            options.setExperimentalOption("useAutomationExtension", false);

            driver = new ChromeDriver(options);

            // 자동화 감지 우회를 위한 웹드라이버 설정
            ((JavascriptExecutor) driver).executeScript(
                    "Object.defineProperty(navigator, 'webdriver', {get: () => undefined})");

            // 검색 쿼리 구성 (더 효과적인 검색을 위해 쿼리 최적화)
            // site: 연산자를 사용하여 LinkedIn 프로필 직접 검색
            String searchQuery = keyword + " site:linkedin.com/in/ OR \"kr linkedin com in\"";
            String encodedQuery = URLEncoder.encode(searchQuery, StandardCharsets.UTF_8.toString());
            String searchUrl = GOOGLE_SEARCH_URL + encodedQuery + "&num=100";
            logger.info("Google 검색 URL: {}", searchUrl);

            // Google 검색 결과 페이지 로드
            driver.get(searchUrl);

            // 페이지 완전 로딩 대기
            waitForPageToLoad(driver);

            // 동적 콘텐츠 로드를 위한 스크롤 다운
            scrollToLoadAllContent(driver);

            // 페이지 소스 로깅 (처음 500자)
            logger.info("페이지 HTML: {}", driver.getPageSource().substring(0, Math.min(500, driver.getPageSource().length())));

            // 여러 페이지의 결과 처리
            processMultiplePages(driver, linkedInUrls, processedUrls, limit);

            // 혹시 충분한 URL을 찾지 못했다면 다른 방법으로도 시도
            if (linkedInUrls.size() < limit) {
                logger.info("충분한 LinkedIn URL을 찾지 못했습니다. 추가 방법 시도...");
                collectLinksFromAllMethods(driver, linkedInUrls, processedUrls, limit);
            }

            // 여전히 충분한 결과가 없다면 다른 검색 쿼리 시도
            if (linkedInUrls.size() < limit) {
                logger.info("일반 검색으로 충분한 결과를 얻지 못했습니다. 대체 검색 쿼리 시도...");
                try {
                    // 대안적인 검색 쿼리
                    String alternativeSearchQuery = keyword + " filetype:html inurl:linkedin.com/in";
                    String alternativeEncodedQuery = URLEncoder.encode(alternativeSearchQuery, StandardCharsets.UTF_8.toString());
                    String alternativeSearchUrl = GOOGLE_SEARCH_URL + alternativeEncodedQuery + "&num=100";

                    logger.info("대안 검색 URL: {}", alternativeSearchUrl);
                    driver.get(alternativeSearchUrl);
                    waitForPageToLoad(driver);
                    scrollToLoadAllContent(driver);
                    processMultiplePages(driver, linkedInUrls, processedUrls, limit);
                } catch (Exception e) {
                    logger.error("대안 검색 중 오류 발생: {}", e.getMessage(), e);
                }
            }

            // 최종 결과에서 완전히 디코딩되지 않은 URL 필터링
            List<String> fullyDecodedUrls = filterFullyDecodedUrls(linkedInUrls);
            logger.info("완전히 디코딩된 URL 수: {}/{}", fullyDecodedUrls.size(), linkedInUrls.size());

            // 완전히 디코딩된 URL만 반환
            return fullyDecodedUrls;

        } catch (Exception e) {
            logger.error("Google 동적 검색 중 오류 발생: {}", e.getMessage(), e);
        } finally {
            // WebDriver 종료
            if (driver != null) {
                driver.quit();
            }
        }

        logger.info("최종 수집된 LinkedIn URL 수: {}", linkedInUrls.size());

        // 디코딩 문제가 있는 URL 로깅
        if (!problematicUrls.isEmpty()) {
            logger.warn("디코딩 문제가 있는 URL {} 개:", problematicUrls.size());
            for (String url : problematicUrls) {
                logger.warn("문제 URL: {}", url);
            }
        }

        return new ArrayList<>(); // 오류 발생 시 빈 리스트 반환
    }

    /**
     * 완전히 디코딩된 URL만 필터링합니다.
     * URL에 '%' 문자가 남아있으면 제외합니다.
     *
     * @param urls 원본 URL 리스트
     * @return 완전히 디코딩된 URL 리스트
     */
    private List<String> filterFullyDecodedUrls(List<String> urls) {
        List<String> fullyDecodedUrls = new ArrayList<>();

        for (String url : urls) {
            // URL이 이미 완전히 디코딩되었는지 확인 ('%' 문자가 없음)
            if (!url.contains("%")) {
                fullyDecodedUrls.add(url);
                continue;
            }

            // 다시 한번 디코딩 시도
            String decodedUrl = tryToFullyDecodeUrl(url);

            // 디코딩 후에도 '%' 문자가 남아있는지 확인
            if (!decodedUrl.contains("%")) {
                fullyDecodedUrls.add(decodedUrl);
                logger.info("성공적으로 완전히 디코딩된 URL: {}", decodedUrl);
            } else {
                // 완전히 디코딩되지 않은 URL 기록
                logger.warn("디코딩 후에도 인코딩된 문자가 남아있는 URL: {}", decodedUrl);
                problematicUrls.add(decodedUrl);
            }
        }

        return fullyDecodedUrls;
    }

    /**
     * URL을 완전히 디코딩하려고 여러 번 시도합니다.
     *
     * @param url 디코딩할 URL
     * @return 디코딩된 URL
     */
    private String tryToFullyDecodeUrl(String url) {
        String decodedUrl = url;
        int maxAttempts = 3;

        for (int i = 0; i < maxAttempts; i++) {
            if (!decodedUrl.contains("%")) {
                break; // 더 이상 디코딩할 필요가 없음
            }

            try {
                String prevUrl = decodedUrl;
                decodedUrl = URLDecoder.decode(decodedUrl, StandardCharsets.UTF_8.toString());
                logger.debug("디코딩 시도 {}/{}: {} -> {}", i+1, maxAttempts, prevUrl, decodedUrl);

                // 디코딩 후 변화가 없으면 중단
                if (prevUrl.equals(decodedUrl)) {
                    break;
                }
            } catch (Exception e) {
                logger.warn("URL 디코딩 시도 {}/{} 중 오류: {}", i+1, maxAttempts, e.getMessage());
                break;
            }
        }

        return decodedUrl;
    }

    /**
     * 페이지가 완전히 로드될 때까지 대기합니다.
     */
    private void waitForPageToLoad(WebDriver driver) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
        wait.until((ExpectedCondition<Boolean>) wd ->
                ((JavascriptExecutor) wd).executeScript("return document.readyState").equals("complete"));

        // 추가 로딩 시간 부여
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 페이지 끝까지 스크롤하여 동적 콘텐츠를 로드합니다.
     */
    private void scrollToLoadAllContent(WebDriver driver) {
        JavascriptExecutor jsExecutor = (JavascriptExecutor) driver;

        // 이전 높이 저장
        long lastHeight = (long) jsExecutor.executeScript("return document.body.scrollHeight");

        // 최대 스크롤 시도 횟수
        int maxScrolls = 10;
        int scrollAttempts = 0;

        while (scrollAttempts < maxScrolls) {
            // 페이지 끝까지 스크롤
            jsExecutor.executeScript("window.scrollTo(0, document.body.scrollHeight);");

            // 새 콘텐츠 로드 대기
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // 새 높이 계산
            long newHeight = (long) jsExecutor.executeScript("return document.body.scrollHeight");

            // 높이가 같으면 더 이상 로드할 콘텐츠가 없음
            if (newHeight == lastHeight) {
                break;
            }

            lastHeight = newHeight;
            scrollAttempts++;
            logger.info("스크롤 다운 시도 {}/{}, 새 높이: {}", scrollAttempts, maxScrolls, newHeight);
        }

        // "더보기" 버튼이 있으면 클릭 (수정된 메서드 사용)
        clickMoreResultsIfPresent(driver);
    }

    /**
     * "더보기" 버튼이 있으면 클릭하여 추가 결과를 로드합니다.
     * 수정: 유효한 CSS 선택자만 사용
     */
    private void clickMoreResultsIfPresent(WebDriver driver) {
        try {
            // 유효한 CSS 선택자만 사용 (contains 사용 X)
            String[] moreResultsSelectors = {
                    "input[value='Google 검색']",
                    "button.mye4qd", // 이미지 검색에서 사용되는 선택자
                    "div.AaVjTc a" // 추가 결과 페이지 링크
            };

            for (String selector : moreResultsSelectors) {
                List<WebElement> moreButtons = driver.findElements(By.cssSelector(selector));
                if (!moreButtons.isEmpty() && moreButtons.get(0).isDisplayed()) {
                    logger.info("'더보기' 버튼 발견, 클릭 시도...");
                    moreButtons.get(0).click();
                    waitForPageToLoad(driver);
                    return;
                }
            }

            // 텍스트로 버튼 찾기 (XPath 사용)
            String[] buttonTexts = {"더보기", "더 보기", "See more", "More results"};
            for (String text : buttonTexts) {
                String xPath = "//button[contains(text(),'" + text + "')] | //a[contains(text(),'" + text + "')]";
                List<WebElement> buttons = driver.findElements(By.xpath(xPath));
                if (!buttons.isEmpty() && buttons.get(0).isDisplayed()) {
                    logger.info("XPath로 '{}' 버튼 발견, 클릭 시도...", text);
                    buttons.get(0).click();
                    waitForPageToLoad(driver);
                    return;
                }
            }
        } catch (Exception e) {
            logger.warn("'더보기' 버튼 클릭 중 오류: {}", e.getMessage());
        }
    }

    /**
     * 여러 페이지의 검색 결과를 처리합니다.
     */
    private void processMultiplePages(WebDriver driver, List<String> linkedInUrls, Set<String> processedUrls, int limit) {
        int pageCount = 1;
        int maxPages = 5; // 최대 5페이지까지만 처리

        while (linkedInUrls.size() < limit && pageCount <= maxPages) {
            logger.info("검색 결과 페이지 {} 처리 중", pageCount);

            // 현재 페이지에서 LinkedIn URL 추출
            extractLinkedInUrlsFromPageSource(driver, linkedInUrls, processedUrls, limit);

            // 결과가 충분하면 중단
            if (linkedInUrls.size() >= limit) {
                break;
            }

            // 다음 페이지 버튼 찾기
            try {
                // 다음 페이지 버튼을 id 또는 텍스트로 찾기
                WebElement nextButton = null;
                try {
                    nextButton = driver.findElement(By.id("pnnext"));
                } catch (Exception e) {
                    // id로 찾지 못한 경우 XPath로 시도
                    String xPath = "//a[contains(text(),'다음') or contains(text(),'Next')]";
                    List<WebElement> nextButtons = driver.findElements(By.xpath(xPath));
                    if (!nextButtons.isEmpty()) {
                        nextButton = nextButtons.get(0);
                    }
                }

                if (nextButton != null && nextButton.isDisplayed() && nextButton.isEnabled()) {
                    logger.info("다음 페이지로 이동합니다.");
                    nextButton.click();
                    waitForPageToLoad(driver);
                    scrollToLoadAllContent(driver);
                    pageCount++;
                } else {
                    logger.info("다음 페이지 버튼이 없거나 활성화되지 않았습니다.");
                    break;
                }
            } catch (Exception e) {
                logger.info("다음 페이지 버튼을 찾을 수 없음. 마지막 페이지에 도달했거나 오류 발생: {}", e.getMessage());
                break;
            }
        }
    }

    /**
     * 페이지 소스에서 정규식을 사용하여 LinkedIn URL을 추출합니다.
     */
    private void extractLinkedInUrlsFromPageSource(WebDriver driver, List<String> linkedInUrls, Set<String> processedUrls, int limit) {
        String pageSource = driver.getPageSource();
        Matcher matcher = LINKEDIN_URL_PATTERN.matcher(pageSource);

        while (matcher.find() && linkedInUrls.size() < limit) {
            String url = matcher.group();
            logger.info("정규식으로 찾은 LinkedIn URL: {}", url);
            addLinkToResults(linkedInUrls, processedUrls, url, limit);
        }

        // 추가적으로 웹 요소를 직접 찾는 방법 시도
        try {
            // 다양한 CSS 선택자로 링크 찾기 시도
            String[] selectors = {
                    "div.g a", // 일반 검색 결과
                    "div.yuRUbf a", // 일반 링크
                    "div.kCrYT a", // 모바일 검색 결과
                    "a[href*='linkedin.com/in']" // href에 linkedin.com/in이 포함된 모든 링크
            };

            for (String selector : selectors) {
                if (linkedInUrls.size() >= limit) break;

                List<WebElement> results = driver.findElements(By.cssSelector(selector));
                logger.info("선택자 '{}'로 발견된 링크 수: {}", selector, results.size());

                for (WebElement result : results) {
                    if (linkedInUrls.size() >= limit) break;

                    try {
                        String href = result.getAttribute("href");
                        if (href != null && href.contains("linkedin.com/in")) {
                            logger.info("검색 결과에서 LinkedIn 링크 발견: {}", href);
                            addLinkToResults(linkedInUrls, processedUrls, href, limit);
                        }
                    } catch (Exception e) {
                        logger.warn("링크 처리 중 오류: {}", e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("검색 결과에서 링크 추출 중 오류: {}", e.getMessage());
        }
    }

    /**
     * 모든 가능한 방법을 사용하여 링크를 수집합니다.
     */
    private void collectLinksFromAllMethods(WebDriver driver, List<String> linkedInUrls, Set<String> processedUrls, int limit) {
        // 1. 모든 a 태그 검색
        List<WebElement> allLinks = driver.findElements(By.tagName("a"));
        logger.info("페이지에서 발견된 총 링크 수: {}", allLinks.size());

        for (WebElement link : allLinks) {
            if (linkedInUrls.size() >= limit) break;

            try {
                String href = link.getAttribute("href");
                if (href != null && href.contains("linkedin.com/in")) {
                    logger.info("LinkedIn 링크 발견: {}", href);
                    addLinkToResults(linkedInUrls, processedUrls, href, limit);
                }
            } catch (Exception e) {
                logger.warn("링크 처리 중 오류 발생: {}", e.getMessage());
            }
        }

        // 2. Jsoup으로 정적 분석 시도
        if (linkedInUrls.size() < limit) {
            try {
                logger.info("Jsoup으로 정적 분석 시도...");
                String pageSource = driver.getPageSource();
                Document doc = Jsoup.parse(pageSource);
                Elements links = doc.select("a[href]");

                logger.info("Jsoup으로 발견된 총 링크 수: {}", links.size());
                for (Element link : links) {
                    if (linkedInUrls.size() >= limit) break;

                    String href = link.attr("href");
                    if (href != null && href.contains("linkedin.com/in")) {
                        logger.info("Jsoup으로 LinkedIn 링크 발견: {}", href);
                        addLinkToResults(linkedInUrls, processedUrls, href, limit);
                    }
                }
            } catch (Exception e) {
                logger.warn("Jsoup 분석 중 오류: {}", e.getMessage());
            }
        }

        // 3. JavaScript 실행으로 모든 href 추출 시도
        if (linkedInUrls.size() < limit) {
            try {
                logger.info("JavaScript를 사용하여 모든 링크 추출 시도...");
                String script =
                        "var links = document.getElementsByTagName('a');" +
                                "var hrefs = [];" +
                                "for(var i=0; i<links.length; i++) {" +
                                "  if(links[i].href && links[i].href.includes('linkedin.com/in')) {" +
                                "    hrefs.push(links[i].href);" +
                                "  }" +
                                "}" +
                                "return hrefs;";

                @SuppressWarnings("unchecked")
                ArrayList<String> jsLinks = (ArrayList<String>) ((JavascriptExecutor) driver).executeScript(script);

                logger.info("JavaScript로 발견된 LinkedIn 링크 수: {}", jsLinks.size());
                for (String href : jsLinks) {
                    if (linkedInUrls.size() >= limit) break;
                    addLinkToResults(linkedInUrls, processedUrls, href, limit);
                }
            } catch (Exception e) {
                logger.warn("JavaScript 링크 추출 중 오류: {}", e.getMessage());
            }
        }
    }

    /**
     * URL을 결과 목록에 추가합니다 (중복 체크 포함).
     * 이중 인코딩된 URL을 완전히 디코딩합니다.
     */
    private void addLinkToResults(List<String> linkedInUrls, Set<String> processedUrls, String href, int limit) {
        if (href == null || href.isEmpty()) {
            return;
        }

        try {
            // 디버깅을 위해 URL 원본 기록
            logger.debug("처리 시작 URL: {}", href);

            // 모든 종류의 Google 리디렉션 URL 감지
            boolean isGoogleRedirect = href.contains("/url?") ||
                    href.contains("google.com/url") ||
                    (href.contains("www.google.") && href.contains("url="));

            if (isGoogleRedirect) {
                logger.info("Google 리디렉션 URL 감지: {}", href);

                // 추출 방법 강화
                String extractedUrl = extractUrlFromGoogleRedirect(href);
                if (extractedUrl != null && !extractedUrl.isEmpty()) {
                    href = extractedUrl;
                    logger.info("추출된 URL: {}", href);
                }
            }

            // 불필요한 매개변수 제거
            int paramIndex = href.indexOf("?");
            if (paramIndex > 0) {
                href = href.substring(0, paramIndex);
                logger.debug("매개변수 제거 후 URL: {}", href);
            }

            // URL 정규화 및 최종 검증
            href = sanitizeAndNormalizeUrl(href);

            // 이중 인코딩 URL을 완전히 디코딩
            String decodedUrl = tryToFullyDecodeUrl(href);

            // 디코딩 후에도 '%' 문자가 포함되어 있는지 확인
            // (이중 인코딩된 URL이 아닌, 단순히 '%' 문자를 포함하는 경우도 있으므로 로깅만 함)
            if (decodedUrl.contains("%")) {
                logger.warn("완전히 디코딩되지 않은 URL: {}", decodedUrl);
                problematicUrls.add(decodedUrl);
            }

            // 정규화된 URL로 중복 체크
            String normalizedUrl = normalizeLinkedInUrl(decodedUrl);

            // 이전에 처리한 URL인지 확인
            if (processedUrls.contains(normalizedUrl)) {
                logger.debug("이미 처리된 URL 건너뜀: {}", normalizedUrl);
                return;
            }

            // 처리 완료 표시
            processedUrls.add(normalizedUrl);

            // 추가할 URL이 유효한 LinkedIn 프로필 URL인지 확인
            if (normalizedUrl.contains("linkedin.com/in") && linkedInUrls.size() < limit) {
                linkedInUrls.add(decodedUrl);
                logger.info("결과 목록에 추가된 LinkedIn URL: {}", decodedUrl);
            } else if (!normalizedUrl.contains("linkedin.com/in")) {
                logger.debug("유효하지 않은 LinkedIn URL 형식, 건너뜀: {}", normalizedUrl);
            }

        } catch (Exception e) {
            logger.error("URL 처리 중 예상치 못한 오류 발생: {}", e.getMessage(), e);
        }
    }

    /**
     * Google 리디렉션 URL에서 실제 URL을 추출합니다.
     * 다양한 방법을 시도합니다.
     */
    private String extractUrlFromGoogleRedirect(String googleUrl) {
        if (googleUrl == null || googleUrl.isEmpty()) {
            return null;
        }

        String extractedUrl = null;

        // 방법 1: 정규식으로 추출
        Pattern urlPattern = Pattern.compile("[?&]url=([^&]+)");
        Matcher urlMatcher = urlPattern.matcher(googleUrl);

        if (urlMatcher.find()) {
            extractedUrl = urlMatcher.group(1);
            logger.debug("방법 1 (정규식) 추출 URL: {}", extractedUrl);
        }

        // 방법 2: 문자열 파싱
        if (extractedUrl == null || extractedUrl.isEmpty()) {
            int startIndex = googleUrl.indexOf("url=");
            if (startIndex >= 0) {
                extractedUrl = googleUrl.substring(startIndex + 4);
                int endIndex = extractedUrl.indexOf("&");
                if (endIndex >= 0) {
                    extractedUrl = extractedUrl.substring(0, endIndex);
                }
                logger.debug("방법 2 (문자열 파싱) 추출 URL: {}", extractedUrl);
            }
        }

        // 방법 3: URLComponents 파싱
        if (extractedUrl == null || extractedUrl.isEmpty()) {
            try {
                java.net.URL url = new java.net.URL(googleUrl);
                String query = url.getQuery();
                if (query != null && !query.isEmpty()) {
                    String[] params = query.split("&");
                    for (String param : params) {
                        if (param.startsWith("url=")) {
                            extractedUrl = param.substring(4);
                            logger.debug("방법 3 (URLComponents) 추출 URL: {}", extractedUrl);
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                logger.warn("URL 파싱 오류: {}", e.getMessage());
            }
        }

        // 추출된 URL이 있으면 디코딩 시도
        if (extractedUrl != null && !extractedUrl.isEmpty()) {
            return tryToFullyDecodeUrl(extractedUrl);
        }

        return null;
    }

    /**
     * URL을 정리하고 정규화합니다.
     */
    private String sanitizeAndNormalizeUrl(String url) {
        if (url == null || url.isEmpty()) {
            return url;
        }

        // 특수 문자 정리
        String sanitized = url
                .replace("\\", "/")
                .replace(" ", "%20");

        // 프로토콜 확인 및 추가
        if (!sanitized.startsWith("http://") && !sanitized.startsWith("https://")) {
            sanitized = "https://" + sanitized;
        }

        // 올바른 URL 확인
        try {
            new java.net.URL(sanitized);
        } catch (Exception e) {
            logger.warn("유효하지 않은 URL 형식: {}", sanitized);
            return url; // 원래 URL로 돌아가기
        }

        return sanitized;
    }

    /**
     * LinkedIn URL을 정규화합니다. (대소문자 구분 없이, 끝 슬래시 제거 등)
     */
    private String normalizeLinkedInUrl(String url) {
        if (url == null) return "";

        // 소문자로 변환
        String normalized = url.toLowerCase();

        // 끝 슬래시 제거
        if (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }

        // URL의 기본 형식만 유지 (프로토콜, 도메인, 경로)
        try {
            java.net.URL parsedUrl = new java.net.URL(normalized);
            String protocol = parsedUrl.getProtocol();
            String host = parsedUrl.getHost();
            String path = parsedUrl.getPath();

            normalized = protocol + "://" + host + path;
        } catch (Exception e) {
            // URL 파싱 실패 시 원래 문자열 사용
            logger.warn("URL 정규화 중 오류: {}", e.getMessage());
        }

        return normalized;
    }

    /**
     * URL 파싱 디버깅을 위한 메서드
     */
    private void debugUrl(String href) {
        logger.info("==== URL 디버깅 시작 ====");
        logger.info("원본 URL: {}", href);

        // 1. URL 구성 요소 분석
        try {
            java.net.URL parsedUrl = new java.net.URL(href);
            logger.info("프로토콜: {}", parsedUrl.getProtocol());
            logger.info("호스트: {}", parsedUrl.getHost());
            logger.info("경로: {}", parsedUrl.getPath());
            logger.info("쿼리: {}", parsedUrl.getQuery());
        } catch (Exception e) {
            logger.info("URL 파싱 불가: {}", e.getMessage());
        }

        // 2. 디코딩 시도
        try {
            String decoded = URLDecoder.decode(href, StandardCharsets.UTF_8.toString());
            logger.info("1차 디코딩 결과: {}", decoded);

            if (!decoded.equals(href) && decoded.contains("%")) {
                try {
                    String doubleDecoded = URLDecoder.decode(decoded, StandardCharsets.UTF_8.toString());
                    logger.info("2차 디코딩 결과: {}", doubleDecoded);

                    if (!doubleDecoded.equals(decoded) && doubleDecoded.contains("%")) {
                        try {
                            String tripleDecoded = URLDecoder.decode(doubleDecoded, StandardCharsets.UTF_8.toString());
                            logger.info("3차 디코딩 결과: {}", tripleDecoded);
                        } catch (Exception ex) {
                            logger.info("3차 디코딩 오류: {}", ex.getMessage());
                        }
                    }
                } catch (Exception ex) {
                    logger.info("2차 디코딩 오류: {}", ex.getMessage());
                }
            }
        } catch (Exception e) {
            logger.info("디코딩 오류: {}", e.getMessage());
        }

        // 3. Google 리디렉션 URL 여부 확인
        boolean isGoogleRedirect = href.contains("/url?") ||
                href.contains("google.com/url") ||
                href.contains("www.google.") && href.contains("url=");
        logger.info("Google 리디렉션 URL 여부: {}", isGoogleRedirect);

        if (isGoogleRedirect) {
            String extracted = extractUrlFromGoogleRedirect(href);
            logger.info("추출된 URL: {}", extracted);
        }

        // 4. 수동 디코딩 시도
        if (href.contains("%")) {
            String manualDecoded = href
                    .replace("%3A", ":")
                    .replace("%2F", "/")
                    .replace("%3F", "?")
                    .replace("%3D", "=")
                    .replace("%26", "&")
                    .replace("%25", "%");
            logger.info("수동 디코딩 결과: {}", manualDecoded);
        }

        logger.info("==== URL 디버깅 종료 ====");
    }

    /**
     * 특정 URL 문제 테스트
     */
    public void testProblemUrl(String url) {
        logger.info("===== 문제 URL 테스트 시작 =====");
        logger.info("테스트 URL: {}", url);

        // 1. 이중 인코딩 테스트
        String decodedUrl = tryToFullyDecodeUrl(url);
        logger.info("완전 디코딩 결과: {}", decodedUrl);
        logger.info("완전히 디코딩됨: {}", !decodedUrl.contains("%"));

        // 2. 디버깅 정보 출력
        debugUrl(url);

        logger.info("===== 문제 URL 테스트 종료 =====");
    }
}