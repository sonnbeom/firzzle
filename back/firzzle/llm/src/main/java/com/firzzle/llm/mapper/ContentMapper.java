package com.firzzle.llm.mapper;

import com.firzzle.llm.dto.ContentDTO;
import com.firzzle.llm.dto.RecommendContentDTO;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface ContentMapper {

    /**
     * content_seq에 해당하는 콘텐츠의 요약 정보(title, url, thumbnail)를 조회합니다.
     */
    ContentDTO selectContentSummaryByContentSeq(@Param("contentSeq") Long contentSeq);

    /**
     * 콘텐츠에 연결된 태그들을 한 번에 저장합니다.
     * @param contentSeq 콘텐츠 고유 번호
     * @param tags 태그 문자열 리스트
     * @return 삽입된 행 수
     */
    int insertContentTags(@Param("contentSeq") Long contentSeq, @Param("tags") List<String> tags);
    
    /**
     * Qdrant 유사도 검색 결과 기반 콘텐츠 리스트 조회
     */
    List<RecommendContentDTO> selectRecommendContentListBySeqList(@Param("list") List<Long> contentSeqList);
    
    // completed_at 업데이트
    int updateProcessStatusAndCompletedAtByContentSeq(@Param("contentSeq") Long contentSeq,
                                                       @Param("processStatus") String processStatus,
                                                       @Param("completedAt") String completedAt);

    /**
     * userContentSeq에 해당하는 데이터를 삭제합니다.
     */
    int deleteUserContent(@Param("userContentSeq") Long userContentSeq);

    /**
     * contentSeq에 해당하는 레코드의 delete_yn 값을 업데이트합니다.
     */
    int updateDeleteYnByContentSeq(@Param("contentSeq") Long contentSeq, @Param("deleteYn") String deleteYn);

    /**
     * contentSeq에 해당하는 userContentSeq 목록을 조회합니다.
     */
    List<Long> selectUserContentSeqsByContentSeq(@Param("contentSeq") Long contentSeq);

}
