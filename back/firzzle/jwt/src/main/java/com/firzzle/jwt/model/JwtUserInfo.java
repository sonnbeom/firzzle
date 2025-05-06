package com.firzzle.jwt.model;

import java.util.List;

/**
 * @Class Name : JwtUserInfo.java
 * @Description : JWT 토큰에서 추출한 사용자 정보를 담는 모델 클래스
 * @author Firzzle
 * @since 2025. 5. 6.
 */
public class JwtUserInfo {
    private final String uuid;
    private final String role;
    private final List<String> scopes;

    public JwtUserInfo(String uuid, String role, List<String> scopes) {
        this.uuid = uuid;
        this.role = role;
        this.scopes = scopes;
    }

    public String getUuid() {
        return uuid;
    }

    public String getRole() {
        return role;
    }

    public List<String> getScopes() {
        return scopes;
    }

    public boolean hasScope(String scope) {
        return scopes != null && scopes.contains(scope);
    }
}