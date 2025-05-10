package com.firzzle.learning.dao;

import com.firzzle.common.library.MyBatisSupport;
import com.firzzle.common.library.RequestBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

/**
 * @Class Name : ChatDAO.java
 * @Description : 채팅 데이터 접근 객체
 * @author Firzzle
 * @since 2025. 5. 9.
 */
@Repository
public class ChatDAO extends MyBatisSupport {

    private static final Logger logger = LoggerFactory.getLogger(ChatDAO.class);
    private static final String NAMESPACE = "ChatMapper";

    public Long insertChat(RequestBox box) {
        logger.debug("채팅 저장 DAO - 사용자: {}, 콘텐츠: {}", box.get("uuid"), box.get("contentSeq"));
        sqlSession.insert(NAMESPACE + ".insertChat", box);
        Long chatSeq = box.getLong("chatSeq");
        logger.debug("채팅 저장 완료 - chatSeq: {}", chatSeq);
        return chatSeq;
    }
}