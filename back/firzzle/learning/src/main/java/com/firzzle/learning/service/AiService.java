package com.firzzle.learning.service;

import com.firzzle.common.library.DataBox;
import com.firzzle.common.library.RequestBox;
import com.firzzle.learning.dao.AiDao;
import com.firzzle.learning.dao.AiLogDao;
import lombok.RequiredArgsConstructor;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @Class Name : AiService.java
 * @Description : GPT 관리 서비스
 * @author Firzzle
 * @since
 */
@Service
@RequiredArgsConstructor
@Transactional
public class AiService {

    private final AiDao dao;
    private final AiLogDao daoLog;


    /**
     * 메뉴별 기능 조회
     * @param box
     * @return
     */
    public DataBox selectAiPrompt(RequestBox box) {
        return dao.selectAiPrompt(box);

    }

    /**
     * 주관처별 API Key 셋팅
     * @param box
     * @return
     */
    public DataBox selectApiKey(RequestBox box) {
        return dao.selectApiKey(box);

    }


    /**
     * 성공시 로깅 처리 (req, res 전부다 집어넣는다.)
     * @param box
     * @return
     */
    public void insertApiLog(RequestBox box, String request, ResponseEntity<String> response,String modelType) {
        try {
            //TODO 후처리 진행
            JSONObject jsonResponse = new JSONObject(response.getBody());
            if("chat".equals(modelType)){
                if(jsonResponse.has("usage")) {
                   JSONObject usage = jsonResponse.getJSONObject("usage");
                   int promptTokens = usage.getInt("prompt_tokens");
                   int completionTokens = usage.getInt("completion_tokens");
                   int totalTokens = usage.getInt("total_tokens");
                   String model = jsonResponse.getString("model");
                   // 추출된 토큰 정보를 로그로 남기거나 DB에 저장하는 로직 추가
                   box.put("p_model", model);
                   box.put("p_request_json", request);
                   box.put("p_response_json", jsonResponse.toString());
                   box.put("p_prompt_tokens", promptTokens);
                   box.put("p_completion_tokens", completionTokens);
                   box.put("p_total_tokens", totalTokens);
               }
            } else if ("audio".equals(modelType)) {
                // Whisper 전사 API 응답인 경우 "text" 필드만 포함되어 있음
               String transcription = jsonResponse.optString("text", "");
               box.put("p_model", "whisper-1");
               box.put("p_request_json", request);
               box.put("p_response_json", jsonResponse.toString());
               box.put("p_transcription", transcription);
            } else if ("img".equals(modelType)) {
                //데이터가 너무커서 삭제해야 로깅들어감
                if (jsonResponse.has("data")) {
                    JSONArray dataArray = jsonResponse.getJSONArray("data");
                    for (int i = 0; i < dataArray.length(); i++) {
                        JSONObject dataObj = dataArray.getJSONObject(i);
                        if (dataObj.has("b64_json")) {
                            dataObj.remove("b64_json");
                        }
                    }
                }
               box.put("p_model", "dall-e-3");
               box.put("p_request_json", request);
               box.put("p_response_json", jsonResponse.toString());
            }else if ("embed".equals(modelType)){
                if (jsonResponse.has("data")) {
                   JSONArray dataArray = jsonResponse.getJSONArray("data");
                   for (int i = 0; i < dataArray.length(); i++) {
                       JSONObject dataObj = dataArray.getJSONObject(i);
                       if (dataObj.has("embedding")) {
                           dataObj.remove("embedding");
                       }
                   }
               }
                JSONObject usage = jsonResponse.getJSONObject("usage");
               int promptTokens = usage.getInt("prompt_tokens");
               int totalTokens = usage.getInt("total_tokens");
               String model = jsonResponse.getString("model");
               // 추출된 토큰 정보를 로그로 남기거나 DB에 저장하는 로직 추가
               box.put("p_model", model);
               box.put("p_request_json", request);
               box.put("p_response_json", jsonResponse.toString());
               box.put("p_prompt_tokens", promptTokens);
               box.put("p_total_tokens", totalTokens);
            }
            daoLog.insertApiLog(box);
        }catch (Exception e){
            throw e;
        }
    }
    /**
     * 성공시 로깅 처리 (req, res 전부다 집어넣는다.)
     * @param box
     * @return
     */
    public void insertApiImageLog(RequestBox box, String request, ResponseEntity<String> response) {
        try {
            //TODO 후처리 진행
            JSONObject jsonResponse = new JSONObject(response.getBody());
            if(jsonResponse.has("usage")) {
                JSONObject usage = jsonResponse.getJSONObject("usage");
                int promptTokens = usage.getInt("prompt_tokens");
                int completionTokens = usage.getInt("completion_tokens");
                int totalTokens = usage.getInt("total_tokens");
                String model = jsonResponse.getString("model");
                // 추출된 토큰 정보를 로그로 남기거나 DB에 저장하는 로직 추가
                box.put("p_model", model);
                box.put("p_request_json", request);
                box.put("p_response_json", jsonResponse.toString());
                box.put("p_prompt_tokens", promptTokens);
                box.put("p_completion_tokens", completionTokens);
                box.put("p_total_tokens", totalTokens);
            }else {
                // Whisper 전사 API 응답인 경우 "text" 필드만 포함되어 있음
                String transcription = jsonResponse.optString("text", "");
                box.put("p_model", "whisper-1");
                box.put("p_request_json", request);
                box.put("p_response_json", jsonResponse.toString());
                box.put("p_transcription", transcription);
            }
            daoLog.insertApiLog(box);
        }catch (Exception e){
            throw e;
        }
    }



}
