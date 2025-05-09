package com.firzzle.common.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Response<T> {
    private Status status;
    private String cause;
    private String message;
    private String prevUrl;
    private String redirectUrl;
    private T data;
}
