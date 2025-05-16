package com.firzzle.learning.logging.common.service;

import com.firzzle.common.library.RequestBox;
import com.firzzle.common.library.RequestManager;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

@Service
public class HttpServletRequestCheckService {
    public String getUserUUID(HttpServletRequest request) {
        try {
            RequestBox box = RequestManager.getBox(request);
            return box.getString("uuid");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
