package com.firzzle.llm.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {

    /**
     * UUID로 user_seq 조회
     * @param uuid 사용자 UUID
     * @return user_seq
     */
    Long selectUserSeqByUuid(@Param("uuid") String uuid);
}