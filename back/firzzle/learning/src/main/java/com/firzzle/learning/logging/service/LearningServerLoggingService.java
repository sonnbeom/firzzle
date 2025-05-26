package com.firzzle.learning.logging.service;

import com.firzzle.common.logging.dto.UserActionLog;
import com.firzzle.common.logging.service.LoggingService;
import com.firzzle.learning.logging.dto.request.RequestTransitionDto;
import org.springframework.stereotype.Service;

@Service
public class LearningServerLoggingService {
    public void loggingLearningTransition(String userId, RequestTransitionDto reqDto) {
        LoggingService.log(UserActionLog.userPreferenceLog(userId, reqDto.getFromContent(), reqDto.getToContent()));
    }
}
