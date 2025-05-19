package com.firzzle.llm.mapper;

import com.firzzle.llm.dto.ContentDTO;
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
}
