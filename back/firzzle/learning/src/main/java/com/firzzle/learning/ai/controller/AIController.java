package com.firzzle.learning.ai.controller;

import com.firzzle.common.exception.BusinessException;
import com.firzzle.common.exception.ErrorCode;
import com.firzzle.common.library.DataBox;
import com.firzzle.common.library.RequestBox;
import com.firzzle.common.library.RequestManager;
import com.firzzle.learning.ai.dto.*;
import com.firzzle.learning.ai.service.AiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Class Name : AIController.java
 * @Description : GPT API 컨트롤러
 * @author Firzzle
 * @since 2025. 5. 9.
 */
@Tag(name = "GPT API", description = "GPT API")
@RestController
@RequiredArgsConstructor
public class AIController {

    private static final Logger logger = LoggerFactory.getLogger(AIController.class);

    private final AiService service;

    // Spring Boot 3.x 스타일로 코드 현대화
    @Autowired
    private RestTemplateBuilder restTemplateBuilder;


    @Value("${openai.api.key}")
    private String apiKey;

    @Operation(summary = "GPT Chat API",
            description = "1. RequestBody에 담겨온 paramerter 및 대화 내역을 불러와서 OpenAPI 로 전송합니다." +
                    "2. seq 에 담겨온 시퀀스로 Prompt 조회해서 메세지에 담는다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "API 요청 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping(value = "/api/v1/gpt/chat/")
    public ResponseEntity<String> askGPT(HttpServletRequest request,
                                         @Parameter(description = "GPT 채팅 요청 정보", required = true)
                                         @Valid @RequestBody GPTChatRequestDTO requestDTO) throws Exception {
        try {
            RequestBox box = RequestManager.getBox(request);
            // OpenAI API URL
            String url = "https://api.openai.com/v1/chat/completions";

            //Parameter 셋팅(필수)
            String model = requestDTO.getModel();
            box.put("p_seq", requestDTO.getSeq());

            // 프롬프트가 여러개 있을경우 순번 파라메터를 추가로 받는다.
            if (requestDTO.getSeqNum() != null) {
                box.put("p_seq_num", requestDTO.getSeqNum());
            }

            List<Map<String, Object>> conversationHistory = requestDTO.getMessages();
            Map<String, Object> placeholders = requestDTO.getPlaceholders(); //치환문자 (system, user, Assistant 3개들어올수있음)

            //API KEY db에서 땡겨와서 처리해야한다.
            // => fb_member 테이블로 관리하지 않기 때문에 현재 사용 불가
//            DataBox apiKeyMap = service.selectApiKey(box);
//            String apiKey = apiKeyMap.getString("d_api_key");
            
            String apiKey = this.apiKey;
            //Prompt 조회 (system, user, Assistant 조회해온다 각 seq 마다 )
            //db조회해와서 알아서 있으면 셋팅하고 없으면 셋팅안함
            //TODO 프롬프트 없이 단순 요청일때 문제있을수있다. 요청이 여러건일때
            DataBox dboxPrompt = service.selectAiPrompt(box);

            // HTTP 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);
            headers.set("Accept-Charset", "UTF-8");

            JSONArray messages = new JSONArray();
            // db에 등록된 프롬프트를 있는것들만 처리해서 만든다.
            if (Objects.nonNull(dboxPrompt) && !dboxPrompt.isEmpty()) {

                if (Objects.nonNull(placeholders) && !placeholders.isEmpty()) {
                    //TOdo dboxPrompt 보내서 처리한다음에 다시 받아오자 치환한거를
                    processTemplate(dboxPrompt, placeholders);
                }
                String[] roleArr = {"system", "assistant", "user"};
                for (String roleStr : roleArr) {
                    if (dboxPrompt.containsKey("d_" + roleStr)) {
                        JSONObject prompt = new JSONObject();
                        prompt.put("role", roleStr);
                        prompt.put("content", dboxPrompt.getString("d_" + roleStr));
                        messages.put(prompt);
                    }
                }
            }
            if (Objects.nonNull(conversationHistory) && !conversationHistory.isEmpty()) {
                // 사용자쪽에서 넘어온 대화대역
                JSONArray fullHistory = new JSONArray(conversationHistory);
                for (int i = 0; i < fullHistory.length(); i++) {
                    messages.put(fullHistory.get(i));
                }
            }

            //추가적인 변수들 동적 삽입
            String[] hyperparameters = {"temperature", "max_tokens", "top_p", "frequency_penalty"
                    , "presence_penalty", "stop", "logit_bias", "response_format"};
            // 요청 바디 구성
            JSONObject body = new JSONObject();

            body.put("model", model); //필수요소
            body.put("messages", messages); //필수요소

            // 하이퍼파라미터 추가
            addHyperparametersToBody(body, requestDTO);

            // RestTemplate을 커스터마이징하여 SSLHandshake 오류 해결
            RestTemplate restTemplate = gptRestTemplat();
            // HTTP 요청 전송
            HttpEntity<String> entity = new HttpEntity<>(body.toString(), headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            //로깅처리
            service.insertApiLog(box, body.toString(), response, "chat");

            // 응답 반환
            return response;

        } catch (Exception e) {
            logger.error("GPT 채팅 API 요청 중 예외 발생: {}", e.getMessage(), e);
            throw e;
            // 예외 처리
            //return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    /**
     * 오디오 파일을 Whisper API를 통해 전사하는 엔드포인트
     * 기본 모델은 whisper-1이며, 선택적으로 언어 파라미터를 받을 수 있습니다.
     */
    @Operation(summary = "오디오 변환 API", description = "Whisper API를 사용하여 오디오 파일을 텍스트로 변환합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "API 요청 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping(value = "/api/v1/gpt/audio/", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> transcribeAudio(
            @Parameter(description = "오디오 파일", required = true) @RequestPart("file") MultipartFile audioFile,
            @Parameter(description = "시퀀스 번호") @RequestParam(value = "seq", required = false) String seq,
            @Parameter(description = "사용할 모델명", schema = @io.swagger.v3.oas.annotations.media.Schema(defaultValue = "whisper-1"))
            @RequestParam(value = "model", defaultValue = "whisper-1") String model,
            @Parameter(description = "언어 설정") @RequestParam(value = "language", required = false) String language,
            HttpServletRequest request
    ) throws Exception {

        try {
            // RequestBox 세팅 (로그 및 추후 활용)
            RequestBox box = RequestManager.getBox(request);
            box.put("p_seq", seq);

            // API KEY 조회 (DB 등에서 가져온다고 가정)
            // => fb_member 테이블로 관리하지 않기 때문에 현재 사용 불가
//            DataBox apiKeyMap = service.selectApiKey(box);
//            String apiKey = apiKeyMap.getString("d_api_key");
            String url = "https://api.openai.com/v1/audio/transcriptions";
            // HTTP 헤더 설정: multipart/form-data, 인증 헤더 포함
            HttpHeaders headers = new HttpHeaders();
            //headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.set("Authorization", "Bearer " + apiKey);

            // 요청 본문 구성: MultiValueMap 사용
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

            // MultipartFile을 ByteArrayResource로 감싸서 전송 (파일 필드 이름은 "file")
            ByteArrayResource fileResource = new ByteArrayResource(audioFile.getBytes()) {
                @Override
                public String getFilename() {
                    return audioFile.getOriginalFilename();
                }
            };
            body.add("file", fileResource);
            body.add("model", model);
            if (Objects.nonNull(language) && !language.isEmpty()) {
                body.add("language", language);
            }

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            // SSL 설정이 포함된 RestTemplate 생성
            RestTemplate restTemplate = gptRestTemplat();
            ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);

            // API 호출 로그 기록 (필요시)
            service.insertApiLog(box, body.toString(), response, "audio");

            return response;
        } catch (BusinessException e) {
            logger.error("오디오 변환 API 요청 중 비즈니스 예외 발생: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("오디오 변환 API 요청 중 예외 발생: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "오디오 변환 API 요청 중 오류가 발생했습니다.");
            // 예외 처리
            //return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    @Operation(summary = "이미지 생성 API", description = "DALL-E API를 사용하여 프롬프트 기반의 이미지를 생성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "API 요청 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping(value = "/api/v1/gpt/image/")
    public ResponseEntity<String> generateImage(HttpServletRequest request,
                                                @Parameter(description = "이미지 생성 요청 정보", required = true)
                                                @Valid @RequestBody ImageGenerationRequestDTO requestDTO) throws Exception {
        try {
            RequestBox box = RequestManager.getBox(request);
            // OpenAI API URL
            String url = "https://api.openai.com/v1/images/generations";

            //Parameter 셋팅(필수)
            String model = requestDTO.getModel();
            box.put("p_seq", requestDTO.getSeq());

            //API KEY db에서 땡겨와서 처리해야한다.
            // => fb_member 테이블로 관리하지 않기 때문에 현재 사용 불가
//            DataBox apiKeyMap = service.selectApiKey(box);
//            String apiKey = apiKeyMap.getString("d_api_key");

            //Prompt 조회 (system, user, Assistant 조회해온다 각 seq 마다 )

            // HTTP 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);
            headers.set("Accept-Charset", "UTF-8");

            // 요청 바디 구성
            JSONObject body = new JSONObject();
            body.put("model", model); //필수요소

            // 파라미터 추가
            if (requestDTO.getPrompt() != null) body.put("prompt", requestDTO.getPrompt());
            if (requestDTO.getN() != null) body.put("n", requestDTO.getN());
            if (requestDTO.getSize() != null) body.put("size", requestDTO.getSize());
            if (requestDTO.getResponseFormat() != null) body.put("response_format", requestDTO.getResponseFormat());
            if (requestDTO.getTemperature() != null) body.put("temperature", requestDTO.getTemperature());

            // RestTemplate을 커스터마이징하여 SSLHandshake 오류 해결
            RestTemplate restTemplate = gptRestTemplat();
            // HTTP 요청 전송
            HttpEntity<String> entity = new HttpEntity<>(body.toString(), headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            //로깅처리
            service.insertApiLog(box, body.toString(), response, "img");

            // 응답 반환
            return response;

        } catch (BusinessException e) {
            logger.error("이미지 생성 API 요청 중 비즈니스 예외 발생: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("이미지 생성 API 요청 중 예외 발생: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "이미지 생성 API 요청 중 오류가 발생했습니다.");
            // 예외 처리
            //return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    @Operation(summary = "임베딩 API", description = "텍스트를 벡터 임베딩으로 변환합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "API 요청 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping(value = "/api/v1/gpt/embeddings/")
    public ResponseEntity<String> askGPTEmbedding(HttpServletRequest request,
                                                  @Parameter(description = "임베딩 요청 정보", required = true)
                                                  @Valid @RequestBody EmbeddingRequestDTO requestDTO) throws Exception {
        try {
            RequestBox box = RequestManager.getBox(request);
            // OpenAI API URL
            String url = "https://api.openai.com/v1/embeddings";

            //Parameter 셋팅(필수)
            String model = requestDTO.getModel();
            box.put("p_seq", requestDTO.getSeq());

            // 프롬프트가 여러개 있을경우 순번 파라메터를 추가로 받는다.
            if (requestDTO.getSeqNum() != null) {
                box.put("p_seq_num", requestDTO.getSeqNum());
            }

            List<Map<String, Object>> conversationHistory = requestDTO.getMessages();
            Map<String, Object> placeholders = requestDTO.getPlaceholders(); //치환문자 (system, user, Assistant 3개들어올수있음)

            //API KEY db에서 땡겨와서 처리해야한다.
            // => fb_member 테이블로 관리하지 않기 때문에 현재 사용 불가
//            DataBox apiKeyMap = service.selectApiKey(box);
//            String apiKey = apiKeyMap.getString("d_api_key");

            //Prompt 조회 (system, user, Assistant 조회해온다 각 seq 마다 )
            //db조회해와서 알아서 있으면 셋팅하고 없으면 셋팅안함
            //TODO 프롬프트 없이 단순 요청일때 문제있을수있다. 요청이 여러건일때
            DataBox dboxPrompt = service.selectAiPrompt(box);

            // HTTP 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);
            headers.set("Accept-Charset", "UTF-8");

            JSONArray messages = new JSONArray();
            // db에 등록된 프롬프트를 있는것들만 처리해서 만든다.
            if (Objects.nonNull(dboxPrompt) && !dboxPrompt.isEmpty()) {

                if (Objects.nonNull(placeholders) && !placeholders.isEmpty()) {
                    //TOdo dboxPrompt 보내서 처리한다음에 다시 받아오자 치환한거를
                    processTemplate(dboxPrompt, placeholders);
                }
                String[] roleArr = {"system", "assistant", "user"};
                for (String roleStr : roleArr) {
                    if (dboxPrompt.containsKey("d_" + roleStr)) {
                        JSONObject prompt = new JSONObject();
                        prompt.put("role", roleStr);
                        prompt.put("content", dboxPrompt.getString("d_" + roleStr));
                        messages.put(prompt);
                    }
                }
            }
            if (Objects.nonNull(conversationHistory) && !conversationHistory.isEmpty()) {
                // 사용자쪽에서 넘어온 대화대역
                JSONArray fullHistory = new JSONArray(conversationHistory);
                for (int i = 0; i < fullHistory.length(); i++) {
                    messages.put(fullHistory.get(i));
                }
            }

            // 요청 바디 구성
            JSONObject body = new JSONObject();
            body.put("model", model); //필수요소

            // input 파라미터가 있으면 추가
            if (requestDTO.getInput() != null) {
                body.put("input", requestDTO.getInput());
            }

            // 하이퍼파라미터 추가
            addHyperparametersToBody(body, requestDTO);

            // RestTemplate을 커스터마이징하여 SSLHandshake 오류 해결
            RestTemplate restTemplate = gptRestTemplat();
            // HTTP 요청 전송
            HttpEntity<String> entity = new HttpEntity<>(body.toString(), headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            //로깅처리
            service.insertApiLog(box, body.toString(), response, "embed");

            // 응답 반환
            return response;

        } catch (BusinessException e) {
            logger.error("임베딩 API 요청 중 비즈니스 예외 발생: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("임베딩 API 요청 중 예외 발생: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "임베딩 API 요청 중 오류가 발생했습니다.");
            // 예외 처리
            //return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    @Operation(summary = "Google 검색 API", description = "Google Custom Search API를 사용하여 웹 검색을 수행합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "API 요청 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping(value = "/api/v1/google/customsearch/")
    public ResponseEntity<String> googleSearchApi(HttpServletRequest request,
                                                  @Parameter(description = "Google 검색 요청 정보", required = true)
                                                  @Valid @RequestBody GoogleSearchRequestDTO requestDTO) throws Exception {
        try {
            RequestBox box = RequestManager.getBox(request);

            String query = requestDTO.getQuery();
            if (query == null || query.trim().isEmpty()) {
                throw new IllegalArgumentException("검색어(query)가 제공되지 않았습니다.");
            }

            String googleApiKey = "AIzaSyAJnMeXXWY2UcDaR-fU4OtE6iBAPzBEGL8";
            String googleCx = "44f809ce1066f4580"; // Custom Search Engine ID

            // 요청 URL 구성
            String url = "https://www.googleapis.com/customsearch/v1?q="
                    + query
                    + "&key=" + googleApiKey
                    + "&cx=" + googleCx;

            RestTemplate restTemplate = gptRestTemplat();
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Google API error: " + response.getStatusCode() + " " + response.getBody());
            }

            // API 로그 저장
            service.insertApiLog(box, query, response, "google");

            return ResponseEntity.ok(response.getBody());
        } catch (BusinessException e) {
            logger.error("Google 검색 API 요청 중 비즈니스 예외 발생: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Google 검색 API 요청 중 예외 발생: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "Google 검색 API 요청 중 오류가 발생했습니다.");
            // 예외 처리
            //return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    /**
     * 프롬프트 템플릿 처리
     * 템플릿에서 ${key} 형태의 변수를 찾아 placeholders의 값으로 대체합니다.
     */
    private void processTemplate(DataBox dboxPrompt, Map<String, Object> placeholders) {
        String[] roleArr = {"system", "user", "assistant"};
        for (int i = 0; i < roleArr.length; i++) { //선택요소
            if (placeholders.containsKey(roleArr[i])) {
                Map<String, Object> getPlaceholders = (Map<String, Object>) placeholders.get(roleArr[i]);
                // 역할에 맞는 템플릿을 가져옴
                String template = dboxPrompt.getString("d_" + roleArr[i]);
                // 템플릿에서 `${key}` 형식으로 변수를 찾기 위한 정규식
                Pattern pattern = Pattern.compile("\\$\\{(.*?)\\}");
                Matcher matcher = pattern.matcher(template);
                StringBuffer result = new StringBuffer();
                while (matcher.find()) {
                    String placeholder = matcher.group(1);
                    String replacement = null;

                    // placeholders에서 key에 해당하는 값을 찾아 대체
                    if (getPlaceholders.containsKey(placeholder)) {
                        replacement = String.valueOf(getPlaceholders.get(placeholder));
                    }

                    // 치환할 때 replacement 문자열에 Matcher.quoteReplacement를 적용
                    matcher.appendReplacement(result, Matcher.quoteReplacement(replacement != null ? replacement : matcher.group(0)));
                }
                matcher.appendTail(result);
                // 치환된 템플릿을 dboxPrompt에 다시 설정
                dboxPrompt.put("d_" + roleArr[i], result.toString());
            }
        }
    }

    /**
     * 하이퍼파라미터를 JSON 요청 바디에 추가
     */
    private void addHyperparametersToBody(JSONObject body, BaseGPTRequestDTO requestDTO) {
        // 공통 하이퍼파라미터 추가
        if (requestDTO.getTemperature() != null) body.put("temperature", requestDTO.getTemperature());
        if (requestDTO.getMaxTokens() != null) body.put("max_tokens", requestDTO.getMaxTokens());
        if (requestDTO.getTopP() != null) body.put("top_p", requestDTO.getTopP());
        if (requestDTO.getFrequencyPenalty() != null) body.put("frequency_penalty", requestDTO.getFrequencyPenalty());
        if (requestDTO.getPresencePenalty() != null) body.put("presence_penalty", requestDTO.getPresencePenalty());
        if (requestDTO.getStop() != null) body.put("stop", requestDTO.getStop());
        if (requestDTO.getLogitBias() != null) body.put("logit_bias", requestDTO.getLogitBias());
        if (requestDTO.getResponseFormat() != null) body.put("response_format", requestDTO.getResponseFormat());
    }

//    private RestTemplate gptRestTemplat() {
//        try {
//            SSLContext sslContext = SSLContextBuilder.create()
//                    .setProtocol("TLSv1.2")
//                    .build();
//            CloseableHttpClient httpClient = HttpClients.custom()
//                    .setSSLContext(sslContext)
//                    .build();
//            HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
//            requestFactory.setConnectTimeout(60000);
//            requestFactory.setReadTimeout(60000);
//            return new RestTemplate(requestFactory);
//        } catch (Exception e) {
//            logger.error("SSL RestTemplate 생성 중 오류 발생: {}", e.getMessage(), e);
//            throw new RuntimeException("Failed to create custom RestTemplate", e);
//        }
//    }

    // Spring Boot 3.x 스타일로 코드 현대화
    private RestTemplate gptRestTemplat() {
        // 최신 방식으로 ClientHttpRequestFactory 구성
        Supplier<ClientHttpRequestFactory> requestFactorySupplier = () -> {
            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
            factory.setConnectTimeout(60000); // 60초
            factory.setReadTimeout(60000);    // 60초
            return factory;
        };

        // RestTemplateBuilder를 사용하여 RestTemplate 생성
        return restTemplateBuilder
                .requestFactory(requestFactorySupplier)
                .build();
    }

//    // SSLHandshake 오류를 해결하기 위한 RestTemplate 커스터마이징
//    private RestTemplate createCustomRestTemplate() {
//        try {
//            // SSLContext 생성
//            SSLContext sslContext = SSLContextBuilder.create()
//                    .loadTrustMaterial(new TrustSelfSignedStrategy())
//                    .build();
//
//            // 커스터마이징된 HttpClient 생성
//            CloseableHttpClient httpClient = HttpClients.custom()
//                    .setSSLContext(sslContext)
//                    .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
//                    .build();
//
//            // RestTemplate에 HttpClient 연결
//            return new RestTemplate(new HttpComponentsClientHttpRequestFactory(httpClient));
//        } catch (Exception e) {
//            throw new RuntimeException("Failed to create custom RestTemplate", e);
//        }
//    }
}