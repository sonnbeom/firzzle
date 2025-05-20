package com.firzzle.llm.service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.firzzle.common.exception.BusinessException;
import com.firzzle.common.exception.ErrorCode;
import com.firzzle.llm.dto.RecommendContentDTO;
import com.firzzle.llm.dto.RecommendRequestDTO;
import com.firzzle.llm.dto.RecommendResponseDTO;
import com.firzzle.llm.dto.UserContentDTO;
import com.firzzle.llm.mapper.ContentMapper;
import com.firzzle.llm.mapper.UserContentMapper;
import com.firzzle.llm.mapper.UserMapper;
import com.firzzle.llm.util.QdrantCollections;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RecommendationService {
    private final RagService ragService;
    private final UserContentMapper userContentMapper;
    private final UserMapper userMapper;
    private final ContentMapper contentMapper;
    

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
        Long actualUserSeq = userMapper.selectUserSeqByUuid(uuid);

        UserContentDTO userContent =
                userContentMapper.selectUserAndContentByUserContentSeq(userContentSeq);
        Long userSeq    = userContent.getUserSeq();
        Long contentSeq = userContent.getContentSeq();

        if (!actualUserSeq.equals(userSeq)) {
            throw new BusinessException(
                ErrorCode.UNAUTHORIZED_ACCESS,
                "해당 콘텐츠에 대한 접근 권한이 없습니다."
            );
        }

        return ragService.getVectorWithPayloadByContentSeq(QdrantCollections.CONTENT, contentSeq)
            .flatMap(data -> {
                @SuppressWarnings("unchecked")
                List<Float> vector = (List<Float>) data.get("vector");

                @SuppressWarnings("unchecked")
                List<String> originKeywords = (List<String>) ((Map<String, Object>) data.get("payload"))
                    .getOrDefault("keywords", Collections.emptyList());

                return ragService.searchSimilarByVectorExcludingSelf(
                        QdrantCollections.CONTENT,
                        vector,
                        18,
                        0.22,
                        contentSeq
                ).map(results -> Map.of(
                    "results", results,
                    "originTags", originKeywords
                ));
            })
            .map(data -> {
                List<Map<String, Object>> results = (List<Map<String, Object>>) data.get("results");
                List<String> originKeywords = (List<String>) data.get("originTags");

                int page = Math.max(request.getP_pageno(), 1);
                int size = Math.max(request.getP_pagesize(), 6);
                int from = (page - 1) * size;
                int to   = Math.min(from + size, results.size());

                List<Map<String, Object>> pageContentRaw =
                    from >= results.size()
                        ? Collections.emptyList()
                        : results.subList(from, to);

                // ✅ contentSeq 목록 추출
                List<Long> contentSeqList = pageContentRaw.stream()
                    .map(r -> ((Map<String, Object>) r.get("payload")).get("contentSeq"))
                    .map(v -> ((Number) v).longValue())
                    .collect(Collectors.toList());

                // ✅ DB에서 RecommendContentDTO 목록 조회
                List<RecommendContentDTO> contentList =
                    contentMapper.selectRecommendContentListBySeqList(contentSeqList);

                // ✅ contentSeq 기준으로 매핑
                Map<Long, RecommendContentDTO> contentMap = contentList.stream()
                    .collect(Collectors.toMap(RecommendContentDTO::getContentSeq, r -> r));

                // ✅ Qdrant 결과 순서에 따라 재정렬
                List<RecommendContentDTO> sortedList = contentSeqList.stream()
                    .map(contentMap::get)
                    .filter(c -> c != null)
                    .collect(Collectors.toList());

                RecommendResponseDTO dto = RecommendResponseDTO.builder()
                    .content(sortedList)
                    .originTags(String.join(",", originKeywords))
                    .p_pageno(page)
                    .p_pagesize(size)
                    .totalElements(results.size())
                    .totalPages((results.size() + size - 1) / size)
                    .last(page * size >= results.size())
                    .hasNext(page * size < results.size())
                    .build();

                return List.of(dto);
            })
            .toFuture();
    }

}
