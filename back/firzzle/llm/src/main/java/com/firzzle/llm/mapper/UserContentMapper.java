package com.firzzle.llm.mapper;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserContentMapper {
    int selectContentSeqByUserContentSeq(int userContentSeq);
}
