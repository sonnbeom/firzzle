package com.firzzle.llm.service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.springframework.stereotype.Service;

import com.firzzle.common.exception.BusinessException;
import com.firzzle.common.exception.ErrorCode;
import com.firzzle.llm.client.OpenAiClient;
import com.firzzle.llm.client.QdrantClient;
import com.firzzle.llm.dto.RecommendRequestDTO;
import com.firzzle.llm.dto.RecommendResponseDTO;
import com.firzzle.llm.dto.UserContentDTO;
import com.firzzle.llm.mapper.ChatMapper;
import com.firzzle.llm.mapper.ExamsMapper;
import com.firzzle.llm.mapper.UserContentMapper;
import com.firzzle.llm.mapper.UserMapper;
import com.firzzle.llm.prompt.PromptFactory;
import com.firzzle.llm.util.QdrantCollections;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RecommendationService {
    private final RagService ragService;
    private final UserContentMapper userContentMapper;
    private final UserMapper userMapper;
    

    /**
     * 입력된 contentSeq 기반으로 유사한 콘텐츠 ID 리스트를 추천합니다.
     * - 저장된 content_vectors 컬렉션을 기반으로 검색
     * - payload에는 contentSeq만 저장되어 있음
     */
    public CompletableFuture<List<RecommendResponseDTO>> searchSimilarContents(
            String uuid,
            Long userContentSeq,
            RecommendRequestDTO request
    ) {
        // 1. UUID로 사용자 번호 조회
        Long actualUserSeq = userMapper.selectUserSeqByUuid(uuid);

        // 2. 콘텐츠 매핑 정보 조회
        UserContentDTO userContent =
                userContentMapper.selectUserAndContentByUserContentSeq(userContentSeq);
        Long userSeq    = userContent.getUserSeq();
        Long contentSeq = userContent.getContentSeq();

        // 3. 권한 체크
        if (!actualUserSeq.equals(userSeq)) {
            throw new BusinessException(
                ErrorCode.UNAUTHORIZED_ACCESS,
                "해당 콘텐츠에 대한 접근 권한이 없습니다."
            );
        }

        // 4. 벡터+키워드 조회 → 유사 콘텐츠 검색 → 필터링/페이징 → DTO 변환
        return ragService
        	    .getVectorWithPayloadByContentSeq(QdrantCollections.CONTENT, contentSeq)
        	    .flatMap(data -> {
        	        @SuppressWarnings("unchecked")
        	        List<Float> vector = (List<Float>) data.get("vector");
        	        @SuppressWarnings("unchecked")
        	        List<String> keywords = (List<String>) ((Map<String, Object>) data.get("payload"))
        	            .getOrDefault("keywords", Collections.emptyList());

        	        return ragService.searchSimilarByVectorExcludingSelfWithKeywords(
        	            QdrantCollections.CONTENT,
        	            vector,
        	            18,
        	            0.2,
        	            contentSeq,
        	            keywords
        	        );
        	    })
        	    // <— 여기서 flatMap 대신 map 으로 바꿔주세요
        	    .map(results -> {
        	        int page = Math.max(request.getP_pageno(), 1);
        	        int size = Math.max(request.getP_pagesize(), 6);
        	        int from = (page - 1) * size;
        	        int to   = Math.min(from + size, results.size());

        	        List<Map<String, Object>> pageContent =
        	            from >= results.size()
        	                ? Collections.emptyList()
        	                : results.subList(from, to);

        	        String originTags = pageContent.isEmpty()
        	            ? ""
        	            : String.join(",",
        	                (List<String>) ((Map<String,Object>) pageContent.get(0).get("payload"))
        	                    .get("keywords")
        	              );

        	        RecommendResponseDTO dto = RecommendResponseDTO.builder()
        	            .content(pageContent)
        	            .originTags(originTags)
        	            .p_pageno(page)
        	            .p_pagesize(size)
        	            .totalElements(results.size())
        	            .totalPages((results.size() + size - 1) / size)
        	            .last(page * size >= results.size())
        	            .hasNext(page * size < results.size())
        	            .build();

        	        // List<RecommendResponseDTO> 하나를 반환
        	        return List.of(dto);
        	    })
        	    .toFuture();
    }


}
