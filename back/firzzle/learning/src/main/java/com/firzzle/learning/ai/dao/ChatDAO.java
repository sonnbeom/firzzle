package com.firzzle.learning.ai.dao;

import com.firzzle.common.library.DataBox;
import com.firzzle.common.library.MyBatisSupport;
import com.firzzle.common.library.RequestBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.List;

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

    /**
     * 채팅 내역 조회 (커서 기반 페이징)
     */
    public List<DataBox> selectChatHistoryWithCursor(RequestBox box) {
        logger.debug("채팅 내역 조회 DAO (커서 기반) - 사용자: {}, 콘텐츠: {}, 커서: {}, 정렬: {} {}, 크기: {}",
                box.getString("uuid"), box.getLong("contentSeq"), box.getLong("cursor"),
                box.getString("orderBy"), box.getString("direction"), box.getInt("size"));

        return sqlSession.selectDataBoxList(NAMESPACE + ".selectChatHistoryWithCursor", box);
    }

    public Long insertChat(RequestBox box) {
        logger.debug("채팅 저장 DAO - 사용자: {}, 콘텐츠: {}", box.get("uuid"), box.get("contentSeq"));
        sqlSession.insert(NAMESPACE + ".insertChat", box);
        Long chatSeq = box.getLong("chatSeq");
        logger.debug("채팅 저장 완료 - chatSeq: {}", chatSeq);
        return chatSeq;
    }

    /**
     * 이전 대화 이력 조회 (최신 N개)
     */
    public List<DataBox> selectRecentChats(RequestBox box) {
        logger.debug("최근 채팅 내역 조회 - 사용자: {}, 콘텐츠: {}, 개수: {}",
                box.getString("uuid"), box.getLong("contentSeq"), box.getInt("limit"));
        return sqlSession.selectDataBoxList(NAMESPACE + ".selectRecentChats", box);
    }

}