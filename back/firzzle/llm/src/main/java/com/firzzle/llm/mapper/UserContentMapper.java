package com.firzzle.llm.mapper;

import com.firzzle.llm.dto.UserContentDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserContentMapper {

    int insertUserContent(UserContentDTO dto);

    Long selectContentSeqByUserContentSeq(@Param("userContentSeq") Long userContentSeq);
    
    Long selectUserSeqByUserContentSeq(Long userContentSeq);
    
    UserContentDTO selectUserAndContentByUserContentSeq(@Param("userContentSeq") Long userContentSeq);
}
