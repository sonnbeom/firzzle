package com.firzzle.common.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CustomApiResponse<T> {
    private Status status;
    private T data;
    private Error error;
    
    public static <T> CustomApiResponse<T> success(T data) {
        return CustomApiResponse.<T>builder()
                .status(Status.SUCCESS)
                .data(data)
                .build();
    }
    
    public static <T> CustomApiResponse<T> error(int statusCode, String message) {
        return CustomApiResponse.<T>builder()
                .status(Status.ERROR)
                .error(new Error(statusCode, message))
                .build();
    }

    public enum Status {
        SUCCESS, ERROR
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Error {
        private int statusCode;
        private String message;
    }
}