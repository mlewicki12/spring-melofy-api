package com.mlewicki12.melofyapi.models;

import lombok.Getter;

@Getter
public class ApiError {
    private final String message;
    private final Exception exception;

    public ApiError(String message, Exception exception) {
        this.message = message;
        this.exception = exception;
    }
}
