package com.firzzle.auth.service;

import com.firzzle.auth.dao.TokenDAO;
import com.firzzle.auth.dao.UserDAO;
import com.firzzle.common.constant.CubeOneItem;
import com.firzzle.common.exception.BusinessException;
import com.firzzle.common.exception.ErrorCode;
import com.firzzle.common.library.AESUtil;
import com.firzzle.common.library.DataBox;
import com.firzzle.common.library.FormatDate;
import com.firzzle.common.library.RequestBox;
import com.firzzle.jwt.model.JwtToken;
import com.firzzle.jwt.provider.JwtTokenProvider;
import com.firzzle.jwt.util.JwtTokenUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @Class Name : AuthService.java
 * @Description : 인증 관련 서비스 인터페이스 구현
 * @author Firzzle
 * @since 2025. 5. 6.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UserDAO userDAO;
    private final TokenDAO tokenDAO;
    private final JwtTokenProvider jwtTokenProvider;
    private final KakaoOAuthService kakaoOAuthService;

    /**
     * 카카오 로그인 처리
     */
    @Transactional
    public DataBox kakaoLogin(RequestBox box) {
        try {
            String kakaoAccessToken = box.getString("accessToken");

            // 카카오 API로 사용자 정보 요청
            RequestBox kakaoUserInfo = kakaoOAuthService.getKakaoUserInfo(kakaoAccessToken);
            logger.info("카카오 사용자 정보 조회 성공 - ID: {}, Email: {}",
                    kakaoUserInfo.getString("id"), kakaoUserInfo.getString("email"));

            return processUserAndCreateToken(kakaoUserInfo);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            logger.error("카카오 로그인 처리 중 오류: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "카카오 로그인 처리 중 오류가 발생했습니다.");
        }
    }

    /**
     * 카카오 콜백 처리
     */
    @Transactional
    public DataBox processKakaoCallback(RequestBox box) {
        try {
            String code = box.getString("code");

            // 인증 코드로 액세스 토큰 요청
            String kakaoAccessToken = kakaoOAuthService.getKakaoAccessToken(code);

            // 액세스 토큰으로 사용자 정보 요청
            RequestBox kakaoUserInfo = kakaoOAuthService.getKakaoUserInfo(kakaoAccessToken);

            return processUserAndCreateToken(kakaoUserInfo);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            logger.error("카카오 콜백 처리 중 오류: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "카카오 콜백 처리 중 오류가 발생했습니다.");
        }
    }

    /**
     * 토큰 갱신
     */
    @Transactional
    public DataBox refreshToken(RequestBox box) {
        String refreshToken = box.getString("refreshToken");

        try {
            // 리프레시 토큰 검증
            if (!jwtTokenProvider.validateToken(refreshToken)) {
                throw new BusinessException(ErrorCode.INVALID_TOKEN, "유효하지 않은 리프레시 토큰입니다.");
            }

            // 토큰에서 정보 추출
            Claims claims = jwtTokenProvider.validateAndGetClaims(refreshToken);
            String uuid = claims.getSubject();
            String jti = JwtTokenProvider.extractTokenId(claims);

            // 토큰 ID로 저장된 리프레시 토큰 검사 (토큰 재사용 방지)
            RequestBox jtiBox = new RequestBox("requestbox");
            jtiBox.put("jti", jti);
            DataBox tokenDataBox = tokenDAO.selectRefreshToken(jtiBox);

            if (tokenDataBox == null) {
                throw new BusinessException(ErrorCode.INVALID_TOKEN, "존재하지 않는 리프레시 토큰입니다.");
            }

            // 사용자 정보 조회
            RequestBox uuidBox = new RequestBox("requestbox");
            uuidBox.put("uuid", uuid);
            DataBox userDataBox = userDAO.selectUserByUuid(uuidBox);

            if (userDataBox == null) {
                throw new BusinessException(ErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다.");
            }

            String role = userDataBox.getString("d_role");
            List<String> scopes = JwtTokenUtil.convertScopeToList(tokenDataBox.getString("d_scope"));

            // 액세스 토큰만 새로 발급 (리프레시 토큰은 그대로 사용)
            String newAccessToken = jwtTokenProvider.refreshAccessToken(refreshToken, role, scopes);

            // 응답 DataBox 구성
            DataBox resultDataBox = new DataBox();
            resultDataBox.put("accessToken", newAccessToken);
            resultDataBox.put("refreshToken", refreshToken); // 기존 리프레시 토큰 유지
            resultDataBox.put("expiresIn", jwtTokenProvider.getJwtConfig().getAccessTokenValiditySeconds());
            resultDataBox.put("tokenType", "Bearer");

            return resultDataBox;

        } catch (BusinessException e) {
            throw e;
        } catch (JwtException e) {
            logger.error("리프레시 토큰 처리 중 JWT 오류: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.INVALID_TOKEN, "리프레시 토큰 처리 중 오류가 발생했습니다.");
        } catch (Exception e) {
            logger.error("리프레시 토큰 처리 중 오류: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "리프레시 토큰 처리 중 오류가 발생했습니다.");
        }
    }

    /**
     * 로그아웃 처리
     */
    @Transactional
    public void logout(RequestBox box) {
        String refreshToken = box.getString("refreshToken");

        try {
            // 리프레시 토큰 검증
            if (!jwtTokenProvider.validateToken(refreshToken)) {
                throw new BusinessException(ErrorCode.INVALID_TOKEN, "유효하지 않은 리프레시 토큰입니다.");
            }

            // 토큰에서 정보 추출
            Claims claims = jwtTokenProvider.validateAndGetClaims(refreshToken);
            String jti = JwtTokenProvider.extractTokenId(claims);

            // 토큰 삭제
            RequestBox jtiBox = new RequestBox("requestbox");
            jtiBox.put("jti", jti);
            tokenDAO.deleteRefreshToken(jtiBox);

            logger.info("리프레시 토큰 삭제 완료 - JTI: {}", jti);

        } catch (BusinessException e) {
            throw e;
        } catch (JwtException e) {
            logger.error("로그아웃 처리 중 JWT 오류: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.INVALID_TOKEN, "로그아웃 처리 중 오류가 발생했습니다.");
        } catch (Exception e) {
            logger.error("로그아웃 처리 중 오류: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "로그아웃 처리 중 오류가 발생했습니다.");
        }
    }

    /**
     * 사용자 정보 조회
     */
    public DataBox getUserInfo(RequestBox box) {
        try {
            String uuid = box.getString("uuid");

            // UUID로 사용자 조회
            RequestBox uuidBox = new RequestBox("requestbox");
            uuidBox.put("uuid", uuid);
            DataBox userDataBox = userDAO.selectUserByUuid(uuidBox);

            if (userDataBox == null) {
                throw new BusinessException(ErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다.");
            }

            return userDataBox;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            logger.error("사용자 정보 조회 중 오류: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "사용자 정보 조회 중 오류가 발생했습니다.");
        }
    }

    /**
     * 관리자 로그인 처리
     */
    @Transactional
    public DataBox adminLogin(RequestBox box) {
        try {
            String username = box.getString("username");
            String password = box.getString("password");

            // 1. 사용자명으로 사용자 조회
            RequestBox usernameBox = new RequestBox("requestbox");
            usernameBox.put("username", username);
            DataBox userDataBox = userDAO.selectUserByUsername(usernameBox);

            // 2. 사용자가 존재하지 않는 경우
            if (userDataBox == null) {
                throw new BusinessException(ErrorCode.INVALID_CREDENTIALS, "잘못된 사용자명 또는 비밀번호입니다.");
            }

            // 3. 비밀번호 검증
            String encryptedPassword = AESUtil.encrypt(password, CubeOneItem.PWD);
            String storedPassword = userDataBox.getString("d_password");
            logger.info("encryptedPassword: {}, storedPassword: {}", encryptedPassword, storedPassword);
            if (!storedPassword.equals(encryptedPassword)) {
                throw new BusinessException(ErrorCode.INVALID_CREDENTIALS, "잘못된 사용자명 또는 비밀번호입니다.");
            }

            // 4. 관리자 권한 확인
            String role = userDataBox.getString("d_role");
            if (!"admin".equals(role)) {
                throw new BusinessException(ErrorCode.ACCESS_DENIED, "관리자 권한이 없습니다.");
            }

            // 5. 계정 활성화 여부 확인
            String activeYn = userDataBox.getString("d_active_yn");
            if (!"Y".equals(activeYn)) {
                throw new BusinessException(ErrorCode.ACCOUNT_DISABLED, "계정이 비활성화되었습니다.");
            }

            // 6. 마지막 로그인 시간 업데이트
            String uuid = userDataBox.getString("d_uuid");
            updateLastLoginTime(userDataBox);

            // 7. 권한 범위 설정 및 JWT 토큰 발급
            List<String> scopes = jwtTokenProvider.getDefaultScopes(role);
            return createTokenPair(uuid, role, scopes);

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            logger.error("관리자 로그인 처리 중 오류: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "관리자 로그인 처리 중 오류가 발생했습니다.");
        }
    }

    /**
     * 사용자 처리 및 토큰 생성
     */
    private DataBox processUserAndCreateToken(RequestBox kakaoUserInfo) {
        try {
            // 이메일로 기존 사용자 조회
            RequestBox emailBox = new RequestBox("requestbox");
            emailBox.put("email", kakaoUserInfo.getString("email"));
            DataBox userDataBox = userDAO.selectUserByEmail(emailBox);

            // 사용자가 없으면 새로 등록
            if (userDataBox == null) {
                userDataBox = kakaoOAuthService.registerNewUser(kakaoUserInfo);
                logger.info("새로운 사용자 등록 - UUID: {}, Email: {}",
                        userDataBox.getString("d_uuid"), userDataBox.getString("d_email"));
            } else {
                // 기존 사용자 로그인 시간 업데이트
                updateLastLoginTime(userDataBox);
                logger.info("기존 사용자 로그인 - UUID: {}, Email: {}",
                        userDataBox.getString("d_uuid"), userDataBox.getString("d_email"));
            }

            // 사용자 정보 확인
            String uuid = userDataBox.getString("d_uuid");
            String role = userDataBox.getString("d_role");

            // 권한 범위 설정
            List<String> scopes = jwtTokenProvider.getDefaultScopes(role);

            // JWT 토큰 발급
            return createTokenPair(uuid, role, scopes);
        } catch (Exception e) {
            logger.error("사용자 처리 및 토큰 생성 중 오류: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "사용자 처리 중 오류가 발생했습니다.");
        }
    }

    /**
     * 토큰 쌍 생성
     */
    @Transactional
    public DataBox createTokenPair(String uuid, String role, List<String> scopes) {
        try {
            // JWT 토큰 생성
            JwtToken jwtToken = jwtTokenProvider.createTokenPair(uuid, role, scopes);

            // 리프레시 토큰 정보 저장
            Claims refreshClaims = jwtTokenProvider.validateAndGetClaims(jwtToken.getRefreshToken());
            String jti = JwtTokenProvider.extractTokenId(refreshClaims);

            RequestBox tokenRequestBox = new RequestBox("requestbox");
            tokenRequestBox.put("jti", jti);
            tokenRequestBox.put("uuid", uuid);
            tokenRequestBox.put("role", role);
            tokenRequestBox.put("scope", String.join(" ", scopes));
            tokenRequestBox.put("refresh_token", jwtToken.getRefreshToken());

            try {
                String now = FormatDate.getDate("yyyyMMddHHmmss");
                // 리프레시 토큰 유효 기간(초)를 일수로 변환하여 더함
                String expireDate = FormatDate.getRelativeDate(now, (int)(jwtTokenProvider.getJwtConfig().getRefreshTokenValiditySeconds() / 86400));

                tokenRequestBox.put("indate", now);
                tokenRequestBox.put("expire_date", expireDate);
            } catch (Exception e) {
                logger.error("날짜 계산 중 오류: {}", e.getMessage(), e);
                // 오류 발생 시 기본값 사용
                tokenRequestBox.put("indate", "20250506000000");
                tokenRequestBox.put("expire_date", "20250513000000"); // 기본 7일 후
            }

            tokenDAO.insertRefreshToken(tokenRequestBox);
            logger.info("리프레시 토큰 저장 완료 - JTI: {}, UUID: {}", jti, uuid);

            // 응답 DataBox 구성
            DataBox resultDataBox = new DataBox();
            resultDataBox.put("accessToken", jwtToken.getAccessToken());
            resultDataBox.put("refreshToken", jwtToken.getRefreshToken());
            resultDataBox.put("expiresIn", jwtToken.getExpiresIn());
            resultDataBox.put("tokenType", "Bearer");

            return resultDataBox;
        } catch (Exception e) {
            logger.error("토큰 쌍 생성 중 오류: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "토큰 생성 중 오류가 발생했습니다.");
        }
    }

    /**
     * 마지막 로그인 시간 업데이트
     */
    private void updateLastLoginTime(DataBox userDataBox) throws Exception {
        RequestBox updateBox = new RequestBox("requestbox");
        String now = FormatDate.getDate("yyyyMMddHHmmss");
        updateBox.put("uuid", userDataBox.getString("d_uuid"));
        updateBox.put("last_login", now);
        updateBox.put("ldate", now);
        userDAO.updateUser(updateBox);
    }
}