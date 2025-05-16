package com.firzzle.learning.logging.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RequestTransitionDto {
    private String fromContent;
    private String toContent;
}
