package com.firzzle.auth.dao;

import com.firzzle.common.library.DataBox;
import com.firzzle.common.library.MyBatisSupport;
import com.firzzle.common.library.RequestBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

/**
 * @Class Name : TokenDAO.java
 * @Description : 토큰 데이터 접근 객체
 * @author Firzzle
 * @since 2025. 5. 6.
 */
@Repository
public class TokenDAO extends MyBatisSupport {

    private static final Logger logger = LoggerFactory.getLogger(TokenDAO.class);

    private static final String NAMESPACE = "TokenMapper";

    /**
     * 리프레시 토큰 조회
     */
    public DataBox selectRefreshToken(RequestBox box) {
        logger.debug("리프레시 토큰 조회 DAO - JTI: {}", box.getString("jti"));
        return sqlSession.selectDataBox(NAMESPACE + ".selectRefreshToken", box);
    }

    /**
     * 리프레시 토큰 등록
     */
    public int insertRefreshToken(RequestBox box) {
        logger.debug("리프레시 토큰 등록 DAO - JTI: {}, UUID: {}", box.getString("jti"), box.getString("uuid"));
        return sqlSession.insert(NAMESPACE + ".insertRefreshToken", box);
    }

    /**
     * 리프레시 토큰 삭제
     */
    public int deleteRefreshToken(RequestBox box) {
        logger.debug("리프레시 토큰 삭제 DAO - JTI: {}", box.getString("jti"));
        return sqlSession.delete(NAMESPACE + ".deleteRefreshToken", box);
    }

    /**
     * 사용자의 모든 리프레시 토큰 삭제
     */
    public int deleteAllRefreshTokensByUser(RequestBox box) {
        logger.debug("사용자의 모든 리프레시 토큰 삭제 DAO - UUID: {}", box.getString("uuid"));
        return sqlSession.delete(NAMESPACE + ".deleteAllRefreshTokensByUser", box);
    }
}